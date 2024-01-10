package com.truvideo.sdk.media.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.data.DatabaseInstance
import com.truvideo.sdk.media.model.MediaEntity
import com.truvideo.sdk.media.model.MediaEntityStatus
import java.util.Date

internal class MediaRepositoryImpl : MediaRepositoryInterface {

    override fun insertMedia(context: Context, media: MediaEntity) {
        DatabaseInstance.getDatabase(context).mediaDao().insertMedia(media)
    }

    override fun update(context: Context, media: MediaEntity) {
        media.updatedAt = Date()
        DatabaseInstance.getDatabase(context).mediaDao().updateMedia(media)
    }

    override fun updateProgress(context: Context, id: String, progress: Int) {
        val media = getMediaById(context, id)
        media.progress = progress
        update(context, media)
    }

    override fun updateToIdleStatus(context: Context, id: String) {
        val media = getMediaById(context, id)
        media.status = MediaEntityStatus.IDLE
        update(context, media)
    }

    override fun updateToPausedStatus(context: Context, id: String) {
        val media = getMediaById(context, id)
        media.status = MediaEntityStatus.PAUSED
        update(context, media)
    }

    override fun updateToCanceledStatus(context: Context, id: String) {
        val media = getMediaById(context, id)
        media.status = MediaEntityStatus.CANCELED
        update(context, media)
    }

    override fun updateToCompletedStatus(context: Context, id: String, mediaURL: String) {
        val media = getMediaById(context, id)
        media.status = MediaEntityStatus.COMPLETED
        media.mediaURL = mediaURL
        update(context, media)
    }

    override fun updateToErrorStatus(
        context: Context, id: String, errorMessage: String?
    ) {
        val media = getMediaById(context, id)
        media.status = MediaEntityStatus.ERROR
        media.errorMessage = errorMessage
        update(context, media)
    }

    override fun getExternalId(context: Context, id: String): Int? {
        return DatabaseInstance.getDatabase(context).mediaDao().getExternalId(id)
    }

    override fun getMediaById(context: Context, id: String): MediaEntity {
        return DatabaseInstance.getDatabase(context).mediaDao().getMediaById(id)
    }

    override fun getAllUploadRequests(context: Context): List<MediaEntity> {
        return DatabaseInstance.getDatabase(context).mediaDao().getAllUploadRequests()
    }

    override fun getAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): List<MediaEntity> {
        return DatabaseInstance.getDatabase(context).mediaDao().getAllUploadRequestsByStatus(status)
    }

    override fun streamMediaById(context: Context, id: String): LiveData<MediaEntity> {
        return DatabaseInstance.getDatabase(context).mediaDao().streamMediaById(id)
    }

    override fun streamAllUploadRequests(context: Context): LiveData<List<MediaEntity>> {
        return DatabaseInstance.getDatabase(context).mediaDao().streamAllUploadRequests()
    }

    override fun streamAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): LiveData<List<MediaEntity>> {
        return DatabaseInstance.getDatabase(context).mediaDao()
            .streamAllUploadRequestsByStatus(status)
    }
}