package com.truvideo.sdk.media.usecases

import android.content.Context
import android.net.Uri
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.truvideo.sdk.media.interfaces.FileUploadCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import com.truvideo.sdk.media.util.FileUriUtil
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File
import java.util.UUID

internal class UploadFileUseCase(
    private val context: Context,
    private val s3ClientUseCase: S3ClientUseCase
) {

    suspend operator fun invoke(
        filePath: String,
        bucketName: String,
        region: String,
        poolId: String,
        folder: String,
        callback: FileUploadCallback
    ): Int {
        val file = File(filePath)
        if (!file.exists()) {
            throw TruvideoSdkException("File not found")
        }

        val uri = Uri.fromFile(File(filePath))
        if (!FileUriUtil.isPhotoOrVideo(context, uri)) {
            throw TruvideoSdkException("Invalid file type")
        }

        // Calculate file name
        val fileName: String
        try {
            val fileExtension = FileUriUtil.getExtension(context, uri)
            fileName = "${UUID.randomUUID()}.$fileExtension"
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw TruvideoSdkException("Invalid file path")
        }

        val client = s3ClientUseCase.getClient(region, poolId)
        val transferUtility = s3ClientUseCase.getTransferUtility(client)

        val awsPath = if (folder.isNotEmpty()) {
            "${folder}/$fileName"
        } else {
            fileName
        }

        val transferObserver = transferUtility.upload(
            bucketName,
            awsPath,
            file,
            CannedAccessControlList.PublicRead
        )

        transferObserver.setTransferListener(
            object : TransferListener {
                var size = 0L

                override fun onStateChanged(s3Id: Int, state: TransferState) {
                    val truvideoState = when (state) {
                        TransferState.WAITING -> TruvideoSdkMediaFileUploadStatus.IDLE
                        TransferState.IN_PROGRESS -> TruvideoSdkMediaFileUploadStatus.UPLOADING
                        TransferState.PAUSED -> TruvideoSdkMediaFileUploadStatus.PAUSED
                        TransferState.RESUMED_WAITING -> TruvideoSdkMediaFileUploadStatus.UPLOADING
                        TransferState.COMPLETED -> TruvideoSdkMediaFileUploadStatus.COMPLETED
                        TransferState.CANCELED -> TruvideoSdkMediaFileUploadStatus.CANCELED
                        TransferState.FAILED -> TruvideoSdkMediaFileUploadStatus.ERROR
                        TransferState.WAITING_FOR_NETWORK -> TruvideoSdkMediaFileUploadStatus.UPLOADING
                        TransferState.PENDING_NETWORK_DISCONNECT -> TruvideoSdkMediaFileUploadStatus.UPLOADING
                        TransferState.UNKNOWN -> TruvideoSdkMediaFileUploadStatus.IDLE
                        TransferState.PART_COMPLETED -> null
                        TransferState.PENDING_CANCEL -> null
                        TransferState.PENDING_PAUSE -> null
                    }

                    if (truvideoState != null) {
                        if (state == TransferState.COMPLETED) {
                            val url = client.getUrl(bucketName, awsPath).toString()
                            callback.onComplete(s3Id, url)
                        } else {
                            callback.onStateChanged(s3Id, truvideoState)
                        }
                    }
                }

                override fun onProgressChanged(
                    s3Id: Int, bytesCurrent: Long, bytesTotal: Long
                ) {
                    size = bytesTotal
                    val progress = bytesCurrent.toFloat() / bytesTotal
                    callback.onProgressChanged(s3Id, progress)
                }

                override fun onError(s3Id: Int, ex: Exception) {
                    callback.onStateChanged(
                        s3Id,
                        TruvideoSdkMediaFileUploadStatus.ERROR,
                        TruvideoSdkException(ex.localizedMessage ?: "Unknown error")
                    )
                }
            }
        )

        return transferObserver.id
    }


    suspend fun cancel(
        region: String,
        poolId: String,
        id: Int
    ) {
        val client = s3ClientUseCase.getClient(
            region = region,
            poolId = poolId
        )
        val transferUtility = s3ClientUseCase.getTransferUtility(client)
        transferUtility.deleteTransferRecord(id)
    }


    suspend fun pause(
        region: String,
        poolId: String,
        id: Int
    ) {
        val client = s3ClientUseCase.getClient(
            region = region,
            poolId = poolId
        )
        val transferUtility = s3ClientUseCase.getTransferUtility(client)
        transferUtility.pause(id)
    }

    suspend fun resume(
        region: String,
        poolId: String,
        id: Int
    ) {
        val client = s3ClientUseCase.getClient(
            region = region,
            poolId = poolId
        )
        val transferUtility = s3ClientUseCase.getTransferUtility(client)
        transferUtility.resume(id)
    }
}