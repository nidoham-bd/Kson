package com.nidoham.kson.parser

open class ParseException(
    message: String,
    val line: Int = -1,
    val column: Int = -1,
    val offset: Int = -1,
    val path: String = ""
) : Exception(formatMessage(message, line, column, path)) {

    fun locationString(): String {
        return if (line >= 0 && column >= 0) {
            "at line $line, column $column"
        } else if (offset >= 0) {
            "at offset $offset"
        } else {
            "unknown"
        }
    }

    companion object {
        private fun formatMessage(message: String, line: Int, column: Int, path: String): String {
            return buildString {
                append("JSON Parse Error: ")
                append(message)
                if (path.isNotEmpty()) {
                    append(" (path: $path)")
                }
                if (line >= 0 && column >= 0) {
                    append(" [line: $line, column: $column]")
                }
            }
        }
    }
}

open class JsonSyntaxException(
    message: String,
    line: Int = -1,
    column: Int = -1,
    offset: Int = -1
) : ParseException(message, line, column, offset)

class DuplicateKeyException(key: String, line: Int = -1, column: Int = -1) : ParseException("Duplicate key: '$key'", line, column)

class UnexpectedTokenException(expected: String, actual: String, line: Int = -1, column: Int = -1) : ParseException("Expected $expected but found $actual", line, column)

class MalformedJsonException(message: String, line: Int = -1, column: Int = -1) : JsonSyntaxException(message, line, column)