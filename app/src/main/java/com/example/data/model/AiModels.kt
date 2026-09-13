package com.example.data.model

enum class AiMode(val labelBn: String, val labelEn: String) {
    OFFLINE("অফলাইন মোড", "Offline Mode"),
    ONLINE("অনলাইন মোড", "Online Mode"),
    HYBRID("হাইব্রিড মোড", "Hybrid Mode")
}

enum class AiState(val labelBn: String, val labelEn: String) {
    STOPPED("বন্ধ", "Stopped"),
    IDLE("প্রস্তুত", "Idle / Ready"),
    LISTENING("শুনছি...", "Listening..."),
    PROCESSING("বিশ্লেষণ করছি...", "Processing..."),
    AWAITING_CONFIRMATION("অনুমতির অপেক্ষা", "Awaiting Confirmation"),
    SPEAKING("কথা বলছি...", "Speaking...")
}

enum class ActionType(val labelBn: String, val labelEn: String) {
    CALL("ফোন কল", "Phone Call"),
    SMS("বার্তা প্রেরণ", "Send SMS"),
    OPEN_APP("অ্যাপ খোলা", "Open App"),
    FILE_MODIFY("ফাইল পরিবর্তন", "File Modify"),
    DELETE_DATA("ডেটা মুছা", "Delete Data"),
    SETTINGS_CHANGE("সেটিংস পরিবর্তন", "Settings Change"),
    INTERNET_DATA("ইন্টারনেটে ডেটা পাঠানো", "Send Data to Cloud"),
    SAVE_MEMORY("মেমোরিতে সংরক্ষণ", "Save to Memory"),
    ALARM_REMINDER("অ্যালার্ম / রিমাইন্ডার", "Alarm / Reminder")
}

data class ActionProposal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ActionType,
    val title: String,
    val description: String,
    val requiresExplicitConsent: Boolean = true,
    val payload: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
