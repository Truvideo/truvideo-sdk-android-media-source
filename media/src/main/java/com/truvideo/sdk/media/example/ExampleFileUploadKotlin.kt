package com.truvideo.sdk.media.example

import com.truvideo.sdk.media.TruvideoSdkMedia
import com.truvideo.sdk.media.interfaces.TruvideoSdkMediaFileUploadCallback
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import truvideo.sdk.common.exception.TruvideoSdkException

private class TestKotlin {
    suspend fun uploadFile(filePath: String) {
        val builder = TruvideoSdkMedia.FileUploadRequestBuilder(filePath)
        builder.addTag("key", "value")
        builder.addTag("color", "red")
        builder.addTag("order-number", "123")

        val request = builder.build()
        request.upload(object : TruvideoSdkMediaFileUploadCallback {
            override fun onComplete(id: String, response: TruvideoSdkMediaFileUploadRequest) {
                // Handle completion
                val url = response.url
//                val transcriptionURL = response.transcriptionURL
                val tags = response.tags
            }

            override fun onProgressChanged(id: String, progress: Float) {
                // Handle progress
            }

            override fun onError(id: String, ex: TruvideoSdkException) {
                // Handle error
            }
        })
    }
}