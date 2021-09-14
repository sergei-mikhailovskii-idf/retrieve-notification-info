package com.mikhailovskii.notificationstatusretriever

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.text.HtmlCompat
import com.google.android.gms.auth.api.credentials.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvSendNotification: AppCompatTextView
    private lateinit var tvSpan: AppCompatTextView

    companion object {
        const val CREDENTIAL_PICKER_REQUEST = 1
    }

    private val notificationManagerCompat by lazy { NotificationManagerCompat.from(this) }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSendNotification = findViewById(R.id.tv_send_notification)
        tvSpan = findViewById(R.id.tv_span)

        val text = HtmlCompat.fromHtml(
            "<a href=\"https://html-online.com\">This is</a> a link.",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        tvSpan.text = text
        tvSpan.movementMethod = LinkMovementMethod.getInstance()

        createNotificationChannel()

        println("Notifications available for app: ${areNotificationsForAppEnabled()}")

        tvSendNotification.setOnClickListener {
//            showNotification()
//            println("Notification channel 1234 available: ${isNotificationChannelEnabled("1234")}")
//            checkDoNotDisturbMode()
            requestHint()
        }
    }

    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()

        val options = CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()

        val credentialsClient = Credentials.getClient(this, options)

        val intent = credentialsClient.getHintPickerIntent(hintRequest)

        try {
            startIntentSenderForResult(
                intent.intentSender,
                CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0, Bundle()
            )
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == RESULT_OK) {

            val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)

            // set the received data t the text view
            credential?.apply {
                println(credential.id)
            }
        } else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
            Toast.makeText(this, "No phone numbers found", Toast.LENGTH_LONG).show();
        } else {
            requestHint()
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