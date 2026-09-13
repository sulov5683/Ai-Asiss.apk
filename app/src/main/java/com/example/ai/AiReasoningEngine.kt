package com.example.ai

import com.example.data.model.ActionProposal
import com.example.data.model.AiMode
import com.example.data.preferences.AppPreferences
import com.example.util.NetworkMonitor

data class ReasoningResult(
    val spokenResponse: String,
    val isOnlineGenerated: Boolean,
    val pendingAction: ActionProposal? = null
)

class AiReasoningEngine(
    private val preferences: AppPreferences,
    private val networkMonitor: NetworkMonitor,
    private val geminiApiClient: GeminiApiClient = GeminiApiClient(),
    private val offlineReasoningEngine: OfflineReasoningEngine = OfflineReasoningEngine()
) {

    suspend fun executeCommand(
        prompt: String,
        contextMemories: List<String> = emptyList()
    ): ReasoningResult {
        val selectedMode = preferences.aiMode.value
        val hasInternet = networkMonitor.isConnected.value
        val personality = preferences.personality.value

        // First check offline engine for action proposals / safety rules
        val offlineCheck = offlineReasoningEngine.processCommand(prompt, personality, contextMemories)
        if (offlineCheck.proposal != null) {
            // Sensitive actions or memories ALWAYS require explicit confirmation, regardless of mode!
            return ReasoningResult(
                spokenResponse = offlineCheck.reply,
                isOnlineGenerated = false,
                pendingAction = offlineCheck.proposal
            )
        }

        // Determine execution path
        return when (selectedMode) {
            AiMode.OFFLINE -> {
                ReasoningResult(
                    spokenResponse = offlineCheck.reply,
                    isOnlineGenerated = false,
                    pendingAction = null
                )
            }
            AiMode.ONLINE -> {
                if (hasInternet) {
                    val onlineResult = geminiApiClient.generateResponse(prompt, personality, contextMemories)
                    if (onlineResult.isSuccess) {
                        ReasoningResult(
                            spokenResponse = onlineResult.getOrThrow(),
                            isOnlineGenerated = true,
                            pendingAction = null
                        )
                    } else {
                        // Fallback to offline with notice
                        ReasoningResult(
                            spokenResponse = "অনলাইন ত্রুটি: ${onlineResult.exceptionOrNull()?.message}। অফলাইন বিশ্লেষণ: ${offlineCheck.reply}",
                            isOnlineGenerated = false,
                            pendingAction = null
                        )
                    }
                } else {
                    ReasoningResult(
                        spokenResponse = "ইন্টারনেট সংযোগ নেই। অফলাইন বিশ্লেষণে: ${offlineCheck.reply}",
                        isOnlineGenerated = false,
                        pendingAction = null
                    )
                }
            }
            AiMode.HYBRID -> {
                if (hasInternet) {
                    val onlineResult = geminiApiClient.generateResponse(prompt, personality, contextMemories)
                    if (onlineResult.isSuccess) {
                        ReasoningResult(
                            spokenResponse = onlineResult.getOrThrow(),
                            isOnlineGenerated = true,
                            pendingAction = null
                        )
                    } else {
                        // Transparent fallback to offline
                        ReasoningResult(
                            spokenResponse = offlineCheck.reply,
                            isOnlineGenerated = false,
                            pendingAction = null
                        )
                    }
                } else {
                    // Automatic offline mode
                    ReasoningResult(
                        spokenResponse = offlineCheck.reply,
                        isOnlineGenerated = false,
                        pendingAction = null
                    )
                }
            }
        }
    }
}
