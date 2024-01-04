package com.truvideo.sdk.media.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.truvideo.sdk.media.model.MediaEntity

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMedia(media: MediaEntity)

    @Query("SELECT externalId FROM MediaEntity WHERE id = :id")
    fun getExternalId(id: String): Int?

    @Query("SELECT * FROM MediaEntity")
    fun getAllUploadRequests(): LiveData<List<MediaEntity>>
}
