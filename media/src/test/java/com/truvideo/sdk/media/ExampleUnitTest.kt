package com.truvideo.sdk.media

import com.truvideo.sdk.media.util.MapSerializer
import kotlinx.serialization.json.Json
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val nestedMap: Map<String, Any?> = mapOf(
            "name" to "John Doe",
            "age" to 30,
            "isEmployed" to true,
            "height" to 5.9,
            "address" to null,
            "contacts" to mapOf(
                "email" to "john.doe@example.com",
                "phone" to "123-456-7890"
            ),
            "hobbies" to listOf("reading", "travelling", "coding")
        )

        val json = Json { encodeDefaults = true }

        // Serialize
        val jsonString = json.encodeToString(MapSerializer, nestedMap)
        println("Serialized JSON: $jsonString")

        // Deserialize
        val deserializedMap = json.decodeFromString<Map<String, Any?>>(MapSerializer, jsonString)
        println("Deserialized Map: $deserializedMap")

        assert(deserializedMap != null)
    }
}