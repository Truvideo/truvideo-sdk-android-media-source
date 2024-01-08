package com.truvideo.sdk.media.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.truvideo.sdk.media.data.Converters
import java.util.Date

@Entity
data class MediaEntity(
    @PrimaryKey val id: String,
    var externalId: Int? = null,
    var progress: Int? = null,
    @TypeConverters(Converters::class) var status: MediaEntityStatus,
    @TypeConverters(Converters::class) val createdAt: Date = Date(),
    @TypeConverters(Converters::class) var updatedAt: Date = Date()
)

enum class MediaEntityStatus {
    IDLE, PROCESSING, ERROR, COMPLETED
}
