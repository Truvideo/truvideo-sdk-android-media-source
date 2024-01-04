package com.truvideo.sdk.media.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.truvideo.sdk.media.data.Converters

@Entity
data class MediaEntity(
    @PrimaryKey val id: String,
    val externalId: Int? = null,
    @TypeConverters(Converters::class) val status: MediaEntityStatus // status (idle, processing, error, completed)
)

enum class MediaEntityStatus {
    IDLE, PROCESSING, ERROR, COMPLETED
}
