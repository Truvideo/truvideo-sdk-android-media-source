package com.truvideo.sdk.media.model

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TruvideoSdkMediaResponseTest {

    @Test
    fun `fromJson parses valid JSON correctly`() {
        // Arrange
        val json = JSONObject(
            """
            {
                "id": "123",
                "url": "http://example.com",
                "transcriptionUrl": "http://example.com/transcription",
                "transcriptionLength": "60",
                "tags": {
                    "tag1": "value1",
                    "tag2": "value2"
                }
            }
        """
        )

        // Act
        val result = TruvideoSdkMediaResponse.fromJson(json)

        // Assert
        assertEquals("123", result.id)
        assertEquals("http://example.com", result.url)
        assertEquals("http://example.com/transcription", result.transcriptionUrl)
        assertEquals("60", result.transcriptionLength)
        assertEquals(2, result.tags.size)
        assertEquals("value1", result.tags["tag1"])
        assertEquals("value2", result.tags["tag2"])
    }

    @Test
    fun `fromJson handles missing optional fields correctly`() {
        // Arrange
        val json = JSONObject(
            """
            {
                "id": "123",
                "url": "http://example.com",
                "tags": {
                    "tag1": "value1",
                    "tag2": "value2"
                }
            }
        """
        )

        // Act
        val result = TruvideoSdkMediaResponse.fromJson(json)

        // Assert
        assertEquals("123", result.id)
        assertEquals("http://example.com", result.url)
        assertNull(result.transcriptionUrl)
        assertNull(result.transcriptionLength)
        assertEquals(2, result.tags.size)
        assertEquals("value1", result.tags["tag1"])
        assertEquals("value2", result.tags["tag2"])
    }

    @Test
    fun `fromJson handles empty tags correctly`() {
        // Arrange
        val json = JSONObject(
            """
            {
                "id": "123",
                "url": "http://example.com",
                "transcriptionUrl": "http://example.com/transcription",
                "transcriptionLength": "60"
            }
        """
        )

        // Act
        val result = TruvideoSdkMediaResponse.fromJson(json)

        // Assert
        assertEquals("123", result.id)
        assertEquals("http://example.com", result.url)
        assertEquals("http://example.com/transcription", result.transcriptionUrl)
        assertEquals("60", result.transcriptionLength)
        assertEquals(0, result.tags.size)
    }
}
