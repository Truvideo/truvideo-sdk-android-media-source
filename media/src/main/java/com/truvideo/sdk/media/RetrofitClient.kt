package com.truvideo.sdk.media

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://todo-url.com/"

    fun getApiService(): ApiService {
        val httpClient = OkHttpClient.Builder()

        // Add the simulated response interceptor
        httpClient.addInterceptor(SimulatedResponseInterceptor())

        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build()).build()

        return retrofit.create(ApiService::class.java)
    }
}
