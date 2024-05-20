package com.truvideo.sdk.media.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

fun List<*>.toJsonElement(): JsonElement {
    val list: MutableList<JsonElement> = mutableListOf()
    this.forEach {
        val value = it as? Any ?: return@forEach
        when(value) {
            is Map<*, *> -> list.add((value).toJsonElement())
            is List<*> -> list.add(value.toJsonElement())
            else -> list.add(JsonPrimitive(value.toString()))
        }
    }
    return JsonArray(list)
}

fun Map<*, *>.toJsonElement(): JsonElement {
    val map: MutableMap<String, JsonElement> = mutableMapOf()
    this.forEach {
        val key = it.key as? String ?: return@forEach
        val value = it.value ?: return@forEach
        when(value) {
            is Map<*, *> -> map[key] = (value).toJsonElement()
            is List<*> -> map[key] = value.toJsonElement()
            else -> map[key] = JsonPrimitive(value.toString())
        }
    }
    return JsonObject(map)
}

fun String.toListFromJson(): List<Any?> {
    val jsonElement = Json.parseToJsonElement(this)
    if (jsonElement !is JsonArray) throw IllegalArgumentException("String is not a JSON array")
    return jsonElement.toList()
}

private fun JsonElement.toList(): List<Any?> {
    if (this !is JsonArray) throw IllegalArgumentException("JsonElement is not a JsonArray")
    return this.map { it.toKotlinValue() }
}

private fun JsonElement.toKotlinValue(): Any? {
    return when (this) {
        is JsonPrimitive -> {
            when {
                this.isString -> this.content
                this.booleanOrNull != null -> this.boolean
                this.intOrNull != null -> this.int
                this.longOrNull != null -> this.long
                this.doubleOrNull != null -> this.double
                else -> throw IllegalArgumentException("Unknown primitive type")
            }
        }
        JsonNull -> null
        is JsonObject -> this.toMap()
        is JsonArray -> this.toList()
        else -> throw IllegalArgumentException("Unsupported JSON element type")
    }
}

private fun JsonElement.toKotlinString(): String {
    return when (this) {
        is JsonPrimitive -> {
            when {
                this.isString -> this.content
                this.booleanOrNull != null -> this.boolean.toString()
                this.intOrNull != null -> this.int.toString()
                this.longOrNull != null -> this.long.toString()
                this.doubleOrNull != null -> this.double.toString()
                else -> throw IllegalArgumentException("Unknown primitive type")
            }
        }
        else -> throw IllegalArgumentException("Unsupported JSON element type")
    }
}

fun String.toMapAnyFromJson(): Map<String, Any?> {
    val jsonElement = Json.parseToJsonElement(this)
    if (jsonElement !is JsonObject) throw IllegalArgumentException("String is not a JSON object")
    return jsonElement.toMapAny()
}

private fun JsonElement.toMapAny(): Map<String, Any?> {
    if (this !is JsonObject) throw IllegalArgumentException("JsonElement is not a JsonObject")
    return this.mapValues { it.value.toKotlinValue() }
}

fun String.toMapStringFromJson(): Map<String, String> {
    val jsonElement = Json.parseToJsonElement(this)
    if (jsonElement !is JsonObject) throw IllegalArgumentException("String is not a JSON object")
    return jsonElement.toMapString()
}

private fun JsonElement.toMapString(): Map<String, String> {
    if (this !is JsonObject) throw IllegalArgumentException("JsonElement is not a JsonObject")
    return this.mapValues { it.value.toKotlinString() }
}