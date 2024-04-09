package com.truvideo.sdk.media.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus

@Dao
internal interface FileUploadRequestDAO {

    @Delete
    fun delete(model: TruvideoSdkMediaFileUploadRequest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: TruvideoSdkMediaFileUploadRequest)

    @Update
    fun update(model: TruvideoSdkMediaFileUploadRequest)

    @Query("SELECT * FROM FileUploadRequest WHERE id = :id")
    fun getById(id: String): TruvideoSdkMediaFileUploadRequest?

    @Query("SELECT * FROM FileUploadRequest WHERE id = :id")
    fun streamById(id: String): LiveData<TruvideoSdkMediaFileUploadRequest?>

    @Query("SELECT * FROM FileUploadRequest")
    fun getAll(): List<TruvideoSdkMediaFileUploadRequest>

    @Query("SELECT * FROM FileUploadRequest WHERE status = :status")
    fun getAllByStatus(status: TruvideoSdkMediaFileUploadStatus): List<TruvideoSdkMediaFileUploadRequest>

    @Query("SELECT * FROM FileUploadRequest")
    fun streamAll(): LiveData<List<TruvideoSdkMediaFileUploadRequest>>

    @Query("SELECT * FROM FileUploadRequest WHERE status = :status")
    fun streamAllByStatus(status: TruvideoSdkMediaFileUploadStatus): LiveData<List<TruvideoSdkMediaFileUploadRequest>>

}
