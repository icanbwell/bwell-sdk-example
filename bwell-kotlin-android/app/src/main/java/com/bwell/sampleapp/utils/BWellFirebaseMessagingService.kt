package com.bwell.sampleapp.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class BWellFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //check if the message contains data
        if (remoteMessage.data.isNotEmpty()) {
            //handle message data
        }

        // check if message contains a notification payload
        if (remoteMessage.notification != null) {
            // handle notification payload
        }
    }
}