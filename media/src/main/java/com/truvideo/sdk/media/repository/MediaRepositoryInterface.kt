package com.truvideo.sdk.media.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.model.MediaEntity
import com.truvideo.sdk.media.model.MediaEntityStatus

internal interface MediaRepositoryInterface {

    fun insertMedia(context: Context, media: MediaEntity)

    fun update(context: Context, media: MediaEntity)

    fun updateProgress(context: Context, id: String, progress: Int)

    fun updateToCompletedStatus(context: Context, id: String, mediaURL: String)

    fun updateToErrorStatus(context: Context, id: String, errorMessage: String? = "Unknown error")

    fun getExternalId(context: Context, id: String): Int?

    fun getMediaById(context: Context, id: String): MediaEntity

    fun getAllUploadRequests(context: Context): List<MediaEntity>

    fun getAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): List<MediaEntity>

    fun streamMediaById(context: Context, id: String): LiveData<MediaEntity>

    fun streamAllUploadRequests(context: Context): LiveData<List<MediaEntity>>

    fun streamAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): LiveData<List<MediaEntity>>

    fun updateToPausedStatus(context: Context, id: String)
    fun updateToCanceledStatus(context: Context, id: String)
    fun delete(context: Context, media: MediaEntity)
}