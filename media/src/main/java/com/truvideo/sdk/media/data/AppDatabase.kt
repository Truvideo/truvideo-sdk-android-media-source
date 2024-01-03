package com.truvideo.sdk.media.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.truvideo.sdk.media.model.MediaEntity

@Database(entities = [MediaEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
}
