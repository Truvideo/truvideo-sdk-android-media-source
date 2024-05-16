package com.truvideo.sdk.media.builder

import com.truvideo.sdk.media.engines.TruvideoSdkMediaEngine
import com.truvideo.sdk.media.engines.TruvideoSdkMediaFileUploadEngine
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.repository.TruvideoSdkMediaFileUploadRequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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


    fun setMetadata(map: Map<String, Any?>) {

    }

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun build(): TruvideoSdkMediaFileUploadRequest {
        if (engine !is TruvideoSdkMediaFileUploadEngine) throw TruvideoSdkMediaException("Invalid engine")

        val media = TruvideoSdkMediaFileUploadRequest(
            id = UUID.randomUUID().toString(),
            filePath = filePath,
            tags = tags
        )
        media.engine = engine

        mediaRepository.insert(media)
        return media
    }

    fun build(callback: TruvideoSdkMediaCallback<TruvideoSdkMediaFileUploadRequest>) {
        scope.launch {
            val request = build()
            callback.onComplete(request)
        }
    }
}