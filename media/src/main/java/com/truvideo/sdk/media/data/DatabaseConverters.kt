package com.truvideo.sdk.media.data

import android.net.Uri
import androidx.room.TypeConverter
import com.truvideo.sdk.media.model.MediaEntityStatus
import java.util.Date

internal class DatabaseConverters {
    @TypeConverter
    fun fromStatus(status: MediaEntityStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(status: String): MediaEntityStatus {
        return enumValueOf<MediaEntityStatus>(status)
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
    fun fromUri(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun toUri(uriString: String): Uri {
        return Uri.parse(uriString)
    }

}
