package com.truvideo.sdk.media.service.media

import com.truvideo.sdk.media.interfaces.TruvideoSdkVideoAuthAdapter
import org.json.JSONObject
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkMediaServiceImpl(
    private val authAdapter: TruvideoSdkVideoAuthAdapter
) : TruvideoSdkMediaService {


    override suspend fun createMedia(
        title: String,
        url: String,
        size: Long,
        type: String
    ): String {
        authAdapter.validateAuthentication()
        authAdapter.refresh()

        val token = authAdapter.token

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

        val response = sdk_common.http.post(
            url = "https://sdk-mobile-api-beta.truvideo.com:443/api/media",
            headers = headers,
            body = body.toString(),
            retry = true
        )

        if (response == null || !response.isSuccess) {
            throw TruvideoSdkException("Error creating media")
        }

        return JSONObject(response.body).getString("url")
    }
}