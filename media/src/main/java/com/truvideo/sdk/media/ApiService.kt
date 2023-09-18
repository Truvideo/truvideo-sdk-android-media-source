package com.truvideo.sdk.media

import retrofit2.Call
import retrofit2.http.GET


interface ApiService {
    @GET("todo/get/credentials")
    fun getCredentials(): Call<TruvideoSdkUploadCredentials?>?
}