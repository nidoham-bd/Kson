package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonPrimitive

object PrimitiveAdapters {
    class StringAdapter : BaseTypeAdapter<String>(String::class) {
        override fun serialize(value: String): JsonElement { return JsonPrimitive.of(value) }
        override fun deserialize(element: JsonElement): String? { return if (element.isJsonPrimitive) element.asString() else element.toString() }
    }
    class IntAdapter : BaseTypeAdapter<Int>(Int::class) {
        override fun serialize(value: Int): JsonElement { return JsonPrimitive.of(value) }
        override fun deserialize(element: JsonElement): Int? { return if (element.isJsonPrimitive) element.asInt() else null }
        override fun nullValue(): Int? { return 0 }
    }
    class LongAdapter : BaseTypeAdapter<Long>(Long::class) {
        override fun serialize(value: Long): JsonElement { return JsonPrimitive.of(value) }
        override fun deserialize(element: JsonElement): Long? { return if (element.isJsonPrimitive) element.asLong() else null }
        override fun nullValue(): Long? { return 0L }
    }
    class DoubleAdapter : BaseTypeAdapter<Double>(Double::class) {
        override fun serialize(value: Double): JsonElement { return JsonPrimitive.of(value) }
        override fun deserialize(element: JsonElement): Double? { return if (element.isJsonPrimitive) element.asDouble() else null }
        override fun nullValue(): Double? { return 0.0 }
    }
    class FloatAdapter : BaseTypeAdapter<Float>(Float::class) {
        override fun serialize(value: Float): JsonElement { return JsonPrimitive.of(value) }
        override fun deserialize(element: JsonElement): Float? { return if (element.isJsonPrimitive) element.asFloat() else null }
        override fun nullValue(): Float? { return 0f }
    }
    class BooleanAdapter : BaseTypeAdapter<Boolean>(Boolean::class) {
        override fun serialize(value: Boolean): JsonElement { return JsonPrimitive.of(value) }
        override fun deserialize(element: JsonElement): Boolean? { return if (element.isJsonPrimitive) element.asBoolean() else null }
        override fun nullValue(): Boolean? { return false }
    }
    class ShortAdapter : BaseTypeAdapter<Short>(Short::class) {
        override fun serialize(value: Short): JsonElement { return JsonPrimitive.of(value.toInt()) }
        override fun deserialize(element: JsonElement): Short? { return if (element.isJsonPrimitive) element.asInt().toShort() else null }
        override fun nullValue(): Short? { return 0 }
    }
    class ByteAdapter : BaseTypeAdapter<Byte>(Byte::class) {
        override fun serialize(value: Byte): JsonElement { return JsonPrimitive.of(value.toInt()) }
        override fun deserialize(element: JsonElement): Byte? { return if (element.isJsonPrimitive) element.asInt().toByte() else null }
        override fun nullValue(): Byte? { return 0 }
    }
    class CharAdapter : BaseTypeAdapter<Char>(Char::class) {
        override fun serialize(value: Char): JsonElement { return JsonPrimitive.of(value.toString()) }
        override fun deserialize(element: JsonElement): Char? { return if (element.isJsonPrimitive) element.asString().firstOrNull() else null }
        override fun nullValue(): Char? { return '\u0000' }
    }

    fun all(): List<com.nidoham.kson.adapter.TypeAdapter<*>> {
        return listOf(StringAdapter(), IntAdapter(), LongAdapter(), DoubleAdapter(), FloatAdapter(), BooleanAdapter(), ShortAdapter(), ByteAdapter(), CharAdapter())
    }
}