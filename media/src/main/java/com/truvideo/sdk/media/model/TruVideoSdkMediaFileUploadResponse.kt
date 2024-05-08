package com.truvideo.sdk.media.model

import kotlinx.serialization.Serializable

@Serializable
data class TruVideoSdkMediaFileUploadResponse(
    val id: String = "",
    var url: String = "",
    var transcriptionUrl: String? = null,
    var transcriptionLength: String? = null,
    var tags: Map<String, String> = mapOf()
)