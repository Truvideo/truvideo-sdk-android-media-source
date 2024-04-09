package com.truvideo.sdk.media.repository

import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus

interface TruvideoSdkMediaFileUploadRequestRepository {

    suspend fun insert(media: TruvideoSdkMediaFileUploadRequest)

    suspend fun update(media: TruvideoSdkMediaFileUploadRequest)

    suspend fun updateToIdle(id: String)

    suspend fun updateToUploading(id: String)

    suspend fun updateToSynchronizing(id: String)

    suspend fun updateToError(id: String, errorMessage: String)

    suspend fun updateToPaused(id: String)

    suspend fun updateToCanceled(id: String)

    suspend fun updateProgress(id: String, progress: Float)

    suspend fun updateToCompleted(id: String, url: String)

    suspend fun getById(id: String): TruvideoSdkMediaFileUploadRequest?

    suspend fun getAll(status: TruvideoSdkMediaFileUploadStatus? = null): List<TruvideoSdkMediaFileUploadRequest>

    suspend fun streamById(id: String): LiveData<TruvideoSdkMediaFileUploadRequest?>

    suspend fun streamAll(status: TruvideoSdkMediaFileUploadStatus? = null): LiveData<List<TruvideoSdkMediaFileUploadRequest>>

    suspend fun delete(id: String)

    suspend fun cancelAllProcessing()
}