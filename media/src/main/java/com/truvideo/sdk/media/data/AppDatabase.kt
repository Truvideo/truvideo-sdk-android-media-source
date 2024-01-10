package com.truvideo.sdk.media.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.truvideo.sdk.media.model.MediaEntity

@Database(entities = [MediaEntity::class], version = 9)
@TypeConverters(DatabaseConverters::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
}
