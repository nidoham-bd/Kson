package com.nidoham.kson.core

/**
 * Represents a JSON null value. Singleton pattern.
 */
class JsonNull private constructor() : JsonElement() {

    override fun asString(): String = "null"
    override fun deepCopy(): JsonNull = this
    override fun toJsonString(): String = "null"
    override fun toPrettyJsonString(indent: Int): String = "null"
    override fun equals(other: Any?): Boolean = other is JsonNull
    override fun hashCode(): Int = 0x2a2a2a2a.toInt()
    override fun toString(): String = "JsonNull"

    companion object {
        @JvmField val INSTANCE: JsonNull = JsonNull()
        @JvmStatic fun get(): JsonNull = INSTANCE
    }
}