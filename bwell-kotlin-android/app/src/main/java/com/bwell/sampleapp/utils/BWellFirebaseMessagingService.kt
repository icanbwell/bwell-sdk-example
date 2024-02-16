package com.bwell.sampleapp.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.NavigationActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


private const val CHANNEL_ID = "1"


class BWellFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data?.isNotEmpty() == true) {
            val data: Map<String, String> = remoteMessage.data
            val notificationId = data["notification_id"]
            val title = data["title"] ?: ""
            val body = data["body"] ?: ""
            val actionType = data["action_type"] ?: ""
            val action = data["action"] ?: ""

            createNotification(title, body, action, actionType)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        storeDeviceToken(token)
    }

    private fun storeDeviceToken(token: String) {
        println("FCM_TOKEN storeDeviceToken: $token")
        // save the fcm token in encrypted shared preferences
        val encryptedPreferences = getEncryptedSharedPreferences(applicationContext)
        val encryptedEditor = encryptedPreferences.edit()
        encryptedEditor.putString(R.string.fcm_device_token.toString(), token)
        encryptedEditor.apply()

        // save a flag indicating that the new fcm token has not been registered
        val sharedPreferences = getSharedPreferences(applicationContext)
        val editor = sharedPreferences.edit()
        editor.putBoolean(R.string.fcm_device_token_registered.toString(), false)
        editor.apply()
    }

    private fun createNotification(title: String, body: String, action: String, actionType: String) {
        println("TEST_LOG createNotification called: ($title, $body, $action, $actionType)",)
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, Notification(
        ))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setChannelId(CHANNEL_ID)
            .setSilent(true)
            .setSmallIcon(coil.base.R.drawable.notification_icon_background)

        if (actionType == "deep_link") {
            println("TEST_LOG actionType == deep_link")
            var resultIntent: Intent? = null
            if (action.startsWith("ActivityDefinition/")) {
                resultIntent = Intent(this, NavigationActivity::class.java)
                val bundle = Bundle().apply {
                    putString("action", action)
                }
                resultIntent.putExtras(bundle)
            }

            if (resultIntent != null) {
                val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                notificationBuilder.setContentIntent(pendingIntent)
                println("TEST_LOG resultIntent != null")
            }
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())
        println("TEST_LOG notificationManager.notify")
    }
}