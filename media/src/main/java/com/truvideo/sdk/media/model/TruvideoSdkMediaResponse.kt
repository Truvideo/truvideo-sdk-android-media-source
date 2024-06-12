package com.truvideo.sdk.media.model

import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class TruvideoSdkMediaResponse(
    val id: String,
    val url: String,
    val transcriptionUrl: String?,
    val transcriptionLength: String?,
    val tags: Map<String, String>,
    val createdDate: Date?,
    val metadata: Map<String, Any>?,
    val type: String?,
    val title: String?
) {
    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun fromJson(json: JSONObject): TruvideoSdkMediaResponse {
            val id = json.getString("id")
            val url = json.getString("url")
            val transcriptionUrl = json.safeOptString("transcriptionUrl")
            val transcriptionLength = json.safeOptString("transcriptionLength")

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

            val createdDate = json.safeOptString("createdDate")?.let {
                try {
                    dateFormat.parse(it)
                } catch (e: ParseException) {
                    null
                }
            }

            val metadataJson = json.safeOptString("metadata")?.let {
                try {
                    JSONObject(it).toMap()
                } catch (e: Exception) {
                    null
                }
            } ?: emptyMap()

            val type = json.safeOptString("type")
            val title = json.safeOptString("title")

            return TruvideoSdkMediaResponse(
                id = id,
                url = url,
                transcriptionUrl = transcriptionUrl,
                transcriptionLength = transcriptionLength,
                tags = tags,
                createdDate = createdDate,
                metadata = metadataJson,
                type = type,
                title = title
            )
        }

        private fun JSONObject.safeOptString(key: String): String? {
            return try {
                this.optString(key, null)
            } catch (e: JSONException) {
                null
            }
        }

        private fun JSONObject.toMap(): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            val keys = keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = when (val value = get(key)) {
                    is JSONObject -> value.toMap()
                    else -> value
                }
            }
            return map
        }
    }

    override fun toString(): String {
        val formattedDate = createdDate?.let { dateFormat.format(it) }
        return "TruvideoSdkMediaResponse(id='$id', url='$url', transcriptionUrl='$transcriptionUrl', transcriptionLength='$transcriptionLength', tags=$tags, createdDate=$formattedDate, metadata=$metadata, type=$type, title=$title)"
    }
}