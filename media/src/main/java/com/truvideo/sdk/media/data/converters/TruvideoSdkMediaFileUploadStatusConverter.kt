package com.truvideo.sdk.media.data.converters

import androidx.room.TypeConverter
import com.truvideo.sdk.media.model.TruvideoSdkMediaFileUploadStatus

internal class TruvideoSdkMediaFileUploadStatusConverter {
    @TypeConverter
    fun fromStatus(status: TruvideoSdkMediaFileUploadStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(status: String): TruvideoSdkMediaFileUploadStatus {
        return enumValueOf<TruvideoSdkMediaFileUploadStatus>(status)
    }
}