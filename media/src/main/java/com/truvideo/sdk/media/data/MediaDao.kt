package com.truvideo.sdk.media.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.truvideo.sdk.media.model.MediaEntity
import com.truvideo.sdk.media.model.MediaEntityStatus

@Dao
internal interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMedia(media: MediaEntity)

    @Update
    fun updateMedia(media: MediaEntity)

    @Query("SELECT externalId FROM MediaEntity WHERE id = :id")
    fun getExternalId(id: String): Int?

    @Query("SELECT * FROM MediaEntity WHERE id = :id")
    fun getMediaById(id: String): MediaEntity

    @Query("SELECT * FROM MediaEntity")
    fun getAllUploadRequests(): List<MediaEntity>

    @Query("SELECT * FROM MediaEntity WHERE status = :status")
    fun getAllUploadRequestsByStatus(status: MediaEntityStatus): List<MediaEntity>

    @Query("SELECT * FROM MediaEntity WHERE id = :id")
    fun streamMediaById(id: String): LiveData<MediaEntity>

    @Query("SELECT * FROM MediaEntity")
    fun streamAllUploadRequests(): LiveData<List<MediaEntity>>

    @Query("SELECT * FROM MediaEntity WHERE status = :status")
    fun streamAllUploadRequestsByStatus(status: MediaEntityStatus): LiveData<List<MediaEntity>>

    //get all upload requests
    //get upload request where status = X
    //get request by id
    //TODO delete request
    //stream all requests
    //stream request where status = X
    //stream request by id
    //TODO delete all request where status = x
}
