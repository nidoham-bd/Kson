package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.core.*

class PairAdapter<A, B>(
    private val firstAdapter: TypeAdapter<A>?,
    private val secondAdapter: TypeAdapter<B>?,
    private val firstType: kotlin.reflect.KClass<A>,
    private val secondType: kotlin.reflect.KClass<B>
) : BaseTypeAdapter<Pair<A, B>>(Pair::class) {

    override fun serialize(value: Pair<A, B>): JsonElement = JsonArray().apply {
        add(serializeElement(value.first, firstAdapter))
        add(serializeElement(value.second, secondAdapter))
    }

    override fun deserialize(element: JsonElement): Pair<A, B>? {
        if (!element.isJsonArray || element.asJsonArray().size() < 2) return null
        val arr = element.asJsonArray()
        val first = deserializeElement(arr[0], firstAdapter, firstType)
        val second = deserializeElement(arr[1], secondAdapter, secondType)
        return if (first != null && second != null) Pair(first, second) else null
    }

    override fun nullValue(): Pair<A, B>? = null

    private fun <T> serializeElement(value: T?, adapter: TypeAdapter<T>?): JsonElement = when {
        value == null -> JsonNull.INSTANCE
        adapter != null -> adapter.toJson(value)
        else -> when (value) { is String -> JsonPrimitive(value); is Number -> JsonPrimitive(value); is Boolean -> JsonPrimitive(value); else -> JsonPrimitive(value.toString()) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> deserializeElement(element: JsonElement, adapter: TypeAdapter<T>?, type: kotlin.reflect.KClass<T>): T? = when {
        element.isJsonNull -> null
        adapter != null -> adapter.fromJson(element)
        else -> when (type) {
            String::class -> element.asString as T; Int::class -> element.asInt as T; Long::class -> element.asLong as T
            Double::class -> element.asDouble as T; Float::class -> element.asFloat as T; Boolean::class -> element.asBoolean as T
            else -> null
        }
    }
}