package com.truvideo.sdk.media.service.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.truvideo.sdk.media.interfaces.TruvideoSdkUploadCallback
import com.truvideo.sdk.media.model.MediaEntity
import com.truvideo.sdk.media.model.MediaEntityStatus
import com.truvideo.sdk.media.repository.MediaRepository
import com.truvideo.sdk.media.service.media.TruvideoSdkMediaService
import com.truvideo.sdk.media.util.FileUriUtil
import io.ktor.util.toUpperCasePreservingASCIIRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdk
import truvideo.sdk.common.exception.TruvideoSdkConnectivityRequiredException
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File
import java.util.UUID
import kotlin.coroutines.suspendCoroutine

internal class TruvideoSdkUploadServiceImpl(
    val mediaService: TruvideoSdkMediaService
) : TruvideoSdkUploadServiceInterface {

    private var ioScope = CoroutineScope(Dispatchers.IO)
    private var mainScope = CoroutineScope(Dispatchers.Main)
    private val common = TruvideoSdk.instance
    private val mediaRepository = MediaRepository()

    override suspend fun upload(
        context: Context,
        bucketName: String,
        region: String,
        poolId: String,
        folder: String,
        id: String,
        file: Uri,
        callback: TruvideoSdkUploadCallback
    ) {
        if (!FileUriUtil.isPhotoOrVideo(context, file)) {
            callback.onError(id, TruvideoSdkException("Invalid file type"))
            return
        }

        // Calculate file name
        val fileName: String
        try {
            val fileExtension = FileUriUtil.getExtension(context, file)
            fileName = "${UUID.randomUUID()}.$fileExtension"
        } catch (ex: Exception) {
            //TODO: remove this to avoid expose internal errors to the final user
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                callback.onError(id, ex)
            } else {
                callback.onError(id, TruvideoSdkException("Invalid uri"))
            }
            return
        }

        val client = getClient(region, poolId)
        val transferUtility = getTransferUtility(context, client)

        val awsPath = if (folder.isNotEmpty()) {
            "${folder}/$fileName"
        } else {
            fileName
        }

        // Generate temp file
        val fileToUpload: File
        try {
            fileToUpload = FileUriUtil.createTempFile(context, file, fileName)
        } catch (ex: Exception) {
            //TODO: remove this to avoid expose internal errors to the final user
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                callback.onError(id, ex)
            } else {
                callback.onError(id, TruvideoSdkException("File not found"))
            }
            return
        }

        val acl = CannedAccessControlList.PublicRead

        val isOnline = common.connectivity.isOnline()
        if (!isOnline) {
            mainScope.launch {
                callback.onError(
                    id, TruvideoSdkConnectivityRequiredException()
                )
            }
            return
        }

        mediaRepository.insertMedia(context, MediaEntity(id, status = MediaEntityStatus.IDLE))

        val transferObserver = transferUtility.upload(
            bucketName, awsPath, fileToUpload, acl
        )

        transferObserver.setTransferListener(object : TransferListener {
            var size = 0L

            override fun onStateChanged(s3Id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    tryDeleteFile(fileToUpload)

                    ioScope.launch {
                        val url = client.getUrl(bucketName, awsPath).toString()
                        val mimeType = FileUriUtil.getMimeType(context, file)
                        val type = mimeType.split("/")[0].toUpperCasePreservingASCIIRules()

                        try {
                            val mediaURL = mediaService.createMedia(
                                title = fileName, url = url, size = size, type = type
                            )

                            mainScope.launch {
                                callback.onComplete(
                                    id, mediaURL
                                )
                            }
                            mediaRepository.updateToCompletedStatus(context, id)
                        } catch (ex: Exception) {
                            mediaRepository.updateToErrorStatus(context, id, ex.message)
                            //TODO: remove this to avoid expose internal errors to the final user
                            ex.printStackTrace()

                            if (ex is TruvideoSdkException) {
                                mainScope.launch {
                                    callback.onError(id, ex)
                                }
                            } else {
                                mainScope.launch {
                                    callback.onError(
                                        id, TruvideoSdkException("Error creating file media")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun onProgressChanged(
                s3Id: Int, bytesCurrent: Long, bytesTotal: Long
            ) {
                size = bytesTotal
                val progress = (bytesCurrent * 100 / bytesTotal).toInt()
                ioScope.launch {
                    mediaRepository.updateProgress(context, id, progress)
                }
                mainScope.launch {
                    callback.onProgressChanged(id, progress)
                }
            }

            override fun onError(s3Id: Int, ex: Exception) {
                ioScope.launch {
                    mediaRepository.updateToErrorStatus(context, id, ex.message)
                }
                tryDeleteFile(fileToUpload)

                //TODO: remove this to avoid expose internal errors to the final user
                ex.printStackTrace()

                mainScope.launch {
                    callback.onError(
                        id, TruvideoSdkException("Error uploading the file")
                    )
                }
            }
        })

        // Store the external media id
        val externalId = transferObserver.id

        val media = mediaRepository.getMediaById(context, id)
        media.externalId = externalId
        media.status = MediaEntityStatus.PROCESSING
        mediaRepository.update(context, media)
    }

    override suspend fun getAllUploadRequests(
        context: Context
    ): List<MediaEntity> {
        return mediaRepository.getAllUploadRequests(context)
    }

    override suspend fun getAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): List<MediaEntity> {
        return mediaRepository.getAllUploadRequestsByStatus(context, status)
    }

    override suspend fun streamMediaById(
        context: Context, id: String
    ): LiveData<MediaEntity> {
        return mediaRepository.streamMediaById(context, id)
    }

    override suspend fun streamAllUploadRequests(context: Context): LiveData<List<MediaEntity>> {
        return mediaRepository.streamAllUploadRequests(context)
    }

    override suspend fun streamAllUploadRequestsByStatus(
        context: Context, status: MediaEntityStatus
    ): LiveData<List<MediaEntity>> {
        return mediaRepository.streamAllUploadRequestsByStatus(context, status)
    }

    override suspend fun cancel(
        context: Context,
        id: String,
        region: String,
        poolId: String,
    ) {
        val s3Id = mediaRepository.getExternalId(context, id) ?: -1
        if (s3Id == -1) {
            throw TruvideoSdkException("Upload request not found")
        }

        val client = getClient(region = region, poolId = poolId)
        val transferUtility = getTransferUtility(context, client)
        transferUtility.cancel(s3Id)
    }

    override suspend fun pause(
        context: Context,
        id: String,
        region: String,
        poolId: String,
    ) {
        val s3Id = mediaRepository.getExternalId(context, id) ?: -1
        if (s3Id == -1) {
            throw TruvideoSdkException("Upload request not found")
        }

        val client = getClient(region = region, poolId = poolId)
        val transferUtility = getTransferUtility(context, client)
        transferUtility.pause(s3Id)
    }

    override suspend fun resume(
        context: Context,
        id: String,
        region: String,
        poolId: String,
    ) {
        val s3Id = mediaRepository.getExternalId(context, id) ?: -1
        if (s3Id == -1) {
            throw TruvideoSdkException("Upload request not found")
        }

        val client = getClient(region = region, poolId = poolId)
        val transferUtility = getTransferUtility(context, client)
        transferUtility.resume(s3Id)
    }

    override suspend fun getState(
        context: Context, region: String, poolId: String, id: String
    ): String {
        val client = getClient(region, poolId)
        val transferUtility = getTransferUtility(context, client)

        val s3Id = mediaRepository.getExternalId(context, id) ?: -1
        if (s3Id == -1) {
            throw TruvideoSdkException("Upload request not found")
        }

        val transfer = transferUtility.getTransferById(s3Id)
            ?: throw TruvideoSdkException("Upload request not found")
        return transfer.state.name
    }

    private fun tryDeleteFile(file: File) {
        try {
            file.delete()
        } catch (ex: Exception) {
            //TODO: remove this to avoid expose internal errors to the final user
            ex.printStackTrace()
        }
    }

    private suspend fun getClient(
        region: String,
        poolId: String,
    ): AmazonS3Client = suspendCoroutine {
        val parsedRegion = Regions.fromName(region)
        val clientConfiguration = ClientConfiguration()
        clientConfiguration.maxErrorRetry = 0
        clientConfiguration.socketTimeout = 10 * 60 * 1000
        val credentialsProvider = CognitoCredentialsProvider(poolId, parsedRegion)
        val client = AmazonS3Client(
            credentialsProvider, Region.getRegion(parsedRegion), clientConfiguration
        )

        //TODO: check accelerate
        val accelerate = false
        client.setS3ClientOptions(
            S3ClientOptions.builder().setAccelerateModeEnabled(accelerate).build()
        )

        it.resumeWith(Result.success(client))
    }

    private suspend fun getTransferUtility(
        context: Context, client: AmazonS3Client
    ): TransferUtility = suspendCoroutine {
        TransferNetworkLossHandler.getInstance(context)
        val awsConfiguration = AWSMobileClient.getInstance().configuration
        val transferUtility = TransferUtility.builder().context(context).s3Client(client)
            .awsConfiguration(awsConfiguration).build()

        it.resumeWith(Result.success(transferUtility))
    }

}