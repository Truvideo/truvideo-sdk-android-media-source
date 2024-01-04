package com.truvideo.sdk.media.interfaces

import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.model.MediaEntity
import truvideo.sdk.common.exception.TruvideoSdkException

/**
 * Interface for receiving callbacks related to file transfer operations in Truvideo SDK.
 */
interface TruvideoSdkGetCallback {

    /**
     * Called when a get information operation is completed successfully.
     *
     * @param urlLiveData The list of [MediaEntity] objects within the LiveData represents the transferred media entities.
     */
    fun onComplete(urlLiveData: LiveData<List<MediaEntity>>)

    /**
     * Called when an error occurs during a get information operation.
     *
     * @param ex The [TruvideoSdkException] that describes the error encountered during the transfer.
     */
    fun onError(ex: TruvideoSdkException)
}
