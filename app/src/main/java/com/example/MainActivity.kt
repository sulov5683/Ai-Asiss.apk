package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ai.AiReasoningEngine
import com.example.audio.AudioFeedbackManager
import com.example.data.preferences.AppPreferences
import com.example.service.AiAssistantService
import com.example.ui.HomeScreen
import com.example.ui.MemoryScreen
import com.example.ui.ModelManagerScreen
import com.example.ui.PermissionScreen
import com.example.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NetworkMonitor

enum class Screen {
    HOME,
    MEMORY,
    PERMISSIONS,
    MODELS,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: AppPreferences
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var audioFeedback: AudioFeedbackManager
    private lateinit var reasoningEngine: AiReasoningEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = AppPreferences(this)
        networkMonitor = NetworkMonitor(this)
        audioFeedback = AudioFeedbackManager(this, preferences)
        reasoningEngine = AiReasoningEngine(preferences, networkMonitor)

        // Start background service if active
        if (preferences.isAiActive.value) {
            AiAssistantService.startService(this)
        }

        val appDb = (application as AiAssistantApp).database
        val memoryDao = appDb.memoryDao()

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(Screen.HOME) }

                BackHandler(enabled = currentScreen != Screen.HOME) {
                    currentScreen = Screen.HOME
                }

                Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            preferences = preferences,
                            networkMonitor = networkMonitor,
                            audioFeedback = audioFeedback,
                            reasoningEngine = reasoningEngine,
                            memoryDao = memoryDao,
                            onNavigateToMemory = { currentScreen = Screen.MEMORY },
                            onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                            onNavigateToPermissions = { currentScreen = Screen.PERMISSIONS },
                            onNavigateToModels = { currentScreen = Screen.MODELS }
                        )
                        Screen.MEMORY -> MemoryScreen(
                            memoryDao = memoryDao,
                            onBack = { currentScreen = Screen.HOME }
                        )
                        Screen.PERMISSIONS -> PermissionScreen(
                            onBack = { currentScreen = Screen.HOME }
                        )
                        Screen.MODELS -> ModelManagerScreen(
                            preferences = preferences,
                            networkMonitor = networkMonitor,
                            reasoningEngine = reasoningEngine,
                            onBack = { currentScreen = Screen.HOME }
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            preferences = preferences,
                            audioFeedback = audioFeedback,
                            onBack = { currentScreen = Screen.HOME },
                            onNavigateToMemory = { currentScreen = Screen.MEMORY },
                            onNavigateToPermissions = { currentScreen = Screen.PERMISSIONS },
                            onNavigateToModels = { currentScreen = Screen.MODELS }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        audioFeedback.release()
        super.onDestroy()
    }
}
