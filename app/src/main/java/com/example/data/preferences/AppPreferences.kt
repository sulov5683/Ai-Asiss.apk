package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AiMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_assistant_prefs", Context.MODE_PRIVATE)

    private val _aiMode = MutableStateFlow(loadAiMode())
    val aiMode: StateFlow<AiMode> = _aiMode.asStateFlow()

    private val _isAiActive = MutableStateFlow(prefs.getBoolean(KEY_AI_ACTIVE, true))
    val isAiActive: StateFlow<Boolean> = _isAiActive.asStateFlow()

    private val _isListeningActive = MutableStateFlow(prefs.getBoolean(KEY_LISTENING_ACTIVE, true))
    val isListeningActive: StateFlow<Boolean> = _isListeningActive.asStateFlow()

    private val _wakeWord = MutableStateFlow(prefs.getString(KEY_WAKE_WORD, "Hey Assistant") ?: "Hey Assistant")
    val wakeWord: StateFlow<String> = _wakeWord.asStateFlow()

    private val _beepBefore = MutableStateFlow(prefs.getBoolean(KEY_BEEP_BEFORE, true))
    val beepBefore: StateFlow<Boolean> = _beepBefore.asStateFlow()

    private val _beepAfter = MutableStateFlow(prefs.getBoolean(KEY_BEEP_AFTER, true))
    val beepAfter: StateFlow<Boolean> = _beepAfter.asStateFlow()

    private val _personality = MutableStateFlow(
        prefs.getString(KEY_PERSONALITY, "Senior Software Engineer & System Architect")
            ?: "Senior Software Engineer & System Architect"
    )
    val personality: StateFlow<String> = _personality.asStateFlow()

    private val _speechLanguage = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "auto") ?: "auto")
    val speechLanguage: StateFlow<String> = _speechLanguage.asStateFlow()

    private val _speechRate = MutableStateFlow(prefs.getFloat(KEY_SPEECH_RATE, 1.0f))
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(prefs.getFloat(KEY_SPEECH_PITCH, 1.0f))
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _autoStartOnBoot = MutableStateFlow(prefs.getBoolean(KEY_AUTO_BOOT, true))
    val autoStartOnBoot: StateFlow<Boolean> = _autoStartOnBoot.asStateFlow()

    private val _strictConfirmation = MutableStateFlow(prefs.getBoolean(KEY_STRICT_CONFIRMATION, true))
    val strictConfirmation: StateFlow<Boolean> = _strictConfirmation.asStateFlow()

    private fun loadAiMode(): AiMode {
        val modeStr = prefs.getString(KEY_AI_MODE, AiMode.HYBRID.name)
        return try {
            AiMode.valueOf(modeStr ?: AiMode.HYBRID.name)
        } catch (e: Exception) {
            AiMode.HYBRID
        }
    }

    fun setAiMode(mode: AiMode) {
        prefs.edit().putString(KEY_AI_MODE, mode.name).apply()
        _aiMode.value = mode
    }

    fun setAiActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_AI_ACTIVE, active).apply()
        _isAiActive.value = active
    }

    fun setListeningActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_LISTENING_ACTIVE, active).apply()
        _isListeningActive.value = active
    }

    fun setWakeWord(word: String) {
        prefs.edit().putString(KEY_WAKE_WORD, word).apply()
        _wakeWord.value = word
    }

    fun setBeepBefore(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BEEP_BEFORE, enabled).apply()
        _beepBefore.value = enabled
    }

    fun setBeepAfter(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BEEP_AFTER, enabled).apply()
        _beepAfter.value = enabled
    }

    fun setPersonality(p: String) {
        prefs.edit().putString(KEY_PERSONALITY, p).apply()
        _personality.value = p
    }

    fun setSpeechLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _speechLanguage.value = lang
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
        _speechRate.value = rate
    }

    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat(KEY_SPEECH_PITCH, pitch).apply()
        _speechPitch.value = pitch
    }

    fun setAutoStartOnBoot(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_BOOT, enabled).apply()
        _autoStartOnBoot.value = enabled
    }

    fun setStrictConfirmation(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STRICT_CONFIRMATION, enabled).apply()
        _strictConfirmation.value = enabled
    }

    companion object {
        private const val KEY_AI_MODE = "pref_ai_mode"
        private const val KEY_AI_ACTIVE = "pref_ai_active"
        private const val KEY_LISTENING_ACTIVE = "pref_listening_active"
        private const val KEY_WAKE_WORD = "pref_wake_word"
        private const val KEY_BEEP_BEFORE = "pref_beep_before"
        private const val KEY_BEEP_AFTER = "pref_beep_after"
        private const val KEY_PERSONALITY = "pref_personality"
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_SPEECH_RATE = "pref_speech_rate"
        private const val KEY_SPEECH_PITCH = "pref_speech_pitch"
        private const val KEY_AUTO_BOOT = "pref_auto_boot"
        private const val KEY_STRICT_CONFIRMATION = "pref_strict_confirmation"
    }
}
