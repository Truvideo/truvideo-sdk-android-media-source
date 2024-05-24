package com.truvideo.sdk.media.interfaces

import com.truvideo.sdk.media.exception.TruvideoSdkMediaException

interface TruvideoSdkMediaCallback<T : Any?> {

    fun onComplete(data: T)

    fun onError(exception: TruvideoSdkMediaException)
}
