package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.core.content.ContextCompat
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class PermissionItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val description: String,
    val icon: ImageVector,
    val manifestPermission: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }

    fun checkPermission(permission: String?): Boolean {
        if (permission == null) return false
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    var currentRequestingPermission by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshTrigger++
    }

    val permissionsList = remember {
        listOf(
            PermissionItem(
                id = "mic",
                titleBn = "মাইক্রোফোন",
                titleEn = "Microphone",
                description = "ভয়েস কমান্ড ও ওয়েক ওয়ার্ড শোনার জন্য",
                icon = Icons.Default.Mic,
                manifestPermission = Manifest.permission.RECORD_AUDIO
            ),
            PermissionItem(
                id = "notification",
                titleBn = "নোটিফিকেশন",
                titleEn = "Notifications",
                description = "ব্যাকগ্রাউন্ড সার্ভিস স্ট্যাটাস ও সতর্কতার জন্য",
                icon = Icons.Default.Notifications,
                manifestPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else null
            ),
            PermissionItem(
                id = "camera",
                titleBn = "ক্যামেরা",
                titleEn = "Camera",
                description = "ওসিআর ও ছবি বিশ্লেষণের জন্য",
                icon = Icons.Default.CameraAlt,
                manifestPermission = Manifest.permission.CAMERA
            ),
            PermissionItem(
                id = "storage",
                titleBn = "স্টোরেজ ও ফাইল",
                titleEn = "Storage & Files",
                description = "মেমোরি ব্যাকআপ ও ডকুমেন্ট পড়ার জন্য",
                icon = Icons.Default.Folder,
                manifestPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else null
            ),
            PermissionItem(
                id = "contacts",
                titleBn = "পরিচিতি",
                titleEn = "Contacts",
                description = "অনুমোদন সাপেক্ষে নাম দিয়ে যোগাযোগ খোঁজার জন্য",
                icon = Icons.Default.Contacts,
                manifestPermission = Manifest.permission.READ_CONTACTS
            ),
            PermissionItem(
                id = "calendar",
                titleBn = "ক্যালেন্ডার",
                titleEn = "Calendar",
                description = "ইভেন্ট ও মিটিং শিডিউল করার জন্য",
                icon = Icons.Default.CalendarMonth,
                manifestPermission = Manifest.permission.READ_CALENDAR
            ),
            PermissionItem(
                id = "phone",
                titleBn = "ফোন কল",
                titleEn = "Phone",
                description = "স্পষ্ট নিশ্চিতকরণ সাপেক্ষে কল করার জন্য",
                icon = Icons.Default.Call,
                manifestPermission = Manifest.permission.CALL_PHONE
            ),
            PermissionItem(
                id = "sms",
                titleBn = "এসএমএস",
                titleEn = "SMS",
                description = "অনুমোদন সাপেক্ষে জরুরি মেসেজ পাঠানোর জন্য",
                icon = Icons.Default.Sms,
                manifestPermission = Manifest.permission.SEND_SMS
            ),
            PermissionItem(
                id = "bluetooth",
                titleBn = "ব্লুটুথ",
                titleEn = "Bluetooth",
                description = "ওয়্যারলেস হেডসেট ও ডিভাইসের সাথে সংযোগের জন্য",
                icon = Icons.Default.Bluetooth,
                manifestPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Manifest.permission.BLUETOOTH_CONNECT
                } else null
            ),
            PermissionItem(
                id = "location",
                titleBn = "অবস্থান",
                titleEn = "Location",
                description = "স্থানীয় আবহাওয়া ও সার্চের জন্য",
                icon = Icons.Default.MyLocation,
                manifestPermission = Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PermissionItem(
                id = "accessibility",
                titleBn = "অ্যাক্সেসিবিলিটি (ঐচ্ছিক)",
                titleEn = "Accessibility",
                description = "স্ক্রিন সহায়তার ঐচ্ছিক ফিচার",
                icon = Icons.Default.Accessibility,
                manifestPermission = null
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("permission_manager_screen"),
        containerColor = ObsidianBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("পারমিশন ম্যানেজার", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("প্রতিটি অনুমতি আলাদাভাবে নিয়ন্ত্রণ করুন", color = TextMuted, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("permission_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("permission_open_settings_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "System Settings", tint = CyanAccent)
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
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "সবচেয়ে গুরুত্বপূর্ণ নিয়ম: কোনো পারমিশন দেওয়া থাকলেও আপনার স্পষ্ট মৌখিক বা লিখিত অনুমোদন ছাড়া AI কখনো নিজে থেকে কোনো অ্যাকশন চালাবে না।",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(permissionsList, key = { it.id }) { item ->
                    val isGranted = remember(refreshTrigger, item.manifestPermission) {
                        if (item.manifestPermission == null) false else checkPermission(item.manifestPermission)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("permission_card_${item.id}"),
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
                                        .size(44.dp)
                                        .background(
                                            if (isGranted) NeonEmerald.copy(alpha = 0.15f) else ObsidianSurfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (isGranted) NeonEmerald else TextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.size(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.titleBn,
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text(
                                            text = "(${item.titleEn})",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.description,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.size(8.dp))

                            if (isGranted) {
                                Box(
                                    modifier = Modifier
                                        .background(NeonEmerald.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text("সক্রিয়", color = NeonEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (item.manifestPermission != null) {
                                            currentRequestingPermission = item.manifestPermission
                                            permissionLauncher.launch(item.manifestPermission)
                                        } else {
                                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                            context.startActivity(intent)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanAccent.copy(alpha = 0.15f),
                                        contentColor = CyanAccent
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("grant_button_${item.id}")
                                ) {
                                    Text("অনুমতি দিন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
