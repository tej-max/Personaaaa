package com.example.engine

import com.example.data.model.BotPersonaEntity
import com.example.data.model.KeywordTriggerEntity
import com.example.data.model.MatchingMode
import com.example.data.model.ResponseAlternativeEntity
import org.json.JSONArray
import java.util.regex.Pattern

data class TriggerMatchResult(
    val trigger: KeywordTriggerEntity,
    val matchedKeyword: String,
    val selectedAlternative: ResponseAlternativeEntity?,
    val fullSequence: List<ResponseAlternativeEntity> = emptyList(),
    val computedDelayMs: Long
)

class TriggerEngine {

    /**
     * Evaluates a user message against a list of active triggers for the persona.
     */
    fun evaluate(
        userMessage: String,
        triggers: List<KeywordTriggerEntity>,
        alternativesByTriggerId: Map<Long, List<ResponseAlternativeEntity>>,
        persona: BotPersonaEntity,
        previousBotMessageContent: String? = null,
        currentTime: Long = System.currentTimeMillis()
    ): List<TriggerMatchResult> {
        val matches = mutableListOf<TriggerMatchResult>()

        // Sort triggers by priority DESC, id ASC
        val sortedTriggers = triggers.sortedWith(
            compareByDescending<KeywordTriggerEntity> { it.priority }
                .thenBy { it.id }
        )

        for (trigger in sortedTriggers) {
            if (!trigger.isEnabled) continue

            // Check Cooldown
            if (trigger.cooldownSeconds > 0) {
                val elapsed = currentTime - trigger.lastTriggeredTimestamp
                if (elapsed < trigger.cooldownSeconds * 1000) {
                    continue // Cooldown active, skip
                }
            }

            // Check Context Condition (e.g., previous bot response must contain specific text)
            if (!trigger.contextConditionBotContains.isNullOrBlank()) {
                val botPrev = previousBotMessageContent.orEmpty()
                if (!botPrev.contains(trigger.contextConditionBotContains, ignoreCase = true)) {
                    continue // Context condition not satisfied
                }
            }

            // Collect all test keywords (primary keyword + aliases)
            val keywords = mutableListOf(trigger.primaryKeyword)
            try {
                val array = JSONArray(trigger.aliasesJson)
                for (i in 0 until array.length()) {
                    val alias = array.getString(i).trim()
                    if (alias.isNotEmpty()) keywords.add(alias)
                }
            } catch (_: Exception) {}

            var matchedKeyword: String? = null
            for (kw in keywords) {
                if (matchesKeyword(userMessage, kw, trigger.matchingMode, trigger.caseSensitive, trigger.ignorePunctuation)) {
                    matchedKeyword = kw
                    break
                }
            }

            if (matchedKeyword != null) {
                val alternatives = alternativesByTriggerId[trigger.id]?.filter { it.isEnabled }.orEmpty()
                if (alternatives.isNotEmpty()) {
                    // Check if it's a multi-step sequence
                    val isSequence = alternatives.any { it.sequenceOrder > 0 }
                    val selectedAlt: ResponseAlternativeEntity?
                    val sequenceList: List<ResponseAlternativeEntity>

                    if (isSequence) {
                        sequenceList = alternatives.sortedBy { it.sequenceOrder }
                        selectedAlt = sequenceList.firstOrNull()
                    } else {
                        sequenceList = emptyList()
                        selectedAlt = selectAlternative(alternatives, persona.avoidImmediateRepeat)
                    }

                    val delay = calculateDelay(selectedAlt?.textContent.orEmpty(), persona)
                    matches.add(
                        TriggerMatchResult(
                            trigger = trigger,
                            matchedKeyword = matchedKeyword,
                            selectedAlternative = selectedAlt,
                            fullSequence = sequenceList,
                            computedDelayMs = delay
                        )
                    )

                    // If persona is configured for HIGHEST_ONLY, break on first matching trigger
                    if (persona.priorityHandling == "HIGHEST_ONLY") {
                        break
                    }
                }
            }
        }

        return matches
    }

