package com.mikhailovskii.notificationstatusretriever

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvSendNotification: AppCompatTextView

    private val notificationManagerCompat by lazy { NotificationManagerCompat.from(this) }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSendNotification = findViewById(R.id.tv_send_notification)

        createNotificationChannel()

        println("Notifications available for app: ${areNotificationsForAppEnabled()}")

        tvSendNotification.setOnClickListener {
            showNotification()
            println("Notification channel 1234 available: ${isNotificationChannelEnabled("1234")}")
            checkDoNotDisturbMode()
        }
    }

    private fun showNotification() {
        val builder = NotificationCompat.Builder(this, "1234")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("test title")
            .setContentText("test text")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(notificationManagerCompat) {
            notify((Calendar.getInstance().time.time % 10).toInt(), builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel name"
            val descriptionText = "channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1234", name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun areNotificationsForAppEnabled() =
        notificationManagerCompat.areNotificationsEnabled()

    private fun isNotificationChannelEnabled(channelId: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.getNotificationChannel(channelId).importance != NotificationManager.IMPORTANCE_NONE
        } else {
            notificationManagerCompat.areNotificationsEnabled()
        }

    /**
     * ZEN_MODE_OFF = 0
     *
     * ZEN_MODE_IMPORTANT_INTERRUPTIONS
     *
     * ZEN_MODE_NO_INTERRUPTIONS = 2
     *
     * ZEN_MODE_ALARMS = 3
     */
    private fun checkDoNotDisturbMode() {
        val dndStatus = Settings.Global.getInt(contentResolver, "zen_mode")
        val state = ZenModeState.values()[dndStatus]
        println("Zen mode: ${state.name}")
    }
}