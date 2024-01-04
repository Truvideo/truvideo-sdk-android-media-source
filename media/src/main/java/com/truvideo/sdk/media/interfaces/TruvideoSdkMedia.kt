package com.truvideo.sdk.media.interfaces

import android.content.Context
import android.net.Uri
import com.truvideo.sdk.media.model.MediaEntityStatus

interface TruvideoSdkMedia {

    /**
     * Initiates the upload of a file to a remote server.
     *
     * This method begins the process of uploading a file to a remote server. It generates
     * a unique key for the upload operation and checks the authentication status before proceeding.
     *
     * @param context The Android application context.
     * @param callback The listener to handle transfer progress and errors.
     * @param file The URI of the file to be uploaded.
     * @return A unique key associated with the upload process, which can be used for tracking and error handling.
     */
    fun upload(
        context: Context,
        file: Uri,
        callback: TruvideoSdkUploadCallback
    ): String


    /**
     * Cancels the ongoing file transfer associated with the provided key.
     *
     * This method cancels the transfer of a file associated with a given key, provided that the user is authenticated
     * and the necessary credentials are available.
     *
     * @param context The Android application context.
     * @param id The id associated with the file transfer operation to be canceled.
     */
    suspend fun cancel(context: Context, id: String)


    fun cancel(context: Context, id: String, callback: TruvideoSdkCancelCallback)

    fun getAllUploadRequests(context: Context, callback: TruvideoSdkGetCallback)
    fun getAllUploadRequestsByStatus(
        context: Context,
        status: MediaEntityStatus,
        callback: TruvideoSdkGetCallback
    )
}