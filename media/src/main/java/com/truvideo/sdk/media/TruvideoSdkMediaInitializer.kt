package com.truvideo.sdk.media

import android.content.Context
import androidx.startup.Initializer
import com.truvideo.sdk.media.adapter.AuthAdapterImpl
import com.truvideo.sdk.media.adapter.VersionPropertiesAdapter
import com.truvideo.sdk.media.engines.TruvideoSdkMediaFileUploadEngine
import com.truvideo.sdk.media.repository.TruvideoSdkMediaFileUploadRequestRepositoryImpl
import com.truvideo.sdk.media.service.media.TruvideoSdkMediaServiceImpl
import com.truvideo.sdk.media.usecases.S3ClientUseCase
import com.truvideo.sdk.media.usecases.UploadFileUseCase

@Suppress("unused")
class TruvideoSdkMediaInitializer : Initializer<Unit> {

    companion object {
        fun init(context: Context) {
            val versionPropertiesAdapter = VersionPropertiesAdapter(context)
            val authAdapter = AuthAdapterImpl(versionPropertiesAdapter)
            val mediaFileUploadRequestRepository = TruvideoSdkMediaFileUploadRequestRepositoryImpl(context)
            val s3ClientUseCase = S3ClientUseCase(context)
            val mediaService = TruvideoSdkMediaServiceImpl(
                authAdapter = authAdapter
            )
            val uploadFileUseCase = UploadFileUseCase(
                context = context,
                s3ClientUseCase = s3ClientUseCase
            )
            val fileUploadEngine = TruvideoSdkMediaFileUploadEngine(
                context = context,
                uploadFileUseCase = uploadFileUseCase,
                repository = mediaFileUploadRequestRepository,
                mediaService = mediaService
            )

            TruvideoSdkMedia = TruvideoSdkMediaImpl(
                authAdapter = authAdapter,
                mediaFileUploadRequestRepository = mediaFileUploadRequestRepository,
                fileUploadEngine = fileUploadEngine
            )
        }
    }

    override fun create(context: Context) {
        init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}