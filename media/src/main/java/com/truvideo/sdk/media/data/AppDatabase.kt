package com.truvideo.sdk.media.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.truvideo.sdk.media.model.MediaEntity

@Database(entities = [MediaEntity::class], version = 6)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
}
