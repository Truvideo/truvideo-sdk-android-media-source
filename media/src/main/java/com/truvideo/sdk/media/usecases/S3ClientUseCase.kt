package com.truvideo.sdk.media.usecases

import android.content.Context
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

internal class S3ClientUseCase(
    private val context: Context
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun getClient(
        region: String,
        poolId: String,
    ): AmazonS3Client {
        val parsedRegion = Regions.fromName(region)
        val clientConfiguration = ClientConfiguration()
        clientConfiguration.maxErrorRetry = 0
        clientConfiguration.socketTimeout = 10 * 60 * 1000
        val credentialsProvider = CognitoCredentialsProvider(poolId, parsedRegion)
        val client = AmazonS3Client(
            credentialsProvider,
            Region.getRegion(parsedRegion),
            clientConfiguration
        )

        //TODO: check accelerate
        val accelerate = false
        val options = S3ClientOptions.builder()
            .setAccelerateModeEnabled(accelerate)
            .build()
        client.setS3ClientOptions(options)
        return client
    }

    fun getTransferUtility(client: AmazonS3Client): TransferUtility {
        TransferNetworkLossHandler.getInstance(context)
        val awsConfiguration = AWSMobileClient.getInstance().configuration
        return TransferUtility.builder()
            .context(context)
            .s3Client(client)
            .awsConfiguration(awsConfiguration)
            .build()
    }
}