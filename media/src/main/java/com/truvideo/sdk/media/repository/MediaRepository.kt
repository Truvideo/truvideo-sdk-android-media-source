package com.truvideo.sdk.media.repository

import android.content.Context
import com.truvideo.sdk.media.data.DatabaseSingleton
import com.truvideo.sdk.media.model.MediaEntity

class MediaRepository {

    fun insertMedia(context: Context, media: MediaEntity) {
        DatabaseSingleton.getDatabase(context).mediaDao().insertMedia(media)
    }

    fun getExternalId(context: Context, id: String): Int? {
        return DatabaseSingleton.getDatabase(context).mediaDao().getExternalId(id)
    }
}