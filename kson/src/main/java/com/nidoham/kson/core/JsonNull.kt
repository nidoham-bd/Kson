package com.nidoham.kson.core

class JsonNull private constructor() : JsonElement() {

    override fun asString(): String {
        return "null"
    }

    override fun deepCopy(): JsonNull {
        return this
    }

    override fun toJsonString(): String {
        return "null"
    }

    override fun toPrettyJsonString(indent: Int): String {
        return "null"
    }

    override fun equals(other: Any?): Boolean {
        return other is JsonNull
    }

    override fun hashCode(): Int {
        return 0x2a2a2a2a.toInt()
    }

    override fun toString(): String {
        return "JsonNull"
    }

    companion object {
        @JvmField
        val INSTANCE: JsonNull = JsonNull()

        @JvmStatic
        fun get(): JsonNull {
            return INSTANCE
        }
    }
}