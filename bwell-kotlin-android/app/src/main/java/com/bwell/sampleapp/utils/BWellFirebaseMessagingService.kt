package com.bwell.sampleapp.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.NavigationActivity
import com.bwell.sampleapp.singletons.BWellSdk
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.Serializable


private const val CHANNEL_ID = "1"


class BWellFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: ""
            val body = remoteMessage.data["body"] ?: ""

            createNotification(title, body, remoteMessage.data)
        }
    }

    private fun createNotification(title: String, body: String, remoteMessageData: Map<String, String>) {
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, Notification(
        ))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setChannelId(CHANNEL_ID)
            .setSilent(true)
            .setSmallIcon(coil.base.R.drawable.notification_icon_background)

        if (remoteMessageData["action_type"] == "deeplink") {
            var resultIntent: Intent? = null
            if (remoteMessageData["action"]?.startsWith("ActivityDefinition/") == true) {
                resultIntent = Intent(this, NavigationActivity::class.java).apply {
                    putExtra("remoteMessageDataStringified", JSONObject(remoteMessageData).toString())
                }
            }

            if (resultIntent != null) {
                val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                notificationBuilder.setContentIntent(pendingIntent)
            }
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BWell Sample App Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}