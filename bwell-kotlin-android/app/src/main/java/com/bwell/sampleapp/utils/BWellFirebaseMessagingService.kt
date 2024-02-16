package com.bwell.sampleapp.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.bwell.device.requests.deviceToken.DevicePlatform
import com.bwell.device.requests.deviceToken.RegisterDeviceTokenRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.activities.NavigationActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val CHANNEL_ID = "1"


class BWellFirebaseMessagingService : FirebaseMessagingService() {
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

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
        registerDeviceToken(token)
    }

    private fun registerDeviceToken(token: String) {
        val registerDeviceTokenRequest: RegisterDeviceTokenRequest = RegisterDeviceTokenRequest.Builder()
            .deviceToken(token)
            .applicationName("com.bwell.sampleapp")
            .platform(DevicePlatform.ANDROID)
            .build()

        val repository = (this.application as? BWellSampleApplication)?.bWellRepository!!
        coroutineScope.launch {
            val registerOutcome = repository.registerDeviceToken(registerDeviceTokenRequest)
            registerOutcome.collect { outcome ->
                outcome?.let {
                    if (outcome.success()) {
                        println("FCM_TOKEN Registered Successfully")
                    } else {
                        println("FCM_TOKEN Failed to register")
                    }
                }
            }
        }
    }

    private fun createNotification(title: String, body: String, action: String, actionType: String) {
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, Notification(
        ))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setChannelId(CHANNEL_ID)
            .setSilent(true)
            .setSmallIcon(coil.base.R.drawable.notification_icon_background)
            .setSound(null)

        if (actionType == "deep_link") {
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
            }
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}