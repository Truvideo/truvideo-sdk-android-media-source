package com.truvideo.sdk.media.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

interface TruvideoSdkGenericCallback<T : Any> {

    fun onComplete(data: T)

    fun onError(exception: TruvideoSdkException)
}
