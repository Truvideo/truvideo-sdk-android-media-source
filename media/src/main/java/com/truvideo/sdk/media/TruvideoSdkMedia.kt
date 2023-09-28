@file:Suppress("unused")

package com.truvideo.sdk.media

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import io.ktor.util.toUpperCasePreservingASCIIRules
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import truvideo.sdk.common.TruvideoSdk
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.model.TruvideoSdkStorageCredentials
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


object TruvideoSdkMedia {
    fun upload(
        context: Context, listener: TruvideoSdkTransferListener, fileUri: Uri
    ): String {
        val isAuthenticated = TruvideoSdk.instance.auth.isAuthenticated
        if (!isAuthenticated) {
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val mediaLocalKey = UUID.randomUUID().toString()

        TruvideoSdk.instance.auth.settings?.mediaStorageCredentials?.let {
            uploadVideo(context, it, listener, mediaLocalKey, fileUri)
        } ?: run {
            // TODO exception
            listener.onError(mediaLocalKey, java.lang.Exception("to be defined 1"))
        }

        return mediaLocalKey
    }

    //TODO review this coroutine
    @OptIn(DelicateCoroutinesApi::class)
    private fun uploadVideo(
        context: Context,
        credentials: TruvideoSdkStorageCredentials,
        listener: TruvideoSdkTransferListener,
        mediaLocalKey: String,
        fileUri: Uri
    ) {
        var mediaLocalId = -1

        val bucketName: String = credentials.bucketName
        val poolID: String = credentials.poolId
        val region: String = credentials.region
        val accelerate: Boolean = credentials.accelerate

        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val cursor = context.contentResolver.query(fileUri, projection, null, null, null)

        var fileExtension = "mp4"
        if (cursor != null && cursor.moveToFirst()) {
            var columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (columnIndex < 0) {
                columnIndex = 0
            }
            val displayName = cursor.getString(columnIndex)
            cursor.close()

            displayName?.let {
                val lastDot = it.lastIndexOf(".")
                if (lastDot != -1) {
                    fileExtension = it.substring(lastDot + 1)
                }
            }
        }

        val fileName = "${UUID.randomUUID()}.$fileExtension"

        val client = getClient(context, region, poolID, accelerate)
        val transferUtility: TransferUtility =
            getTransferUtility(context, client, region, poolID, accelerate)

        val awsPath = if (credentials.folder.trim().isNotEmpty()) {
            "${credentials.folder.trim()}/$fileName"
        } else {
            fileName
        }

        val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)

        inputStream?.let {
            val fileToUpload = File(
                context.cacheDir, fileName
            )

            val outputStream: OutputStream = FileOutputStream(fileToUpload)

            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()

            val transferObserver: TransferObserver = transferUtility.upload(
                bucketName, awsPath, fileToUpload
            )

            transferObserver.setTransferListener(object : TransferListener {
                var size = 0L

                fun getMimeType(uri: Uri, context: Context): String {
                    val contentResolver = context.contentResolver
                    return contentResolver.getType(uri) ?: "unknown/unknown"
                }

                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state == TransferState.COMPLETED) {
                        GlobalScope.launch(Dispatchers.IO) {
                            val url = client.getUrl(bucketName, awsPath).toString()
                            listener.onComplete(mediaLocalKey, url)

                            //TODO createMedia
//                            val type = getMimeType(
//                                fileUri, context
//                            ).split("/")[0].toUpperCasePreservingASCIIRules()
//
//                            createMedia(listener, mediaLocalKey, fileName, url, size, type)
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
                    listener.onError(mediaLocalKey, ex)
                }
            })
            mediaLocalId = transferObserver.id
        }

        storeMediaLocalValues(context, mediaLocalKey, mediaLocalId)
    }

    private suspend fun createMedia(
        listener: TruvideoSdkTransferListener,
        mediaLocalKey: String,
        title: String,
        url: String,
        size: Long,
        type: String?
    ) {
        val token = TruvideoSdk.instance.auth.authentication?.accessToken
        //TODO return exception
        if (token.isNullOrEmpty()) {
            listener.onError(
                mediaLocalKey, java.lang.Exception("to be defined 2")
            )
            return
        }
        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json",
        )
        val body = mapOf(
            "title" to title,
            "type" to type,
            "url" to url,
            "resolution" to "LOW",
            "size" to size,
        )
        val response = withContext(Dispatchers.IO) {
            TruvideoSdk.instance.http.post(
                url = "https://sdk-mobile-api-beta.truvideo.com:443/api/media",
                headers = headers,
                body = body,
                retry = false,
                printLogs = false
            )
        }

        if (response?.isSuccess == true) {
            response.body.let {
                listener.onComplete(mediaLocalKey, url)
            }
        } else {
            //TODO add exception
            listener.onError(
                mediaLocalKey,
                java.lang.Exception("to be defined 3 | code: ${response?.code} | body: ${response?.body}")
            )
        }
    }

    private fun storeMediaLocalValues(context: Context, key: String, value: Int) {
        val sharedPreferences =
            context.getSharedPreferences("MediaLocalValues", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    private fun getClient(
        context: Context, region: String, poolID: String, accelerate: Boolean
    ): AmazonS3Client {
        val parsedRegion = Regions.fromName(region)
        val clientConfiguration = ClientConfiguration()
        clientConfiguration.maxErrorRetry = 1
        clientConfiguration.socketTimeout = 10 * 60 * 1000
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context, poolID, parsedRegion
        )
        val client = AmazonS3Client(
            credentialsProvider, Region.getRegion(parsedRegion), clientConfiguration
        )
        client.setS3ClientOptions(
            S3ClientOptions.builder().setAccelerateModeEnabled(accelerate).build()
        )
        return client
    }

    private fun getTransferUtility(
        context: Context,
        client: AmazonS3Client,
        region: String,
        poolID: String,
        accelerate: Boolean
    ): TransferUtility {
        TransferNetworkLossHandler.getInstance(context)
        val awsConfiguration = AWSMobileClient.getInstance().configuration
        return TransferUtility.builder().context(context).s3Client(client)
            .awsConfiguration(awsConfiguration).build()
    }
}
