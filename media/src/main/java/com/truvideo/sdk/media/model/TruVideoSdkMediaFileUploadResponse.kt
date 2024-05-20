package com.truvideo.sdk.media.model


data class TruVideoSdkMediaFileUploadResponse(
    val id: String = "",
    var url: String = "",
    var transcriptionUrl: String? = null,
    var transcriptionLength: Float? = null,
    var tags: Map<String, String> = mapOf(),
    var metadata: String = ""
)