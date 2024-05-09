package com.truvideo.sdk.media.engines

import android.content.Context
import android.net.Uri
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.interfaces.FileUploadCallback
import com.truvideo.sdk.media.interfaces.TruvideoSdkMedia
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import com.truvideo.sdk.media.repository.TruvideoSdkMediaFileUploadRequestRepository
import com.truvideo.sdk.media.service.media.TruvideoSdkMediaServiceImpl
import com.truvideo.sdk.media.usecases.UploadFileUseCase
import com.truvideo.sdk.media.util.FileUriUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.sdk_common
import java.io.File
import java.util.Locale

internal class TruvideoSdkMediaFileUploadEngine(
    private val context: Context,
    private val uploadFileUseCase: UploadFileUseCase,
    private val repository: TruvideoSdkMediaFileUploadRequestRepository,
    private val mediaService: TruvideoSdkMediaServiceImpl
) : TruvideoSdkMediaEngine {
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun upload(id: String, callback: TruvideoSdkMediaFileUploadCallback) {
        val mutex = Mutex()

        // Get credentials
        val credentials = sdk_common.auth.settings.value?.credentials
        if (credentials == null) {
            callback.onError(id, TruvideoSdkMediaException("Invalid credentials"))
            return
        }

        // Get entity
        val entity = repository.getById(id)
        if (entity == null) {
            callback.onError(id, TruvideoSdkMediaException("File upload request not found"))
            return
        }

        // Validate state
        val validStatus = listOf(
            TruvideoSdkMediaFileUploadStatus.IDLE,
            TruvideoSdkMediaFileUploadStatus.ERROR,
            TruvideoSdkMediaFileUploadStatus.CANCELED,
        )
        if (!validStatus.contains(entity.status)) {
            val statusString = validStatus.joinToString(", ") { it.name }
            throw TruvideoSdkException("Invalid state. Must be on: $statusString")
        }

        // Update to uploading
        entity.status = TruvideoSdkMediaFileUploadStatus.UPLOADING
        entity.externalId = null
        entity.errorMessage = null
        entity.progress = 0f
        entity.mediaURL = null
        entity.poolId = credentials.identityPoolID
        entity.region = credentials.region
        entity.bucketName = credentials.bucketName
        entity.folder = credentials.bucketFolderMedia
        repository.update(entity)

        try {
            val s3Id = uploadFileUseCase(
                filePath = entity.filePath,
                bucketName = entity.bucketName,
                region = entity.region,
                poolId = entity.poolId,
                folder = entity.folder,
                callback = object : FileUploadCallback {
                    override fun onStateChanged(uploadId: Int, state: TruvideoSdkMediaFileUploadStatus, ex: TruvideoSdkException?) {
                        scope.launch {
                            mutex.withLock {
                                when (state) {
                                    TruvideoSdkMediaFileUploadStatus.IDLE -> {
                                        repository.updateToIdle(id)
                                    }

                                    TruvideoSdkMediaFileUploadStatus.UPLOADING -> {
                                        repository.updateToUploading(id)
                                    }

                                    TruvideoSdkMediaFileUploadStatus.SYNCHRONIZING -> {
                                        repository.updateToSynchronizing(id)
                                    }

                                    TruvideoSdkMediaFileUploadStatus.ERROR -> {
                                        val errorMessage = ex?.localizedMessage ?: "Unknown error"
                                        repository.updateToError(id, errorMessage)
                                    }

                                    TruvideoSdkMediaFileUploadStatus.COMPLETED -> {

                                    }

                                    TruvideoSdkMediaFileUploadStatus.PAUSED -> {
                                        repository.updateToPaused(id)
                                    }

                                    TruvideoSdkMediaFileUploadStatus.CANCELED -> {
                                        repository.updateToCanceled(id)
                                    }
                                }
                            }
                        }
                    }


                    override fun onComplete(uploadId: Int, url: String) {
                        scope.launch {
                            mutex.withLock {
                                try {
                                    // Move to synchronizing
                                    repository.updateToSynchronizing(id)

                                    // Create truvideo entity
                                    val file = File(entity.filePath)
                                    val mimeType = FileUriUtil.getMimeType(context, Uri.fromFile(file))
                                    val type = mimeType.split("/")[0].uppercase(Locale.ROOT)
                                    val finalUrl = mediaService.createMedia(
                                        title = file.name,
                                        url = url,
                                        size = file.length(),
                                        type = type
                                    )

                                    // Move to completed
                                    repository.updateToCompleted(id, finalUrl)
                                    callback.onComplete(entity.id, finalUrl)
                                } catch (exception: Exception) {
                                    exception.printStackTrace()

                                    val message = if (exception is TruvideoSdkException) exception.message else "Unknown error"
                                    val externalException = TruvideoSdkMediaException(message)

                                    // Move to error
                                    repository.updateToError(id, message)
                                    callback.onError(entity.id, externalException)
                                }
                            }
                        }
                    }

                    override fun onProgressChanged(uploadId: Int, progress: Float) {
                        scope.launch {
                            mutex.withLock {
                                repository.updateProgress(id, progress)
                                callback.onProgressChanged(entity.id, progress)
                            }
                        }
                    }
                }
            )

            // Update external id
            repository.update(entity.apply { externalId = s3Id })
        } catch (exception: Exception) {
            exception.printStackTrace()

            val message = if (exception is TruvideoSdkException) exception.message else "Unknown error"
            val externalException = TruvideoSdkMediaException(message)

            // Move to error
            repository.updateToError(id, message)
            callback.onError(entity.id, externalException)
        }
    }

    suspend fun cancel(id: String) {
        val entity = repository.getById(id) ?: throw TruvideoSdkException("File upload request not found")

        // Validate status
        val validStatus = listOf(
            TruvideoSdkMediaFileUploadStatus.IDLE,
            TruvideoSdkMediaFileUploadStatus.PAUSED,
            TruvideoSdkMediaFileUploadStatus.UPLOADING,
            TruvideoSdkMediaFileUploadStatus.ERROR
        )

        if (!validStatus.contains(entity.status)) {
            val statusString = validStatus.joinToString(", ") { it.name }
            throw TruvideoSdkException("Invalid state. Must be on: $statusString")
        }

        // Cancel s3
        val externalId = entity.externalId
        if (externalId != null) {
            uploadFileUseCase.cancel(
                region = entity.region,
                poolId = entity.poolId,
                id = externalId
            )
        }

        // Update to cancel
        entity.externalId = null
        entity.mediaURL = null
        entity.progress = null
        entity.errorMessage = null
        entity.status = TruvideoSdkMediaFileUploadStatus.CANCELED
        repository.update(entity)
    }

    suspend fun delete(id: String) {
        val entity = repository.getById(id)

        // IF exists, cancel upload from s3
        if (entity != null) {
            val externalId = entity.externalId
            if (externalId != null) {
                uploadFileUseCase.cancel(
                    region = entity.region,
                    poolId = entity.poolId,
                    id = externalId
                )
            }
        }

        // Delete entity
        repository.delete(id)
    }

    suspend fun pause(id: String) {
        val entity = repository.getById(id) ?: throw TruvideoSdkException("File upload request not found")

        // Validate status
        val validStatus = listOf(TruvideoSdkMediaFileUploadStatus.UPLOADING)
        if (!validStatus.contains(entity.status)) {
            val statusString = validStatus.joinToString(", ") { it.name }
            throw TruvideoSdkException("Invalid state. Must be on: $statusString")
        }

        // Pause s3
        val externalId = entity.externalId ?: throw TruvideoSdkException("Upload request not found")
        uploadFileUseCase.pause(
            region = entity.region,
            poolId = entity.poolId,
            id = externalId
        )

        // Update to paused
        entity.status = TruvideoSdkMediaFileUploadStatus.PAUSED
        repository.update(entity)
    }

    suspend fun resume(id: String) {
        val entity = repository.getById(id) ?: throw TruvideoSdkException("File upload request not found")

        // Valid status
        val validStatus = listOf(TruvideoSdkMediaFileUploadStatus.PAUSED)
        if (!validStatus.contains(entity.status)) {
            val statusString = validStatus.joinToString(", ") { it.name }
            throw TruvideoSdkException("Invalid state. Must be on $statusString")
        }

        // Resume s3
        val externalId = entity.externalId ?: throw TruvideoSdkException("Upload request not found")
        uploadFileUseCase.resume(
            region = entity.region,
            poolId = entity.poolId,
            id = externalId
        )

        // Update to uploading
        entity.status = TruvideoSdkMediaFileUploadStatus.UPLOADING
        repository.update(entity)
    }
}