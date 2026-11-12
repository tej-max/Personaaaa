package com.example

import com.example.data.model.BotPersonaEntity
import com.example.data.model.KeywordTriggerEntity
import com.example.data.model.MatchingMode
import com.example.data.model.ResponseAlternativeEntity
import com.example.engine.TriggerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val engine = TriggerEngine()

    @Test
    fun testExactMatchingMode() {
        val mode = MatchingMode.EXACT.name
        assertTrue(engine.matchesKeyword("Hi", "Hi", mode, caseSensitive = false, ignorePunctuation = true))
        assertTrue(engine.matchesKeyword("  hi!  ", "hi", mode, caseSensitive = false, ignorePunctuation = true))
        assertFalse(engine.matchesKeyword("Hi Nana", "Hi", mode, caseSensitive = false, ignorePunctuation = true))
        assertFalse(engine.matchesKeyword("Hello Hi", "Hi", mode, caseSensitive = false, ignorePunctuation = true))
    }

    @Test
    fun testWholeWordMatchingMode() {
        val mode = MatchingMode.WHOLE_WORD.name
        // Should match whole word anywhere
        assertTrue(engine.matchesKeyword("Hi Nana", "Hi", mode, caseSensitive = false, ignorePunctuation = true))
        assertTrue(engine.matchesKeyword("Hi, how are you?", "Hi", mode, caseSensitive = false, ignorePunctuation = true))
        assertTrue(engine.matchesKeyword("Say Hi to him.", "Hi", mode, caseSensitive = false, ignorePunctuation = true))

        // Should NOT match substring inside words
        assertFalse(engine.matchesKeyword("Hilarious", "Hi", mode, caseSensitive = false, ignorePunctuation = true))
        assertFalse(engine.matchesKeyword("High", "Hi", mode, caseSensitive = false, ignorePunctuation = true))
        assertFalse(engine.matchesKeyword("This is it", "is", mode, caseSensitive = false, ignorePunctuation = true).not()) // "is" is standalone here, matches
        assertFalse(engine.matchesKeyword("History", "Hi", mode, caseSensitive = false, ignorePunctuation = true))
    }

    @Test
    fun testPhraseMatchingMode() {
        val mode = MatchingMode.PHRASE.name
        assertTrue(engine.matchesKeyword("Hey, good morning Nana!", "good morning", mode, caseSensitive = false, ignorePunctuation = true))
        assertFalse(engine.matchesKeyword("Good evening everyone", "good morning", mode, caseSensitive = false, ignorePunctuation = true))
    }

    @Test
    fun testContainsMatchingMode() {
        val mode = MatchingMode.CONTAINS.name
        assertTrue(engine.matchesKeyword("Lovely", "love", mode, caseSensitive = false, ignorePunctuation = true))
        assertTrue(engine.matchesKeyword("I love this", "love", mode, caseSensitive = false, ignorePunctuation = true))
        assertTrue(engine.matchesKeyword("Glove", "love", mode, caseSensitive = false, ignorePunctuation = true))
    }

    @Test
    fun testTriggerPriorityResolution() {
        val persona = BotPersonaEntity(id = 1, name = "TestBot", priorityHandling = "HIGHEST_ONLY")
        val lowPriority = KeywordTriggerEntity(
            id = 1, personaId = 1, name = "Low", primaryKeyword = "hi",
            priority = 1, matchingMode = MatchingMode.WHOLE_WORD.name
        )
        val highPriority = KeywordTriggerEntity(
            id = 2, personaId = 1, name = "High", primaryKeyword = "hi",
            priority = 10, matchingMode = MatchingMode.WHOLE_WORD.name
        )

        val alts = mapOf(
            1L to listOf(ResponseAlternativeEntity(id = 101, triggerId = 1, textContent = "Low reply")),
            2L to listOf(ResponseAlternativeEntity(id = 102, triggerId = 2, textContent = "High reply"))
        )

        val results = engine.evaluate(
            userMessage = "Hi Nana",
            triggers = listOf(lowPriority, highPriority),
            alternativesByTriggerId = alts,
            persona = persona
        )

        assertEquals(1, results.size)
        assertEquals("High", results.first().trigger.name)
        assertEquals("High reply", results.first().selectedAlternative?.textContent)
    }

    @Test
    fun testCooldownEnforcement() {
        val persona = BotPersonaEntity(id = 1, name = "TestBot")
        val trigger = KeywordTriggerEntity(
            id = 1, personaId = 1, name = "CooldownTrigger", primaryKeyword = "test",
            cooldownSeconds = 60, lastTriggeredTimestamp = System.currentTimeMillis() - 10000 // 10s ago, cooldown 60s
        )

        val alts = mapOf(
            1L to listOf(ResponseAlternativeEntity(id = 1, triggerId = 1, textContent = "Reply"))
        )

        val results = engine.evaluate(
            userMessage = "test",
            triggers = listOf(trigger),
            alternativesByTriggerId = alts,
            persona = persona
        )

        // Must be empty because cooldown is still active
        assertTrue(results.isEmpty())
    }

    @Test
    fun testTypingDelayCalculation() {
        val persona = BotPersonaEntity(
            id = 1,
            name = "Nana",
            typingMode = "CHARACTER_BASED",
            charsPerSecond = 20,
            minDelaySeconds = 1.0f,
            maxDelaySeconds = 4.0f
        )

        val shortDelay = engine.calculateDelay("Hi", persona)
        assertEquals(1000L, shortDelay) // clamped to min 1.0s

        val longText = "This is a much longer response that should simulate realistic human conversation typing speed properly."
        val longDelay = engine.calculateDelay(longText, persona)
        assertTrue(longDelay in 1000L..4000L)
    }

    @Test
    fun testScreenBottomNavItemsNotNull() {
        val items = com.example.ui.navigation.Screen.bottomNavItems
        assertEquals(5, items.size)
        items.forEach { screen ->
            assertNotNull(screen)
            assertNotNull(screen.route)
            assertTrue(screen.route.isNotEmpty())
            assertNotNull(screen.title)
            assertTrue(screen.title.isNotEmpty())
            assertNotNull(screen.selectedIcon)
            assertNotNull(screen.unselectedIcon)
        }
    }
}
