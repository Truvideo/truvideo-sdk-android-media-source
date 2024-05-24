package com.truvideo.sdk.media.example

import com.truvideo.sdk.media.TruvideoSdkMedia
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest

private class TestKotlin {
    suspend fun uploadFile(filePath: String) {
        // Create a file upload request builder
        val builder = TruvideoSdkMedia.FileUploadRequestBuilder(filePath)
        builder.addTag("key", "value")
        builder.addTag("color", "red")
        builder.addTag("order-number", "123")

        // Metadata
        builder.setMetadata(
            mapOf<String, Any?>(
                "key" to "value",
                "key1" to 1,
                "nested" to mapOf<String, Any?>(
                    "key2" to 2
                )
            )
        )

        // Build the request
        val request = builder.build()

        // Upload the file
        request.upload(object : TruvideoSdkMediaFileUploadCallback {
            override fun onComplete(id: String, response: TruvideoSdkMediaFileUploadRequest) {
                // Handle completion
                val remoteId = response.remoteId
                val remoteUrl = response.remoteUrl
                val transcriptionURL = response.transcriptionUrl
                val tags = response.tags
                val metadata = response.metadata
            }

            override fun onProgressChanged(id: String, progress: Float) {
                // Handle progress
            }

            override fun onError(id: String, ex: TruvideoSdkMediaException) {
                // Handle error
            }
        })
    }
}