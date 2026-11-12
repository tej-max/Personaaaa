package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BotPersonaEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.KeywordTriggerEntity
import com.example.data.model.MessageType
import com.example.data.model.ResponseAlternativeEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.PersonaChatRepository
import com.example.engine.TriggerEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val currentPersona: BotPersonaEntity? = null,
    val userProfile: UserProfileEntity? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val isBotTyping: Boolean = false,
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val showManualDialog: Boolean = false,
    val triggersWithAlternatives: List<Pair<KeywordTriggerEntity, List<ResponseAlternativeEntity>>> = emptyList()
)

class ChatViewModel(
    private val repository: PersonaChatRepository,
    private val triggerEngine: TriggerEngine = TriggerEngine()
) : ViewModel() {

    private val _isBotTyping = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchOpen = MutableStateFlow(false)
    private val _showManualDialog = MutableStateFlow(false)
    private val _triggersWithAlts = MutableStateFlow<List<Pair<KeywordTriggerEntity, List<ResponseAlternativeEntity>>>>(emptyList())

    private var typingJob: Job? = null

    val currentPersona = repository.getCurrentPersona()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userProfile = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val messagesFlow = combine(currentPersona, _searchQuery) { persona, query ->
        persona to query
    }.flatMapLatest { (persona, query) ->
        if (persona != null) {
            if (query.isBlank()) {
                repository.getMessagesForPersona(persona.id)
            } else {
                repository.searchMessages(persona.id, query.trim())
            }
        } else {
            flowOf(emptyList())
        }
    }

    private data class UiFlags(
        val isTyping: Boolean,
        val query: String,
        val searchOpen: Boolean,
        val manualOpen: Boolean
    )

    private val flagsFlow = combine(
        _isBotTyping,
        _searchQuery,
        _isSearchOpen,
        _showManualDialog
    ) { isTyping, query, searchOpen, manualOpen ->
        UiFlags(isTyping, query, searchOpen, manualOpen)
    }

    val uiState: StateFlow<ChatUiState> = combine(
        currentPersona,
        userProfile,
        messagesFlow,
        flagsFlow,
        _triggersWithAlts
    ) { persona, user, messages, flags, triggersList ->
        ChatUiState(
            currentPersona = persona,
            userProfile = user,
            messages = messages,
            isBotTyping = flags.isTyping,
            searchQuery = flags.query,
            isSearchOpen = flags.searchOpen,
            showManualDialog = flags.manualOpen,
            triggersWithAlternatives = triggersList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState())

    init {
        loadTriggersForManualPicker()
    }

    fun loadTriggersForManualPicker() {
        viewModelScope.launch {
            val persona = currentPersona.value ?: repository.getCurrentPersonaSync() ?: return@launch
            val triggers = repository.getActiveTriggersForPersonaSync(persona.id)
            val list = mutableListOf<Pair<KeywordTriggerEntity, List<ResponseAlternativeEntity>>>()
            for (trig in triggers) {
                val alts = repository.getAllAlternativesForTriggerSync(trig.id)
                list.add(trig to alts)
            }
            _triggersWithAlts.value = list
        }
    }

    fun sendMessage(text: String, mediaUri: String? = null) {
        val persona = currentPersona.value ?: return
        if (text.isBlank() && mediaUri == null) return

        viewModelScope.launch {
            // 1. Insert user message
            val userMsg = ChatMessageEntity(
                personaId = persona.id,
                sender = "USER",
                messageType = if (mediaUri != null) {
                    if (mediaUri.endsWith(".gif", ignoreCase = true)) MessageType.GIF.name else MessageType.IMAGE.name
                } else MessageType.TEXT.name,
                content = text.trim(),
                mediaUri = mediaUri,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(userMsg)

            // 2. Fetch active triggers and their alternatives
            val activeTriggers = repository.getActiveTriggersForPersonaSync(persona.id)
            val alternativesMap = mutableMapOf<Long, List<ResponseAlternativeEntity>>()
            for (t in activeTriggers) {
                alternativesMap[t.id] = repository.getActiveAlternativesForTriggerSync(t.id)
            }

            val recentBotMsg = repository.getRecentBotMessageSync(persona.id)
            val matches = triggerEngine.evaluate(
                userMessage = text,
                triggers = activeTriggers,
                alternativesByTriggerId = alternativesMap,
                persona = persona,
                previousBotMessageContent = recentBotMsg?.content
            )

            if (matches.isNotEmpty()) {
                // Execute matched triggers in order
                typingJob?.cancel()
                typingJob = launch {
                    for (match in matches) {
                        // Typing simulation
                        if (persona.enableTypingIndicator && match.computedDelayMs > 0) {
                            _isBotTyping.value = true
                            delay(match.computedDelayMs)
                            _isBotTyping.value = false
                        }

                        if (match.fullSequence.isNotEmpty()) {
                            // Multi-step sequence
                            for ((idx, step) in match.fullSequence.withIndex()) {
                                if (idx > 0 && step.delaySeconds > 0) {
                                    if (persona.enableTypingIndicator) _isBotTyping.value = true
                                    delay((step.delaySeconds * 1000).toLong())
                                    if (persona.enableTypingIndicator) _isBotTyping.value = false
                                }
                                val botMsg = ChatMessageEntity(
                                    personaId = persona.id,
                                    sender = "BOT",
                                    messageType = step.responseType,
                                    content = step.textContent,
                                    mediaUri = step.mediaUri,
                                    timestamp = System.currentTimeMillis(),
                                    triggerId = match.trigger.id
                                )
                                repository.insertMessage(botMsg)
                                repository.updateAlternativeLastUsed(step.id, System.currentTimeMillis())
                            }
                        } else {
                            val alt = match.selectedAlternative
                            if (alt != null) {
                                val botMsg = ChatMessageEntity(
                                    personaId = persona.id,
                                    sender = "BOT",
                                    messageType = alt.responseType,
                                    content = alt.textContent,
                                    mediaUri = alt.mediaUri,
                                    timestamp = System.currentTimeMillis(),
                                    triggerId = match.trigger.id
                                )
                                repository.insertMessage(botMsg)
                                repository.updateAlternativeLastUsed(alt.id, System.currentTimeMillis())
                            }
                        }

                        // Update trigger last triggered timestamp
                        repository.updateTriggerTimestamp(match.trigger.id, System.currentTimeMillis())
                    }
                }
            } else {
                // No trigger matched: evaluate fallback
                val fallbackText = triggerEngine.getFallbackResponse(persona)
                if (!fallbackText.isNullOrBlank()) {
                    typingJob?.cancel()
                    typingJob = launch {
                        val delayMs = triggerEngine.calculateDelay(fallbackText, persona)
                        if (persona.enableTypingIndicator && delayMs > 0) {
                            _isBotTyping.value = true
                            delay(delayMs)
                            _isBotTyping.value = false
                        }
                        val botMsg = ChatMessageEntity(
                            personaId = persona.id,
                            sender = "BOT",
                            messageType = MessageType.TEXT.name,
                            content = fallbackText,
                            timestamp = System.currentTimeMillis()
                        )
                        repository.insertMessage(botMsg)
                    }
                }
            }
        }
    }

    fun sendManualResponse(alternative: ResponseAlternativeEntity) {
        val persona = currentPersona.value ?: return
        viewModelScope.launch {
            val botMsg = ChatMessageEntity(
                personaId = persona.id,
                sender = "BOT",
                messageType = alternative.responseType,
                content = alternative.textContent,
                mediaUri = alternative.mediaUri,
                timestamp = System.currentTimeMillis(),
                triggerId = alternative.triggerId
            )
            repository.insertMessage(botMsg)
            _showManualDialog.value = false
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessageById(id)
        }
    }

    fun clearChat() {
        val persona = currentPersona.value ?: return
        viewModelScope.launch {
            repository.clearMessagesForPersona(persona.id)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchOpen.update { !it }
        if (!_isSearchOpen.value) {
            _searchQuery.value = ""
        }
    }

    fun setManualDialogVisible(visible: Boolean) {
        _showManualDialog.value = visible
        if (visible) loadTriggersForManualPicker()
    }

    companion object {
        fun provideFactory(repository: PersonaChatRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(repository) as T
                }
            }
    }
}
