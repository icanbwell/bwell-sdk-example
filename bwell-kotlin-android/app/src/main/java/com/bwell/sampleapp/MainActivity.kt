package com.bwell.sampleapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.bwell.sampleapp.ui.theme.MyTestAppTheme
// BWell SDK Usage
import com.bwell.BWellSdk
import com.bwell.core.auth.Credentials
import com.bwell.core.config.BWellConfig
import com.bwell.core.config.LogLevel
import com.bwell.core.config.RetryPolicy
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BWellSdk.initialize(
            config = BWellConfig.Builder()
                .clientKey("CLIENT_KEY")
                .logLevel(LogLevel.DEBUG)
                .timeout(20000)
                .retryPolicy(RetryPolicy(maxRetries = 5, retryInterval = 500))
                .build()
        )

        val credentials = Credentials.OAuthCredentials("token")
        Log.d("BWell Sample App", credentials.token)
        BWellSdk.authenticate(credentials)
        setContent {
            MyTestAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UserProfile()
                }
            }
        }
    }
}

@Composable
fun UserProfile() {
    var profileText by remember { mutableStateOf("Loading profile...") }

    LaunchedEffect(key1 = Unit) {
        profileText = BWellSdk.user?.getProfile()?.toString() ?: "Profile not available"
    }

    Greeting(name = profileText, modifier = Modifier.wrapContentSize(Alignment.Center))
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyTestAppTheme {
        Greeting("Android")
    }
}
