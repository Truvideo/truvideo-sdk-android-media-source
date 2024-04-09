package com.truvideo.sdk.media.service.media

internal interface TruvideoSdkMediaService {
    suspend fun createMedia(
        title: String,
        url: String,
        size: Long,
        type: String
    ): String
}