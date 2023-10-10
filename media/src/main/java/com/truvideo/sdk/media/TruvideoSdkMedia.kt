@file:Suppress("unused")

package com.truvideo.sdk.media

import android.content.Context
import android.net.Uri
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
import com.truvideo.sdk.media.service.media.TruvideoSdkMediaService
import com.truvideo.sdk.media.service.media.TruvideoSdkMediaServiceInterface
import com.truvideo.sdk.media.util.FileUriUtil
import io.ktor.util.toUpperCasePreservingASCIIRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdk
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exception.TruvideoSdkConnectivityRequiredException
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.model.TruvideoSdkStorageCredentials
import java.io.File
import java.util.UUID

object TruvideoSdkMedia: TruvideoSdkMediaInterface {

    private val mediaService: TruvideoSdkMediaServiceInterface = TruvideoSdkMediaService()

    private val common = TruvideoSdk.instance

    private var scope = CoroutineScope(Dispatchers.IO)

    override fun upload(
        context: Context,
        listener: TruvideoSdkTransferListener,
        fileUri: Uri
    ): String {
        val mediaLocalKey = UUID.randomUUID().toString()

        val isAuthenticated = common.auth.isAuthenticated
        if (!isAuthenticated) {
            listener.onError(mediaLocalKey, TruvideoSdkAuthenticationRequiredException())
            return mediaLocalKey
        }

        val credentials = common.auth.settings?.credentials
        if (credentials == null) {
            listener.onError(mediaLocalKey, TruvideoSdkException("Credentials not found"))
            return mediaLocalKey
        }

        // TODO: check common settings to check if i can upload a file

        uploadVideo(context, credentials, listener, mediaLocalKey, fileUri)
        return mediaLocalKey
    }

    override fun cancel(context: Context, key: String) {
        val isAuthenticated = common.auth.isAuthenticated
        if (!isAuthenticated) {
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val credentials = common.auth.settings?.credentials ?: throw TruvideoSdkException("Credentials not found")

        val poolID: String = credentials.identityPoolID
        val region: String = credentials.region
        val client = getClient(region, poolID)

        val id = common.localStorage.readInt("media-id-$key", -1)

        if (id < 0) {
            throw TruvideoSdkException("Invalid key")
        }

        val transferUtility = getTransferUtility(context, client)
        transferUtility.cancel(id)
    }

    private fun uploadVideo(
        context: Context,
        credentials: TruvideoSdkStorageCredentials,
        listener: TruvideoSdkTransferListener,
        mediaLocalKey: String,
        fileUri: Uri
    ) {
        if (!FileUriUtil.isPhotoOrVideo(context, fileUri)) {
            listener.onError(mediaLocalKey, TruvideoSdkException("Invalid file type"))
            return
        }

        val bucketName: String = credentials.bucketName
        val poolID: String = credentials.identityPoolID
        val region: String = credentials.region
        val folder: String = credentials.bucketFolderMedia

        // Calculate file name
        val fileName: String
        try {
            val fileExtension = FileUriUtil.getExtension(context, fileUri)
            fileName = "${UUID.randomUUID()}.$fileExtension"
        } catch (ex: Exception) {
            //TODO: remove this to avoid expose internal errors to the final user
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                listener.onError(mediaLocalKey, ex)
            } else {
                listener.onError(mediaLocalKey, TruvideoSdkException("Invalid uri"))
            }
            return
        }

        val client = getClient(region, poolID)
        val transferUtility = getTransferUtility(context, client)

        val awsPath = if (folder.isNotEmpty()) {
            "${folder}/$fileName"
        } else {
            fileName
        }

        // Generate temp file
        val fileToUpload: File
        try {
            fileToUpload = FileUriUtil.createTempFile(context, fileUri, fileName)
        } catch (ex: Exception) {
            //TODO: remove this to avoid expose internal errors to the final user
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                listener.onError(mediaLocalKey, ex)
            } else {
                listener.onError(mediaLocalKey, TruvideoSdkException("File not found"))
            }
            return
        }

        val acl = CannedAccessControlList.PublicRead

        scope.launch {
            val isOnline = common.connectivity.isOnline()
            if (isOnline) {
                val transferObserver = transferUtility.upload(
                    bucketName,
                    awsPath,
                    fileToUpload,
                    acl
                )
                transferObserver.setTransferListener(object : TransferListener {
                    var size = 0L

                    override fun onStateChanged(id: Int, state: TransferState) {
                        if (state == TransferState.COMPLETED) {
                            tryDeleteFile(fileToUpload)

                            scope.launch {
                                val url = client.getUrl(bucketName, awsPath).toString()
                                val mimeType = FileUriUtil.getMimeType(context, fileUri)
                                val type = mimeType.split("/")[0].toUpperCasePreservingASCIIRules()

                                try {
                                    val result = mediaService.createMedia(
                                        title = fileName, url = url, size = size, type = type
                                    )
                                    listener.onComplete(mediaLocalKey, result)
                                } catch (ex: Exception) {
                                    //TODO: remove this to avoid expose internal errors to the final user
                                    ex.printStackTrace()

                                    if (ex is TruvideoSdkException) {
                                        listener.onError(mediaLocalKey, ex)
                                    } else {
                                        listener.onError(
                                            mediaLocalKey,
                                            TruvideoSdkException("Error creating file media")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    override fun onProgressChanged(
                        id: Int, bytesCurrent: Long, bytesTotal: Long
                    ) {
                        size = bytesTotal
                        val progress = bytesCurrent * 100 / bytesTotal
                        listener.onProgressChanged(mediaLocalKey, progress.toInt())
                    }

                    override fun onError(id: Int, ex: Exception) {
                        tryDeleteFile(fileToUpload)

                        //TODO: remove this to avoid expose internal errors to the final user
                        ex.printStackTrace()

                        listener.onError(
                            mediaLocalKey, TruvideoSdkException("Error uploading the file")
                        )
                    }
                })

                // Store the local media id
                val mediaLocalId = transferObserver.id
                common.localStorage.storeInt("media-id-$mediaLocalKey", mediaLocalId)
            } else {
                listener.onError(mediaLocalKey, TruvideoSdkConnectivityRequiredException())
            }
        }
    }

    private fun tryDeleteFile(file: File) {
        try {
            file.delete()
        } catch (ex: Exception) {
            //TODO: remove this to avoid expose internal errors to the final user
            ex.printStackTrace()
        }
    }

    private fun getClient(
        region: String,
        poolID: String,
    ): AmazonS3Client {
        val parsedRegion = Regions.fromName(region)
        val clientConfiguration = ClientConfiguration()
        clientConfiguration.maxErrorRetry = 0
        clientConfiguration.socketTimeout = 10 * 60 * 1000
        val credentialsProvider = CognitoCredentialsProvider(poolID, parsedRegion)
        val client = AmazonS3Client(
            credentialsProvider, Region.getRegion(parsedRegion), clientConfiguration
        )

        //TODO: check accelerate
        val accelerate = false
        client.setS3ClientOptions(
            S3ClientOptions.builder().setAccelerateModeEnabled(accelerate).build()
        )
        return client
    }

    private fun getTransferUtility(
        context: Context, client: AmazonS3Client
    ): TransferUtility {
        TransferNetworkLossHandler.getInstance(context)
        val awsConfiguration = AWSMobileClient.getInstance().configuration
        return TransferUtility.builder().context(context).s3Client(client)
            .awsConfiguration(awsConfiguration).build()
    }
}
