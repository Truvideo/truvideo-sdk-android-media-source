package com.truvideo.sdk.media.service.media

import org.junit.Assert.assertEquals
import org.junit.Test

class TruvideoSdkMediaServiceImplTest {

    @Test
    fun testBuildSearchUrl() {
        val baseUrl = "https://example.com"
        val pageNumber = 1
        val pageSize = 50

        val expectedUrl = "$baseUrl/api/media/search?page=$pageNumber&size=$pageSize"
        val actualUrl = TruvideoSdkMediaServiceImpl.buildSearchUrl(baseUrl, pageNumber, pageSize)

        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun testBuildSearchUrlDefaultValues() {
        val baseUrl = "https://example.com"
        val expectedUrl = "$baseUrl/api/media/search?page=0&size=20"
        val actualUrl = TruvideoSdkMediaServiceImpl.buildSearchUrl(baseUrl, null, null)

        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun testBuildSearchUrlMaxPageSize() {
        val baseUrl = "https://example.com"
        val pageNumber = 1
        val pageSize = 150

        val expectedUrl = "$baseUrl/api/media/search?page=$pageNumber&size=100"
        val actualUrl = TruvideoSdkMediaServiceImpl.buildSearchUrl(baseUrl, pageNumber, pageSize)

        assertEquals(expectedUrl, actualUrl)
    }
}
