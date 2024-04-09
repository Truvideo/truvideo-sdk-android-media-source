package com.truvideo.sdk.media.interfaces

import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import truvideo.sdk.common.exception.TruvideoSdkException

internal interface FileUploadCallback {
    fun onStateChanged(uploadId: Int, state: TruvideoSdkMediaFileUploadStatus, ex: TruvideoSdkException? = null)
    fun onComplete(uploadId: Int, url: String)
    fun onProgressChanged(uploadId: Int, progress: Float)
}
