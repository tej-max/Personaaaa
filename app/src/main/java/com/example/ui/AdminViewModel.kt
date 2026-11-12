package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BotPersonaEntity
import com.example.data.model.KeywordTriggerEntity
import com.example.data.model.MediaItemEntity
import com.example.data.model.ResponseAlternativeEntity
import com.example.data.model.ScheduledMessageEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.PersonaChatRepository
import com.example.engine.SchedulerManager
import com.example.engine.TriggerEngine
import com.example.engine.TriggerMatchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SandboxSimulationResult(
    val matched: Boolean,
    val triggerName: String = "",
    val matchingMode: String = "",
    val matchedKeyword: String = "",
    val selectedReply: String = "",
    val estimatedDelayMs: Long = 0,
    val fallbackReply: String? = null,
    val reason: String = ""
)

class AdminViewModel(
    private val repository: PersonaChatRepository,
    private val schedulerManager: SchedulerManager,
    private val triggerEngine: TriggerEngine = TriggerEngine()
) : ViewModel() {

    val currentPersona = repository.getCurrentPersona()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val allPersonas = repository.getAllPersonas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // --- Trigger Filters ---
    val selectedCategory = MutableStateFlow("All")
    val triggerSearchQuery = MutableStateFlow("")

    val triggers: StateFlow<List<KeywordTriggerEntity>> = combine(
        currentPersona,
        selectedCategory,
        triggerSearchQuery
    ) { persona, cat, query ->
        Triple(persona, cat, query)
    }.flatMapLatest { (persona, cat, query) ->
        if (persona != null) {
            repository.getTriggersForPersona(persona.id)
        } else {
            flowOf(emptyList())
        }
    }.combine(selectedCategory) { list, cat ->
        if (cat == "All") list else list.filter { it.category.equals(cat, ignoreCase = true) }
    }.combine(triggerSearchQuery) { list, query ->
        if (query.isBlank()) list else list.filter {
            it.name.contains(query, ignoreCase = true) || it.primaryKeyword.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Scheduled Messages ---
    val scheduledMessages: StateFlow<List<ScheduledMessageEntity>> = currentPersona.flatMapLatest { persona ->
        if (persona != null) repository.getScheduledMessagesForPersona(persona.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Media Items ---
    val mediaItems = repository.getAllMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Sandbox State ---
    private val _sandboxResult = MutableStateFlow<SandboxSimulationResult?>(null)
    val sandboxResult: StateFlow<SandboxSimulationResult?> = _sandboxResult.asStateFlow()

    // --- Trigger Operations ---
    fun toggleTriggerEnabled(trigger: KeywordTriggerEntity) {
        viewModelScope.launch {
            repository.updateTrigger(trigger.copy(isEnabled = !trigger.isEnabled))
        }
    }

    fun deleteTrigger(triggerId: Long) {
        viewModelScope.launch {
            repository.deleteTriggerById(triggerId)
        }
    }

    fun duplicateTrigger(triggerId: Long) {
        viewModelScope.launch {
            repository.duplicateTrigger(triggerId)
        }
    }

    fun saveTriggerWithAlternatives(
        trigger: KeywordTriggerEntity,
        alternatives: List<ResponseAlternativeEntity>
    ) {
        viewModelScope.launch {
            val triggerId = if (trigger.id == 0L) {
                repository.saveTrigger(trigger)
            } else {
                repository.updateTrigger(trigger)
                repository.deleteAlternativesForTrigger(trigger.id)
                trigger.id
            }

            val altsToInsert = alternatives.mapIndexed { index, alt ->
                alt.copy(id = 0, triggerId = triggerId, sequenceOrder = index)
            }
            repository.saveAlternatives(altsToInsert)
        }
    }

    suspend fun getAlternativesForTriggerSync(triggerId: Long): List<ResponseAlternativeEntity> {
        return repository.getAllAlternativesForTriggerSync(triggerId)
    }

    // --- Sandbox Simulator ---
    fun testSandboxInput(input: String) {
        if (input.isBlank()) {
            _sandboxResult.value = null
            return
        }
        viewModelScope.launch {
            val persona = currentPersona.value ?: return@launch
            val activeTriggers = repository.getActiveTriggersForPersonaSync(persona.id)
            val altsMap = mutableMapOf<Long, List<ResponseAlternativeEntity>>()
            for (t in activeTriggers) {
                altsMap[t.id] = repository.getActiveAlternativesForTriggerSync(t.id)
            }
            val recentBot = repository.getRecentBotMessageSync(persona.id)
            val matches = triggerEngine.evaluate(
                userMessage = input,
                triggers = activeTriggers,
                alternativesByTriggerId = altsMap,
                persona = persona,
                previousBotMessageContent = recentBot?.content
            )

            if (matches.isNotEmpty()) {
                val best = matches.first()
                val replyText = if (best.fullSequence.isNotEmpty()) {
                    best.fullSequence.joinToString(" ➔ ") { it.textContent.ifBlank { "[${it.responseType}]" } }
                } else {
                    best.selectedAlternative?.textContent ?: "[${best.selectedAlternative?.responseType}]"
                }

                _sandboxResult.value = SandboxSimulationResult(
                    matched = true,
                    triggerName = best.trigger.name,
                    matchingMode = best.trigger.matchingMode,
                    matchedKeyword = best.matchedKeyword,
                    selectedReply = replyText,
                    estimatedDelayMs = best.computedDelayMs,
                    reason = "Matched trigger '${best.trigger.name}' via keyword '${best.matchedKeyword}' (${best.trigger.matchingMode})"
                )
            } else {
                val fallback = triggerEngine.getFallbackResponse(persona)
                _sandboxResult.value = SandboxSimulationResult(
                    matched = false,
                    fallbackReply = fallback,
                    reason = "No active trigger matched. Mode: ${persona.noMatchFallbackMode}"
                )
            }
        }
    }

    fun clearSandbox() {
        _sandboxResult.value = null
    }

    // --- Scheduled Message Operations ---
    fun saveSchedule(schedule: ScheduledMessageEntity) {
        viewModelScope.launch {
            val id = if (schedule.id == 0L) {
                repository.saveScheduledMessage(schedule)
            } else {
                repository.updateScheduledMessage(schedule)
                schedule.id
            }
            val updated = schedule.copy(id = id)
            schedulerManager.scheduleMessage(updated)
        }
    }

    fun toggleSchedule(schedule: ScheduledMessageEntity) {
        viewModelScope.launch {
            val updated = schedule.copy(isEnabled = !schedule.isEnabled)
            repository.updateScheduledMessage(updated)
            if (updated.isEnabled) {
                schedulerManager.scheduleMessage(updated)
            } else {
                schedulerManager.cancelSchedule(updated.id)
            }
        }
    }

    fun deleteSchedule(scheduleId: Long) {
        viewModelScope.launch {
            schedulerManager.cancelSchedule(scheduleId)
            repository.deleteScheduledMessage(ScheduledMessageEntity(id = scheduleId, personaId = 0, name = "", content = "", hour = 0, minute = 0))
        }
    }

    // --- Media Operations ---
    fun importMedia(uri: Uri, name: String, category: String) {
        viewModelScope.launch {
            repository.importMediaFromUri(uri, name, category)
        }
    }

    fun deleteMedia(item: MediaItemEntity) {
        viewModelScope.launch {
            repository.deleteMedia(item)
        }
    }

    // --- Personas & Profiles ---
    fun saveUserProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun saveBotPersona(persona: BotPersonaEntity) {
        viewModelScope.launch {
            if (persona.id == 0L) {
                repository.savePersona(persona)
            } else {
                repository.updatePersona(persona)
            }
        }
    }

    fun switchPersona(personaId: Long) {
        viewModelScope.launch {
            repository.switchPersona(personaId)
        }
    }

    fun deletePersona(persona: BotPersonaEntity) {
        viewModelScope.launch {
            repository.deletePersona(persona)
        }
    }

    // --- JSON Backup / Restore ---
    suspend fun exportJson(): String {
        val persona = currentPersona.value ?: return "{}"
        return repository.exportBackupJson(persona.id)
    }

    suspend fun importJson(json: String): Boolean {
        val persona = currentPersona.value ?: return false
        return repository.importBackupJson(persona.id, json)
    }

    companion object {
        fun provideFactory(
            repository: PersonaChatRepository,
            schedulerManager: SchedulerManager
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AdminViewModel(repository, schedulerManager) as T
                }
            }
    }
}
