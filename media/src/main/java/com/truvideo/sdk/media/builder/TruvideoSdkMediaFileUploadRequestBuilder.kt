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
    suspend fun build(): TruvideoSdkMediaFileUploadRequest {
        val media = TruvideoSdkMediaFileUploadRequest(
            id = UUID.randomUUID().toString(),
            filePath = filePath,
        )
        media.engine = engine

        mediaRepository.insert(media)
        return media
    }
}