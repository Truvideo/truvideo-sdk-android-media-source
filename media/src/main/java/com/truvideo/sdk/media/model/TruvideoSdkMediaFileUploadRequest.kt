@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.truvideo.sdk.media.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.truvideo.sdk.media.data.DatabaseConverters
import com.truvideo.sdk.media.engines.TruvideoSdkMediaEngine
import com.truvideo.sdk.media.engines.TruvideoSdkMediaFileUploadEngine
import com.truvideo.sdk.media.interfaces.TruvideoSdkGenericCallback
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.exception.TruvideoSdkException
import java.util.Date

@Entity(tableName = "FileUploadRequest")
data class TruvideoSdkMediaFileUploadRequest(
    @PrimaryKey val id: String,
    var filePath: String,
    var errorMessage: String? = null,
    var mediaURL: String? = null,
    var url: String? = null,
    var progress: Float? = null,
    var transcriptionUrl: String? = null,
    var transcriptionLength: String? = null,

    @TypeConverters(DatabaseConverters::class) var tags: MutableMap<String, String> = mutableMapOf(),
    @TypeConverters(DatabaseConverters::class) var status: TruvideoSdkMediaFileUploadStatus = TruvideoSdkMediaFileUploadStatus.IDLE,
    @TypeConverters(DatabaseConverters::class) val createdAt: Date = Date(),
    @TypeConverters(DatabaseConverters::class) var updatedAt: Date = Date(),

    internal var externalId: Int? = null,
    internal var bucketName: String = "",
    internal var region: String = "",
    internal var poolId: String = "",
    internal var folder: String = ""
) {
    @Ignore
    internal var engine: TruvideoSdkMediaEngine? = null

    suspend fun cancel() {
        (engine as? TruvideoSdkMediaFileUploadEngine)?.cancel(id)
    }

    fun cancel(callback: TruvideoSdkGenericCallback<Unit>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cancel()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                val ex = if (exception is TruvideoSdkException) exception else TruvideoSdkException("Unknown error")
                callback.onError(ex)
            }
        }
    }

    suspend fun pause() {
        (engine as? TruvideoSdkMediaFileUploadEngine)?.pause(id)
    }

    fun pause(callback: TruvideoSdkGenericCallback<Unit>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                pause()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                val ex = if (exception is TruvideoSdkException) exception else TruvideoSdkException("Unknown error")
                callback.onError(ex)
            }
        }
    }

    suspend fun resume() {
        (engine as? TruvideoSdkMediaFileUploadEngine)?.resume(id)
    }

    fun resume(callback: TruvideoSdkGenericCallback<Unit>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                resume()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                val ex = if (exception is TruvideoSdkException) exception else TruvideoSdkException("Unknown error")
                callback.onError(ex)
            }
        }
    }

    fun upload(callback: TruvideoSdkMediaFileUploadCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            (engine as? TruvideoSdkMediaFileUploadEngine)?.upload(id, callback)
        }
    }

    suspend fun delete() {
        (engine as? TruvideoSdkMediaFileUploadEngine)?.delete(id)
    }

    fun delete(callback: TruvideoSdkGenericCallback<Unit>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delete()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                val ex = if (exception is TruvideoSdkException) exception else TruvideoSdkException("Unknown error")
                callback.onError(ex)
            }
        }
    }

}