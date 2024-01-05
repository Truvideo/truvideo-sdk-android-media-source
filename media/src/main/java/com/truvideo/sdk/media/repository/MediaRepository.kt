package com.truvideo.sdk.media.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.data.DatabaseSingleton
import com.truvideo.sdk.media.model.MediaEntity
import com.truvideo.sdk.media.model.MediaEntityStatus
import java.util.Date

class MediaRepository {

    fun insertMedia(context: Context, media: MediaEntity) {
        DatabaseSingleton.getDatabase(context).mediaDao().insertMedia(media)
    }

    fun update(context: Context, media: MediaEntity) {
        media.updatedAt = Date()
        DatabaseSingleton.getDatabase(context).mediaDao().updateMedia(media)
    }

    fun updateStatus(context: Context, id: String, status: MediaEntityStatus) {
        val media = getMediaById(context, id)
        media.status = status
        update(context, media)
    }

    fun getExternalId(context: Context, id: String): Int? {
        return DatabaseSingleton.getDatabase(context).mediaDao().getExternalId(id)
    }

    fun getMediaById(context: Context, id: String): MediaEntity {
        return DatabaseSingleton.getDatabase(context).mediaDao().getMediaById(id)
    }

    fun getAllUploadRequests(context: Context): List<MediaEntity> {
        return DatabaseSingleton.getDatabase(context).mediaDao().getAllUploadRequests()
    }

    fun getAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): List<MediaEntity> {
        return DatabaseSingleton.getDatabase(context).mediaDao()
            .getAllUploadRequestsByStatus(status)
    }

    fun streamMediaById(context: Context, id: String): LiveData<MediaEntity> {
        return DatabaseSingleton.getDatabase(context).mediaDao().streamMediaById(id)
    }

    fun streamAllUploadRequests(context: Context): LiveData<List<MediaEntity>> {
        return DatabaseSingleton.getDatabase(context).mediaDao().streamAllUploadRequests()
    }

    fun streamAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): LiveData<List<MediaEntity>> {
        return DatabaseSingleton.getDatabase(context).mediaDao()
            .streamAllUploadRequestsByStatus(status)
    }
}