package com.truvideo.sdk.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.truvideo.sdk.media.builder.TruvideoSdkMediaFileUploadRequestBuilder
import com.truvideo.sdk.media.engines.TruvideoSdkMediaFileUploadEngine
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.interfaces.TruvideoSdkMedia
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaAuthAdapter
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import com.truvideo.sdk.media.model.TruvideoSdkMediaResponse
import com.truvideo.sdk.media.model.TruvideoSdkPaginatedResponse
import com.truvideo.sdk.media.repository.TruvideoSdkMediaFileUploadRequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class TruvideoSdkMediaImpl(
    private val authAdapter: TruvideoSdkMediaAuthAdapter,
    private val mediaFileUploadRequestRepository: TruvideoSdkMediaFileUploadRequestRepository,
    private val fileUploadEngine: TruvideoSdkMediaFileUploadEngine
) : TruvideoSdkMedia {

    private var scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch { mediaFileUploadRequestRepository.cancelAllProcessing() }
    }

    override fun FileUploadRequestBuilder(filePath: String): TruvideoSdkMediaFileUploadRequestBuilder {
        authAdapter.validateAuthentication()

        return TruvideoSdkMediaFileUploadRequestBuilder(
            filePath = filePath,
            mediaRepository = mediaFileUploadRequestRepository,
            engine = fileUploadEngine
        )
    }

    override fun streamAllFileUploadRequests(
        status: TruvideoSdkMediaFileUploadStatus?,
        callback: TruvideoSdkMediaCallback<LiveData<List<TruvideoSdkMediaFileUploadRequest>>>
    ) {
        scope.launch {
            try {
                val data = streamAllFileUploadRequests(status)
                callback.onComplete(data)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(TruvideoSdkMediaException(exception.message ?: "Unknown message"))
            }
        }
    }

    override suspend fun streamAllFileUploadRequests(
        status: TruvideoSdkMediaFileUploadStatus?
    ): LiveData<List<TruvideoSdkMediaFileUploadRequest>> {
        authAdapter.validateAuthentication()

        val stream = mediaFileUploadRequestRepository.streamAll(status)
        return stream.map { list ->
            list.onEach { item ->
                item.engine = fileUploadEngine
            }
        }
    }

    override suspend fun getFileUploadRequestById(id: String): TruvideoSdkMediaFileUploadRequest? {
        authAdapter.validateAuthentication()

        val request = mediaFileUploadRequestRepository.getById(id) ?: return null
        request.engine = fileUploadEngine
        return request
    }

    override fun getFileUploadRequestById(
        id: String, callback: TruvideoSdkMediaCallback<TruvideoSdkMediaFileUploadRequest?>
    ) {
        scope.launch {
            try {
                val request = getFileUploadRequestById(id)
                callback.onComplete(request)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(TruvideoSdkMediaException(exception.message ?: "Unknown message"))
            }
        }
    }

    override fun streamFileUploadRequestById(
        id: String, callback: TruvideoSdkMediaCallback<LiveData<TruvideoSdkMediaFileUploadRequest?>>
    ) {
        scope.launch {
            try {
                val data = streamFileUploadRequestById(id)
                callback.onComplete(data)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(TruvideoSdkMediaException(exception.message ?: "Unknown message"))
            }
        }
    }

    override suspend fun streamFileUploadRequestById(id: String): LiveData<TruvideoSdkMediaFileUploadRequest?> {
        authAdapter.validateAuthentication()

        val stream = mediaFileUploadRequestRepository.streamById(id)
        return stream.map {
            it?.apply {
                engine = fileUploadEngine
            }
        }

    }

    override fun getAllFileUploadRequests(
        status: TruvideoSdkMediaFileUploadStatus?,
        callback: TruvideoSdkMediaCallback<List<TruvideoSdkMediaFileUploadRequest>>
    ) {
        scope.launch {
            try {
                val data = getAllFileUploadRequests(status)
                callback.onComplete(data)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(TruvideoSdkMediaException(exception.message ?: "Unknown message"))
            }
        }
    }

    override suspend fun getAllFileUploadRequests(status: TruvideoSdkMediaFileUploadStatus?): List<TruvideoSdkMediaFileUploadRequest> {
        authAdapter.validateAuthentication()

        val items = mediaFileUploadRequestRepository.getAll(status)
        items.forEach { it.engine = fileUploadEngine }
        return items
    }

    override fun getById(
        id: String, callback: TruvideoSdkMediaCallback<TruvideoSdkMediaResponse?>
    ) {
        val internalCallback = object :
            TruvideoSdkMediaCallback<TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse>> {
            override fun onComplete(data: TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse>) {
                callback.onComplete(data.data.firstOrNull())
            }

            override fun onError(exception: TruvideoSdkMediaException) {
                callback.onError(exception)
            }

        }
        fetchAllMedia(null, listOf(id), null, null, null, internalCallback)
    }

    override suspend fun getById(id: String): TruvideoSdkMediaResponse? {
        return fetchAllMedia(null, listOf(id), null, null, null).data.firstOrNull()
    }

    override fun search(
        tags: Map<String, String>?,
        type: String?,
        pageNumber: Int?,
        size: Int?,
        callback: TruvideoSdkMediaCallback<TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse>>
    ) {
        fetchAllMedia(tags, null, type, pageNumber, size, callback)
    }

    override suspend fun search(
        tags: Map<String, String>?, type: String?, pageNumber: Int?,
        size: Int?,
    ): TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse> {
        return fetchAllMedia(tags, null, type, pageNumber, size)
    }

    private fun fetchAllMedia(
        tags: Map<String, String>?,
        idList: List<String>?,
        type: String?,
        pageNumber: Int?,
        size: Int?,
        callback: TruvideoSdkMediaCallback<TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse>>
    ) {
        scope.launch {
            try {
                val data = fetchAllMedia(tags, idList, type, pageNumber, size)
                callback.onComplete(data)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(TruvideoSdkMediaException(exception.message ?: "Unknown message"))
            }
        }
    }

    private suspend fun fetchAllMedia(
        tags: Map<String, String>?,
        idList: List<String>?,
        type: String?,
        pageNumber: Int?,
        size: Int?
    ): TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse> {
        authAdapter.validateAuthentication()

        return mediaFileUploadRequestRepository.fetchAll(tags, idList, type, pageNumber, size)
    }

    override val environment: String
        get() = BuildConfig.FLAVOR
}
