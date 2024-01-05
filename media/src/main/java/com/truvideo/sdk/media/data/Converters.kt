package com.truvideo.sdk.media.data

import androidx.room.TypeConverter
import com.truvideo.sdk.media.model.MediaEntityStatus
import java.util.Date

class Converters {
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

}
