package com.truvideo.sdk.media.model

class TruvideoSdkMediaPaginatedResponse<T>(
    val data: List<T>,
    val totalPages: Int,
    val totalElements: Int,
    val numberOfElements: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val empty: Boolean,
    val last: Boolean
)
