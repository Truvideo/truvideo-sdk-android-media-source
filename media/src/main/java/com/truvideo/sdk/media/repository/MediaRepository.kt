package com.truvideo.sdk.media.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.data.DatabaseSingleton
import com.truvideo.sdk.media.model.MediaEntity
import com.truvideo.sdk.media.model.MediaEntityStatus

class MediaRepository {

    fun insertMedia(context: Context, media: MediaEntity) {
        DatabaseSingleton.getDatabase(context).mediaDao().insertMedia(media)
    }

    fun update(context: Context, media: MediaEntity) {
        DatabaseSingleton.getDatabase(context).mediaDao().updateMedia(media)
    }

    fun updateStatus(context: Context, id: String, status: MediaEntityStatus) {
        DatabaseSingleton.getDatabase(context).mediaDao().updateStatus(id, status)
    }

    fun getExternalId(context: Context, id: String): Int? {
        return DatabaseSingleton.getDatabase(context).mediaDao().getExternalId(id)
    }

    fun getAllUploadRequests(context: Context): LiveData<List<MediaEntity>> {
        return DatabaseSingleton.getDatabase(context).mediaDao().getAllUploadRequests()
    }

    fun getAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): LiveData<List<MediaEntity>> {
        return DatabaseSingleton.getDatabase(context).mediaDao()
            .getAllUploadRequestsByStatus(status)
    }
}