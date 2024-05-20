package com.truvideo.media.app

import android.Manifest
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.truvideo.media.app.ui.theme.TruvideosdkmediaTheme
import com.truvideo.media.app.utils.RealPathUtil
import com.truvideo.sdk.media.TruvideoSdkMedia
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import kotlinx.coroutines.launch
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.sdk_common
import truvideo.sdk.components.button.TruvideoButton
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TruvideosdkmediaTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Content()
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Content() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val scope = rememberCoroutineScope()

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_IMAGES,
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val permissionStatus = rememberMultiplePermissionsState(permissions)
        LaunchedEffect(Unit) {
            if (!permissionStatus.allPermissionsGranted) {
                permissionStatus.launchMultiplePermissionRequest()
            }
        }

        var requests by remember { mutableStateOf(listOf<TruvideoSdkMediaFileUploadRequest>()) }

        val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            val uri = it
            if (uri == null) {
                Log.d("TruvideoSdkMedia", "Uri null")
                return@rememberLauncherForActivityResult
            }

            val path = RealPathUtil.realPathFromUri(context, uri) ?: ""
            if (path.trim().isEmpty()) {
                Log.d("TruvideoSdkMedia", "Path is empty")
                return@rememberLauncherForActivityResult
            }

            val file = File(path)

            if (file.exists()) {
                scope.launch {
                    val builder = TruvideoSdkMedia.FileUploadRequestBuilder(path)

                    builder.addTag("MyTag", "this is my tag")
                    builder.addTag("key", "value")
                    builder.addTag("color", "red")
                    builder.addTag("order-id", "123")

                    val metadata = mapOf(
                        "key" to "value",
                        "key2" to 2,
                        "nested" to mapOf(
                            "key3" to 3,
                            "key4" to "value4"
                        )
                    )

                    builder.addMetadata(metadata)
                    builder.build()
                }
            } else {
                Log.d("TruvideoSdkMedia", "File do not exists. $path")
            }
        }

        LaunchedEffect(Unit) {
            TruvideoSdkMedia.streamAllFileUploadRequests().observe(lifecycleOwner) {
                requests = it
            }

            Log.d("JOSE", "isAuthenticated: ${sdk_common.auth.isInitialized.value}")
        }


        fun showError(error: String) {
            AlertDialog.Builder(context)
                .setMessage(error)
                .show()
        }


        Column {
            Column(Modifier.padding(16.dp)) {
                TruvideoButton(
                    text = "Pick file",
                    onPressed = { filePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
                )
            }

            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(requests, key = { it.id }) {
                    Card(Modifier.padding(bottom = 8.dp)) {
                        Column(Modifier.padding(8.dp)) {
                            Row {
                                Text(it.id, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                Text(it.status.name)
                            }
                            Text("Created At", fontSize = 10.sp, fontWeight = FontWeight.W500)
                            Text("${it.createdAt}", fontSize = 10.sp)

                            Text("Updated At", fontSize = 10.sp, fontWeight = FontWeight.W500)
                            Text("${it.updatedAt}", fontSize = 10.sp)

                            if (it.progress != null) {
                                Text("Progress", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                Text("${(it.progress!! * 100)}%", fontSize = 10.sp)
                            }

                            if (it.url != null) {
                                Text("URL", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                Text("${it.url}", fontSize = 10.sp)
                            }

                            if (it.transcriptionUrl != null) {
                                Text("Transcription URL", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                Text("${it.transcriptionUrl}", fontSize = 10.sp)
                            }

                            if (it.tags.entries.isNotEmpty()) {
                                Text("Tags", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                it.tags.entries.forEach {
                                    Text("${it.key} = ${it.value}", fontSize = 10.sp)
                                }
                            }

                            if (it.metadata.entries.isNotEmpty()) {


                                Text("Metadata", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                it.metadata.entries.forEach {
                                    if (it.value is Map<*, *>) {
                                        Text("${it.key} = ", fontSize = 10.sp)
                                        (it.value as Map<*, *>).entries.forEach { nested ->
                                            Text(
                                                "${nested.key} = ${nested.value}",
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    } else {
                                        Text("${it.key} = ${it.value}", fontSize = 10.sp)
                                    }
                                }
                            }

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                TruvideoButton(
                                    text = "Upload",
                                    onPressed = {
                                        scope.launch {
                                            try {
                                                it.upload(
                                                    object : TruvideoSdkMediaFileUploadCallback {

                                                        override fun onComplete(
                                                            id: String,
                                                            response: TruvideoSdkMediaFileUploadRequest
                                                        ) {
                                                            Log.d("TruvideoSdkMedia", "$id Complete")
                                                            response.tags.entries.forEach {
                                                                Log.d("TruvideoSdkMedia", "${it.key} = ${it.value}")
                                                            }
                                                        }

                                                        override fun onProgressChanged(
                                                            id: String,
                                                            progress: Float
                                                        ) {
                                                            Log.d("TruvideoSdkMedia", "$id $progress")
                                                        }

                                                        override fun onError(
                                                            id: String,
                                                            ex: TruvideoSdkException
                                                        ) {
                                                            Log.d("TruvideoSdkMedia", "$id $ex")
                                                        }
                                                    }
                                                )
                                            } catch (exception: Exception) {
                                                exception.printStackTrace()
                                                showError(exception.localizedMessage ?: "Uknown error")
                                            }
                                        }
                                    }
                                )

                                Box(Modifier.width(8.dp))

                                TruvideoButton(
                                    text = "Pause",
                                    onPressed = {
                                        scope.launch {
                                            try {
                                                it.pause()
                                            } catch (exception: Exception) {
                                                exception.printStackTrace()
                                                showError(exception.localizedMessage ?: "Uknown error")
                                            }
                                        }
                                    }
                                )

                                Box(Modifier.width(8.dp))

                                TruvideoButton(
                                    text = "Resume",
                                    onPressed = {
                                        scope.launch {
                                            try {
                                                it.resume()
                                            } catch (exception: Exception) {
                                                exception.printStackTrace()
                                                showError(exception.localizedMessage ?: "Uknown error")
                                            }
                                        }
                                    }
                                )

                                Box(Modifier.width(8.dp))

                                TruvideoButton(
                                    text = "Cancel",
                                    onPressed = {
                                        scope.launch {
                                            try {
                                                it.cancel()
                                            } catch (exception: Exception) {
                                                exception.printStackTrace()
                                                showError(exception.localizedMessage ?: "Uknown error")
                                            }
                                        }
                                    }
                                )

                                Box(Modifier.width(8.dp))

                                TruvideoButton(
                                    text = "Delete",
                                    onPressed = {
                                        scope.launch {
                                            try {
                                                it.delete()
                                            } catch (exception: Exception) {
                                                exception.printStackTrace()
                                                showError(exception.localizedMessage ?: "Uknown error")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
