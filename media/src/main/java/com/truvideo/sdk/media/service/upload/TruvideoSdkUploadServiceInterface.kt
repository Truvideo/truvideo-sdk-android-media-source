package com.truvideo.sdk.media.service.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.interfaces.TruvideoSdkUploadCallback
import com.truvideo.sdk.media.model.MediaEntity
import com.truvideo.sdk.media.model.MediaEntityStatus

internal interface TruvideoSdkUploadServiceInterface {

    suspend fun init(context: Context, region: String, poolId: String)

    suspend fun upload(
        context: Context,
        bucketName: String,
        region: String,
        poolId: String,
        folder: String,
        id: String,
        file: Uri,
        callback: TruvideoSdkUploadCallback
    )

    suspend fun cancel(
        context: Context,
        id: String,
        region: String,
        poolId: String,
    )

    suspend fun cancel(context: Context, id: String, s3Id: Int, region: String, poolId: String)

    suspend fun streamAllUploadRequests(context: Context): LiveData<List<MediaEntity>>
    suspend fun streamAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): LiveData<List<MediaEntity>>

    suspend fun pause(context: Context, id: String, region: String, poolId: String)

    suspend fun streamMediaById(context: Context, id: String): LiveData<MediaEntity>
    suspend fun getAllUploadRequests(context: Context): List<MediaEntity>
    suspend fun getAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): List<MediaEntity>

    suspend fun retry(
        context: Context,
        bucketName: String,
        region: String,
        poolId: String,
        folder: String,
        id: String,
        callback: TruvideoSdkUploadCallback
    )

    suspend fun resume(
        context: Context,
        bucketName: String,
        region: String,
        poolId: String,
        folder: String,
        id: String,
        callback: TruvideoSdkUploadCallback
    )

    suspend fun delete(context: Context, id: String, region: String, poolId: String)
}