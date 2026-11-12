package com.example.data.model

enum class MatchingMode(val displayName: String, val description: String) {
    EXACT("Exact Message", "Matches only when the message exactly equals the keyword"),
    WHOLE_WORD("Whole Word", "Matches when keyword appears as a standalone word"),
    PHRASE("Phrase Detection", "Matches when complete phrase is found inside message"),
    CONTAINS("Contains Text", "Matches whenever character sequence appears anywhere")
}

enum class ResponseType(val displayName: String) {
    TEXT("Text"),
    IMAGE("Image"),
    GIF("GIF"),
    SEQUENCE("Multi-Step Sequence")
}

enum class Sender {
    USER,
    BOT
}

enum class MessageType {
    TEXT,
    IMAGE,
    GIF
}

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

enum class RepeatFrequency(val displayName: String) {
    ONCE("Once"),
    DAILY("Every Day"),
    WEEKLY("Weekly"),
    CUSTOM_WEEKDAYS("Selected Days")
}

enum class FallbackMode(val displayName: String) {
    SILENT("Do Nothing (Silent)"),
    DEFAULT_MESSAGE("Single Default Offline Response"),
    RANDOM_FALLBACK("Random Fallback Reply")
}

enum class TypingMode(val displayName: String) {
    CHARACTER_BASED("Realistic (Character-length based)"),
    FIXED_DELAY("Fixed Delay"),
    RANDOM_DELAY("Random Range"),
    INSTANT("Instant Reply")
}
