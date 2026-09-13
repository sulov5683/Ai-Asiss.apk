package com.example.ui

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AudioFeedbackManager
import com.example.data.preferences.AppPreferences
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    audioFeedback: AudioFeedbackManager,
    onBack: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToModels: () -> Unit
) {
    val context = LocalContext.current

    val wakeWord by preferences.wakeWord.collectAsStateWithLifecycle()
    val beepBefore by preferences.beepBefore.collectAsStateWithLifecycle()
    val beepAfter by preferences.beepAfter.collectAsStateWithLifecycle()
    val personality by preferences.personality.collectAsStateWithLifecycle()
    val speechLang by preferences.speechLanguage.collectAsStateWithLifecycle()
    val speechRate by preferences.speechRate.collectAsStateWithLifecycle()
    val speechPitch by preferences.speechPitch.collectAsStateWithLifecycle()
    val autoBoot by preferences.autoStartOnBoot.collectAsStateWithLifecycle()
    val strictConfirmation by preferences.strictConfirmation.collectAsStateWithLifecycle()

    var customWakeWord by remember { mutableStateOf(wakeWord) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        containerColor = ObsidianBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("সেটিংস", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("ব্যক্তিগত সহকারী কনফিগারেশন", color = TextMuted, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wake Word Setting
            SettingsCard(
                icon = Icons.Default.Hearing,
                title = "Wake Word (ওয়েক ওয়ার্ড)",
                subtitle = "যে শব্দ বললে AI কথা শোনা শুরু করবে"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customWakeWord,
                        onValueChange = {
                            customWakeWord = it
                            preferences.setWakeWord(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wake_word_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    // Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Hey Assistant", "সহকারী", "Jarvis").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (wakeWord == preset) CyanAccent.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        customWakeWord = preset
                                        preferences.setWakeWord(preset)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = preset,
                                    color = if (wakeWord == preset) CyanAccent else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Beep Sound Settings
            SettingsCard(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Beep সাউন্ড সেটিংস",
                subtitle = "কথা বলার আগে ও পরে ছোট বিপ ধ্বনি"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsSwitchRow(
                        title = "কথা বলার পূর্বে ছোট Beep",
                        subtitle = "AI কথা শুরু করার পূর্বে ছোট বিপ দেবে",
                        checked = beepBefore,
                        onCheckedChange = { preferences.setBeepBefore(it) }
                    )

                    SettingsSwitchRow(
                        title = "কথা শেষে সমাপ্তি Beep",
                        subtitle = "AI কথা শেষ করার পর একটি ছোট বিপ দেবে",
                        checked = beepAfter,
                        onCheckedChange = { preferences.setBeepAfter(it) }
                    )

                    Button(
                        onClick = {
                            audioFeedback.playStartBeep()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ObsidianSurfaceVariant,
                            contentColor = CyanAccent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Beep টেস্ট করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Voice & Speech Settings
            SettingsCard(
                icon = Icons.Default.RecordVoiceOver,
                title = "ভয়েস ও কণ্ঠস্বর",
                subtitle = "বাংলা ও ইংরেজি স্বাভাবিক উচ্চারণের সমন্বয়"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Language Selection
                    Text("ভাষার অগ্রাধিকার", color = TextMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("auto" to "স্বয়ংক্রিয় (Auto)", "bn" to "বাংলা (Bangla)", "en" to "ইংরেজি (English)").forEach { (code, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (speechLang == code) CyanAccent.copy(alpha = 0.2f) else ObsidianSurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { preferences.setSpeechLanguage(code) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (speechLang == code) CyanAccent else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Pitch
                    Text("ভয়েস পিচ (সুর): ${"%.1f".format(speechPitch)}x", color = TextMuted, fontSize = 12.sp)
                    Slider(
                        value = speechPitch,
                        onValueChange = { preferences.setSpeechPitch(it) },
                        valueRange = 0.6f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )

                    // Speed
                    Text("কথার গতি: ${"%.1f".format(speechRate)}x", color = TextMuted, fontSize = 12.sp)
                    Slider(
                        value = speechRate,
                        onValueChange = { preferences.setSpeechRate(it) },
                        valueRange = 0.6f..1.6f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )

                    Button(
                        onClick = {
                            audioFeedback.speak("নমস্কার! আমি আপনার ব্যক্তিগত AI Assistant। I am ready.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent.copy(alpha = 0.15f),
                            contentColor = CyanAccent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ভয়েস টেস্ট করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // AI Personality
            SettingsCard(
                icon = Icons.Default.Psychology,
                title = "AI ব্যক্তিত্ব ও জ্ঞানস্তর",
                subtitle = "সিনিয়র সফটওয়্যার ইঞ্জিনিয়ার ও সিস্টেম আর্কিটেক্ট"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Senior Software Engineer & System Architect" to "সিনিয়র ইঞ্জিনিয়ার ও আর্কিটেক্ট (উন্নত কোড ও টেকনিক্যাল গভীরতা)",
                        "Helpful Personal Assistant" to "সহকারী ও বন্ধুভাবাপন্ন (সংক্ষিপ্ত ও স্পষ্ট কথোপকথন)",
                        "Ultra-Concise Direct Mode" to "অতি-সংক্ষিপ্ত মোড (শুধুমাত্র বুলেটপয়েন্ট ও উত্তর)"
                    ).forEach { (id, desc) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (personality == id) CyanAccent.copy(alpha = 0.15f) else ObsidianSurfaceVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { preferences.setPersonality(id) }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = desc,
                                color = if (personality == id) CyanAccent else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (personality == id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Strict Privacy & Safety
            SettingsCard(
                icon = Icons.Default.Security,
                title = "নিরাপত্তা ও গোপনীয়তা",
                subtitle = "অনুমোদন ছাড়া কোনো কাজ না করার কঠোর নিয়ম"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsSwitchRow(
                        title = "কঠোর অ্যাকশন নিশ্চিতকরণ",
                        subtitle = "কল, মেসেজ, অ্যাপ ওপেন ও পরিবর্তনের পূর্বে সর্বদা স্পষ্ট অনুমতি চাইবে",
                        checked = strictConfirmation,
                        onCheckedChange = { preferences.setStrictConfirmation(it) }
                    )

                    SettingsSwitchRow(
                        title = "ফোন বুটে স্বয়ংক্রিয় শুরু",
                        subtitle = "ফোন রিস্টার্ট হলে ব্যাকগ্রাউন্ডে রেডি থাকবে",
                        checked = autoBoot,
                        onCheckedChange = { preferences.setAutoStartOnBoot(it) }
                    )
                }
            }

            // Battery Optimization Info
            SettingsCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "ব্যাটারি অপ্টিমাইজেশন",
                subtitle = "কম RAM ও কম ব্যাটারি ব্যবহার করে ব্যাকগ্রাউন্ডে চলার কনফিগারেশন"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Android যাতে ব্যাকগ্রাউন্ড সার্ভিসটি বন্ধ না করে দেয়, সেজন্য ব্যাটারি অপ্টিমাইজেশন বন্ধ রাখার অনুরোধ করতে পারেন।",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "ব্যাটারি সেটিংস খোলা যায়নি", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ObsidianSurfaceVariant,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ব্যাটারি সেটিংস খুলুন", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyanAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.size(10.dp))
                Column {
                    Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Spacer(modifier = Modifier.size(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanAccent,
                checkedTrackColor = CyanAccent.copy(alpha = 0.3f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = ObsidianSurfaceVariant
            )
        )
    }
}
