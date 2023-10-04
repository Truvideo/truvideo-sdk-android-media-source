@file:Suppress("unused")

package com.truvideo.sdk.media

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import truvideo.sdk.common.TruvideoSdk
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.model.TruvideoSdkStorageCredentials
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


object TruvideoSdkMedia {

    var scope = CoroutineScope(Dispatchers.IO)

    fun upload(
        context: Context, listener: TruvideoSdkTransferListener, fileUri: Uri
    ): String {
        val isAuthenticated = TruvideoSdk.instance.auth.isAuthenticated
        if (!isAuthenticated) {
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val mediaLocalKey = UUID.randomUUID().toString()

        TruvideoSdk.instance.auth.settings?.credentials?.let {
            uploadVideo(context, it, listener, mediaLocalKey, fileUri)
        } ?: run {
            listener.onError(mediaLocalKey, TruvideoSdkException("Credentials not found"))
        }

        return mediaLocalKey
    }

    private fun getMimeType(uri: Uri, contentResolver: ContentResolver): String {
        return contentResolver.getType(uri) ?: "unknown/unknown"
    }

    private fun isPhotoOrVideo(uri: Uri, contentResolver: ContentResolver): Boolean {
        // Get the MIME type of the file from the URI
        val mimeType = getMimeType(uri, contentResolver)

        // Check if the file extension is one of the common image or video extensions
        val isImage = mimeType.startsWith("image/")
        val isVideo = mimeType.startsWith("video/")

        // Return true if it's an image or video, false otherwise
        return isImage || isVideo
    }

    private fun uploadVideo(
        context: Context,
        credentials: TruvideoSdkStorageCredentials,
        listener: TruvideoSdkTransferListener,
        mediaLocalKey: String,
        fileUri: Uri
    ) {

        val contentResolver = context.contentResolver

        if (!isPhotoOrVideo(fileUri, contentResolver)) {
            listener.onError(mediaLocalKey, TruvideoSdkException("Invalid file"))
            return
        }

        var mediaLocalId = -1

        val bucketName: String = credentials.bucketName
        val poolID: String = credentials.identityID
        val region: String = credentials.region
        //TODO
        // val accelerate: Boolean = credentials.accelerate
        val accelerate: Boolean = false
        val folder: String = credentials.bucketFolderMedia

        Log.d("uploadVideo", "uploadVideo: credentials: $credentials")
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

        val cursor = contentResolver.query(fileUri, projection, null, null, null)

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
        val transferUtility: TransferUtility = getTransferUtility(context, client)

        val awsPath = if (folder.isNotEmpty()) {
            "${folder}/$fileName"
        } else {
            fileName
        }

        val inputStream: InputStream? = contentResolver.openInputStream(fileUri)

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

                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state == TransferState.COMPLETED) {
                        scope.launch {
                            val url = client.getUrl(bucketName, awsPath).toString()

                            val type = getMimeType(
                                fileUri, contentResolver
                            ).split("/")[0].toUpperCasePreservingASCIIRules()

                            createMedia(listener, mediaLocalKey, fileName, url, size, type)
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
        } ?: run {
            listener.onError(mediaLocalKey, TruvideoSdkException("File Not Found"))
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
        TruvideoSdk.instance.configuration.log.enabled = true
        TruvideoSdk.instance.configuration.log.printEnabled = true

        var token = TruvideoSdk.instance.auth.authentication?.accessToken
        if (token.isNullOrEmpty()) {
            TruvideoSdk.instance.auth.refreshAuthentication()
        }
        token = TruvideoSdk.instance.auth.authentication?.accessToken
        if (token.isNullOrEmpty()) {
            listener.onError(
                mediaLocalKey, TruvideoSdkAuthenticationRequiredException()
            )
            return
        }
        val headers = mapOf(
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json",
        )
        val body = JSONObject()
        body.apply {
            put("title", title)
            put("type", type)
            put("url", url)
            put("resolution", "LOW")
            put("size", size)
        }

        Log.d("TruvideoSdkMedia", "createMedia - headers: $headers")
        Log.d("TruvideoSdkMedia", "createMedia - body: $body")
        val response = TruvideoSdk.instance.http.post(
            url = "https://sdk-mobile-api-beta.truvideo.com:443/api/media",
            headers = headers,
            body = body.toString(),
            retry = true,
            printLogs = true
        )

        Log.d("TruvideoSdkMedia", "createMedia - response: $response")

        if (response?.isSuccess == true) {
            response.body.let {
                Log.d("body", "createMedia: $it")
                // TODO get url and return it
                listener.onComplete(mediaLocalKey, url)
            }
        } else {
            Log.w(
                "TruvideoSdkMedia",
                "to be defined 3 | code: ${response?.code} | body: ${response?.body}"
            )
            listener.onError(
                mediaLocalKey, TruvideoSdkException("Error creating media")
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
        context: Context, client: AmazonS3Client
    ): TransferUtility {
        TransferNetworkLossHandler.getInstance(context)
        val awsConfiguration = AWSMobileClient.getInstance().configuration
        return TransferUtility.builder().context(context).s3Client(client)
            .awsConfiguration(awsConfiguration).build()
    }
}
