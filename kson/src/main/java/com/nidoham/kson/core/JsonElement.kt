package com.nidoham.kson.core

import com.nidoham.kson.logging.KsonLogger

sealed class JsonElement {

    val isJsonObject: Boolean get() = this is JsonObject
    val isJsonArray: Boolean get() = this is JsonArray
    val isJsonPrimitive: Boolean get() = this is JsonPrimitive
    val isJsonNull: Boolean get() = this is JsonNull

    open val isString: Boolean get() = false
    open val isNumber: Boolean get() = false
    open val isBoolean: Boolean get() = false

    fun asJsonObject(): JsonObject {
        if (this !is JsonObject) throw IllegalStateException("Not a JSON Object")
        return this
    }

    fun asJsonArray(): JsonArray {
        if (this !is JsonArray) throw IllegalStateException("Not a JSON Array")
        return this
    }

    fun asJsonPrimitive(): JsonPrimitive {
        if (this !is JsonPrimitive) throw IllegalStateException("Not a JSON Primitive")
        return this
    }

    fun asJsonNull(): JsonNull {
        if (this !is JsonNull) throw IllegalStateException("Not a JSON Null")
        return this
    }

    open fun asString(): String {
        return asJsonPrimitive().asString()
    }

    open fun asBoolean(): Boolean {
        return asJsonPrimitive().asBoolean()
    }

    open fun asInt(): Int {
        return asJsonPrimitive().asInt()
    }

    open fun asLong(): Long {
        return asJsonPrimitive().asLong()
    }

    open fun asFloat(): Float {
        return asJsonPrimitive().asFloat()
    }

    open fun asDouble(): Double {
        return asJsonPrimitive().asDouble()
    }

    abstract fun deepCopy(): JsonElement

    override fun toString(): String {
        return toJsonString()
    }

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