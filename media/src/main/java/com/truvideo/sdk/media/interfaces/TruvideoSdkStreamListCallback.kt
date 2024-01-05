package com.truvideo.sdk.media.interfaces

import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.model.MediaEntity

/**
 * Interface for receiving callbacks as streams related to file transfer operations in Truvideo SDK.
 */
interface TruvideoSdkStreamListCallback : TruvideoSdkAuthCallback {

    /**
     * Called when a get information operation is completed successfully.
     *
     * @param urlLiveData The list of [MediaEntity] objects within the LiveData represents the transferred media entities.
     */
    fun onComplete(urlLiveData: LiveData<List<MediaEntity>>)
}
