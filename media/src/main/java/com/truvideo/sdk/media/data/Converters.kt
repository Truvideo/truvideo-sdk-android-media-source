package com.truvideo.sdk.media.data

import androidx.room.TypeConverter
import com.truvideo.sdk.media.model.MediaEntityStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: MediaEntityStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(status: String): MediaEntityStatus {
        return enumValueOf<MediaEntityStatus>(status)
    }
}
