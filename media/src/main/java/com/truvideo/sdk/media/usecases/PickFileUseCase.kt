package com.truvideo.sdk.media.usecases

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileType
import com.truvideo.sdk.media.util.FileUriUtil
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

internal class PickFileUseCase {

    private var activity: ComponentActivity? = null
    private var startForResult: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var continuation: CancellableContinuation<String?>? = null

    fun init(activity: ComponentActivity) {
        this.activity = activity
        this.startForResult = activity.registerForActivityResult(PickVisualMedia()) { uri ->
            Log.d("TruvideoSdkMedia", "File $uri")

            if (uri != null) {
                val path = FileUriUtil.realPathFromUri(activity, uri)
                continuation?.resumeWith(Result.success(path))
            } else {
                continuation?.resumeWith(Result.success(null))
            }
        }
    }

    suspend fun pick(type: TruvideoSdkMediaFileType): String? {
        val startForResult = this.startForResult ?: throw TruvideoSdkMediaException("Not initialized")

        val mediaType = when (type) {
            TruvideoSdkMediaFileType.Video -> PickVisualMedia.VideoOnly
            TruvideoSdkMediaFileType.Picture -> PickVisualMedia.ImageOnly
            TruvideoSdkMediaFileType.VideoAndPicture -> PickVisualMedia.ImageAndVideo
        }

        startForResult.launch(PickVisualMediaRequest(mediaType = mediaType))
        return suspendCancellableCoroutine { cont -> continuation = cont }
    }
}