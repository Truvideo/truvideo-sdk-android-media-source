package com.truvideo.sdk.media.repository

import com.truvideo.sdk.media.model.TruvideoSdkMediaFileType
import com.truvideo.sdk.media.model.TruvideoSdkMediaResponse
import com.truvideo.sdk.media.model.TruvideoSdkMediaPaginatedResponse

interface TruvideoSdkMediaFetchRequestRepository {
    suspend fun fetchAll(
        tags: Map<String, String>?,
        idList: List<String>?,
        type: TruvideoSdkMediaFileType?,
        pageNumber: Int?,
        size: Int?
    ): TruvideoSdkMediaPaginatedResponse<TruvideoSdkMediaResponse>
}