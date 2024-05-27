package com.truvideo.sdk.media.model

import org.json.JSONObject

data class TruvideoSdkMediaResponse(
    val id: String,
    val url: String,
    val transcriptionUrl: String?,
    val transcriptionLength: String?,
    val tags: Map<String, String>
) {
    companion object {
        fun fromJson(json: JSONObject): TruvideoSdkMediaResponse {
            val id = json.getString("id")
            val url = json.getString("url")
            val transcriptionUrl = json.optString("transcriptionUrl", null)
            val transcriptionLength = json.optString("transcriptionLength", null)

            val tagsJson = json.optJSONObject("tags")
            val tags: Map<String, String> = tagsJson?.let {
                val tagMap = mutableMapOf<String, String>()
                val keys = it.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    tagMap[key] = it.getString(key)
                }
                tagMap
            } ?: emptyMap()


            return TruvideoSdkMediaResponse(
                id = id,
                url = url,
                transcriptionUrl = transcriptionUrl,
                transcriptionLength = transcriptionLength,
                tags = tags
            )
        }
    }
}