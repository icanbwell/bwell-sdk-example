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
import com.bwell.core.network.auth.Credentials
import com.bwell.core.config.types.BWellConfig
import com.bwell.core.config.types.LogLevel
import com.bwell.core.config.types.RetryPolicy
import com.bwell.sampleapp.singletons.BWellSdk
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
