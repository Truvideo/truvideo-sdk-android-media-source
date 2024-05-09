package com.truvideo.sdk.media.data

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import java.util.Date

internal class DatabaseConverters {
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
    fun fromMap(map:  MutableMap<String, String>?): String? {
        if (map == null) return null
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toMap(json: String?): MutableMap<String, String>? {
        if (json == null) return null
        return Gson().fromJson(json, object: TypeToken<MutableMap<String, String>>() {}.type)
    }

}
