package com.woynex.kimbu.core.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class AppFirebaseMessagingService : FirebaseMessagingService() {

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder

    @Inject
    lateinit var dao: NotificationDao

    override fun onMessageReceived(message: RemoteMessage) {

        if (message.data.isNotEmpty()) {
            message.notification?.let {

                val title = message.notification!!.title
                val text = message.notification!!.body

                CoroutineScope(Dispatchers.IO).launch {
                    dao.insertNotification(
                        NotificationModel(
                            title = title ?: "",
                            text = text ?: ""
                        )
                    )
                }

                val CHANNEL_ID = "KIM_BU_NOTIFICATION"
                val intent = Intent(this, AuthActivity::class.java)

                val pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


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
                super.onMessageReceived(message)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}