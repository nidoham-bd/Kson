package com.nidoham.kson.serializer

import com.nidoham.kson.core.JsonArray
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull
import com.nidoham.kson.core.JsonObject
import com.nidoham.kson.core.JsonPrimitive
import com.nidoham.kson.logging.KsonLogger

class JsonWriter(
    private var prettyPrint: Boolean = false,
    private var indent: String = "  ",
    private var serializeNulls: Boolean = false,
    private var escapeHtmlChars: Boolean = false
) {
    private val logger = KsonLogger.getLogger(JsonWriter::class)

    fun write(element: JsonElement): String {
        logger.debug("Starting JSON writing, prettyPrint: $prettyPrint")
        val startTime = System.nanoTime()
        val result = buildString {
            writeElement(element, this, 0)
        }
        val elapsed = (System.nanoTime() - startTime) / 1_000_000.0
        logger.debug("JSON writing completed in ${elapsed}ms, length: ${result.length}")
        return result
    }

    private fun writeElement(element: JsonElement, sb: StringBuilder, depth: Int) {
        when (element) {
            is JsonObject -> writeObject(element, sb, depth)
            is JsonArray -> writeArray(element, sb, depth)
            is JsonPrimitive -> writePrimitive(element, sb)
            is JsonNull -> sb.append("null")
        }
    }

    private fun writeObject(obj: JsonObject, sb: StringBuilder, depth: Int) {
        sb.append('{')
        val entries = obj.entrySet().toList()
        if (entries.isEmpty()) {
            sb.append('}')
            return
        }
        if (prettyPrint) sb.append('\n')
        entries.forEachIndexed { index, (key, value) ->
            if (!serializeNulls && value.isJsonNull) return@forEachIndexed
            if (prettyPrint) sb.append(indent.repeat(depth + 1))
            sb.append('"')
            sb.append(escapeString(key))
            sb.append('"')
            sb.append(':')
            if (prettyPrint) sb.append(' ')
            writeElement(value, sb, depth + 1)
            if (index < entries.size - 1) sb.append(',')
            if (prettyPrint) sb.append('\n')
        }
        if (prettyPrint) sb.append(indent.repeat(depth))
        sb.append('}')
    }

    private fun writeArray(arr: JsonArray, sb: StringBuilder, depth: Int) {
        sb.append('[')
        if (arr.isEmpty()) {
            sb.append(']')
            return
        }
        if (prettyPrint) sb.append('\n')
        arr.forEachIndexed { index, element ->
            if (!serializeNulls && element.isJsonNull) return@forEachIndexed
            if (prettyPrint) sb.append(indent.repeat(depth + 1))
            writeElement(element, sb, depth + 1)
            if (index < arr.size() - 1) sb.append(',')
            if (prettyPrint) sb.append('\n')
        }
        if (prettyPrint) sb.append(indent.repeat(depth))
        sb.append(']')
    }

    private fun writePrimitive(primitive: JsonPrimitive, sb: StringBuilder) {
        val value = primitive.getValue()
        when (value) {
            null -> sb.append("null")
            is String -> {
                sb.append('"')
                sb.append(escapeString(value))
                sb.append('"')
            }
            is Boolean -> sb.append(value)
            is Number -> sb.append(formatNumber(value))
            else -> {
                sb.append('"')
                sb.append(escapeString(value.toString()))
                sb.append('"')
            }
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
                    '<' -> if (escapeHtmlChars) append("\\u003c") else append(c)
                    '>' -> if (escapeHtmlChars) append("\\u003e") else append(c)
                    '&' -> if (escapeHtmlChars) append("\\u0026") else append(c)
                    '=' -> if (escapeHtmlChars) append("\\u003d") else append(c)
                    '\'' -> if (escapeHtmlChars) append("\\u0027") else append(c)
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

    private fun formatNumber(number: Number): String {
        return when (number) {
            is Double -> if (number.isNaN()) "NaN" else if (number.isInfinite()) if (number > 0) "Infinity" else "-Infinity" else number.toString()
            is Float -> if (number.isNaN()) "NaN" else if (number.isInfinite()) if (number > 0) "Infinity" else "-Infinity" else number.toString()
            else -> number.toString()
        }
    }

    fun setPrettyPrint(prettyPrint: Boolean): JsonWriter {
        this.prettyPrint = prettyPrint
        return this
    }

    fun setIndent(indent: String): JsonWriter {
        this.indent = indent
        return this
    }

    fun setSerializeNulls(serializeNulls: Boolean): JsonWriter {
        this.serializeNulls = serializeNulls
        return this
    }

    fun setEscapeHtmlChars(escapeHtmlChars: Boolean): JsonWriter {
        this.escapeHtmlChars = escapeHtmlChars
        return this
    }

    companion object {
        @JvmStatic
        fun compact(): JsonWriter {
            return JsonWriter(prettyPrint = false)
        }

        @JvmStatic
        fun pretty(): JsonWriter {
            return JsonWriter(prettyPrint = true)
        }
    }
}