package com.truvideo.sdk.media.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.truvideo.sdk.media.model.MediaEntity

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMedia(media: MediaEntity)

    @Query("SELECT mediaLocalId FROM MediaEntity WHERE id = :id")
    fun getMediaLocalId(id: String): Int?
}
