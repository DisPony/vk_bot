package com.example.vkbot.utils

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat

fun Context.notification(
    channelId: String,
    smallIcon: Int,
    contentTitle: String,
    contentText: String,
    config: NotificationCompat.Builder.() -> Unit = {}
): Notification = NotificationCompat.Builder(this, channelId).apply {
    setSmallIcon(smallIcon)
    setContentTitle(contentTitle)
    setContentText(contentText)
    config()
}.build()