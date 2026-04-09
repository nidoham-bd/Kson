package com.nidoham.kson.core

/**
 * Represents a JSON primitive value (string, number, or boolean).
 * Immutable and thread-safe.
 */
class JsonPrimitive private constructor(private val value: Any?) : JsonElement() {

    val isString: Boolean = value is String
    val isNumber: Boolean = value is Number
    val isBoolean: Boolean = value is Boolean

    val asString: String
        get() = when (value) {
            is String -> value
            null -> "null"
            else -> value.toString()
        }

    val asBoolean: Boolean
        get() = when (value) {
            is Boolean -> value
            is String -> when (value.lowercase()) {
                "true" -> true
                "false" -> false
                "1" -> true
                "0" -> false
                else -> throw IllegalStateException("String '$value' is not a boolean")
            }
            else -> throw IllegalStateException("Not a boolean: $value")
        }

    val asInt: Int
        get() = when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: throw NumberFormatException("String '$value' is not a valid integer")
            is Boolean -> if (value) 1 else 0
            else -> throw NumberFormatException("Not a number: $value")
        }

    val asLong: Long
        get() = when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: throw NumberFormatException("String '$value' is not a valid long")
            is Boolean -> if (value) 1L else 0L
            else -> throw NumberFormatException("Not a number: $value")
        }

    val asFloat: Float
        get() = when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: throw NumberFormatException("String '$value' is not a valid float")
            is Boolean -> if (value) 1f else 0f
            else -> throw NumberFormatException("Not a number: $value")
        }

    val asDouble: Double
        get() = when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: throw NumberFormatException("String '$value' is not a valid double")
            is Boolean -> if (value) 1.0 else 0.0
            else -> throw NumberFormatException("Not a number: $value")
        }

    val asByte: Byte
        get() = when (value) {
            is Number -> value.toByte()
            is String -> value.toByteOrNull() ?: throw NumberFormatException("String '$value' is not a valid byte")
            else -> throw NumberFormatException("Not a number: $value")
        }

    val asShort: Short
        get() = when (value) {
            is Number -> value.toShort()
            is String -> value.toShortOrNull() ?: throw NumberFormatException("String '$value' is not a valid short")
            else -> throw NumberFormatException("Not a number: $value")
        }

    fun getValue(): Any? = value
    fun isNullValue(): Boolean = value == null

    override fun deepCopy(): JsonPrimitive = this

    override fun toJsonString(): String = when (value) {
        null -> "null"
        is String -> "\"${escapeString(value)}\""
        is Boolean -> value.toString()
        is Number -> formatNumber(value)
        else -> "\"${escapeString(value.toString())}\""
    }

    override fun toPrettyJsonString(indent: Int): String = toJsonString()

    private fun formatNumber(number: Number): String = when (number) {
        is Double -> when {
            number.isNaN() -> "NaN"
            number.isInfinite() -> if (number > 0) "Infinity" else "-Infinity"
            else -> {
                val str = number.toString()
                if (str.contains('.') && !str.contains('e') && !str.contains('E') && str.endsWith(".0")) str.dropLast(2) else str
            }
        }
        is Float -> when {
            number.isNaN() -> "NaN"
            number.isInfinite() -> if (number > 0) "Infinity" else "-Infinity"
            else -> {
                val str = number.toString()
                if (str.endsWith(".0")) str.dropLast(2) else str
            }
        }
        else -> number.toString()
    }

    private fun escapeString(s: String): String = buildString {
        for (c in s) {
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (c.code < 32) append("\\u${c.code.toString(16).padStart(4, '0')}") else append(c)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonPrimitive) return false
        if (value is Number && other.value is Number) return value.toDouble() == other.value.toDouble()
        return value == other.value
    }

    override fun hashCode(): Int = if (value is Number) value.toDouble().hashCode() else value?.hashCode() ?: 0

    override fun toString(): String = "JsonPrimitive($value)"

    companion object {
        @JvmStatic fun of(value: String): JsonPrimitive = JsonPrimitive(value)
        @JvmStatic fun of(value: Number): JsonPrimitive = JsonPrimitive(value)
        @JvmStatic fun of(value: Boolean): JsonPrimitive = JsonPrimitive(value)
        @JvmStatic fun of(value: Char): JsonPrimitive = JsonPrimitive(value.toString())
        @JvmStatic fun ofNullable(value: String?): JsonPrimitive = if (value != null) JsonPrimitive(value) else JsonNull.INSTANCE
        @JvmStatic fun ofAny(value: Any?): JsonPrimitive = when (value) {
            null -> JsonNull.INSTANCE
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Char -> JsonPrimitive(value.toString())
            else -> JsonPrimitive(value.toString())
        }

        @JvmField val TRUE: JsonPrimitive = JsonPrimitive(true)
        @JvmField val FALSE: JsonPrimitive = JsonPrimitive(false)
    }
}

fun String.toJsonPrimitive(): JsonPrimitive = JsonPrimitive.of(this)
fun Number.toJsonPrimitive(): JsonPrimitive = JsonPrimitive.of(this)
fun Boolean.toJsonPrimitive(): JsonPrimitive = JsonPrimitive.of(this)
fun Char.toJsonPrimitive(): JsonPrimitive = JsonPrimitive.of(this)