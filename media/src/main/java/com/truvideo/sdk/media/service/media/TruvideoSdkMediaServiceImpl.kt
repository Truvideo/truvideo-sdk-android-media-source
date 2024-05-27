package com.truvideo.sdk.media.service.media

import com.truvideo.sdk.media.data.converters.MetadataConverter
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaAuthAdapter
import com.truvideo.sdk.media.model.TruVideoSdkMediaFileUploadResponse
import com.truvideo.sdk.media.model.TruvideoSdkMediaResponse
import com.truvideo.sdk.media.model.TruvideoSdkPaginatedResponse
import org.json.JSONArray
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
        tags: Map<String, String>,
        metadata: Map<String, Any?>
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
            put("metadata", MetadataConverter().fromMap(metadata))
        }

        val baseUrl = sdk_common.configuration.environment.baseUrl
        val response = sdk_common.http.post(
            url = "$baseUrl/api/media", headers = headers, body = body.toString(), retry = false
        )

        if (response == null || !response.isSuccess) {
            throw TruvideoSdkException("Error creating media")
        }

        try {
            return TruVideoSdkMediaFileUploadResponse.fromJson(response.body)
        } catch (exception: Exception) {
            exception.printStackTrace()
            throw TruvideoSdkMediaException("Error parsing media response")
        }
    }

    override suspend fun fetchAll(
        tags: Map<String, String>?, idList: List<String>?, type: String?
    ): TruvideoSdkPaginatedResponse<TruvideoSdkMediaResponse> {
        authAdapter.validateAuthentication()
        authAdapter.refresh()

        val token = authAdapter.token

        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json",
        )

        val body = JSONObject()
        body.apply {
            tags?.let {
                put("tags", JSONObject().apply { for (tag in it) put(tag.key, tag.value) })
            }
            idList?.let {
                put("ids", JSONArray(it))
            }
            type?.let {
                put("type", it)
            }
        }

        val response = sdk_common.http.post(
            url = "${sdk_common.configuration.environment.baseUrl}/api/media/search",
            headers = headers,
            retry = true,
            body = body.toString()
        )

        if (response == null || !response.isSuccess) {
            throw TruvideoSdkException("Error creating media")
        }

        val json = JSONObject(response.body)
        val contentArray = json.getJSONArray("content")
        val mediaList = mutableListOf<TruvideoSdkMediaResponse>()

        for (i in 0 until contentArray.length()) {
            val item = contentArray.getJSONObject(i)
            val mediaFileUploaded = TruvideoSdkMediaResponse.fromJson(item)
            mediaList.add(mediaFileUploaded)
        }

        return TruvideoSdkPaginatedResponse(
            data = mediaList,
            totalPages = json.getInt("totalPages"),
            totalElements = json.getInt("totalElements"),
            numberOfElements = json.getInt("numberOfElements"),
            size = json.getInt("size"),
            number = json.getInt("number"),
            first = json.getBoolean("first"),
            empty = json.getBoolean("empty"),
            last = json.getInt("number") == json.getInt("totalPages"),
        )
    }
}