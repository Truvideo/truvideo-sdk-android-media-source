package com.truvideo.sdk.media.interfaces

import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.model.MediaEntity
import truvideo.sdk.common.exception.TruvideoSdkException

/**
 * Interface for receiving callbacks related to file transfer operations in Truvideo SDK.
 */
interface TruvideoSdkStreamElementCallback {

    /**
     * Called when a get information operation is completed successfully.
     *
     * @param urlLiveData The [LiveData] containing a [MediaEntity] object representing the transferred media entity.
     *                    Use this LiveData to observe the changes in the transferred media entity.
     */
    fun onComplete(urlLiveData: LiveData<MediaEntity>)

    /**
     * Called when an error occurs during a get information operation.
     *
     * @param ex The [TruvideoSdkException] that describes the error encountered during the transfer.
     */
    fun onError(ex: TruvideoSdkException)
}