package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.AiAssistantApp
import com.example.MainActivity
import com.example.R
import com.example.data.model.AiMode
import com.example.data.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiAssistantService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var preferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = AppPreferences(this)
        _isServiceRunning.value = true

        observePreferences()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                preferences.setAiActive(false)
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_LISTENING -> {
                val current = preferences.isListeningActive.value
                preferences.setListeningActive(!current)
            }
            else -> {
                startInForeground()
            }
        }
        return START_STICKY
    }

    private fun observePreferences() {
        serviceScope.launch {
            preferences.aiMode.collect { updateNotification() }
        }
        serviceScope.launch {
            preferences.isListeningActive.collect { updateNotification() }
        }
    }

    private fun startInForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AiAssistantService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, AiAssistantService::class.java).apply {
            action = ACTION_TOGGLE_LISTENING
        }
        val togglePendingIntent = PendingIntent.getService(
            this, 2, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val modeLabel = when (preferences.aiMode.value) {
            AiMode.OFFLINE -> "Offline Mode"
            AiMode.ONLINE -> "Online Mode"
            AiMode.HYBRID -> "Hybrid Mode"
        }

        val listeningStatus = if (preferences.isListeningActive.value) "Listening Active" else "Listening Paused"

        return NotificationCompat.Builder(this, AiAssistantApp.CHANNEL_ID)
            .setContentTitle("Personal AI Assistant")
            .setContentText("$modeLabel • $listeningStatus")
            .setSmallIcon(R.drawable.ai_assistant_icon_1789308733632)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (preferences.isListeningActive.value) "Mute Mic" else "Listen",
                togglePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop AI",
                stopPendingIntent
            )
            .build()
    }

    private fun stopForegroundService() {
        _isServiceRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        _isServiceRunning.value = false
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_TOGGLE_LISTENING = "com.example.service.ACTION_TOGGLE_LISTENING"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, AiAssistantService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AiAssistantService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
