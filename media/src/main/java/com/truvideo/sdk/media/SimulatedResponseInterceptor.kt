package com.truvideo.sdk.media

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException


class SimulatedResponseInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the original request
        val originalRequest: Request = chain.request()

        // Check if you want to simulate a response for this particular request
        return if (shouldSimulate(originalRequest)) {
            // Create a simulated response
            Response.Builder().request(originalRequest).code(200) // Simulated response code
                .message("Simulated response").body(
                    ResponseBody.create(
                        null, TruvideoSdkUploadCredentials.createMockedJson()
                    )
                ) // Simulated response body
                .build()
        } else {
            // If you don't want to simulate the request, continue with the original request
            chain.proceed(originalRequest)
        }
    }

    private fun shouldSimulate(request: Request): Boolean {
        return request.url.toString().contains("todo/get/credentials");
    }
}
