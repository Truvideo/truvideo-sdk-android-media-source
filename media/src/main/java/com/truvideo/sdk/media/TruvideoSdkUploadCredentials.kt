package com.truvideo.sdk.media

import com.google.gson.Gson

class TruvideoSdkUploadCredentials(val region: String, val poolID: String, val bucketName: String) {

    companion object {
        fun createMockedJson(): String {
            val gson = Gson()
            return gson.toJson(
                TruvideoSdkUploadCredentials(
                    region = "us-east-2",
                    poolID = "us-east-2:6198f939-094e-48e9-a9d0-351ecff1ce2f",
                    bucketName = "luis-piura-bucket-test"
                )
            )
        }
    }
}