package com.truvideo.sdk.media.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.data.DatabaseInstance
import com.truvideo.sdk.media.data.FileUploadRequestDAO
import com.truvideo.sdk.media.data.converters.MetadataConverter
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.model.TruVideoSdkMediaFileUploadResponse
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.coroutines.suspendCoroutine

internal class TruvideoSdkMediaFileUploadRequestRepositoryImpl(
    private val context: Context
) : TruvideoSdkMediaFileUploadRequestRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val dao: FileUploadRequestDAO
        get() = DatabaseInstance.getDatabase(context).mediaDao()

    override suspend fun insert(media: TruvideoSdkMediaFileUploadRequest): TruvideoSdkMediaFileUploadRequest {
        return suspendCoroutine { cont ->
            scope.launch {
                try {
                    dao.insert(media)
                    cont.resumeWith(Result.success(media))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    cont.resumeWith(Result.failure(exception))
                }
            }
        }
    }

    override suspend fun update(media: TruvideoSdkMediaFileUploadRequest): TruvideoSdkMediaFileUploadRequest {
        return suspendCoroutine { cont ->
            scope.launch {
                try {
                    media.updatedAt = Date()
                    dao.update(media)
                    cont.resumeWith(Result.success(media))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    cont.resumeWith(Result.failure(exception))
                }
            }
        }
    }

    override suspend fun updateToIdle(id: String): TruvideoSdkMediaFileUploadRequest {
        val model = getById(id) ?: throw TruvideoSdkMediaException("Media not found")

        model.remoteId = null
        model.remoteUrl = null
        model.transcriptionUrl = null
        model.uploadProgress = null
        model.errorMessage = null
        model.status = TruvideoSdkMediaFileUploadStatus.IDLE
        return update(model)
    }

    override suspend fun updateToUploading(
        id: String,
        poolId: String,
        region: String,
        bucketName: String,
        folder: String
    ): TruvideoSdkMediaFileUploadRequest {
        val model = getById(id) ?: throw TruvideoSdkMediaException("Media not found")

        model.remoteId = null
        model.uploadProgress = null
        model.errorMessage = null
        model.remoteUrl = null
        model.transcriptionUrl = null
        model.poolId = poolId
        model.region = region
        model.bucketName = bucketName
        model.folder = folder
        model.status = TruvideoSdkMediaFileUploadStatus.UPLOADING
        return update(model)
    }

    override suspend fun updateToSynchronizing(id: String) {
        val model = getById(id) ?: return

        model.remoteId = null
        model.remoteUrl = null
        model.transcriptionUrl = null
        model.uploadProgress = null
        model.errorMessage = null
        model.status = TruvideoSdkMediaFileUploadStatus.SYNCHRONIZING
        update(model)
    }

    override suspend fun updateToError(id: String, errorMessage: String) {
        val model = getById(id) ?: return

        model.uploadProgress = null
        model.errorMessage = errorMessage
        model.remoteUrl = null
        model.transcriptionUrl = null
        model.status = TruvideoSdkMediaFileUploadStatus.ERROR
        update(model)
    }

    override suspend fun updateToPaused(id: String) {
        val model = getById(id) ?: return

        model.errorMessage = null
        model.remoteId = null
        model.remoteUrl = null
        model.transcriptionUrl = null
        model.status = TruvideoSdkMediaFileUploadStatus.PAUSED
        update(model)
    }

    override suspend fun updateToCanceled(id: String) {
        val model = getById(id) ?: return

        model.uploadProgress = null
        model.errorMessage = null
        model.remoteUrl = null
        model.transcriptionUrl = null
        model.status = TruvideoSdkMediaFileUploadStatus.CANCELED
        update(model)
    }

    override suspend fun updateProgress(id: String, progress: Float) {
        val model = getById(id) ?: return

        model.uploadProgress = progress
        update(model)
    }

    override suspend fun updateToCompleted(id: String, media: TruVideoSdkMediaFileUploadResponse) {
        val model = getById(id) ?: return

        model.uploadProgress = 1.0f
        model.errorMessage = null
        model.remoteId = media.id
        model.remoteUrl = media.url
        model.tags = media.tags
        model.metadata = MetadataConverter().toMap(media.metadata) ?: mapOf()
        model.status = TruvideoSdkMediaFileUploadStatus.COMPLETED
        model.transcriptionUrl = media.transcriptionUrl
        model.transcriptionLength = media.transcriptionLength
        update(model)
    }

    override suspend fun delete(id: String) {
        suspendCoroutine { cont ->
            scope.launch {
                try {
                    val model = getById(id)
                    if (model != null) {
                        dao.delete(model)
                    }
                    cont.resumeWith(Result.success(Unit))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    cont.resumeWith(Result.failure(exception))
                }
            }
        }
    }

    override suspend fun cancelAllProcessing() {
        val items = mutableListOf<TruvideoSdkMediaFileUploadRequest>()
        items.addAll(getAll(TruvideoSdkMediaFileUploadStatus.UPLOADING))
        items.addAll(getAll(TruvideoSdkMediaFileUploadStatus.SYNCHRONIZING))
        items.addAll(getAll(TruvideoSdkMediaFileUploadStatus.PAUSED))

        items.forEach {
            it.status = TruvideoSdkMediaFileUploadStatus.CANCELED
            it.externalId = null
            it.uploadProgress = null
            it.errorMessage = null
            it.remoteId = null
            it.remoteUrl = null
            it.transcriptionUrl = null
            it.transcriptionLength = null
            update(it)
        }
    }

    override suspend fun getById(id: String): TruvideoSdkMediaFileUploadRequest? {
        return suspendCoroutine { cont ->
            scope.launch {
                try {
                    val model = dao.getById(id)
                    cont.resumeWith(Result.success(model))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    cont.resumeWith(Result.failure(exception))
                }
            }
        }
    }

    override suspend fun getAll(status: TruvideoSdkMediaFileUploadStatus?): List<TruvideoSdkMediaFileUploadRequest> {
        return suspendCoroutine { cont ->
            scope.launch {
                try {
                    val data = if (status != null) dao.getAllByStatus(status) else dao.getAll()
                    cont.resumeWith(Result.success(data))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    cont.resumeWith(Result.failure(exception))
                }
            }
        }
    }


    override suspend fun streamById(id: String): LiveData<TruvideoSdkMediaFileUploadRequest?> {
        return suspendCoroutine { cont ->
            scope.launch {
                try {
                    val stream = dao.streamById(id)
                    cont.resumeWith(Result.success(stream))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    cont.resumeWith(Result.failure(exception))
                }
            }
        }
    }

    override suspend fun streamAll(status: TruvideoSdkMediaFileUploadStatus?): LiveData<List<TruvideoSdkMediaFileUploadRequest>> {
        return suspendCoroutine { cont ->
            scope.launch {
                try {
                    val stream = if (status != null) dao.streamAllByStatus(status) else dao.streamAll()
                    cont.resumeWith(Result.success(stream))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    cont.resumeWith(Result.failure(exception))
                }
            }
        }
    }
}