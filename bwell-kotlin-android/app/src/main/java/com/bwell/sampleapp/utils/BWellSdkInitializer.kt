package com.bwell.sampleapp.utils

import android.content.Context
import com.bwell.core.config.types.BWellConfig
import com.bwell.core.config.types.KeyStoreConfig
import com.bwell.core.config.types.LogLevel
import com.bwell.core.config.types.RetryPolicy
import com.bwell.core.network.auth.Credentials
import com.bwell.sampleapp.singletons.BWellSdk
import kotlinx.coroutines.runBlocking

object BWellSdkInitializer {
    suspend fun initialize(context: Context?, clientKey: String, oAuthCredentials: String) {
        println("Initializing SDK")
        val keystore: KeyStoreConfig = KeyStoreConfig.Builder()
            .path(context?.filesDir?.absolutePath ?: "")
            .build()

        val config: BWellConfig = BWellConfig.Builder()
            .clientKey(clientKey)
            .logLevel(LogLevel.DEBUG)
            .timeout(20000)
            .retryPolicy(
                RetryPolicy.Builder()
                    .maxRetries(5)
                    .retryInterval(500)
                    .build()
            )
            .keystore(keystore)
            .build()

        BWellSdk.initialize(config = config)
        val credentials = Credentials.OAuthCredentials(oAuthCredentials)
        println("Auth token: ${credentials.token}")
        BWellSdk.authenticate(credentials)
    }
}
