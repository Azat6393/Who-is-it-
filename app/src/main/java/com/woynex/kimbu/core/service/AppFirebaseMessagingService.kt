package com.woynex.kimbu.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.woynex.kimbu.AuthActivity
import com.woynex.kimbu.R
import com.woynex.kimbu.core.data.local.room.NotificationDao
import com.woynex.kimbu.core.domain.model.NotificationModel
import com.woynex.kimbu.feature_settings.domain.use_case.InsertNotification
import com.woynex.kimbu.feature_settings.domain.use_case.SettingsUseCases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppFirebaseMessagingService : FirebaseMessagingService() {

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder

    private val scope = CoroutineScope(SupervisorJob())

    @Inject
    lateinit var settingsUseCases: SettingsUseCases

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"]
        val text = message.data["text"]

        scope.launch {
            settingsUseCases.insertNotification(
                NotificationModel(
                    title = message.data["title"] ?: "",
                    text = message.data["text"] ?: ""
                )
            )
        }

        val CHANNEL_ID = "KIM_BU_NOTIFICATION"
        val intent = Intent(this, AuthActivity::class.java)

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Kim bu notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(text)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.ic_launcher_background
                    )
                )
                .setContentIntent(pendingIntent)
        } else {
            builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(text)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.ic_launcher_background
                    )
                )
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1, builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}