package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BotPersonaEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.KeywordTriggerEntity
import com.example.data.model.MediaItemEntity
import com.example.data.model.RepeatFrequency
import com.example.data.model.ResponseAlternativeEntity
import com.example.data.model.ScheduledMessageEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        BotPersonaEntity::class,
        KeywordTriggerEntity::class,
        ResponseAlternativeEntity::class,
        ChatMessageEntity::class,
        ScheduledMessageEntity::class,
        MediaItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun personaDao(): PersonaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personachat.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-populate database with starter user, default "Nana" persona, and sample rules
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    val dao = database.personaDao()
                    populateStarterData(dao)
                }
            }
        }

        private suspend fun populateStarterData(dao: PersonaDao) {
            // 1. User Profile
            dao.insertUserProfile(
                UserProfileEntity(
                    id = 1,
                    displayName = "Me",
                    about = "Chatting with my offline personas",
                    avatarUri = null,
                    themeColorHex = 0xFF8B5CF6
                )
            )

            // 2. Default Bot Persona: Nana
            val nanaId = dao.insertPersona(
                BotPersonaEntity(
                    name = "Nana",
                    about = "Your warm, witty offline conversation partner",
                    avatarUri = null,
                    themeColorHex = 0xFFA78BFA,
                    isCurrent = true,
                    typingMode = "CHARACTER_BASED",
                    fixedDelaySeconds = 2.0f,
                    minDelaySeconds = 1.0f,
                    maxDelaySeconds = 4.0f,
                    charsPerSecond = 24,
                    noMatchFallbackMode = "DEFAULT_MESSAGE",
                    fallbackResponsesJson = "[\"Hmm, tell me more about that!\", \"That's fascinating! What happened next?\", \"I'm right here with you.\"]",
                    avoidImmediateRepeat = true,
                    enableTypingIndicator = true,
                    priorityHandling = "HIGHEST_ONLY"
                )
            )

            // 3. Starter Keyword Triggers & Alternatives
            // Trigger 1: Greeting
            val greetingId = dao.insertTrigger(
                KeywordTriggerEntity(
                    personaId = nanaId,
                    name = "Greeting",
                    category = "Greetings",
                    primaryKeyword = "hi",
                    aliasesJson = "[\"hello\", \"hey\", \"morning\", \"heyy\", \"yo\"]",
                    matchingMode = "WHOLE_WORD",
                    caseSensitive = false,
                    ignorePunctuation = true,
                    priority = 10,
                    cooldownSeconds = 0
                )
            )
            dao.insertAlternatives(
                listOf(
                    ResponseAlternativeEntity(triggerId = greetingId, textContent = "Hey there! How's your day going?"),
                    ResponseAlternativeEntity(triggerId = greetingId, textContent = "Hello! It's so nice to hear from you."),
                    ResponseAlternativeEntity(triggerId = greetingId, textContent = "Hi Nana! What are you up to today?"),
                    ResponseAlternativeEntity(triggerId = greetingId, textContent = "Hey! I was just thinking about you.")
                )
            )

            // Trigger 2: Hug / Affection
            val hugId = dao.insertTrigger(
                KeywordTriggerEntity(
                    personaId = nanaId,
                    name = "Hug & Comfort",
                    category = "Affection",
                    primaryKeyword = "hug",
                    aliasesJson = "[\"hugs\", \"cuddle\", \"miss you\"]",
                    matchingMode = "WHOLE_WORD",
                    caseSensitive = false,
                    ignorePunctuation = true,
                    priority = 5,
                    cooldownSeconds = 0
                )
            )
            dao.insertAlternatives(
                listOf(
                    ResponseAlternativeEntity(triggerId = hugId, textContent = "Sending you the biggest, warmest virtual hug right now! 🤗"),
                    ResponseAlternativeEntity(triggerId = hugId, textContent = "Come here! *Big warm hug* Always here for you."),
                    ResponseAlternativeEntity(triggerId = hugId, textContent = "Aww, hugs! Hope this brings a smile to your face today.")
                )
            )

            // Trigger 3: How are you
            val howAreYouId = dao.insertTrigger(
                KeywordTriggerEntity(
                    personaId = nanaId,
                    name = "How are you",
                    category = "Questions",
                    primaryKeyword = "how are you",
                    aliasesJson = "[\"how are you doing\", \"how's it going\", \"how have you been\"]",
                    matchingMode = "PHRASE",
                    caseSensitive = false,
                    ignorePunctuation = true,
                    priority = 8,
                    cooldownSeconds = 0
                )
            )
            dao.insertAlternatives(
                listOf(
                    ResponseAlternativeEntity(triggerId = howAreYouId, textContent = "I'm doing really well, thanks for asking! How about you?"),
                    ResponseAlternativeEntity(triggerId = howAreYouId, textContent = "Feeling peaceful and happy today. What's on your mind?"),
                    ResponseAlternativeEntity(triggerId = howAreYouId, textContent = "Never better! Tell me something interesting that happened today.")
                )
            )

            // Trigger 4: Feeling Down
            val sadId = dao.insertTrigger(
                KeywordTriggerEntity(
                    personaId = nanaId,
                    name = "Emotional Support",
                    category = "Emotions",
                    primaryKeyword = "sad",
                    aliasesJson = "[\"upset\", \"down\", \"tired\", \"lonely\", \"stressed\"]",
                    matchingMode = "WHOLE_WORD",
                    caseSensitive = false,
                    ignorePunctuation = true,
                    priority = 7,
                    cooldownSeconds = 0
                )
            )
            dao.insertAlternatives(
                listOf(
                    ResponseAlternativeEntity(triggerId = sadId, textContent = "I'm sorry you're feeling that way. Remember to take a deep breath, you're doing better than you think."),
                    ResponseAlternativeEntity(triggerId = sadId, textContent = "I'm right here listening. Take all the time you need, okay?"),
                    ResponseAlternativeEntity(triggerId = sadId, textContent = "Sending you extra comfort. Want to talk about it or just chill together?")
                )
            )

            // Trigger 5: Good Night
            val gnId = dao.insertTrigger(
                KeywordTriggerEntity(
                    personaId = nanaId,
                    name = "Good Night",
                    category = "Greetings",
                    primaryKeyword = "good night",
                    aliasesJson = "[\"gn\", \"sleep well\", \"sweet dreams\"]",
                    matchingMode = "PHRASE",
                    caseSensitive = false,
                    ignorePunctuation = true,
                    priority = 6,
                    cooldownSeconds = 0
                )
            )
            dao.insertAlternatives(
                listOf(
                    ResponseAlternativeEntity(triggerId = gnId, textContent = "Good night! Sleep tight and have wonderful dreams."),
                    ResponseAlternativeEntity(triggerId = gnId, textContent = "Night night! Rest well and talk to you tomorrow.")
                )
            )

            // Trigger 6: Jokes
            val jokeId = dao.insertTrigger(
                KeywordTriggerEntity(
                    personaId = nanaId,
                    name = "Tell a Joke",
                    category = "Reactions",
                    primaryKeyword = "joke",
                    aliasesJson = "[\"funny\", \"make me laugh\"]",
                    matchingMode = "WHOLE_WORD",
                    caseSensitive = false,
                    ignorePunctuation = true,
                    priority = 4,
                    cooldownSeconds = 0
                )
            )
            dao.insertAlternatives(
                listOf(
                    ResponseAlternativeEntity(triggerId = jokeId, textContent = "Why don't scientists trust atoms? Because they make up everything! 😄"),
                    ResponseAlternativeEntity(triggerId = jokeId, textContent = "I told my computer I needed a break, and now it won't stop sending me Kit-Kat ads."),
                    ResponseAlternativeEntity(triggerId = jokeId, textContent = "What do you call a fake noodle? An impasta!")
                )
            )

            // Trigger 7: Love
            val loveId = dao.insertTrigger(
                KeywordTriggerEntity(
                    personaId = nanaId,
                    name = "Love",
                    category = "Affection",
                    primaryKeyword = "love",
                    aliasesJson = "[\"love you\", \"<3\"]",
                    matchingMode = "CONTAINS",
                    caseSensitive = false,
                    ignorePunctuation = true,
                    priority = 3,
                    cooldownSeconds = 0
                )
            )
            dao.insertAlternatives(
                listOf(
                    ResponseAlternativeEntity(triggerId = loveId, textContent = "Much love right back at you! Always."),
                    ResponseAlternativeEntity(triggerId = loveId, textContent = "Aww you're the best! That made my day.")
                )
            )

            // 4. Starter Scheduled Message
            dao.insertScheduledMessage(
                ScheduledMessageEntity(
                    personaId = nanaId,
                    name = "Morning Check-in",
                    content = "Good morning Nana! Hope you have a wonderful and productive day today! ☀️",
                    hour = 8,
                    minute = 30,
                    repeatFrequency = RepeatFrequency.DAILY.name,
                    isEnabled = true
                )
            )

            // 5. Initial Welcome Chat Message from Bot
            dao.insertMessage(
                ChatMessageEntity(
                    personaId = nanaId,
                    sender = "BOT",
                    content = "Hey Nana! I'm your offline conversation companion. Say 'Hi', 'How are you', 'Hug', or tell me a 'Joke'!",
                    timestamp = System.currentTimeMillis() - 60000
                )
            )
        }
    }
}
