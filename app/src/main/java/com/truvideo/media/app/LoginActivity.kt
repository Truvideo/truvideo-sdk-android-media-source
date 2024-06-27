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
import com.truvideo.media.app.ui.activities.main_activity.MainActivity
import com.truvideo.media.app.ui.theme.TruvideosdkmediaTheme
import com.truvideo.sdk.components.login.TruvideoLoginComponent
import truvideo.sdk.common.model.TruvideoSdkEnvironment
import truvideo.sdk.common.sdk_common

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdk_common.auth.clear()
        sdk_common.configuration.environment = TruvideoSdkEnvironment.PROD

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
        val apiKey = when (sdk_common.configuration.environment) {
            TruvideoSdkEnvironment.DEV -> ""
            TruvideoSdkEnvironment.BETA -> "VS2SG9WK"
//            TruvideoSdkEnvironment.RC -> "VS2SG9WK" // Ours
//            TruvideoSdkEnvironment.RC -> "0EeGlpbESu" // Reynolds
            TruvideoSdkEnvironment.RC -> "Fm1tIv3M1h" // COX

//            TruvideoSdkEnvironment.PROD -> "EPhPPsbv7e" // ours
//            TruvideoSdkEnvironment.PROD -> "5esxyUUl0t" // Reynolds
            TruvideoSdkEnvironment.PROD -> "KFdq0Z9mws" // Reynolds Dev

            else -> ""
        }

        val secret = when (sdk_common.configuration.environment) {
            TruvideoSdkEnvironment.DEV -> ""
            TruvideoSdkEnvironment.BETA -> "ST2K33GR"
//            TruvideoSdkEnvironment.RC -> "ST2K33GR" // Ours
//            TruvideoSdkEnvironment.RC -> "QDjx0T9RyD" // Reynolds
            TruvideoSdkEnvironment.RC -> "J0e9AcUwI9" // COX
//            TruvideoSdkEnvironment.PROD -> "9lHCnkfeLl" // Ours
//            TruvideoSdkEnvironment.PROD -> "PCRE0bdAce" // Reynolds
            TruvideoSdkEnvironment.PROD -> "uv6hqwYwvf" // Reynolds dev
            else -> ""
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TruvideoLoginComponent(
                apiKey = apiKey,
                secret = secret,
                isAuthenticated = { sdk_common.auth.isAuthenticated.value },
                isAuthenticationExpired = { sdk_common.auth.isAuthenticationExpired.value },
                generatePayload = { sdk_common.auth.generatePayload() },
                authenticate = { apiKey, payload, secret -> sdk_common.auth.authenticate(apiKey, payload, secret) },
                init = { sdk_common.auth.init() },
                callback = {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)

                    finish()
                }
            )
        }
    }
}