package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val displayName: String = "Me",
    val about: String = "Available",
    val avatarUri: String? = null,
    val themeColorHex: Long = 0xFF8B5CF6
)

@Entity(tableName = "bot_personas")
data class BotPersonaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val about: String = "Offline conversation simulator",
    val avatarUri: String? = null,
    val themeColorHex: Long = 0xFFA78BFA,
    val isCurrent: Boolean = false,
    val typingMode: String = TypingMode.CHARACTER_BASED.name,
    val fixedDelaySeconds: Float = 2.0f,
    val minDelaySeconds: Float = 1.0f,
    val maxDelaySeconds: Float = 4.5f,
    val charsPerSecond: Int = 22,
    val noMatchFallbackMode: String = FallbackMode.DEFAULT_MESSAGE.name,
    val fallbackResponsesJson: String = "[\"Hmm, tell me more about that!\", \"That's interesting! What else?\", \"I'm listening, go on.\"]",
    val avoidImmediateRepeat: Boolean = true,
    val enableTypingIndicator: Boolean = true,
    val priorityHandling: String = "HIGHEST_ONLY", // HIGHEST_ONLY or ALL_SEQUENTIAL
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "keyword_triggers",
    foreignKeys = [
        ForeignKey(
            entity = BotPersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personaId"])]
)
data class KeywordTriggerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personaId: Long,
    val name: String,
    val category: String = "General",
    val primaryKeyword: String,
    val aliasesJson: String = "[]",
    val matchingMode: String = MatchingMode.WHOLE_WORD.name,
    val caseSensitive: Boolean = false,
    val ignorePunctuation: Boolean = true,
    val priority: Int = 1, // Higher priority wins
    val cooldownSeconds: Long = 0,
    val lastTriggeredTimestamp: Long = 0,
    val isEnabled: Boolean = true,
    val contextConditionBotContains: String? = null // Optional: previous bot response must contain this
)

@Entity(
    tableName = "response_alternatives",
    foreignKeys = [
        ForeignKey(
            entity = KeywordTriggerEntity::class,
            parentColumns = ["id"],
            childColumns = ["triggerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["triggerId"])]
)
data class ResponseAlternativeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val triggerId: Long,
    val responseType: String = ResponseType.TEXT.name,
    val textContent: String = "",
    val mediaUri: String? = null,
    val delaySeconds: Float = 1.5f, // delay for this item or typing pause
    val sequenceOrder: Int = 0, // 0 for standalone alternative, or 0..N for multi-step sequences
    val sequenceGroupId: Long = 0, // group identifier for multi-step sequences
    val weight: Int = 1,
    val isEnabled: Boolean = true,
    val lastUsedTimestamp: Long = 0
)

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["personaId"]), Index(value = ["timestamp"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personaId: Long,
    val sender: String, // "USER" or "BOT"
    val messageType: String = MessageType.TEXT.name,
    val content: String,
    val mediaUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerId: Long? = null,
    val scheduledMessageId: Long? = null,
    val status: String = MessageStatus.READ.name
)

@Entity(
    tableName = "scheduled_messages",
    indices = [Index(value = ["personaId"])]
)
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personaId: Long,
    val name: String,
    val messageType: String = MessageType.TEXT.name,
    val content: String,
    val mediaUri: String? = null,
    val hour: Int, // 0..23
    val minute: Int, // 0..59
    val repeatFrequency: String = RepeatFrequency.DAILY.name,
    val daysOfWeekMask: Int = 127, // Bitmask for Sun(1)..Sat(64)
    val isEnabled: Boolean = true,
    val lastSentTimestamp: Long = 0
)

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mediaType: String, // "IMAGE" or "GIF"
    val localPath: String,
    val category: String = "General",
    val fileSizeBytes: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
