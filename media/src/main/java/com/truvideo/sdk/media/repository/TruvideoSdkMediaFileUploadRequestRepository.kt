package com.truvideo.sdk.media.repository

import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.model.TruVideoSdkMediaFileUploadResponse
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import com.truvideo.sdk.media.model.TruvideoSdkMediaResponse
import com.truvideo.sdk.media.model.TruvideoSdkMediaPaginatedResponse

interface TruvideoSdkMediaFileUploadRequestRepository {

    suspend fun insert(media: TruvideoSdkMediaFileUploadRequest): TruvideoSdkMediaFileUploadRequest

    suspend fun update(media: TruvideoSdkMediaFileUploadRequest): TruvideoSdkMediaFileUploadRequest

    suspend fun updateToIdle(id: String): TruvideoSdkMediaFileUploadRequest

    suspend fun updateToUploading(
        id: String, poolId: String, region: String, bucketName: String, folder: String
    ): TruvideoSdkMediaFileUploadRequest

    suspend fun updateToSynchronizing(id: String)

    suspend fun updateToError(id: String, errorMessage: String)

    suspend fun updateToPaused(id: String)

    suspend fun updateToCanceled(id: String)

    suspend fun updateProgress(id: String, progress: Float)

    suspend fun updateToCompleted(id: String, media: TruVideoSdkMediaFileUploadResponse)

    suspend fun getById(id: String): TruvideoSdkMediaFileUploadRequest?

    suspend fun getAll(status: TruvideoSdkMediaFileUploadStatus? = null): List<TruvideoSdkMediaFileUploadRequest>

    suspend fun fetchAll(
        tags: Map<String, String>?,
        idList: List<String>?,
        type: String?,
        pageNumber: Int?,
        size: Int?
    ): TruvideoSdkMediaPaginatedResponse<TruvideoSdkMediaResponse>

    suspend fun streamById(id: String): LiveData<TruvideoSdkMediaFileUploadRequest?>

    suspend fun streamAll(status: TruvideoSdkMediaFileUploadStatus? = null): LiveData<List<TruvideoSdkMediaFileUploadRequest>>

    suspend fun delete(id: String)

    suspend fun cancelAllProcessing()
}