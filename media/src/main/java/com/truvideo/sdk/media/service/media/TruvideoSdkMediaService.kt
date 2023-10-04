package com.truvideo.sdk.media.service.media

import android.util.Log
import org.json.JSONObject
import truvideo.sdk.common.TruvideoSdk
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exception.TruvideoSdkException

internal class TruvideoSdkMediaService : TruvideoSdkMediaServiceInterface {

    private val common = TruvideoSdk.instance

    override suspend fun createMedia(
        title: String,
        url: String,
        size: Long,
        type: String
    ): String {
        common.auth.refreshAuthentication()

        if (!common.auth.isAuthenticated) {
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val token = common.auth.authentication?.accessToken ?: ""

        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json",
        )

        val body = JSONObject()
        body.apply {
            put("title", title)
            put("type", type)
            put("url", url)
            put("resolution", "LOW")
            put("size", size)
        }

        Log.d("TruvideoSdkMedia", "createMedia - headers: $headers")
        Log.d("TruvideoSdkMedia", "createMedia - body: $body")
        val response = TruvideoSdk.instance.http.post(
            url = "https://sdk-mobile-api-beta.truvideo.com:443/api/media",
            headers = headers,
            body = body.toString(),
            retry = true,
            printLogs = true
        )

        Log.d("TruvideoSdkMedia", "createMedia - response: $response")

        if (response == null || !response.isSuccess) {
            throw TruvideoSdkException("Error creating media")
        }

        Log.d("body", "createMedia: ${response.body}")

        // TODO: get url from response and return it
        return url
    }
}