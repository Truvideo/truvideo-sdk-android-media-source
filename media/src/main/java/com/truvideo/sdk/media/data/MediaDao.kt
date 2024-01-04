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
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMedia(media: MediaEntity)

    @Update
    fun updateMedia(media: MediaEntity)

    @Query("UPDATE MediaEntity SET status = :newStatus WHERE id = :id")
    fun updateStatus(id: String, newStatus: MediaEntityStatus)

    @Query("SELECT externalId FROM MediaEntity WHERE id = :id")
    fun getExternalId(id: String): Int?

    @Query("SELECT * FROM MediaEntity")
    fun getAllUploadRequests(): LiveData<List<MediaEntity>>

    @Query("SELECT * FROM MediaEntity WHERE status = :status")
    fun getAllUploadRequestsByStatus(status: MediaEntityStatus): LiveData<List<MediaEntity>>

    //get all upload requests
    //get upload request where status = X
    //TODO get request by id
    //TODO delete request
    //stream all requests
    //stream request where status = X
    //TODO stream request by id
    //TODO delete all request where status = x
}
