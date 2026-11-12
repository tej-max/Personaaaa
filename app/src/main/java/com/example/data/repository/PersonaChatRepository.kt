package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.PersonaDao
import com.example.data.model.BotPersonaEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.KeywordTriggerEntity
import com.example.data.model.MediaItemEntity
import com.example.data.model.ResponseAlternativeEntity
import com.example.data.model.ScheduledMessageEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PersonaChatRepository(
    private val dao: PersonaDao,
    private val context: Context
) {

    // --- Profiles ---
    fun getUserProfile(): Flow<UserProfileEntity?> = dao.getUserProfile()
    suspend fun saveUserProfile(profile: UserProfileEntity) = dao.insertUserProfile(profile)

    fun getCurrentPersona(): Flow<BotPersonaEntity?> = dao.getCurrentPersona()
    fun getAllPersonas(): Flow<List<BotPersonaEntity>> = dao.getAllPersonas()
    suspend fun getCurrentPersonaSync(): BotPersonaEntity? = dao.getCurrentPersonaSync()
    suspend fun getPersonaByIdSync(id: Long): BotPersonaEntity? = dao.getPersonaByIdSync(id)
    suspend fun savePersona(persona: BotPersonaEntity): Long = dao.insertPersona(persona)
    suspend fun updatePersona(persona: BotPersonaEntity) = dao.updatePersona(persona)
    suspend fun deletePersona(persona: BotPersonaEntity) = dao.deletePersona(persona)
    suspend fun switchPersona(personaId: Long) = dao.setCurrentPersona(personaId)

    // --- Triggers ---
    fun getTriggersForPersona(personaId: Long): Flow<List<KeywordTriggerEntity>> =
        dao.getTriggersForPersona(personaId)

    suspend fun getActiveTriggersForPersonaSync(personaId: Long): List<KeywordTriggerEntity> =
        dao.getActiveTriggersForPersonaSync(personaId)

    suspend fun saveTrigger(trigger: KeywordTriggerEntity): Long = dao.insertTrigger(trigger)
    suspend fun updateTrigger(trigger: KeywordTriggerEntity) = dao.updateTrigger(trigger)
    suspend fun deleteTrigger(trigger: KeywordTriggerEntity) = dao.deleteTrigger(trigger)
    suspend fun deleteTriggerById(id: Long) = dao.deleteTriggerById(id)
    suspend fun duplicateTrigger(triggerId: Long): Long = dao.duplicateTriggerWithAlternatives(triggerId)
    suspend fun updateTriggerTimestamp(triggerId: Long, timestamp: Long) =
        dao.updateTriggerTimestamp(triggerId, timestamp)

    // --- Alternatives ---
    fun getAlternativesForTrigger(triggerId: Long): Flow<List<ResponseAlternativeEntity>> =
        dao.getAlternativesForTrigger(triggerId)

    suspend fun getActiveAlternativesForTriggerSync(triggerId: Long): List<ResponseAlternativeEntity> =
        dao.getActiveAlternativesForTriggerSync(triggerId)

    suspend fun getAllAlternativesForTriggerSync(triggerId: Long): List<ResponseAlternativeEntity> =
        dao.getAllAlternativesForTriggerSync(triggerId)

    suspend fun saveAlternative(alternative: ResponseAlternativeEntity): Long =
        dao.insertAlternative(alternative)

    suspend fun saveAlternatives(alternatives: List<ResponseAlternativeEntity>) =
        dao.insertAlternatives(alternatives)

    suspend fun updateAlternative(alternative: ResponseAlternativeEntity) =
        dao.updateAlternative(alternative)

    suspend fun deleteAlternative(alternative: ResponseAlternativeEntity) =
        dao.deleteAlternative(alternative)

    suspend fun deleteAlternativesForTrigger(triggerId: Long) =
        dao.deleteAlternativesForTrigger(triggerId)

    suspend fun updateAlternativeLastUsed(id: Long, timestamp: Long) =
        dao.updateAlternativeLastUsed(id, timestamp)

    // --- Messages ---
    fun getMessagesForPersona(personaId: Long): Flow<List<ChatMessageEntity>> =
        dao.getMessagesForPersona(personaId)

    fun searchMessages(personaId: Long, query: String): Flow<List<ChatMessageEntity>> =
        dao.searchMessages(personaId, query)

    suspend fun getRecentBotMessageSync(personaId: Long): ChatMessageEntity? =
        dao.getRecentBotMessageSync(personaId)

    suspend fun insertMessage(message: ChatMessageEntity): Long = dao.insertMessage(message)
    suspend fun deleteMessageById(id: Long) = dao.deleteMessageById(id)
    suspend fun clearMessagesForPersona(personaId: Long) = dao.clearMessagesForPersona(personaId)

    // --- Scheduled Messages ---
    fun getScheduledMessagesForPersona(personaId: Long): Flow<List<ScheduledMessageEntity>> =
        dao.getScheduledMessagesForPersona(personaId)

    suspend fun getAllEnabledScheduledMessagesSync(): List<ScheduledMessageEntity> =
        dao.getAllEnabledScheduledMessagesSync()

    suspend fun saveScheduledMessage(schedule: ScheduledMessageEntity): Long =
        dao.insertScheduledMessage(schedule)

    suspend fun updateScheduledMessage(schedule: ScheduledMessageEntity) =
        dao.updateScheduledMessage(schedule)

    suspend fun deleteScheduledMessage(schedule: ScheduledMessageEntity) =
        dao.deleteScheduledMessage(schedule)

    // --- Media Library ---
    fun getAllMedia(): Flow<List<MediaItemEntity>> = dao.getAllMedia()
    fun getMediaCount(): Flow<Int> = dao.getMediaCount()
    fun getGifCount(): Flow<Int> = dao.getGifCount()
    fun getImageCount(): Flow<Int> = dao.getImageCount()
    suspend fun insertMedia(media: MediaItemEntity): Long = dao.insertMedia(media)
    suspend fun deleteMedia(media: MediaItemEntity) {
        // Also remove local file
        withContext(Dispatchers.IO) {
            try {
                val file = File(media.localPath)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
        dao.deleteMedia(media)
    }

    suspend fun importMediaFromUri(uri: Uri, name: String, category: String): MediaItemEntity? =
        withContext(Dispatchers.IO) {
            try {
                val mediaDir = File(context.filesDir, "media").apply { mkdirs() }
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val isGif = mimeType.contains("gif", ignoreCase = true) ||
                        uri.toString().contains(".gif", ignoreCase = true)
                val ext = if (isGif) ".gif" else ".png"
                val targetFile = File(mediaDir, "media_${System.currentTimeMillis()}$ext")

                context.contentResolver.openInputStream(uri)?.use { input: InputStream ->
                    FileOutputStream(targetFile).use { output: FileOutputStream ->
                        input.copyTo(output)
                    }
                }

                val mediaEntity = MediaItemEntity(
                    name = name.ifBlank { targetFile.nameWithoutExtension },
                    mediaType = if (isGif) "GIF" else "IMAGE",
                    localPath = targetFile.absolutePath,
                    category = category,
                    fileSizeBytes = targetFile.length(),
                    createdAt = System.currentTimeMillis()
                )
                val id = dao.insertMedia(mediaEntity)
                mediaEntity.copy(id = id)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    // --- Backup & Restore JSON ---
    suspend fun exportBackupJson(personaId: Long): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val persona = dao.getPersonaByIdSync(personaId) ?: return@withContext "{}"
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val personaObj = JSONObject().apply {
            put("name", persona.name)
            put("about", persona.about)
            put("typingMode", persona.typingMode)
            put("fixedDelaySeconds", persona.fixedDelaySeconds)
            put("minDelaySeconds", persona.minDelaySeconds)
            put("maxDelaySeconds", persona.maxDelaySeconds)
            put("charsPerSecond", persona.charsPerSecond)
            put("noMatchFallbackMode", persona.noMatchFallbackMode)
            put("fallbackResponsesJson", persona.fallbackResponsesJson)
            put("avoidImmediateRepeat", persona.avoidImmediateRepeat)
        }
        root.put("persona", personaObj)

        val triggersArray = JSONArray()
        val triggers = dao.getAllTriggersForPersonaSync(personaId)
        for (trigger in triggers) {
            val triggerObj = JSONObject().apply {
                put("name", trigger.name)
                put("category", trigger.category)
                put("primaryKeyword", trigger.primaryKeyword)
                put("aliasesJson", trigger.aliasesJson)
                put("matchingMode", trigger.matchingMode)
                put("caseSensitive", trigger.caseSensitive)
                put("ignorePunctuation", trigger.ignorePunctuation)
                put("priority", trigger.priority)
                put("cooldownSeconds", trigger.cooldownSeconds)
                put("isEnabled", trigger.isEnabled)
            }
            val alts = dao.getAllAlternativesForTriggerSync(trigger.id)
            val altsArray = JSONArray()
            for (alt in alts) {
                val altObj = JSONObject().apply {
                    put("responseType", alt.responseType)
                    put("textContent", alt.textContent)
                    put("mediaUri", alt.mediaUri ?: "")
                    put("delaySeconds", alt.delaySeconds)
                    put("sequenceOrder", alt.sequenceOrder)
                    put("isEnabled", alt.isEnabled)
                }
                altsArray.put(altObj)
            }
            triggerObj.put("alternatives", altsArray)
            triggersArray.put(triggerObj)
        }
        root.put("triggers", triggersArray)
        root.toString(2)
    }

    suspend fun importBackupJson(personaId: Long, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("triggers")) {
                val triggersArray = root.getJSONArray("triggers")
                for (i in 0 until triggersArray.length()) {
                    val triggerObj = triggersArray.getJSONObject(i)
                    val trigger = KeywordTriggerEntity(
                        personaId = personaId,
                        name = triggerObj.getString("name"),
                        category = triggerObj.optString("category", "General"),
                        primaryKeyword = triggerObj.getString("primaryKeyword"),
                        aliasesJson = triggerObj.optString("aliasesJson", "[]"),
                        matchingMode = triggerObj.optString("matchingMode", "WHOLE_WORD"),
                        caseSensitive = triggerObj.optBoolean("caseSensitive", false),
                        ignorePunctuation = triggerObj.optBoolean("ignorePunctuation", true),
                        priority = triggerObj.optInt("priority", 1),
                        cooldownSeconds = triggerObj.optLong("cooldownSeconds", 0),
                        isEnabled = triggerObj.optBoolean("isEnabled", true)
                    )
                    val triggerId = dao.insertTrigger(trigger)
                    if (triggerObj.has("alternatives")) {
                        val altsArray = triggerObj.getJSONArray("alternatives")
                        val altsList = mutableListOf<ResponseAlternativeEntity>()
                        for (j in 0 until altsArray.length()) {
                            val altObj = altsArray.getJSONObject(j)
                            altsList.add(
                                ResponseAlternativeEntity(
                                    triggerId = triggerId,
                                    responseType = altObj.optString("responseType", "TEXT"),
                                    textContent = altObj.optString("textContent", ""),
                                    mediaUri = altObj.optString("mediaUri", null).takeIf { !it.isNullOrBlank() },
                                    delaySeconds = altObj.optDouble("delaySeconds", 1.5).toFloat(),
                                    sequenceOrder = altObj.optInt("sequenceOrder", 0),
                                    isEnabled = altObj.optBoolean("isEnabled", true)
                                )
                            )
                        }
                        dao.insertAlternatives(altsList)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
