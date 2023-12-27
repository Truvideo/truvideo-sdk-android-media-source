package com.truvideo.sdk.media.service.upload

import android.content.Context
import android.net.Uri
import com.truvideo.sdk.media.interfaces.TruvideoSdkUploadCallback

internal interface TruvideoSdkUploadServiceInterface {

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

    suspend fun getState(
        context: Context,
        region: String,
        poolId: String,
        id: String
    ): String
}