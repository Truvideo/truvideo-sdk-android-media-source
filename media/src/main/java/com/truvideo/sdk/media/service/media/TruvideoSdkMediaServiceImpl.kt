package com.truvideo.sdk.media.service.media

import com.google.gson.Gson
import com.truvideo.sdk.media.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.media.model.TruVideoSdkMediaFileUploadResponse
import org.json.JSONObject
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.model.baseUrl
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkMediaServiceImpl(
    private val authAdapter: TruvideoSdkVideoAuthAdapter
) : TruvideoSdkMediaService {


    override suspend fun createMedia(
        title: String,
        url: String,
        size: Long,
        type: String,
        tags: Map<String, String>
    ): TruVideoSdkMediaFileUploadResponse { //TrueVideoSdkMediaFileUploadResponse
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
            put("resolution", "LOW")// Is it gonna be hardcoded???
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
        //Return object
        /*
        *   "title": "Another Title",
    "type": "VIDEO", // VIDEO - IMAGE
    "url": "https://sdkmobileapi-7ea27dee-3875-495a-89b0-41fd4f18b71e.s3.us-west-2.amazonaws.com/media/%2B6EzoOztKL5nbYCerIQbHxOBIlvsdVLhYcCXShI7KuQED4hpRg4NXA%3D%3D_BG+-+44K+PlatinumTM_.mp4",
    "resolution": "NORMAL", // LOW - NORMAL - HIGH
    "size": 150.00,
    "tags": {
        "media": "sdk",
        "account": "reynolds"
    }
        *
        * */


    }
}