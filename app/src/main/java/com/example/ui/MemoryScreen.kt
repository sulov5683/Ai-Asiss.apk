package com.example.ui

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.MemoryDao
import com.example.data.model.MemoryEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    memoryDao: MemoryDao,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val memories by if (searchQuery.isBlank()) {
        memoryDao.getAllMemories().collectAsStateWithLifecycle(initialValue = emptyList())
    } else {
        memoryDao.searchMemories(searchQuery).collectAsStateWithLifecycle(initialValue = emptyList())
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMemory by remember { mutableStateOf<MemoryEntity?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("memory_screen"),
        containerColor = ObsidianBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ব্যক্তিগত মেমোরি", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${memories.size} টি তথ্য স্থানীয়ভাবে সংরক্ষিত", color = TextMuted, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("memory_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }, modifier = Modifier.testTag("memory_export_button")) {
                        Icon(Icons.Default.Download, contentDescription = "Export JSON", tint = CyanAccent)
                    }
                    IconButton(onClick = { showImportDialog = true }, modifier = Modifier.testTag("memory_import_button")) {
                        Icon(Icons.Default.Upload, contentDescription = "Import JSON", tint = NeonEmerald)
                    }
                    IconButton(
                        onClick = { if (memories.isNotEmpty()) showClearAllDialog = true },
                        modifier = Modifier.testTag("memory_clear_all_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Clear All", tint = TextMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanAccent,
                contentColor = ObsidianBg,
                shape = CircleShape,
                modifier = Modifier.testTag("add_memory_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Memory")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Privacy Guarantee Notice Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "১০০% গোপনীয়তা: এই মেমোরি শুধুমাত্র আপনার ডিভাইসে সেভ থাকে। আপনার স্পষ্ট নির্দেশ বাদে কিছুই সেভ বা ক্লাউডে যায় না।",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_search_input"),
                placeholder = { Text("মেমোরিতে খুঁজুন...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedContainerColor = ObsidianSurface,
                    unfocusedContainerColor = ObsidianSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (memories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "কোনো তথ্য পাওয়া যায়নি" else "কোনো মেমোরি সংরক্ষিত নেই",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ভয়েস কমান্ড দিয়ে বলুন: \"এটা মনে রাখো: আমার রক্ত গ্রুপ O+\"",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memories, key = { it.id }) { item ->
                        MemoryItemCard(
                            memory = item,
                            onEdit = { editingMemory = item },
                            onDelete = {
                                scope.launch {
                                    memoryDao.deleteMemory(item)
                                    Toast.makeText(context, "মেমোরি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("সাধারণ") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("নতুন মেমোরি যুক্ত করুন", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("শিরোনাম / বিষয়", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("বিবরণ / তথ্য", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (content.isNotBlank()) {
                            scope.launch {
                                memoryDao.insertMemory(
                                    MemoryEntity(
                                        title = if (title.isBlank()) "তথ্য" else title,
                                        content = content.trim(),
                                        category = category
                                    )
                                )
                                showAddDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = ObsidianBg)
                ) {
                    Text("সংরক্ষণ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("বাতিল", color = TextMuted)
                }
            }
        )
    }

    // Edit Memory Dialog
    editingMemory?.let { memory ->
        var editTitle by remember { mutableStateOf(memory.title) }
        var editContent by remember { mutableStateOf(memory.content) }

        AlertDialog(
            onDismissRequest = { editingMemory = null },
            containerColor = ObsidianSurface,
            title = { Text("মেমোরি সম্পাদনা", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("শিরোনাম", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("তথ্য", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editContent.isNotBlank()) {
                            scope.launch {
                                memoryDao.updateMemory(
                                    memory.copy(title = editTitle, content = editContent)
                                )
                                editingMemory = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = ObsidianBg)
                ) {
                    Text("আপডেট", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMemory = null }) {
                    Text("বাতিল", color = TextMuted)
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("সব মেমোরি মুছে ফেলবেন?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text("এই অ্যাকশনের মাধ্যমে সমস্ত সংরক্ষিত ব্যক্তিগত তথ্য স্থানীয় ডাটাবেস থেকে স্থায়ীভাবে মুছে যাবে।", color = TextSecondary, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            memoryDao.clearAll()
                            showClearAllDialog = false
                            Toast.makeText(context, "সমস্ত মেমোরি মোছা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFF5252))
                ) {
                    Text("সব মুছুন", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("বাতিল", color = TextMuted)
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        val jsonString = remember(memories) {
            val arr = JSONArray()
            memories.forEach { m ->
                val obj = JSONObject().apply {
                    put("id", m.id)
                    put("title", m.title)
                    put("content", m.content)
                    put("category", m.category)
                    put("timestamp", m.timestamp)
                }
                arr.put(obj)
            }
            arr.toString(2)
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("মেমোরি ব্যাকআপ (JSON)", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("ক্লিপবোর্ডে কপি করে নিরাপদে ব্যাকআপ রাখতে পারেন:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(jsonString, color = TextPrimary, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(jsonString))
                        Toast.makeText(context, "ক্লিপবোর্ডে কপি হয়েছে", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = ObsidianBg)
                ) {
                    Text("কপি করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("বন্ধ", color = TextMuted)
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("মেমোরি রিস্টোর (JSON Import)", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("পূর্বে এক্সপোর্ট করা JSON পেস্ট করুন:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonEmerald,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val arr = JSONArray(importJsonText)
                            scope.launch {
                                for (i in 0 until arr.length()) {
                                    val obj = arr.getJSONObject(i)
                                    memoryDao.insertMemory(
                                        MemoryEntity(
                                            title = obj.optString("title", "আমদানিকৃত মেমোরি"),
                                            content = obj.optString("content", ""),
                                            category = obj.optString("category", "General")
                                        )
                                    )
                                }
                                Toast.makeText(context, "${arr.length()} টি মেমোরি রিস্টোর করা হয়েছে", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                                importJsonText = ""
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "অবৈধ JSON ফরম্যাট: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = ObsidianBg)
                ) {
                    Text("ইমপোর্ট", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("বাতিল", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun MemoryItemCard(
    memory: MemoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(memory.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(memory.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("memory_item_${memory.id}"),
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
                Text(
                    text = memory.title,
                    color = CyanAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateStr,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = memory.content,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.size(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = androidx.compose.ui.graphics.Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
