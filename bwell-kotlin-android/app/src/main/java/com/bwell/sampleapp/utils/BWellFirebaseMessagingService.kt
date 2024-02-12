package com.bwell.sampleapp.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class BWellFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data?.isNotEmpty() == true) {
            val data: Map<String, String> = remoteMessage.data
            val notificationId = data["notification_id"]!!
            val title = data["title"]!!
            val body = data["body"]!!
            val actionType = data["action_type"]!!
            val action = data["action"]!!

            createNotification(title, body, action)
        }
    }

    private fun createNotification(title: String, body: String, action: String) {
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, Notification(
        )).setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSilent(true)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())
    }

}