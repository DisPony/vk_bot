package com.example.vkbot.service

import android.app.*
import android.content.Intent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import com.example.vkbot.R
import com.example.vkbot.bot.Bot
import com.example.vkbot.bot.dialogues
import com.example.vkbot.utils.notification
import com.example.vkbot.utils.pendingIntent
import kotlin.properties.Delegates

const val ACTION_LAUNCH = "com.example.vkbot.action.LAUNCH"
private const val ACTION_PAUSE = "com.example.vkbot.action.PAUSE"
private const val ACTION_STOP = "com.example.vkbot.action.STOP"


private const val EXTRA_KEY = "com.example.vkbot.extra.KEY"
private const val CHANNEL_ID: String = "com.example.vkbot.bot_control_channel"
private const val CONTROL_NOTIFICATION_ID = 1
private const val NEW_JOB_NOTIFICATION_ID = 2

class BotService : LifecycleService() {

    private var viewModel: BotViewModel? = null
    private var prevKey: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_LAUNCH -> {
                Log.d("BotService", "key received ${intent.getStringExtra(EXTRA_KEY)}")
                intent.getStringExtra(EXTRA_KEY)?.let { recreateIfNewAndStart(it) }
            }
            ACTION_PAUSE -> {
                viewModel?.onStop()
            }
            ACTION_STOP -> {
                viewModel?.onStop()
                stopForeground(true)
                stopSelf()
            }
        }
        Log.d("BotService", "${intent?.action}")
        return START_NOT_STICKY
    }

    private fun recreateIfNewAndStart(newKey: String) {
        if (newKey != prevKey) viewModel = createNewBot(newKey)
        prevKey = newKey
        Log.d("BotService", "key saved $prevKey")
        viewModel?.onStart()
    }

    private fun createNewBot(apiKey: String): BotViewModel {
        viewModel?.onStop()
        viewModel?.clearSubscriptions()
        return BotViewModel(apiKey, Bot(dialogues)).also {
            it.isStarted.observe(this, Observer { isStarted ->
                Log.d("BotService", "isStarted: $isStarted")
                updateNotification(
                    id = CONTROL_NOTIFICATION_ID,
                    notification = if (isStarted) botStartedNotification()
                    else botPausedNotification(apiKey)
                )
            })
            it.newJobEvent.observe(this, Observer { job ->
                updateNotification(NEW_JOB_NOTIFICATION_ID, newJobNotification(job))
            })
        }
    }

    private fun BotViewModel.clearSubscriptions(owner: LifecycleOwner = this@BotService) {
        isStarted.removeObservers(owner)
        newJobEvent.removeObservers(owner)
    }


    private fun updateNotification(id: Int, notification: Notification) =
        with(NotificationManagerCompat.from(this)) {
            notify(id, notification)
        }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(CONTROL_NOTIFICATION_ID, serviceLaunchingNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.bot_control_channel)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = getString(R.string.channel_description)
            }
            with(NotificationManagerCompat.from(this)) {
                createNotificationChannel(channel)
            }
        }
    }

    private fun serviceLaunchingNotification() = notification(
        channelId = CHANNEL_ID,
        smallIcon = R.drawable.ic_notification,
        contentTitle = "",
        contentText = "Запуск"
    )

    private fun newJobNotification(job: String) = notification(
        channelId = CHANNEL_ID,
        smallIcon = R.drawable.ic_notification,
        contentTitle = getString(R.string.new_job),
        contentText = job
    )

    private fun botStartedNotification() = controlNotification(
        contentText = getString(R.string.paused),
        primaryAction = pauseAction()
    )

    private fun botPausedNotification(key: String) = controlNotification(
        contentText = getString(R.string.start),
        primaryAction = startAction(key)
    )

    private fun controlNotification(
        contentTitle: String = "",
        contentText: String,
        primaryAction: NotificationCompat.Action
    ) = notification(CHANNEL_ID, R.drawable.ic_notification, contentTitle, contentText) {
        setOngoing(true)
        addAction(primaryAction)
        addAction(stopAction())
    }

    private fun startAction(key: String) =
        NotificationCompat.Action(R.drawable.ic_start, getString(R.string.start), startIntent(key))

    private fun startIntent(key: String) = pendingIntent<BotService>(ACTION_LAUNCH) {
        putExtra(EXTRA_KEY, key)
    }

    private fun stopAction() =
        NotificationCompat.Action(R.drawable.ic_close, getString(R.string.stop), stopIntent())

    private fun stopIntent() = pendingIntent<BotService>(ACTION_STOP)

    private fun pauseAction() =
        NotificationCompat.Action(R.drawable.ic_pause, getString(R.string.pause), pauseIntent())

    private fun pauseIntent() = pendingIntent<BotService>(ACTION_PAUSE)

    companion object {
        fun launch(context: Context, key: String) {
            val intent = Intent(context, BotService::class.java).apply {
                action = ACTION_LAUNCH
                putExtra(EXTRA_KEY, key)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
