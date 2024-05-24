package com.truvideo.sdk.media.interfaces

internal interface TruvideoSdkMediaAuthAdapter {

    fun validateAuthentication()

    suspend fun refresh()

    val token: String
}