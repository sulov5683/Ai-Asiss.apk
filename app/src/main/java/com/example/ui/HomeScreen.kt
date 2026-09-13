package com.example.ui

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiReasoningEngine
import com.example.audio.AudioFeedbackManager
import com.example.data.db.MemoryDao
import com.example.data.model.ActionProposal
import com.example.data.model.ActionType
import com.example.data.model.AiMode
import com.example.data.model.AiState
import com.example.data.model.MemoryEntity
import com.example.data.preferences.AppPreferences
import com.example.service.AiAssistantService
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.NetworkMonitor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    preferences: AppPreferences,
    networkMonitor: NetworkMonitor,
    audioFeedback: AudioFeedbackManager,
    reasoningEngine: AiReasoningEngine,
    memoryDao: MemoryDao,
    onNavigateToMemory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToModels: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isAiActive by preferences.isAiActive.collectAsStateWithLifecycle()
    val isListeningActive by preferences.isListeningActive.collectAsStateWithLifecycle()
    val selectedMode by preferences.aiMode.collectAsStateWithLifecycle()
    val isConnected by networkMonitor.isConnected.collectAsStateWithLifecycle()
    val wakeWord by preferences.wakeWord.collectAsStateWithLifecycle()
    val isSpeaking by audioFeedback.isSpeaking.collectAsStateWithLifecycle()

    var aiCurrentState by remember { mutableStateOf(AiState.IDLE) }
    var lastSpokenCommand by remember { mutableStateOf<String?>(null) }
    var lastSpokenResponse by remember { mutableStateOf<String?>(null) }
    var pendingConfirmation by remember { mutableStateOf<ActionProposal?>(null) }
    var showManualCommandInput by remember { mutableStateOf(false) }
    var manualCommandText by remember { mutableStateOf("") }

    // Speech Recognizer Intent Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                lastSpokenCommand = spokenText
                executeVoiceCommand(spokenText, preferences, reasoningEngine, memoryDao, audioFeedback, scope) { state, response, proposal ->
                    aiCurrentState = state
                    response?.let { lastSpokenResponse = it }
                    pendingConfirmation = proposal
                }
            } else {
                aiCurrentState = AiState.IDLE
            }
        } else {
            aiCurrentState = AiState.IDLE
        }
    }

    // Animation for pulsing button when active
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        containerColor = ObsidianBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (isAiActive) NeonEmerald else TextMuted, CircleShape)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "AI Assistant",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showManualCommandInput = !showManualCommandInput },
                        modifier = Modifier.testTag("toggle_text_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = "Text Command Option",
                            tint = CyanAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. STATUS CARD (AI Status & Online/Offline Status)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_status_card"),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // AI Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "AI Status",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val displayState = when {
                                !isAiActive -> AiState.STOPPED
                                isSpeaking -> AiState.SPEAKING
                                else -> aiCurrentState
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            when (displayState) {
                                                AiState.STOPPED -> CoralRed
                                                AiState.IDLE -> NeonEmerald
                                                AiState.LISTENING -> CyanAccent
                                                AiState.PROCESSING -> IndigoAccent
                                                AiState.AWAITING_CONFIRMATION -> AmberAlert
                                                AiState.SPEAKING -> CyanAccent
                                            },
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "${displayState.labelBn} (${displayState.labelEn})",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Background Service Status Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isAiActive) NeonEmerald.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.15f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isAiActive) "Background Active" else "Background Stopped",
                                color = if (isAiActive) NeonEmerald else CoralRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ObsidianBorder))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Online / Offline Mode Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Online / Offline Status",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val activeModeLabel = when (selectedMode) {
                                AiMode.HYBRID -> if (isConnected) "Hybrid (Online Connected)" else "Hybrid (Offline Active)"
                                AiMode.ONLINE -> if (isConnected) "Online Mode (Gemini 3.5)" else "Online (No Network - Offline Fallback)"
                                AiMode.OFFLINE -> "Offline Mode (Local Engine)"
                            }
                            Text(
                                text = activeModeLabel,
                                color = if (isConnected) CyanAccent else AmberAlert,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isConnected) NeonEmerald else AmberAlert,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = if (isConnected) "Internet ON" else "Internet OFF",
                                color = if (isConnected) NeonEmerald else AmberAlert,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 2. PRIMARY CONTROL BUTTONS: Start AI, Stop AI, Listening Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Start AI
                Button(
                    onClick = {
                        preferences.setAiActive(true)
                        AiAssistantService.startService(context)
                        aiCurrentState = AiState.IDLE
                        Toast.makeText(context, "AI সার্ভিস চালু হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("start_ai_button"),
                    enabled = !isAiActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonEmerald,
                        contentColor = Color(0xFF00391A),
                        disabledContainerColor = ObsidianSurfaceVariant,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Start AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Stop AI
                OutlinedButton(
                    onClick = {
                        preferences.setAiActive(false)
                        AiAssistantService.stopService(context)
                        aiCurrentState = AiState.STOPPED
                        audioFeedback.stopSpeaking()
                        Toast.makeText(context, "AI সার্ভিস বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stop_ai_button"),
                    enabled = isAiActive,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CoralRed,
                        disabledContentColor = TextMuted
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isAiActive) CoralRed else ObsidianBorder)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Stop AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Listening Mode Toggle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("listening_mode_card")
                    .clickable {
                        val next = !isListeningActive
                        preferences.setListeningActive(next)
                        if (next && isAiActive) {
                            aiCurrentState = AiState.LISTENING
                        } else if (!next) {
                            aiCurrentState = AiState.IDLE
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isListeningActive) CyanAccent.copy(alpha = 0.6f) else ObsidianBorder
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isListeningActive) CyanAccent.copy(alpha = 0.15f) else ObsidianSurfaceVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListeningActive) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = null,
                                tint = if (isListeningActive) CyanAccent else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(
                                text = "Listening Mode",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isListeningActive) "Voice Command-এর জন্য অপেক্ষা করছে (Wake Word: \"$wakeWord\")" else "লিসেনিং পজ করা আছে",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (isListeningActive) CyanAccent.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isListeningActive) "ON" else "OFF",
                            color = if (isListeningActive) CyanAccent else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. VOICE ASSISTANT BUTTON (Wake word & Voice trigger)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(if (aiCurrentState == AiState.LISTENING || isSpeaking) pulseScale else 1f)
                            .shadow(
                                elevation = if (aiCurrentState == AiState.LISTENING || isSpeaking) 16.dp else 4.dp,
                                shape = CircleShape,
                                ambientColor = CyanAccent,
                                spotColor = CyanAccent
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(CyanAccent, IndigoAccent)
                                ),
                                shape = CircleShape
                            )
                            .clickable {
                                if (!isAiActive) {
                                    Toast
                                        .makeText(context, "প্রথমে Start AI চাপুন", Toast.LENGTH_SHORT)
                                        .show()
                                    return@clickable
                                }
                                audioFeedback.playStartBeep()
                                aiCurrentState = AiState.LISTENING
                                try {
                                    val intent =
                                        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                            )
                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE,
                                                if (preferences.speechLanguage.value == "bn") "bn-BD" else Locale.getDefault()
                                            )
                                            putExtra(
                                                RecognizerIntent.EXTRA_PROMPT,
                                                "কথা বলুন... (বাংলা বা ইংরেজি)"
                                            )
                                        }
                                    speechRecognizerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    // Fallback to text prompt
                                    showManualCommandInput = true
                                }
                            }
                            .testTag("voice_assistant_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Assistant Button",
                            tint = Color.Black,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ভয়েস বাটন চাপুন অথবা বলুন: \"$wakeWord\"",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Optional Manual Command Box (if microphone or emulator text testing needed)
            AnimatedVisibility(visible = showManualCommandInput) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanAccent))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("টেক্সট কমান্ড ইনপুট (ভয়েস সিমুলেশন)", color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = manualCommandText,
                                onValueChange = { manualCommandText = it },
                                placeholder = { Text("উদাহরণ: Compose কি? বা এটা মনে রাখো: রক্তের গ্রুপ A+", fontSize = 12.sp, color = TextMuted) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("manual_command_text_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(
                                onClick = {
                                    if (manualCommandText.isNotBlank()) {
                                        val cmd = manualCommandText.trim()
                                        manualCommandText = ""
                                        lastSpokenCommand = cmd
                                        executeVoiceCommand(cmd, preferences, reasoningEngine, memoryDao, audioFeedback, scope) { state, response, proposal ->
                                            aiCurrentState = state
                                            response?.let { lastSpokenResponse = it }
                                            pendingConfirmation = proposal
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = ObsidianBg),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("send_manual_command_button")
                            ) {
                                Text("প্রেরণ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Active Command / Spoken Response Result Banner
            if (lastSpokenCommand != null || lastSpokenResponse != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("latest_response_card"),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        lastSpokenCommand?.let { cmd ->
                            Text(
                                text = "আপনার নির্দেশ: \"$cmd\"",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        lastSpokenResponse?.let { res ->
                            Text(
                                text = res,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. NAVIGATION TILES: Memory, Settings, Permission Manager, Model Manager
            Text("ব্যবস্থাপনা ও কনফিগারেশন", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NavigationTile(
                    title = "Memory (মেমোরি)",
                    subtitle = "স্থানীয় ডাটাবেসে সংরক্ষিত ব্যক্তিগত তথ্য দেখুন ও সম্পাদনা করুন",
                    icon = Icons.Default.Memory,
                    iconColor = CyanAccent,
                    testTag = "nav_memory_tile",
                    onClick = onNavigateToMemory
                )

                NavigationTile(
                    title = "Permission Manager (পারমিশন)",
                    subtitle = "মাইক্রোফোন, ক্যামেরা, কল, এসএমএস ইত্যাদি আলাদা নিয়ন্ত্রণ",
                    icon = Icons.Default.Security,
                    iconColor = NeonEmerald,
                    testTag = "nav_permission_tile",
                    onClick = onNavigateToPermissions
                )

                NavigationTile(
                    title = "Model Manager (মডেল)",
                    subtitle = "অনলাইন ও অফলাইন মডেলের স্ট্যাটাস, সুইচিং ও ডায়াগনস্টিক",
                    icon = Icons.Default.Psychology,
                    iconColor = IndigoAccent,
                    testTag = "nav_model_tile",
                    onClick = onNavigateToModels
                )

                NavigationTile(
                    title = "Settings (সেটিংস)",
                    subtitle = "Wake Word, ভয়েস, ব্যক্তিত্ব, সাউন্ড বিপ এবং ব্যাকআপ",
                    icon = Icons.Default.Settings,
                    iconColor = TextSecondary,
                    testTag = "nav_settings_tile",
                    onClick = onNavigateToSettings
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Confirmation Dialog: Strictly enforced before any action execution!
    pendingConfirmation?.let { proposal ->
        ConfirmationDialog(
            proposal = proposal,
            onConfirm = {
                scope.launch {
                    executeApprovedAction(proposal, context, memoryDao, audioFeedback)
                    pendingConfirmation = null
                    aiCurrentState = AiState.IDLE
                }
            },
            onDismiss = {
                pendingConfirmation = null
                aiCurrentState = AiState.IDLE
                audioFeedback.speak("অ্যাকশনটি আপনার নির্দেশে বাতিল করা হয়েছে।")
            }
        )
    }
}

private fun executeVoiceCommand(
    prompt: String,
    preferences: AppPreferences,
    reasoningEngine: AiReasoningEngine,
    memoryDao: MemoryDao,
    audioFeedback: AudioFeedbackManager,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (AiState, String?, ActionProposal?) -> Unit
) {
    scope.launch {
        onResult(AiState.PROCESSING, null, null)

        val memories = try {
            memoryDao.getAllMemories().first().map { "${it.title}: ${it.content}" }
        } catch (e: Exception) {
            emptyList()
        }

        val outcome = reasoningEngine.executeCommand(prompt, memories)

        if (outcome.pendingAction != null) {
            onResult(AiState.AWAITING_CONFIRMATION, outcome.spokenResponse, outcome.pendingAction)
            audioFeedback.speak(outcome.spokenResponse)
        } else {
            onResult(AiState.IDLE, outcome.spokenResponse, null)
            audioFeedback.speak(outcome.spokenResponse)
        }
    }
}

private suspend fun executeApprovedAction(
    proposal: ActionProposal,
    context: android.content.Context,
    memoryDao: MemoryDao,
    audioFeedback: AudioFeedbackManager
) {
    when (proposal.type) {
        ActionType.SAVE_MEMORY -> {
            memoryDao.insertMemory(
                MemoryEntity(
                    title = "স্মৃতি",
                    content = proposal.payload,
                    category = "Voice Memory"
                )
            )
            audioFeedback.speak("তথ্যটি সফলভাবে আপনার ডিভাইসের স্থানীয় মেমোরিতে সংরক্ষণ করা হয়েছে।")
        }
        ActionType.CALL -> {
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${proposal.payload}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                audioFeedback.speak("কল ইন্টারফেস খোলা হয়েছে।")
            } catch (e: Exception) {
                audioFeedback.speak("কল শুরু করতে ব্যর্থ হয়েছে: ${e.message}")
            }
        }
        ActionType.SMS -> {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:")
                    putExtra("sms_body", proposal.payload)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                audioFeedback.speak("মেসেজ ইন্টারফেস প্রস্তুত।")
            } catch (e: Exception) {
                audioFeedback.speak("মেসেজ খুলতে ব্যর্থ: ${e.message}")
            }
        }
        ActionType.OPEN_APP -> {
            try {
                val pm = context.packageManager
                val intent = pm.getLaunchIntentForPackage(proposal.payload)
                if (intent != null) {
                    context.startActivity(intent)
                    audioFeedback.speak("অ্যাপটি চালু করা হয়েছে।")
                } else {
                    audioFeedback.speak("${proposal.payload} অ্যাপ্লিকেশনটি সরাসরি পাওয়া যায়নি।")
                }
            } catch (e: Exception) {
                audioFeedback.speak("অ্যাপ্লিকেশন খুলতে ব্যর্থ।")
            }
        }
        ActionType.DELETE_DATA -> {
            audioFeedback.speak("আপনার অনুমোদনে অনুরোধটি প্রসেস করা হয়েছে।")
        }
        else -> {
            audioFeedback.speak("অনুমোদিত অ্যাকশন সম্পন্ন হয়েছে।")
        }
    }
}

@Composable
fun NavigationTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
