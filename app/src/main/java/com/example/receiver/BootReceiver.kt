package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.preferences.AppPreferences
import com.example.service.AiAssistantService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val preferences = AppPreferences(context)
            if (preferences.autoStartOnBoot.value && preferences.isAiActive.value) {
                AiAssistantService.startService(context)
            }
        }
    }
}
