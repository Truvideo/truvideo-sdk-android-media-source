package com.truvideo.media.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.truvideo.media.app.ui.theme.TruvideosdkmediaTheme
import com.truvideo.sdk.core.TruvideoSdk
import kotlinx.coroutines.launch
import truvideo.sdk.components.button.TruvideoButton
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TruvideosdkmediaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Content()
                }
            }
        }
    }

    @Composable
    fun Content() {
        var apiKey by remember { mutableStateOf("VS2SG9WK") }
        var secret by remember { mutableStateOf("ST2K33GR") }
        var isLoading by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            if(TruvideoSdk.isAuthenticated && !TruvideoSdk.isAuthenticationExpired){
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
            }
        }

        fun authenticate() {
            scope.launch {
                isLoading = true
                try {
                    val payload = TruvideoSdk.generatePayload()
                    val signature = generateSignature(payload, secret)
                    TruvideoSdk.authenticate(
                        apiKey = apiKey,
                        payload = payload,
                        signature = signature
                    )


                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

                isLoading = false
            }
        }

        Box {
            Column(Modifier.padding(16.dp)) {
                TextField(
                    value = apiKey,
                    enabled = !isLoading,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(Modifier.height(8.dp))
                TextField(
                    value = secret,
                    enabled = !isLoading,
                    onValueChange = { secret = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(Modifier.height(16.dp))
                TruvideoButton(
                    text = "Authenticate",
                    enabled = !isLoading,
                    onPressed = { authenticate() }
                )
            }
        }
    }
}


private fun generateSignature(payload: String, secret: String): String {
    val keyBytes: ByteArray = secret.toByteArray(StandardCharsets.UTF_8)
    val messageBytes: ByteArray = payload.toByteArray(StandardCharsets.UTF_8)

    try {
        val hmacSha256 = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(keyBytes, "HmacSHA256")
        hmacSha256.init(secretKeySpec)
        val signatureBytes = hmacSha256.doFinal(messageBytes)
        return bytesToHex(signatureBytes)
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: InvalidKeyException) {
        e.printStackTrace()
    }
    return ""
}

private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = "0123456789abcdef"
    val hex = StringBuilder(bytes.size * 2)
    for (i in bytes.indices) {
        val value = bytes[i].toInt() and 0xFF
        hex.append(hexChars[value shr 4])
        hex.append(hexChars[value and 0x0F])
    }
    return hex.toString()
}