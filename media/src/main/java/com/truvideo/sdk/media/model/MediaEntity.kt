package com.truvideo.sdk.media.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.truvideo.sdk.media.data.DatabaseConverters
import java.util.Date

@Entity
data class MediaEntity(
    @PrimaryKey val id: String,
    var externalId: Int? = null,
    var progress: Int? = null,
    var errorMessage: String? = null,
    var mediaURL: String? = null,
    @TypeConverters(DatabaseConverters::class) var uri: Uri,
    @TypeConverters(DatabaseConverters::class) var status: MediaEntityStatus,
    @TypeConverters(DatabaseConverters::class) val createdAt: Date = Date(),
    @TypeConverters(DatabaseConverters::class) var updatedAt: Date = Date()
)

enum class MediaEntityStatus {
    PROCESSING, ERROR, COMPLETED, PAUSED, CANCELED
}
