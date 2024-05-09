package com.truvideo.sdk.media.interfaces

import com.truvideo.sdk.media.exception.TruvideoSdkMediaException


/**
 * Interface for receiving transfer-related events during file upload.
 */
interface TruvideoSdkMediaFileUploadCallback {

    /**
     * Called when a file transfer operation is completed successfully.
     *
     * @param id The unique key associated with the completed transfer operation.
     * @param url The URL or location where the file has been successfully transferred.
     */
    fun onComplete(id: String, url: String)

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
