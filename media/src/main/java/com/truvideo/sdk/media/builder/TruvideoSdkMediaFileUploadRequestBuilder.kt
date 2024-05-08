package com.truvideo.sdk.media.builder

import com.truvideo.sdk.media.engines.TruvideoSdkMediaEngine
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.repository.TruvideoSdkMediaFileUploadRequestRepository
import java.util.UUID

class TruvideoSdkMediaFileUploadRequestBuilder(
    var filePath: String,
    private val mediaRepository: TruvideoSdkMediaFileUploadRequestRepository,
    private val engine: TruvideoSdkMediaEngine
) {
    private val tags = mutableMapOf<String, String>()
    fun addTag(key: String, value: String) {
        tags[key] = value
    }
    suspend fun build(): TruvideoSdkMediaFileUploadRequest {
        val media = TruvideoSdkMediaFileUploadRequest(
            id = UUID.randomUUID().toString(),
            filePath = filePath,
            tags = tags
        )
        media.engine = engine

        mediaRepository.insert(media)
        return media
    }
}