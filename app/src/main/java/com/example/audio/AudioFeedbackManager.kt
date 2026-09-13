package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class AudioFeedbackManager(
    private val context: Context,
    private val preferences: AppPreferences
) : TextToSpeech.OnInitListener {

    private val tag = "AudioFeedbackManager"
    private var toneGenerator: ToneGenerator? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize ToneGenerator", e)
        }
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            setupTts()
        } else {
            Log.e(tag, "TextToSpeech initialization failed with status $status")
        }
    }

    private fun setupTts() {
        textToSpeech?.let { tts ->
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    if (preferences.beepAfter.value) {
                        playBeep(ToneGenerator.TONE_PROP_ACK, 80)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    Log.e(tag, "TTS utterance error: $errorCode")
                }
            })
        }
    }

    fun playStartBeep() {
        playBeep(ToneGenerator.TONE_PROP_BEEP, 110)
    }

    fun playEndBeep() {
        playBeep(ToneGenerator.TONE_PROP_ACK, 90)
    }

    fun playAlertBeep() {
        playBeep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
    }

    private fun playBeep(toneType: Int, durationMs: Int) {
        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            Log.e(tag, "Error playing tone", e)
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isTtsInitialized || textToSpeech == null) {
            Log.w(tag, "TTS not ready yet")
            return
        }

        scope.launch {
            if (preferences.beepBefore.value) {
                playStartBeep()
                kotlinx.coroutines.delay(180)
            }

            val tts = textToSpeech ?: return@launch

            // Detect language
            val isBengali = text.any { it in '\u0980'..'\u09FF' }
            val preferredLanguage = preferences.speechLanguage.value

            val targetLocale = when {
                preferredLanguage == "bn" || (preferredLanguage == "auto" && isBengali) -> {
                    val bnBd = Locale.forLanguageTag("bn-BD")
                    if (tts.isLanguageAvailable(bnBd) >= TextToSpeech.LANG_AVAILABLE) {
                        bnBd
                    } else {
                        Locale.forLanguageTag("bn")
                    }
                }
                else -> Locale.US
            }

            tts.language = targetLocale
            tts.setPitch(preferences.speechPitch.value)
            tts.setSpeechRate(preferences.speechRate.value)

            val utteranceId = UUID.randomUUID().toString()
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(tag, "Error stopping TTS", e)
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(tag, "Error releasing resources", e)
        }
    }
}
