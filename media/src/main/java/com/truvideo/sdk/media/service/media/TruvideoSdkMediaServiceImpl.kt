package com.truvideo.sdk.media.service.media

import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaAuthAdapter
import com.google.gson.Gson
import com.truvideo.sdk.media.model.TruVideoSdkMediaFileUploadResponse
import org.json.JSONObject
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.model.baseUrl
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkMediaServiceImpl(
    private val authAdapter: TruvideoSdkMediaAuthAdapter
) : TruvideoSdkMediaService {


    override suspend fun createMedia(
        title: String,
        url: String,
        size: Long,
        type: String,
        tags: Map<String, String>
    ): TruVideoSdkMediaFileUploadResponse {
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
            put("tags", JSONObject().apply { for (tag in tags) put(tag.key, tag.value) })
        }

        val baseUrl = sdk_common.configuration.environment.baseUrl
        val response = sdk_common.http.post(
            url = "$baseUrl/api/media",
            headers = headers,
            body = body.toString(),
            retry = true
        )

        if (response == null || !response.isSuccess) {
            throw TruvideoSdkException("Error creating media")
        }

        return Gson().fromJson(response.body, TruVideoSdkMediaFileUploadResponse::class.java)


    }
}