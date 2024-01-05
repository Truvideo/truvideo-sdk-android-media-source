package com.truvideo.sdk.media.interfaces

/**
 * Interface for receiving callbacks related to file transfer operations in Truvideo SDK.
 *
 * @param <T> The type of the transferred media entity or entities.
 */
interface TruvideoSdkGenericCallback<T : Any> : TruvideoSdkAuthCallback {

    /**
     * Called when a get information operation is completed successfully.
     *
     * @param data The [T] object or [LiveData<T>] representing the transferred media entity or entities.
     *             Use this object to handle the transferred media entity or entities.
     */
    fun onComplete(data: T)
}
