package com.truvideo.sdk.media.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.truvideo.sdk.media.data.DatabaseInstance
import com.truvideo.sdk.media.data.FileUploadRequestDAO
import com.truvideo.sdk.media.data.converters.MetadataConverter
import com.truvideo.sdk.media.exception.TruvideoSdkMediaException
import com.truvideo.sdk.media.model.TruVideoSdkMediaFileUploadResponse
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileType
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadRequest
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus
import com.truvideo.sdk.media.model.TruvideoSdkMediaResponse
import com.truvideo.sdk.media.model.TruvideoSdkMediaPaginatedResponse
import com.truvideo.sdk.media.service.media.TruvideoSdkMediaServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.coroutines.suspendCoroutine

internal class TruvideoSdkMediaFetchRequestRepositoryImpl(
    private val mediaService: TruvideoSdkMediaServiceImpl
) : TruvideoSdkMediaFetchRequestRepository {

    override suspend fun fetchAll(
        tags: Map<String, String>?,
        idList: List<String>?,
        type: TruvideoSdkMediaFileType?,
        pageNumber: Int?,
        size: Int?
    ): TruvideoSdkMediaPaginatedResponse<TruvideoSdkMediaResponse> {
        return mediaService.fetchAll(tags, idList, type, pageNumber, size)
    }
}
