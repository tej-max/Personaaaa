package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.BotPersonaEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.KeywordTriggerEntity
import com.example.data.model.MediaItemEntity
import com.example.data.model.ResponseAlternativeEntity
import com.example.data.model.ScheduledMessageEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // --- Bot Personas ---
    @Query("SELECT * FROM bot_personas ORDER BY createdAt ASC")
    fun getAllPersonas(): Flow<List<BotPersonaEntity>>

    @Query("SELECT * FROM bot_personas WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentPersona(): Flow<BotPersonaEntity?>

    @Query("SELECT * FROM bot_personas WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentPersonaSync(): BotPersonaEntity?

    @Query("SELECT * FROM bot_personas WHERE id = :id LIMIT 1")
    fun getPersonaById(id: Long): Flow<BotPersonaEntity?>

    @Query("SELECT * FROM bot_personas WHERE id = :id LIMIT 1")
    suspend fun getPersonaByIdSync(id: Long): BotPersonaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: BotPersonaEntity): Long

    @Update
    suspend fun updatePersona(persona: BotPersonaEntity)

    @Delete
    suspend fun deletePersona(persona: BotPersonaEntity)

    @Query("UPDATE bot_personas SET isCurrent = CASE WHEN id = :personaId THEN 1 ELSE 0 END")
    suspend fun setCurrentPersona(personaId: Long)

    // --- Keyword Triggers ---
    @Query("SELECT * FROM keyword_triggers WHERE personaId = :personaId ORDER BY priority DESC, id ASC")
    fun getTriggersForPersona(personaId: Long): Flow<List<KeywordTriggerEntity>>

    @Query("SELECT * FROM keyword_triggers WHERE personaId = :personaId AND isEnabled = 1 ORDER BY priority DESC, id ASC")
    suspend fun getActiveTriggersForPersonaSync(personaId: Long): List<KeywordTriggerEntity>

    @Query("SELECT * FROM keyword_triggers WHERE personaId = :personaId")
    suspend fun getAllTriggersForPersonaSync(personaId: Long): List<KeywordTriggerEntity>

    @Query("SELECT * FROM keyword_triggers WHERE id = :id LIMIT 1")
    fun getTriggerById(id: Long): Flow<KeywordTriggerEntity?>

    @Query("SELECT * FROM keyword_triggers WHERE id = :id LIMIT 1")
    suspend fun getTriggerByIdSync(id: Long): KeywordTriggerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrigger(trigger: KeywordTriggerEntity): Long

    @Update
    suspend fun updateTrigger(trigger: KeywordTriggerEntity)

    @Delete
    suspend fun deleteTrigger(trigger: KeywordTriggerEntity)

    @Query("DELETE FROM keyword_triggers WHERE id = :triggerId")
    suspend fun deleteTriggerById(triggerId: Long)

    @Query("UPDATE keyword_triggers SET lastTriggeredTimestamp = :timestamp WHERE id = :triggerId")
    suspend fun updateTriggerTimestamp(triggerId: Long, timestamp: Long)

    // --- Response Alternatives ---
    @Query("SELECT * FROM response_alternatives WHERE triggerId = :triggerId ORDER BY sequenceOrder ASC, id ASC")
    fun getAlternativesForTrigger(triggerId: Long): Flow<List<ResponseAlternativeEntity>>

    @Query("SELECT * FROM response_alternatives WHERE triggerId = :triggerId AND isEnabled = 1 ORDER BY sequenceOrder ASC, id ASC")
    suspend fun getActiveAlternativesForTriggerSync(triggerId: Long): List<ResponseAlternativeEntity>

    @Query("SELECT * FROM response_alternatives WHERE triggerId = :triggerId ORDER BY sequenceOrder ASC, id ASC")
    suspend fun getAllAlternativesForTriggerSync(triggerId: Long): List<ResponseAlternativeEntity>

    @Query("SELECT ra.* FROM response_alternatives ra INNER JOIN keyword_triggers kt ON ra.triggerId = kt.id WHERE kt.personaId = :personaId")
    suspend fun getAllAlternativesForPersonaSync(personaId: Long): List<ResponseAlternativeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlternative(alternative: ResponseAlternativeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlternatives(alternatives: List<ResponseAlternativeEntity>)

    @Update
    suspend fun updateAlternative(alternative: ResponseAlternativeEntity)

    @Delete
    suspend fun deleteAlternative(alternative: ResponseAlternativeEntity)

    @Query("DELETE FROM response_alternatives WHERE triggerId = :triggerId")
    suspend fun deleteAlternativesForTrigger(triggerId: Long)

    @Query("UPDATE response_alternatives SET lastUsedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateAlternativeLastUsed(id: Long, timestamp: Long)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE personaId = :personaId ORDER BY timestamp ASC")
    fun getMessagesForPersona(personaId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE personaId = :personaId AND sender = 'BOT' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getRecentBotMessageSync(personaId: Long): ChatMessageEntity?

    @Query("SELECT * FROM chat_messages WHERE personaId = :personaId AND content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(personaId: Long, query: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM chat_messages WHERE personaId = :personaId")
    suspend fun clearMessagesForPersona(personaId: Long)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE personaId = :personaId")
    fun getMessageCountForPersona(personaId: Long): Flow<Int>

    // --- Scheduled Messages ---
    @Query("SELECT * FROM scheduled_messages WHERE personaId = :personaId ORDER BY hour ASC, minute ASC")
    fun getScheduledMessagesForPersona(personaId: Long): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE isEnabled = 1")
    suspend fun getAllEnabledScheduledMessagesSync(): List<ScheduledMessageEntity>

    @Query("SELECT * FROM scheduled_messages WHERE id = :id LIMIT 1")
    suspend fun getScheduledMessageByIdSync(id: Long): ScheduledMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMessage(schedule: ScheduledMessageEntity): Long

    @Update
    suspend fun updateScheduledMessage(schedule: ScheduledMessageEntity)

    @Delete
    suspend fun deleteScheduledMessage(schedule: ScheduledMessageEntity)

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun deleteScheduledMessageById(id: Long)

    // --- Media Items ---
    @Query("SELECT * FROM media_items ORDER BY createdAt DESC")
    fun getAllMedia(): Flow<List<MediaItemEntity>>

    @Query("SELECT COUNT(*) FROM media_items")
    fun getMediaCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_items WHERE mediaType = 'GIF'")
    fun getGifCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_items WHERE mediaType = 'IMAGE'")
    fun getImageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaItemEntity): Long

    @Delete
    suspend fun deleteMedia(media: MediaItemEntity)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    // --- Full persona trigger deletion ---
    @Transaction
    suspend fun duplicateTriggerWithAlternatives(triggerId: Long): Long {
        val original = getTriggerByIdSync(triggerId) ?: return -1
        val alternatives = getAllAlternativesForTriggerSync(triggerId)
        val newTrigger = original.copy(
            id = 0,
            name = "${original.name} (Copy)",
            lastTriggeredTimestamp = 0
        )
        val newId = insertTrigger(newTrigger)
        val newAlternatives = alternatives.map {
            it.copy(id = 0, triggerId = newId, lastUsedTimestamp = 0)
        }
        insertAlternatives(newAlternatives)
        return newId
    }
}
