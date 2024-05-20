package com.truvideo.sdk.media.data

import androidx.room.TypeConverter
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import com.truvideo.sdk.media.util.toJsonElement
import com.truvideo.sdk.media.util.toMapAnyFromJson
import com.truvideo.sdk.media.util.toMapStringFromJson
import kotlinx.serialization.json.Json
import java.util.Date

internal class DatabaseConverters {

    private val json = Json { encodeDefaults = true }

    @TypeConverter
    fun fromStatus(status: TruvideoSdkMediaFileUploadStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(status: String): TruvideoSdkMediaFileUploadStatus {
        return enumValueOf<TruvideoSdkMediaFileUploadStatus>(status)
    }

    @TypeConverter
    fun fromDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun toDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromMap(map:  Map<String, String>?): String? {
        if (map == null) return null
        return map.toJsonElement().toString()
    }

    @TypeConverter
    fun toMap(json: String?): Map<String, String>? {
        if (json == null) return null
        return json.toMapStringFromJson()
    }

    @TypeConverter
    fun fromMapAny(map:  Map<String, Any?>?): String? {
        if (map == null) return null
        return map.toJsonElement().toString()
    }

    @TypeConverter
    fun toMapAny(mapString: String?): Map<String, Any?>? {
        if (mapString == null) return null
        return mapString.toMapAnyFromJson()
    }
}
