package com.truvideo.sdk.media.service.media

import com.truvideo.sdk.media.model.TruVideoSdkMediaFileUploadResponse
import com.truvideo.sdk.media.model.TruvideoSdkMediaResponse
import com.truvideo.sdk.media.model.TruvideoSdkPaginatedResponse

internal interface TruvideoSdkMediaService {
    suspend fun createMedia(
        title: String,
        url: String,
        size: Long,
        type: String,
        tags: Map<String, String>,
        metadata: Map<String, Any?>
    ): TruVideoSdkMediaFileUploadResponse

    suspend fun fetchAll(
        tags: Map<String, String>?, idList: List<String>?, type: String?
    ): TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse>
}