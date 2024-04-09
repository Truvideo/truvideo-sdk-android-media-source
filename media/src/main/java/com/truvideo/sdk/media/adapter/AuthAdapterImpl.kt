package com.truvideo.sdk.media.adapter

import com.truvideo.sdk.media.interfaces.TruvideoSdkVideoAuthAdapter
import truvideo.sdk.common.TruvideoSdk
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException

internal class AuthAdapterImpl(
    versionPropertiesAdapter: VersionPropertiesAdapter
) : TruvideoSdkVideoAuthAdapter {

    private val validateAuthentication: Boolean = versionPropertiesAdapter.readProperty("validateAuthentication") == "true"

    override fun validateAuthentication() {
        if (!validateAuthentication) return

        val isAuthenticated = TruvideoSdk.instance.auth.isAuthenticated
        if (!isAuthenticated) {
            throw TruvideoSdkAuthenticationRequiredException()
        }
    }
}