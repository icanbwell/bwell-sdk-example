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
import androidx.navigation.NavController
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.NavigationActivity
import com.bwell.sampleapp.singletons.BWellSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

private const val CHANNEL_ID = "1"

/**
 * Consolidated notification handler that works with or without Firebase.
 * Handles both push notification display and deep link processing.
 */
class NotificationHandler {

    companion object {
        private const val TAG = "NotificationHandler"

        /**
         * Creates and displays a notification (used by Firebase service when enabled)
         */
        fun createNotification(
            context: Context,
            title: String,
            body: String,
            remoteMessageData: Map<String, String>
        ) {
            val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, Notification())
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setChannelId(CHANNEL_ID)
                .setSilent(true)
                .setSmallIcon(coil.base.R.drawable.notification_icon_background)

            if (remoteMessageData["action_type"] == "deep_link") {
                val resultIntent = createDeepLinkIntent(context, remoteMessageData)
                if (resultIntent != null) {
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(
                        context, 
                        0, 
                        resultIntent, 
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    notificationBuilder.setContentIntent(pendingIntent)
                }
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "BWell Sample App Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0, notificationBuilder.build())
        }

        /**
         * Creates an intent for deep link navigation
         */
        private fun createDeepLinkIntent(context: Context, remoteMessageData: Map<String, String>): Intent? {
            return if (remoteMessageData["action"]?.startsWith("ActivityDefinition/") == true) {
                Intent(context, NavigationActivity::class.java).apply {
                    putExtra("remoteMessageDataStringified", JSONObject(remoteMessageData).toString())
                }
            } else null
        }

        /**
         * Handles deep link processing for the NavigationActivity
         * This works whether the data comes from a push notification or other source
         */
        fun handleDeepLink(
            intent: Intent?,
            navController: NavController,
            onNotificationHandled: ((Boolean) -> Unit)? = null
        ) {
            // Handle notification deep links
            val remoteMessageDataStringified = intent?.getStringExtra("remoteMessageDataStringified")
            if (!remoteMessageDataStringified.isNullOrBlank()) {
                processNotificationDeepLink(remoteMessageDataStringified, navController, onNotificationHandled)
                return
            }

            // Handle regular deep links (like OAuth callbacks)
            val intentData = intent?.data
            if (intentData != null && intentData.scheme == "bwell" && intentData.host == "ial2-callback") {
                navController.navigate(R.id.nav_profile)
            }
        }

        /**
         * Processes notification-based deep links
         */
        private fun processNotificationDeepLink(
            remoteMessageDataStringified: String,
            navController: NavController,
            onNotificationHandled: ((Boolean) -> Unit)?
        ) {
            try {
                val remoteMessageData = JSONObject(remoteMessageDataStringified)
                val action = remoteMessageData.getString("action")
                
                if (action.startsWith("ActivityDefinition/")) {
                    // Register the notification event (fire and forget)
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = BWellSdk.event.handleNotification(remoteMessageDataStringified)
                        val success = result.success()
                        if (success) {
                            Log.i(TAG, "Push notification registered.")
                        } else {
                            Log.e(TAG, "There was an error registering a push notification")
                        }
                        onNotificationHandled?.invoke(success)
                    }

                    // Navigate to the appropriate screen
                    val bundle = Bundle().apply {
                        val taskId = action.replace("ActivityDefinition/", "")
                        putString("task_id", taskId)
                    }
                    navController.navigate(R.id.nav_health_journey, bundle)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification deep link", e)
                onNotificationHandled?.invoke(false)
            }
        }
    }
}