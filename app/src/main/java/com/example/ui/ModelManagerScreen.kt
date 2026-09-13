package com.example.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiReasoningEngine
import com.example.data.model.AiMode
import com.example.data.preferences.AppPreferences
import com.example.ui.theme.AmberAlert
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(
    preferences: AppPreferences,
    networkMonitor: NetworkMonitor,
    reasoningEngine: AiReasoningEngine,
    onBack: () -> Unit
) {
    val selectedMode by preferences.aiMode.collectAsStateWithLifecycle()
    val isConnected by networkMonitor.isConnected.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var isRunningDiagnostic by remember { mutableStateOf(false) }
    var diagnosticResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("model_manager_screen"),
        containerColor = ObsidianBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("মডেল ম্যানেজার", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("অনলাইন, অফলাইন ও হাইব্রিড ইন্টেলিজেন্স", color = TextMuted, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("model_back_button")) {
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
            // Current Active Mode Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_mode_status_card"),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isConnected) NeonEmerald else AmberAlert
                    )
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "বর্তমান কার্যকরী মোড",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isConnected) NeonEmerald else AmberAlert,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = if (isConnected) "ইন্টারনেট সংযুক্ত" else "অফলাইন",
                                color = if (isConnected) NeonEmerald else AmberAlert,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val activeDescription = when (selectedMode) {
                        AiMode.HYBRID -> if (isConnected) "হাইব্রিড (অনলাইন AI সক্রিয়)" else "হাইব্রিড (অফলাইন AI সক্রিয়)"
                        AiMode.ONLINE -> if (isConnected) "অনলাইন মোড (Gemini 3.5 Flash)" else "অনলাইন মোড (সংযোগহীন - অফলাইন ফলব্যাক)"
                        AiMode.OFFLINE -> "সম্পূর্ণ অফলাইন মোড (শূন্য ইন্টারনেট ব্যবহার)"
                    }

                    Text(
                        text = activeDescription,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ইন্টারনেট থাকলে স্বয়ংক্রিয়ভাবে অনলাইন মডেল ব্যবহার হয় এবং ইন্টারনেট না থাকলে তাৎক্ষণিক অফলাইনে সুইচ করে।",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Mode Selector
            Text("AI মোড নির্বাচন করুন", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeSelectionItem(
                    title = "হাইব্রিড",
                    subtitle = "স্বয়ংক্রিয় সুইচ",
                    isSelected = selectedMode == AiMode.HYBRID,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setAiMode(AiMode.HYBRID) }
                )
                ModeSelectionItem(
                    title = "অনলাইন",
                    subtitle = "Gemini AI",
                    isSelected = selectedMode == AiMode.ONLINE,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setAiMode(AiMode.ONLINE) }
                )
                ModeSelectionItem(
                    title = "অফলাইন",
                    subtitle = "১০০% প্রাইভেট",
                    isSelected = selectedMode == AiMode.OFFLINE,
                    modifier = Modifier.weight(1f),
                    onClick = { preferences.setAiMode(AiMode.OFFLINE) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Online Model Specs Card
            Text("উপলব্ধ মডেলসমূহ", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("online_model_card"),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CyanAccent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.size(10.dp))
                            Column {
                                Text("Gemini 3.5 Flash", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("অনলাইন ইঞ্জিন • Google AI Studio", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Online", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ModelSpecRow(label = "প্রতিক্রিয়া গতি", value = "~300ms (হাই-স্পিড)")
                    ModelSpecRow(label = "জ্ঞানস্তর", value = "Senior Engineer, DevOps & Architecture")
                    ModelSpecRow(label = "মেমোরি দখল (RAM)", value = "0 MB (ক্লাউড এক্সিকিউশন)")
                    ModelSpecRow(label = "নিরাপত্তা নীতি", value = "কড়া অনুমতি সাপেক্ষে কার্যক্রম")
                }
            }

            // Offline Model Specs Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_model_card"),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(IndigoAccent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Devices, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.size(10.dp))
                            Column {
                                Text("Local Knowledge Core v2.4", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("অন-ডিভাইস এমবেডেড রিজনার", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(IndigoAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Offline", color = IndigoAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ModelSpecRow(label = "প্রতিক্রিয়া গতি", value = "< 10ms (তাৎক্ষণিক)")
                    ModelSpecRow(label = "আকার ও RAM", value = "4.2 MB (< 15 MB RAM)")
                    ModelSpecRow(label = "ইন্টারনেট প্রয়োজন", value = "না (সম্পূর্ণ অফলাইন)")
                    ModelSpecRow(label = "প্রাইভেসি নিশ্চয়তা", value = "১০০% ডিভাইসেই সীমাবদ্ধ")
                }
            }

            // Diagnostic & Self-Test
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("diagnostic_card"),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("মডেল সেলফ-টেস্ট ও ডায়াগনস্টিক", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "মডেলের প্রতিক্রিয়া সময় ও কার্যকারিতা পরীক্ষা করুন।",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isRunningDiagnostic = true
                            diagnosticResult = null
                            scope.launch {
                                val startTime = System.currentTimeMillis()
                                val res = reasoningEngine.executeCommand("Hello AI Assistant, test status")
                                val duration = System.currentTimeMillis() - startTime
                                diagnosticResult = "সফল! রেসপন্স সময়: ${duration}ms | সোর্স: ${if (res.isOnlineGenerated) "অনলাইন Gemini" else "অফলাইন লোকাল কোর"} | ফলাফল: ${res.spokenResponse.take(90)}..."
                                isRunningDiagnostic = false
                            }
                        },
                        enabled = !isRunningDiagnostic,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_diagnostic_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonEmerald,
                            contentColor = Color(0xFF00391A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isRunningDiagnostic) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF00391A), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("পরীক্ষা চলছে...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("টেস্ট রান করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    diagnosticResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(result, color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ModeSelectionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) CyanAccent.copy(alpha = 0.15f) else ObsidianSurface,
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (isSelected) CyanAccent else ObsidianBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isSelected) CyanAccent else TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = if (isSelected) CyanAccent.copy(alpha = 0.8f) else TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun ModelSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
