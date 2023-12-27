package com.truvideo.sdk.media.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException


/**
 * Interface for receiving transfer-related events during file upload.
 */
interface TruvideoSdkCancelCallback {

    fun onReady(id: String)

    fun onError(id: String, ex: TruvideoSdkException)
}
