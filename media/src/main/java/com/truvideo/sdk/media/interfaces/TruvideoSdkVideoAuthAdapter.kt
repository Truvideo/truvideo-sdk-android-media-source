package com.truvideo.sdk.media.interfaces

internal interface TruvideoSdkVideoAuthAdapter {

    fun validateAuthentication()

    suspend fun refresh()

    val token: String
}