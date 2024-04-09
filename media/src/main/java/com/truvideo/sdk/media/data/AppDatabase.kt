package com.truvideo.sdk.media.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest

@Database(
    entities = [TruvideoSdkMediaFileUploadRequest::class],
    autoMigrations = [],
    version = 1
)
@TypeConverters(DatabaseConverters::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): FileUploadRequestDAO
}
