@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.truvideo.sdk.media.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.truvideo.sdk.media.data.converters.DateConverter
import com.truvideo.sdk.media.data.converters.MetadataConverter
import com.truvideo.sdk.media.data.converters.TagsConverter
import com.truvideo.sdk.media.data.converters.TruvideoSdkMediaFileUploadStatusConverter
import com.truvideo.sdk.media.engines.TruvideoSdkMediaFileUploadEngine
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaCallback
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Entity(tableName = "FileUploadRequest")
data class TruvideoSdkMediaFileUploadRequest(
    @PrimaryKey val id: String,
    var filePath: String,
    var errorMessage: String? = null,
    var remoteId: String? = null,
    var remoteUrl: String? = null,
    var uploadProgress: Float? = null,
    var transcriptionUrl: String? = null,
    var transcriptionLength: Float? = null,
    var deleteOnComplete: Boolean = false,

    @TypeConverters(TagsConverter::class)
    var tags: Map<String, String> = mapOf(),

    @TypeConverters(MetadataConverter::class)
    var metadata: Map<String, Any?> = mapOf(),

    @TypeConverters(TruvideoSdkMediaFileUploadStatusConverter::class)
    var status: TruvideoSdkMediaFileUploadStatus = TruvideoSdkMediaFileUploadStatus.IDLE,

    @TypeConverters(DateConverter::class)
    val createdAt: Date = Date(),

    @TypeConverters(DateConverter::class)
    var updatedAt: Date = Date(),

    internal var externalId: Int? = null,
    internal var bucketName: String = "",
    internal var region: String = "",
    internal var poolId: String = "",
    internal var folder: String = ""
) {
    @Ignore
    internal var engine: TruvideoSdkMediaFileUploadEngine? = null

    suspend fun cancel() {
        val e = engine ?: throw TruvideoSdkMediaException("Engine is null")
        e.cancel(id)
    }

    fun cancel(callback: TruvideoSdkMediaCallback<Unit>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cancel()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(TruvideoSdkMediaException(exception.message ?: "Unknown error"))
            }
        }
    }

    suspend fun pause() {
        val e = engine ?: throw TruvideoSdkMediaException("Engine is null")
        e.pause(id)
    }

    fun pause(callback: TruvideoSdkMediaCallback<Unit>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                pause()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(TruvideoSdkMediaException(exception.message ?: "Unknown error"))
            }
        }
    }

    suspend fun resume() {
        val e = engine ?: throw TruvideoSdkMediaException("Engine is null")
        e.resume(id)
    }

    fun resume(callback: TruvideoSdkMediaCallback<Unit>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                resume()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(TruvideoSdkMediaException(exception.message ?: "Unknown error"))
            }
        }
    }


    fun upload(callback: TruvideoSdkMediaFileUploadCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val e = engine ?: throw TruvideoSdkMediaException("Engine is null")
                e.upload(id, callback)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(id, TruvideoSdkMediaException(exception.message ?: "Unknown error"))
            }
        }
    }

    suspend fun delete() {
        val e = engine ?: throw TruvideoSdkMediaException("Engine is null")
        e.delete(id)
    }

    fun delete(callback: TruvideoSdkMediaCallback<Unit>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delete()
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onError(TruvideoSdkMediaException(exception.message ?: "Unknown error"))
            }
        }
    }
}