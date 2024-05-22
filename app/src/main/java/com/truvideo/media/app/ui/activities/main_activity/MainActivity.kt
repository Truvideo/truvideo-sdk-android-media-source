package com.truvideo.media.app.ui.activities.main_activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.media.app.ui.theme.TruvideosdkmediaTheme
import com.truvideo.sdk.media.TruvideoSdkMedia
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileType
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import kotlinx.coroutines.launch
import truvideo.sdk.components.button.TruvideoButton
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TruvideoSdkMedia.init(this)

        setContent {
            TruvideosdkmediaTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Content()
                }
            }
        }
    }

    @Composable
    fun Content() {
        val lifecycleOwner = LocalLifecycleOwner.current
        val scope = rememberCoroutineScope()
        var requests: List<TruvideoSdkMediaFileUploadRequest> by remember { mutableStateOf(listOf()) }

        fun onPickFilePressed(deleteOnComplete: Boolean) {
            scope.launch {
                val path = TruvideoSdkMedia.pickFile(type = TruvideoSdkMediaFileType.VideoAndPicture) ?: return@launch
                val file = File(path)
                if (!file.exists()) return@launch

                val builder = TruvideoSdkMedia.FileUploadRequestBuilder(path)
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

                builder.deleteOnComplete(deleteOnComplete)
                builder.setMetadata(metadata)
                builder.build()
            }
        }

        LaunchedEffect(Unit) {
            TruvideoSdkMedia.streamAllFileUploadRequests().observe(lifecycleOwner) {
                requests = it
            }
        }

        Content(
            requests = requests,
            onPickFilePressed = { onPickFilePressed(it) }
        )
    }

    @Composable
    private fun Content(
        onPickFilePressed: (deleteOnComplete: Boolean) -> Unit = {},
        requests: List<TruvideoSdkMediaFileUploadRequest>
    ) {
        var deleteOnComplete by remember { mutableStateOf(false) }

        Column(Modifier.fillMaxSize()) {
            Box(Modifier.padding(16.dp)) {
                TruvideoButton(
                    text = "Pick file",
                    onPressed = { onPickFilePressed(deleteOnComplete) }
                )
            }

            Row(
                modifier = Modifier.clickable {
                    deleteOnComplete = !deleteOnComplete
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = deleteOnComplete, onCheckedChange = { deleteOnComplete = it })
                Text("Delete on complete", modifier = Modifier.weight(1f))
            }

            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(requests, key = { it.id }) {
                    ListItem(request = it)
                }
            }
        }
    }

    @Composable
    @Preview(showBackground = true)
    private fun Test() {
        Content()
    }
}
