package com.truvideo.sdk.media.interfaces

import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import truvideo.sdk.common.exception.TruvideoSdkException


/**
 * Interface for receiving transfer-related events during file upload.
 */
interface TruvideoSdkMediaFileUploadCallback {

    /**
     * Called when a file transfer operation is completed successfully.
     *
     * @param id The unique key associated with the completed transfer operation.
     * @param response The response with URL or location where the file has been successfully transferred.
     * It also has the Tags, transcriptionUrl and transcriptionLength.
     */
    fun onComplete(id: String, response: TruvideoSdkMediaFileUploadRequest)

    /**
     * Called when the progress of a file transfer operation changes.
     *
     * @param id The unique key associated with the ongoing transfer operation.
     * @param progress The current progress of the transfer operation as a percentage.
     */
    fun onProgressChanged(id: String, progress: Float)

    /**
     * Called when an error occurs during a file transfer operation.
     *
     * @param id The unique key associated with the transfer operation that encountered an error.
     * @param ex The exception that describes the error encountered during the transfer.
     */
    fun onError(id: String, ex: TruvideoSdkMediaException)
}
