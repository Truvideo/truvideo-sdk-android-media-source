package com.truvideo.sdk.media.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class TagsConverter {

    @TypeConverter
    fun fromMap(map: Map<String, String>?): String? {
        if (map == null) return null
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toMap(json: String?): Map<String, String>? {
        if (json == null) return null
        return Gson().fromJson(json, object : TypeToken<Map<String, String>>() {}.type)
    }
}