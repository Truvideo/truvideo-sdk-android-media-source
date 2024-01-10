package com.truvideo.sdk.media

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.interfaces.TruvideoSdkAuthCallback
import com.truvideo.sdk.media.interfaces.TruvideoSdkCancelCallback
import com.truvideo.sdk.media.interfaces.TruvideoSdkGenericCallback
import com.truvideo.sdk.media.interfaces.TruvideoSdkMedia
import com.truvideo.sdk.media.interfaces.TruvideoSdkPauseCallback
import com.truvideo.sdk.media.interfaces.TruvideoSdkUploadCallback
import com.truvideo.sdk.media.model.MediaEntity
import com.truvideo.sdk.media.model.MediaEntityStatus
import com.truvideo.sdk.media.service.media.TruvideoSdkMediaService
import com.truvideo.sdk.media.service.upload.TruvideoSdkUploadServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdk
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exception.TruvideoSdkException
import java.util.UUID


internal object TruvideoSdkMediaImpl : TruvideoSdkMedia {

    private val uploadService = TruvideoSdkUploadServiceImpl(
        mediaService = TruvideoSdkMediaService(),
    )

    private val common = TruvideoSdk.instance
    private var ioScope = CoroutineScope(Dispatchers.IO)

    override fun upload(
        context: Context, file: Uri, callback: TruvideoSdkUploadCallback
    ): String {
        try {
            val mediaLocalKey = UUID.randomUUID().toString()

            val isAuthenticated = common.auth.isAuthenticated
            if (!isAuthenticated) {
                callback.onError(mediaLocalKey, TruvideoSdkAuthenticationRequiredException())
                return mediaLocalKey
            }

            val credentials = common.auth.settings?.credentials
            if (credentials == null) {
                callback.onError(mediaLocalKey, TruvideoSdkException("Credentials not found"))
                return mediaLocalKey
            }

            ioScope.launch {
                uploadService.upload(
                    context = context,
                    file = file,
                    bucketName = credentials.bucketName,
                    region = credentials.region,
                    poolId = credentials.identityPoolID,
                    folder = credentials.bucketFolderMedia,
                    id = mediaLocalKey,
                    callback = callback
                )
            }

            return mediaLocalKey

        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun resume(
        context: Context, mediaLocalKey: String, callback: TruvideoSdkUploadCallback
    ): String {
        try {
            val isAuthenticated = common.auth.isAuthenticated
            if (!isAuthenticated) {
                callback.onError(mediaLocalKey, TruvideoSdkAuthenticationRequiredException())
                return mediaLocalKey
            }

            val credentials = common.auth.settings?.credentials
            if (credentials == null) {
                callback.onError(mediaLocalKey, TruvideoSdkException("Credentials not found"))
                return mediaLocalKey
            }

            ioScope.launch {
                uploadService.resume(
                    context = context,
                    bucketName = credentials.bucketName,
                    region = credentials.region,
                    poolId = credentials.identityPoolID,
                    folder = credentials.bucketFolderMedia,
                    id = mediaLocalKey,
                    callback = callback
                )
            }

            return mediaLocalKey

        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun streamAllUploadRequests(
        context: Context, callback: TruvideoSdkGenericCallback<LiveData<List<MediaEntity>>>
    ) {
        performAuthenticatedAction(callback) {
            ioScope.launch {
                callback.onComplete(
                    uploadService.streamAllUploadRequests(
                        context = context,
                    )
                )
            }
        }
    }

    override fun getAllUploadRequests(
        context: Context, callback: TruvideoSdkGenericCallback<List<MediaEntity>>
    ) {
        performAuthenticatedAction(callback) {
            ioScope.launch {
                callback.onComplete(
                    uploadService.getAllUploadRequests(
                        context = context
                    )
                )
            }
        }
    }

    override fun getAllUploadRequestsByStatus(
        context: Context,
        status: MediaEntityStatus,
        callback: TruvideoSdkGenericCallback<List<MediaEntity>>
    ) {
        performAuthenticatedAction(callback) {
            ioScope.launch {
                callback.onComplete(
                    uploadService.getAllUploadRequestsByStatus(
                        context = context, status = status
                    )
                )
            }
        }
    }

    override fun streamMediaById(
        context: Context, id: String, callback: TruvideoSdkGenericCallback<LiveData<MediaEntity>>
    ) {
        performAuthenticatedAction(callback) {
            ioScope.launch {
                callback.onComplete(
                    uploadService.streamMediaById(
                        context = context, id = id
                    )
                )
            }
        }
    }

    override fun streamAllUploadRequestsByStatus(
        context: Context,
        status: MediaEntityStatus,
        callback: TruvideoSdkGenericCallback<LiveData<List<MediaEntity>>>
    ) {
        performAuthenticatedAction(callback) {
            ioScope.launch {
                callback.onComplete(
                    uploadService.streamAllUploadRequestsByStatus(
                        context = context, status = status
                    )
                )
            }
        }
    }

    override suspend fun cancel(context: Context, id: String) {
        try {
            val isAuthenticated = common.auth.isAuthenticated
            if (!isAuthenticated) {
                throw TruvideoSdkAuthenticationRequiredException()
            }

            val credentials = common.auth.settings?.credentials
                ?: throw TruvideoSdkException("Credentials not found")
            val poolId: String = credentials.identityPoolID
            val region: String = credentials.region

            uploadService.cancel(context = context, region = region, poolId = poolId, id = id)
        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun cancel(
        context: Context, id: String, callback: TruvideoSdkCancelCallback
    ) {
        ioScope.launch {
            try {
                cancel(context, id)
                callback.onReady(id)
            } catch (ex: Exception) {
                ex.printStackTrace()

                if (ex is TruvideoSdkException) {
                    callback.onError(id, ex)
                } else {
                    callback.onError(id, TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override suspend fun pause(context: Context, id: String) {
        try {
            val isAuthenticated = common.auth.isAuthenticated
            if (!isAuthenticated) {
                throw TruvideoSdkAuthenticationRequiredException()
            }

            val credentials = common.auth.settings?.credentials
                ?: throw TruvideoSdkException("Credentials not found")
            val poolId: String = credentials.identityPoolID
            val region: String = credentials.region

            uploadService.pause(context = context, region = region, poolId = poolId, id = id)
        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun pause(
        context: Context, id: String, callback: TruvideoSdkPauseCallback
    ) {
        ioScope.launch {
            try {
                pause(context, id)
                callback.onReady(id)
            } catch (ex: Exception) {
                ex.printStackTrace()

                if (ex is TruvideoSdkException) {
                    callback.onError(id, ex)
                } else {
                    callback.onError(id, TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    private fun <T : TruvideoSdkAuthCallback> performAuthenticatedAction(
        callback: T, action: () -> Unit
    ) {
        try {
            val isAuthenticated = common.auth.isAuthenticated
            if (!isAuthenticated) {
                callback.onAuthError(TruvideoSdkAuthenticationRequiredException())
                return
            }

            val credentials = common.auth.settings?.credentials
            if (credentials == null) {
                callback.onAuthError(TruvideoSdkException("Credentials not found"))
                return
            }
            action.invoke()
        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }
}
