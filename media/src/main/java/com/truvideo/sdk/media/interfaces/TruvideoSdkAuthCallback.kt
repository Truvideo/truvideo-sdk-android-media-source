package com.truvideo.sdk.media.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

/**
 * Base interface for receiving callbacks related to authentication in Truvideo SDK.
 */
interface TruvideoSdkAuthCallback {

    /**
     * Called when an error occurs during the authentication process.
     *
     * @param ex The [TruvideoSdkException] that describes the error encountered during authentication.
     */
    fun onAuthError(ex: TruvideoSdkException)
}