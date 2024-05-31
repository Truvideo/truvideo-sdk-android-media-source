package com.truvideo.sdk.media.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.truvideo.sdk.media.data.converters.DateConverter
import com.truvideo.sdk.media.data.converters.MetadataConverter
import com.truvideo.sdk.media.data.converters.TagsConverter
import com.truvideo.sdk.media.data.converters.TruvideoSdkMediaFileUploadStatusConverter
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest

@Database(
    entities = [TruvideoSdkMediaFileUploadRequest::class],
    autoMigrations = [],
    version = 4,
    exportSchema = false
)

@TypeConverters(
    DateConverter::class,
    MetadataConverter::class,
    TagsConverter::class,
    TruvideoSdkMediaFileUploadStatusConverter::class
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): FileUploadRequestDAO
}
