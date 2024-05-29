@file:Suppress("FunctionName")

package com.truvideo.sdk.media.interfaces

import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.builder.TruvideoSdkMediaFileUploadRequestBuilder
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import com.truvideo.sdk.media.model.TruvideoSdkMediaResponse
import com.truvideo.sdk.media.model.TruvideoSdkPaginatedResponse

interface TruvideoSdkMedia {

    fun FileUploadRequestBuilder(filePath: String): TruvideoSdkMediaFileUploadRequestBuilder

    fun streamAllFileUploadRequests(
        status: TruvideoSdkMediaFileUploadStatus? = null,
        callback: TruvideoSdkMediaCallback<LiveData<List<TruvideoSdkMediaFileUploadRequest>>>
    )

    suspend fun streamAllFileUploadRequests(
        status: TruvideoSdkMediaFileUploadStatus? = null
    ): LiveData<List<TruvideoSdkMediaFileUploadRequest>>

    suspend fun getFileUploadRequestById(id: String): TruvideoSdkMediaFileUploadRequest?

    fun getFileUploadRequestById(
        id: String, callback: TruvideoSdkMediaCallback<TruvideoSdkMediaFileUploadRequest?>
    )

    fun streamFileUploadRequestById(
        id: String, callback: TruvideoSdkMediaCallback<LiveData<TruvideoSdkMediaFileUploadRequest?>>
    )

    suspend fun streamFileUploadRequestById(id: String): LiveData<TruvideoSdkMediaFileUploadRequest?>

    fun getAllFileUploadRequests(
        status: TruvideoSdkMediaFileUploadStatus? = null,
        callback: TruvideoSdkMediaCallback<List<TruvideoSdkMediaFileUploadRequest>>
    )

    suspend fun getAllFileUploadRequests(
        status: TruvideoSdkMediaFileUploadStatus? = null,
    ): List<TruvideoSdkMediaFileUploadRequest>

    fun getById(id: String, callback: TruvideoSdkMediaCallback<TruvideoSdkMediaResponse?>)

    suspend fun getById(id: String): TruvideoSdkMediaResponse?

    fun search(
        tags: Map<String, String>? = null,
        type: String? = null,
        pageNumber: Int?,
        size: Int?,
        callback: TruvideoSdkMediaCallback<TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse>>
    )

    suspend fun search(
        tags: Map<String, String>? = null,
        type: String? = null,
        pageNumber: Int?,
        size: Int?,
    ): TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse>

    val environment: String
}