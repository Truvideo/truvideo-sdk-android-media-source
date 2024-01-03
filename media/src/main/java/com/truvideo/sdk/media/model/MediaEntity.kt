package com.truvideo.sdk.media.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MediaEntity(
    @PrimaryKey val id: String, val externalId: Int
)
