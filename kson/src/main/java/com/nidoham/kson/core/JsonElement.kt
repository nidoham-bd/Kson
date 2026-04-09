package com.nidoham.kson.core

import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.logging.KsonLogger

/**
 * Base class for all JSON elements.
 * Provides type checking and conversion utilities.
 */
sealed class JsonElement {

    fun isJsonObject(): Boolean = this is JsonObject

    fun isJsonArray(): Boolean = this is JsonArray

    fun isJsonPrimitive(): Boolean = this is JsonPrimitive

    fun isJsonNull(): Boolean = this is JsonNull

    fun isString(): Boolean = this is JsonPrimitive && this.isString

    fun isNumber(): Boolean = this is JsonPrimitive && this.isNumber

    fun isBoolean(): Boolean = this is JsonPrimitive && this.isBoolean

    fun asJsonObject(): JsonObject {
        if (this !is JsonObject) {
            throw IllegalStateException("Not a JSON Object: $this")
        }
        return this
    }

    fun asJsonArray(): JsonArray {
        if (this !is JsonArray) {
            throw IllegalStateException("Not a JSON Array: $this")
        }
        return this
    }

    fun asJsonPrimitive(): JsonPrimitive {
        if (this !is JsonPrimitive) {
            throw IllegalStateException("Not a JSON Primitive: $this")
        }
        return this
    }

    fun asJsonNull(): JsonNull {
        if (this !is JsonNull) {
            throw IllegalStateException("Not a JSON Null: $this")
        }
        return this
    }

    fun asString(): String = asJsonPrimitive().asString

    fun asBoolean(): Boolean = asJsonPrimitive().asBoolean

    fun asInt(): Int = asJsonPrimitive().asInt

    fun asLong(): Long = asJsonPrimitive().asLong

    fun asFloat(): Float = asJsonPrimitive().asFloat

    fun asDouble(): Double = asJsonPrimitive().asDouble

    abstract fun deepCopy(): JsonElement

    override fun toString(): String = toJsonString()

    abstract fun toJsonString(): String

    abstract fun toPrettyJsonString(indent: Int = 2): String

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    companion object {
        private val logger = KsonLogger.getLogger(JsonElement::class)

        internal fun logDebug(message: String) {
            logger.debug(message)
        }
    }
}