    /**
     * Checks if user message matches keyword according to matching mode and options.
     */
    fun matchesKeyword(
        message: String,
        keyword: String,
        modeName: String,
        caseSensitive: Boolean,
        ignorePunctuation: Boolean
    ): Boolean {
        if (keyword.isBlank()) return false

        var cleanMsg = message.trim()
        var cleanKw = keyword.trim()

        if (ignorePunctuation) {
            // Strip common punctuation characters but preserve spaces and Unicode word chars
            cleanMsg = cleanMsg.replace(Regex("[\\p{Punct}&&[^']]+"), " ")
            cleanKw = cleanKw.replace(Regex("[\\p{Punct}&&[^']]+"), " ")
        }

        // Normalize multiple spaces
        cleanMsg = cleanMsg.replace(Regex("\\s+"), " ").trim()
        cleanKw = cleanKw.replace(Regex("\\s+"), " ").trim()

        if (!caseSensitive) {
            cleanMsg = cleanMsg.lowercase()
            cleanKw = cleanKw.lowercase()
        }

        val mode = try {
            MatchingMode.valueOf(modeName)
        } catch (_: Exception) {
            MatchingMode.WHOLE_WORD
        }

        return when (mode) {
            MatchingMode.EXACT -> cleanMsg == cleanKw
            MatchingMode.WHOLE_WORD -> {
                // Word boundary matching with Unicode awareness
                val pattern = Pattern.compile("(?i)(?<=^|\\s|\\W)" + Pattern.quote(cleanKw) + "(?=\\s|\\W|$)")
                pattern.matcher(cleanMsg).find()
            }
            MatchingMode.PHRASE -> cleanMsg.contains(cleanKw)
            MatchingMode.CONTAINS -> cleanMsg.contains(cleanKw)
        }
    }

    /**
     * Selects one alternative response randomly, avoiding immediate repeat if configured.
     */
    private fun selectAlternative(
        alternatives: List<ResponseAlternativeEntity>,
        avoidImmediateRepeat: Boolean
    ): ResponseAlternativeEntity? {
        if (alternatives.isEmpty()) return null
        if (alternatives.size == 1) return alternatives.first()

        val candidates = if (avoidImmediateRepeat) {
            // Find the most recently used alternative
            val maxLastUsed = alternatives.maxOfOrNull { it.lastUsedTimestamp } ?: 0L
            if (maxLastUsed > 0) {
                val nonRecent = alternatives.filter { it.lastUsedTimestamp < maxLastUsed }
                if (nonRecent.isNotEmpty()) nonRecent else alternatives
            } else alternatives
        } else alternatives

        return candidates.random()
    }

    /**
     * Calculates realistic human typing delay based on character length and persona configuration.
     */
    fun calculateDelay(text: String, persona: BotPersonaEntity): Long {
        return when (persona.typingMode) {
            "INSTANT" -> 0L
            "FIXED_DELAY" -> (persona.fixedDelaySeconds * 1000).toLong()
            "RANDOM_DELAY" -> {
                val minMs = (persona.minDelaySeconds * 1000).toLong()
                val maxMs = (persona.maxDelaySeconds * 1000).toLong().coerceAtLeast(minMs + 200)
                (minMs..maxMs).random()
            }
            else -> {
                // CHARACTER_BASED
                val charCount = text.length.coerceAtLeast(1)
                val charsPerSec = persona.charsPerSecond.coerceAtLeast(10)
                val estimatedMs = ((charCount.toFloat() / charsPerSec) * 1000).toLong()
                val minMs = (persona.minDelaySeconds * 1000).toLong()
                val maxMs = (persona.maxDelaySeconds * 1000).toLong().coerceAtLeast(minMs + 200)
                estimatedMs.coerceIn(minMs, maxMs)
            }
        }
    }

    /**
     * Selects a fallback response when no trigger matched.
     */
    fun getFallbackResponse(persona: BotPersonaEntity): String? {
        return when (persona.noMatchFallbackMode) {
            "SILENT" -> null
            "DEFAULT_MESSAGE" -> {
                try {
                    val array = JSONArray(persona.fallbackResponsesJson)
                    if (array.length() > 0) array.getString(0) else "I hear you!"
                } catch (_: Exception) {
                    "I hear you!"
                }
            }
            "RANDOM_FALLBACK" -> {
                try {
                    val array = JSONArray(persona.fallbackResponsesJson)
                    if (array.length() > 0) {
                        val index = (0 until array.length()).random()
                        array.getString(index)
                    } else "I hear you!"
                } catch (_: Exception) {
                    "I hear you!"
                }
            }
            else -> null
        }
    }
}
