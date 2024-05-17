package com.truvideo.sdk.media.service.media

import com.truvideo.sdk.media.model.TruVideoSdkMediaFileUploadResponse

internal interface TruvideoSdkMediaService {
    suspend fun createMedia(
        title: String,
        url: String,
        size: Long,
        type: String,
        tags: Map<String, String>,
        metadata: Map<String, Any?>
    ): TruVideoSdkMediaFileUploadResponse
}