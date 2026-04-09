package com.nidoham.kson.core

class JsonPrimitive private constructor(private val value: Any?) : JsonElement() {

    override val isString: Boolean get() = value is String
    override val isNumber: Boolean get() = value is Number
    override val isBoolean: Boolean get() = value is Boolean

    override fun asString(): String {
        return when (value) {
            is String -> value
            null -> "null"
            else -> value.toString()
        }
    }

    override fun asBoolean(): Boolean {
        return when (value) {
            is Boolean -> value
            is String -> when (value.lowercase()) {
                "true" -> true
                "false" -> false
                "1" -> true
                "0" -> false
                else -> throw IllegalStateException("Not a boolean")
            }
            else -> throw IllegalStateException("Not a boolean")
        }
    }

    override fun asInt(): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: throw NumberFormatException()
            is Boolean -> if (value) 1 else 0
            else -> throw NumberFormatException()
        }
    }

    override fun asLong(): Long {
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: throw NumberFormatException()
            is Boolean -> if (value) 1L else 0L
            else -> throw NumberFormatException()
        }
    }

    override fun asFloat(): Float {
        return when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: throw NumberFormatException()
            is Boolean -> if (value) 1f else 0f
            else -> throw NumberFormatException()
        }
    }

    override fun asDouble(): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: throw NumberFormatException()
            is Boolean -> if (value) 1.0 else 0.0
            else -> throw NumberFormatException()
        }
    }

    fun getValue(): Any? {
        return value
    }

    fun isNullValue(): Boolean {
        return value == null
    }

    override fun deepCopy(): JsonPrimitive {
        return this
    }

    override fun toJsonString(): String {
        return when (value) {
            null -> "null"
            is String -> "\"${escapeString(value)}\""
            is Boolean -> value.toString()
            is Number -> formatNumber(value)
            else -> "\"${escapeString(value.toString())}\""
        }
    }

    override fun toPrettyJsonString(indent: Int): String {
        return toJsonString()
    }

    private fun formatNumber(number: Number): String {
        return when (number) {
            is Double -> {
                if (number.isNaN()) {
                    "NaN"
                } else if (number.isInfinite()) {
                    if (number > 0) "Infinity" else "-Infinity"
                } else {
                    val str = number.toString()
                    if (str.endsWith(".0")) str.dropLast(2) else str
                }
            }
            is Float -> {
                if (number.isNaN()) {
                    "NaN"
                } else if (number.isInfinite()) {
                    if (number > 0) "Infinity" else "-Infinity"
                } else {
                    val str = number.toString()
                    if (str.endsWith(".0")) str.dropLast(2) else str
                }
            }
            else -> number.toString()
        }
    }

    private fun escapeString(s: String): String {
        return buildString {
            for (c in s) {
                when (c) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (c.code < 32) {
                            append("\\u${c.code.toString(16).padStart(4, '0')}")
                        } else {
                            append(c)
                        }
                    }
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonPrimitive) return false
        if (value is Number && other.value is Number) {
            return value.toDouble() == other.value.toDouble()
        }
        return value == other.value
    }

    override fun hashCode(): Int {
        return if (value is Number) value.toDouble().hashCode() else value?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "JsonPrimitive($value)"
    }

    companion object {
        @JvmStatic
        fun of(value: String): JsonPrimitive {
            return JsonPrimitive(value)
        }

        @JvmStatic
        fun of(value: Number): JsonPrimitive {
            return JsonPrimitive(value)
        }

        @JvmStatic
        fun of(value: Boolean): JsonPrimitive {
            return JsonPrimitive(value)
        }

        @JvmStatic
        fun of(value: Char): JsonPrimitive {
            return JsonPrimitive(value.toString())
        }

        @JvmStatic
        fun ofNullable(value: String?): JsonElement {
            return if (value != null) JsonPrimitive(value) else JsonNull.INSTANCE
        }

        @JvmStatic
        fun ofAny(value: Any?): JsonElement {
            return when (value) {
                null -> JsonNull.INSTANCE
                is String -> JsonPrimitive(value)
                is Number -> JsonPrimitive(value)
                is Boolean -> JsonPrimitive(value)
                is Char -> JsonPrimitive(value.toString())
                else -> JsonPrimitive(value.toString())
            }
        }

        @JvmField
        val TRUE: JsonPrimitive = JsonPrimitive(true)

        @JvmField
        val FALSE: JsonPrimitive = JsonPrimitive(false)
    }
}

fun String.toJsonPrimitive(): JsonPrimitive {
    return JsonPrimitive.of(this)
}

fun Number.toJsonPrimitive(): JsonPrimitive {
    return JsonPrimitive.of(this)
}

fun Boolean.toJsonPrimitive(): JsonPrimitive {
    return JsonPrimitive.of(this)
}