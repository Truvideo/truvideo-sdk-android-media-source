package com.truvideo.sdk.media.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class MetadataConverter {

    @TypeConverter
    fun fromMap(map: Map<String, Any?>?): String? {
        if (map == null) return null
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toMap(json: String?): Map<String, Any?>? {
        if (json == null) return null
        return Gson().fromJson(json, object : TypeToken<Map<String, Any?>>() {}.type)
    }
}