package com.truvideo.sdk.media.interfaces

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.model.MediaEntity
import com.truvideo.sdk.media.model.MediaEntityStatus

/**
 * Interface for handling media file operations in Truvideo SDK.
 */
interface TruvideoSdkMedia {

    /**
     * Initiates the upload of a file to a remote server.
     *
     * This method begins the process of uploading a file to a remote server. It generates
     * a unique key for the upload operation and checks the authentication status before proceeding.
     *
     * @param context The Android application context.
     * @param file The URI of the file to be uploaded.
     * @param callback The listener to handle transfer progress and errors.
     * @return A unique key associated with the upload process, which can be used for tracking and error handling.
     */
    fun upload(
        context: Context, file: Uri, callback: TruvideoSdkUploadCallback
    ): String

    /**
     * Initiates the upload of a file to a remote server.
     *
     * <p>This method begins the process of uploading a file to a remote server. It generates
     * a unique key for the upload operation and checks the authentication status before proceeding.</p>
     *
     * @param context The Android application context.
     * @param mediaLocalKey The unique key associated with the upload process.
     * @param callback The listener to handle transfer progress and errors.
     * @return A unique key associated with the upload process, which can be used for tracking and error handling.
     *
     * @see TruvideoSdkUploadCallback
     */
    fun resume(
        context: Context, mediaLocalKey: String, callback: TruvideoSdkUploadCallback
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

    /**
     * Cancels the ongoing file transfer associated with the provided key.
     *
     * This method cancels the transfer of a file associated with a given key, provided that the user is authenticated
     * and the necessary credentials are available. The cancellation is handled through the specified callback.
     *
     * @param context The Android application context.
     * @param id The id associated with the file transfer operation to be canceled.
     * @param callback The listener to handle cancellation results.
     */
    fun cancel(context: Context, id: String, callback: TruvideoSdkCancelCallback)

    /**
     * Pauses the ongoing file transfer associated with the provided key.
     *
     * This method pauses the transfer of a file associated with a given key, provided that the user is authenticated
     * and the necessary credentials are available.
     *
     * @param context The Android application context.
     * @param id The id associated with the file transfer operation to be paused.
     */
    suspend fun pause(context: Context, id: String)

    /**
     * Pauses the ongoing file transfer associated with the provided key.
     *
     * This method pauses the transfer of a file associated with a given key, provided that the user is authenticated
     * and the necessary credentials are available. The pause is handled through the specified callback.
     *
     * @param context The Android application context.
     * @param id The id associated with the file transfer operation to be paused.
     * @param callback The listener to handle pause results.
     */
    fun pause(context: Context, id: String, callback: TruvideoSdkPauseCallback)

    /**
     * Streams a list of all upload requests.
     *
     * This method streams a list of all upload requests to the specified callback.
     *
     * @param context The Android application context.
     * @param callback The listener to handle the streamed list of upload requests.
     */
    fun streamAllUploadRequests(
        context: Context, callback: TruvideoSdkGenericCallback<LiveData<List<MediaEntity>>>
    )

    /**
     * Streams a list of upload requests filtered by status.
     *
     * This method streams a list of upload requests with the specified status to the specified callback.
     *
     * @param context The Android application context.
     * @param status The status by which to filter the upload requests.
     * @param callback The listener to handle the streamed list of upload requests.
     */
    fun streamAllUploadRequestsByStatus(
        context: Context,
        status: MediaEntityStatus,
        callback: TruvideoSdkGenericCallback<LiveData<List<MediaEntity>>>
    )

    /**
     * Streams media information for a specific media ID.
     *
     * This method streams information about a media file with the specified ID to the provided callback.
     *
     * @param context The Android application context.
     * @param id The ID of the media file.
     * @param callback The listener to handle the streamed media information.
     */
    fun streamMediaById(
        context: Context, id: String, callback: TruvideoSdkGenericCallback<LiveData<MediaEntity>>
    )

    /**
     * Retrieves a list of all upload requests.
     *
     * This method retrieves a list of all upload requests for further processing.
     *
     * @param context The Android application context.
     * @param callback The listener to handle the retrieved list of upload requests.
     */
    fun getAllUploadRequests(
        context: Context, callback: TruvideoSdkGenericCallback<List<MediaEntity>>
    )

    /**
     * Retrieves a list of upload requests filtered by status.
     *
     * This method retrieves a list of upload requests with the specified status for further processing.
     *
     * @param context The Android application context.
     * @param status The status by which to filter the upload requests.
     * @param callback The listener to handle the retrieved list of upload requests.
     */
    fun getAllUploadRequestsByStatus(
        context: Context,
        status: MediaEntityStatus,
        callback: TruvideoSdkGenericCallback<List<MediaEntity>>
    )
}