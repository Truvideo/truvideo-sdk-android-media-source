package com.truvideo.sdk.media.adapter

import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaAuthAdapter
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.exception.TruvideoSdkNotInitializedException
import truvideo.sdk.common.sdk_common

internal class AuthAdapterImpl(
    versionPropertiesAdapter: VersionPropertiesAdapter
) : TruvideoSdkMediaAuthAdapter {

    private val validateAuthentication: Boolean = versionPropertiesAdapter.readProperty("validateAuthentication") == "true"

    override fun validateAuthentication() {
        if (!validateAuthentication) return

        val isAuthenticated = sdk_common.auth.isAuthenticated.value
        if (!isAuthenticated) {
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val isInitialized = sdk_common.auth.isInitialized.value
        if (!isInitialized) {
            throw TruvideoSdkNotInitializedException()
        }
    }

    override suspend fun refresh() = sdk_common.auth.refresh()

    override val token: String
        get() = sdk_common.auth.authentication.value?.accessToken ?: throw TruvideoSdkException("No access token found")
}