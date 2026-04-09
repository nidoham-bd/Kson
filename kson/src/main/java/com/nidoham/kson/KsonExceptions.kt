package com.nidoham.kson

import kotlin.reflect.KClass

open class KsonException(message: String, cause: Throwable? = null) : Exception(message, cause)

class KsonParseException(
    message: String,
    val lineNumber: Int = -1,
    val columnNumber: Int = -1,
    val jsonPath: String = "",
    cause: Throwable? = null
) : KsonException(
    buildString {
        append("Parse error: $message")
        if (jsonPath.isNotEmpty()) {
            append(" (at $jsonPath)")
        }
        if (lineNumber >= 0 && columnNumber >= 0) {
            append(" [line $lineNumber, column $columnNumber]")
        }
    },
    cause
)

class KsonSerializationException(
    message: String,
    val targetType: KClass<*>? = null,
    cause: Throwable? = null
) : KsonException(
    if (targetType != null) {
        "Serialization failed for ${targetType.simpleName}: $message"
    } else {
        "Serialization failed: $message"
    },
    cause
)

class KsonDeserializationException(
    message: String,
    val targetType: KClass<*>? = null,
    val jsonPath: String = "",
    cause: Throwable? = null
) : KsonException(
    if (targetType != null) {
        "Deserialization failed for ${targetType.simpleName}: $message"
    } else {
        "Deserialization failed: $message"
    },
    cause
)

class MissingFieldException(
    val fieldName: String,
    val targetType: KClass<*>? = null,
    val jsonPath: String = ""
) : KsonException(
    buildString {
        append("Missing required field: '$fieldName'")
        if (targetType != null) {
            append(" in ${targetType.simpleName}")
        }
        if (jsonPath.isNotEmpty()) {
            append(" at $jsonPath")
        }
    }
)

class TypeMismatchException(
    val expectedType: String,
    val actualType: String,
    val jsonPath: String = "",
    val fieldName: String? = null
) : KsonException(
    buildString {
        append("Type mismatch")
        if (fieldName != null) {
            append(" for field '$fieldName'")
        }
        append(": expected $expectedType but got $actualType")
        if (jsonPath.isNotEmpty()) {
            append(" at $jsonPath")
        }
    }
)

class UnknownPropertyException(
    val propertyName: String,
    val targetType: KClass<*>? = null
) : KsonException(
    buildString {
        append("Unknown property: '$propertyName'")
        if (targetType != null) {
            append(" in ${targetType.simpleName}")
        }
    }
)

class JsonOutOfRangeException(
    message: String,
    val value: Any?,
    val min: Any?,
    val max: Any?
) : KsonException("$message (value: $value, range: $min - $max)")

class CircularReferenceException(val objectType: KClass<*>) : KsonException("Circular reference detected for ${objectType.simpleName}")