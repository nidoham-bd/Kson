package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.core.JsonArray
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull
import com.nidoham.kson.core.JsonPrimitive

class PairAdapter<A : Any, B : Any>(
    private val firstAdapter: TypeAdapter<A>?,
    private val secondAdapter: TypeAdapter<B>?,
    private val firstType: kotlin.reflect.KClass<A>,
    private val secondType: kotlin.reflect.KClass<B>
) : BaseTypeAdapter<Pair<A, B>>(Pair::class as kotlin.reflect.KClass<Pair<A, B>>) {

    override fun serialize(value: Pair<A, B>): JsonElement {
        val arr = JsonArray()
        arr.add(serializeElement(value.first, firstAdapter))
        arr.add(serializeElement(value.second, secondAdapter))
        return arr
    }

    override fun deserialize(element: JsonElement): Pair<A, B>? {
        if (!element.isJsonArray || element.asJsonArray().size() < 2) return null
        val arr = element.asJsonArray()
        val first = deserializeElement(arr[0], firstAdapter, firstType)
        val second = deserializeElement(arr[1], secondAdapter, secondType)
        return if (first != null && second != null) Pair(first, second) else null
    }

    override fun nullValue(): Pair<A, B>? {
        return null
    }

    private fun <T : Any> serializeElement(value: T?, adapter: TypeAdapter<T>?): JsonElement {
        return when {
            value == null -> JsonNull.INSTANCE
            adapter != null -> adapter.toJson(value)
            else -> when (value) { is String -> JsonPrimitive.of(value); is Number -> JsonPrimitive.of(value); is Boolean -> JsonPrimitive.of(value); else -> JsonPrimitive.ofAny(value) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> deserializeElement(element: JsonElement, adapter: TypeAdapter<T>?, type: kotlin.reflect.KClass<T>): T? {
        return when {
            element.isJsonNull -> null
            adapter != null -> adapter.fromJson(element)
            else -> when (type) {
                String::class -> element.asString() as T; Int::class -> element.asInt() as T; Long::class -> element.asLong() as T
                Double::class -> element.asDouble() as T; Float::class -> element.asFloat() as T; Boolean::class -> element.asBoolean() as T
                else -> null
            }
        }
    }
}