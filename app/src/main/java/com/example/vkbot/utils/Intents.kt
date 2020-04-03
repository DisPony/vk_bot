package com.example.vkbot.utils

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent

inline fun <reified T : Service> Context.pendingIntent(
    action: String,
    requestCode: Int = 10,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
    config: Intent.() -> Unit = {}
): PendingIntent {
    val intent = Intent(this, T::class.java)
    intent.action = action
    intent.config()
    return PendingIntent.getService(this, requestCode, intent, flags)
}