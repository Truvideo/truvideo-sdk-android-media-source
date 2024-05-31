package com.truvideo.media.app.ui.activities.main_activity

import android.app.AlertDialog
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truvideo.sdk.components.button.TruvideoButton
import com.truvideo.sdk.components.scale_button.TruvideoScaleButton
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import kotlinx.coroutines.launch

@Composable
fun ListItem(request: TruvideoSdkMediaFileUploadRequest) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var infoVisible by remember { mutableStateOf(true) }

    fun showError(error: String) {
        AlertDialog.Builder(context)
            .setMessage(error)
            .show()
    }

    TruvideoScaleButton(
        onPressed = {
            infoVisible = !infoVisible
        }
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(request.id, style = MaterialTheme.typography.bodyMedium)
                        Text(request.filePath, style = MaterialTheme.typography.bodySmall)
                    }

                    Box(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(request.status.name)
                    }

                    Icon(
                        imageVector = if (infoVisible) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }


                Box(
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize { _, _ -> }) {
                    if (infoVisible) {
                        Column {
                            Text("Created At", fontSize = 10.sp, fontWeight = FontWeight.W500)
                            Text("${request.createdAt}", fontSize = 10.sp)

                            Text("Updated At", fontSize = 10.sp, fontWeight = FontWeight.W500)
                            Text("${request.updatedAt}", fontSize = 10.sp)

                            if (request.uploadProgress != null) {
                                Text("Progress", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                Text("${(request.uploadProgress!! * 100)}%", fontSize = 10.sp)
                            }

                            if (request.remoteId != null) {
                                Text("Remote ID", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                Text("${request.remoteId}", fontSize = 10.sp)
                            }

                            if (request.remoteUrl != null) {
                                Text("Remote URL", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                Text("${request.remoteUrl}", fontSize = 10.sp)
                            }

                            if (request.transcriptionUrl != null) {
                                Text("Transcription URL", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                Text("${request.transcriptionUrl}", fontSize = 10.sp)
                            }

                            if (request.tags.entries.isNotEmpty()) {
                                Text("Tags", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                request.tags.entries.forEach {
                                    Text("${it.key} = ${it.value}", fontSize = 10.sp)
                                }
                            }

                            if (request.metadata.entries.isNotEmpty()) {
                                Text("Metadata", fontSize = 10.sp, fontWeight = FontWeight.W500)
                                request.metadata.entries.forEach {
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

                            Box(Modifier.height(16.dp))

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
                                                request.upload(
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
                                                            ex: TruvideoSdkMediaException
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
                                                request.pause()
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
                                                request.resume()
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
                                                request.cancel()
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
                                                request.delete()
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

@Composable
@Preview(showBackground = true)
private fun Test() {
    val request = remember {
        TruvideoSdkMediaFileUploadRequest(
            id = "id",
            filePath = "file-path"
        )
    }

    ListItem(request)
}