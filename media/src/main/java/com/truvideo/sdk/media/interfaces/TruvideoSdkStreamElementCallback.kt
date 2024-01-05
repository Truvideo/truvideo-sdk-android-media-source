package com.truvideo.sdk.media.interfaces

import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.model.MediaEntity

/**
 * Interface for receiving callbacks related to file transfer operations in Truvideo SDK.
 */
interface TruvideoSdkStreamElementCallback : TruvideoSdkAuthCallback {

    /**
     * Called when a get information operation is completed successfully.
     *
     * @param urlLiveData The [LiveData] containing a [MediaEntity] object representing the transferred media entity.
     *                    Use this LiveData to observe the changes in the transferred media entity.
     */
    fun onComplete(urlLiveData: LiveData<MediaEntity>)
}