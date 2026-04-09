package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonPrimitive

object PrimitiveAdapters {

    class StringAdapter : BaseTypeAdapter<String>(String::class) {
        override fun serialize(value: String): JsonElement = JsonPrimitive(value)
        override fun deserialize(element: JsonElement): String? = if (element.isJsonPrimitive) element.asString else element.toString()
    }

    class IntAdapter : BaseTypeAdapter<Int>(Int::class) {
        override fun serialize(value: Int): JsonElement = JsonPrimitive(value)
        override fun deserialize(element: JsonElement): Int? = if (element.isJsonPrimitive) element.asInt else null
        override fun nullValue(): Int? = 0
    }

    class LongAdapter : BaseTypeAdapter<Long>(Long::class) {
        override fun serialize(value: Long): JsonElement = JsonPrimitive(value)
        override fun deserialize(element: JsonElement): Long? = if (element.isJsonPrimitive) element.asLong else null
        override fun nullValue(): Long? = 0L
    }

    class DoubleAdapter : BaseTypeAdapter<Double>(Double::class) {
        override fun serialize(value: Double): JsonElement = JsonPrimitive(value)
        override fun deserialize(element: JsonElement): Double? = if (element.isJsonPrimitive) element.asDouble else null
        override fun nullValue(): Double? = 0.0
    }

    class FloatAdapter : BaseTypeAdapter<Float>(Float::class) {
        override fun serialize(value: Float): JsonElement = JsonPrimitive(value)
        override fun deserialize(element: JsonElement): Float? = if (element.isJsonPrimitive) element.asFloat else null
        override fun nullValue(): Float? = 0f
    }

    class BooleanAdapter : BaseTypeAdapter<Boolean>(Boolean::class) {
        override fun serialize(value: Boolean): JsonElement = JsonPrimitive(value)
        override fun deserialize(element: JsonElement): Boolean? = if (element.isJsonPrimitive) element.asBoolean else null
        override fun nullValue(): Boolean? = false
    }

    class ShortAdapter : BaseTypeAdapter<Short>(Short::class) {
        override fun serialize(value: Short): JsonElement = JsonPrimitive(value.toInt())
        override fun deserialize(element: JsonElement): Short? = if (element.isJsonPrimitive) element.asInt.toShort() else null
        override fun nullValue(): Short? = 0
    }

    class ByteAdapter : BaseTypeAdapter<Byte>(Byte::class) {
        override fun serialize(value: Byte): JsonElement = JsonPrimitive(value.toInt())
        override fun deserialize(element: JsonElement): Byte? = if (element.isJsonPrimitive) element.asInt.toByte() else null
        override fun nullValue(): Byte? = 0
    }

    class CharAdapter : BaseTypeAdapter<Char>(Char::class) {
        override fun serialize(value: Char): JsonElement = JsonPrimitive(value.toString())
        override fun deserialize(element: JsonElement): Char? = if (element.isJsonPrimitive) element.asString.firstOrNull() else null
        override fun nullValue(): Char? = '\u0000'
    }

    fun all(): List<com.nidoham.kson.adapter.TypeAdapter<*>> = listOf(
        StringAdapter(), IntAdapter(), LongAdapter(), DoubleAdapter(),
        FloatAdapter(), BooleanAdapter(), ShortAdapter(), ByteAdapter(), CharAdapter()
    )
}