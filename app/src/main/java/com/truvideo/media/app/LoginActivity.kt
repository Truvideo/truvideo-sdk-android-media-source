package com.truvideo.media.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.truvideo.media.app.ui.theme.TruvideosdkmediaTheme
import com.truvideo.sdk.core.TruvideoSdk
import truvideo.sdk.components.login.TruvideoLoginComponent

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        TruvideoSdk.clear()
//        sdk_common.configuration.environment = TruvideoSdkEnvironment.RC

        setContent {
            TruvideosdkmediaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content()
                }
            }
        }
    }

    @Composable
    fun Content() {
        val activity = this
        val apiKey = "VS2SG9WK"
        val secret = "ST2K33GR"
//        val apiKey = "EPhPPsbv7e"
//        val secret = "9lHCnkfeLl"

        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TruvideoLoginComponent(
                apiKey = apiKey,
                secret = secret,
                isAuthenticated = { TruvideoSdk.isAuthenticated },
                isAuthenticationExpired = { TruvideoSdk.isAuthenticationExpired },
                generatePayload = { TruvideoSdk.generatePayload() },
                authenticate = { apiKey, payload, secret -> TruvideoSdk.authenticate(apiKey, payload, secret) },
                init = { TruvideoSdk.init() },
                callback = {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}