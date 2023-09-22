@file:Suppress("unused")

package com.truvideo.sdk.media

import android.content.Context
import android.net.Uri
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
import com.amazonaws.services.s3.model.ObjectMetadata
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import truvideo.sdk.common.TruvideoSdk
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


object TruvideoSdkMedia {
    fun upload(
        context: Context, listener: TruvideoSdkTransferListener, fileUri: Uri, accelerate: Boolean
    ): String {
        val isAuthenticated = TruvideoSdk.instance.auth.isAuthenticated
        if (!isAuthenticated) {
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val mediaLocalKey = UUID.randomUUID().toString()

        val call: Call<TruvideoSdkUploadCredentials?>? =
            RetrofitClient.getApiService().getCredentials()
        call?.enqueue(object : Callback<TruvideoSdkUploadCredentials?> {
            override fun onResponse(
                call: Call<TruvideoSdkUploadCredentials?>,
                response: Response<TruvideoSdkUploadCredentials?>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        uploadVideo(it)
                    }
                } else {
                    listener.onError(mediaLocalKey, java.lang.Exception("to be defined"))
                }
            }

            override fun onFailure(call: Call<TruvideoSdkUploadCredentials?>, t: Throwable) {
                listener.onError(mediaLocalKey, java.lang.Exception("to be defined"))
            }

            private fun uploadVideo(credentials: TruvideoSdkUploadCredentials) {
                var mediaLocalId = -1

                val bucketName: String = credentials.bucketName
                val poolID: String = credentials.poolID
                val region: String = credentials.region

                // TODO remove hardcoded values
                val folder: String = "nico"
                val fileName: String = "fileName2"
                val contentType: String = "contentType"
                // TODO remove hardcoded values

                val transferUtility: TransferUtility =
                    getTransferUtility(context, region, poolID, accelerate)

                var awsPath = fileName
                if (folder != null && folder != "") {
                    awsPath = "$folder/$fileName"
                }
                val objectMetadata = ObjectMetadata()
                objectMetadata.contentType = contentType

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
                        bucketName, awsPath, fileToUpload, objectMetadata
                    )

                    transferObserver.setTransferListener(object : TransferListener {
                        override fun onStateChanged(id: Int, state: TransferState) {
                            if (state == TransferState.COMPLETED) {
                                listener.onComplete(mediaLocalKey)
                            }
                        }

                        override fun onProgressChanged(
                            id: Int, bytesCurrent: Long, bytesTotal: Long
                        ) {
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

        })

        return mediaLocalKey.toString()
    }

    private fun storeMediaLocalValues(context: Context, key: String, value: Int) {
        val sharedPreferences =
            context.getSharedPreferences("MediaLocalValues", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    private fun getTransferUtility(
        context: Context, region: String, poolID: String, accelerate: Boolean
    ): TransferUtility {
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
        TransferNetworkLossHandler.getInstance(context)
        val awsConfiguration = AWSMobileClient.getInstance().configuration
        return TransferUtility.builder().context(context).s3Client(client)
            .awsConfiguration(awsConfiguration).build()
    }
}
