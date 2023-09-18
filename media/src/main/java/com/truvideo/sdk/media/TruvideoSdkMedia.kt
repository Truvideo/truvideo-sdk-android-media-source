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
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.ObjectMetadata
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


object TruvideoSdkMedia {
    fun upload(
        context: Context, listener: TransferListener, fileUri: Uri, accelerate: Boolean
    ): Int {
        // TODO remove hardcoded values
        val bucketName: String = "luis-piura-bucket-test"
        val folder: String = "nico"
        val fileName: String = "fileName2"
        val poolID: String = "us-east-2:6198f939-094e-48e9-a9d0-351ecff1ce2f"
        val region: String = "us-east-2"
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

            transferObserver.setTransferListener(listener)
            return transferObserver.id
        }
        return -1
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
