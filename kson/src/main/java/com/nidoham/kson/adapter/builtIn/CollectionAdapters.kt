package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.core.JsonArray
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull
import com.nidoham.kson.core.JsonPrimitive

class ListAdapter<T : Any>(
    private val elementAdapter: TypeAdapter<T>?,
    private val elementType: kotlin.reflect.KClass<T>
) : BaseTypeAdapter<List<T>>(List::class as kotlin.reflect.KClass<List<T>>) {

    override fun serialize(value: List<T>): JsonElement {
        val arr = JsonArray()
        for (item in value) {
            arr.add(if (item != null && elementAdapter != null) elementAdapter.toJson(item) else if (item != null) serializeAny(item) else JsonNull.INSTANCE)
        }
        return arr
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(element: JsonElement): List<T>? {
        if (!element.isJsonArray) return null
        val list = mutableListOf<T>()
        for (item in element.asJsonArray()) {
            val value: T? = if (item.isJsonNull) null else if (elementAdapter != null) elementAdapter.fromJson(item) else deserializeAny(item) as? T
            list.add(value ?: continue)
        }
        return list
    }

    override fun nullValue(): List<T>? {
        return emptyList()
    }
}

class SetAdapter<T : Any>(
    private val elementAdapter: TypeAdapter<T>?,
    private val elementType: kotlin.reflect.KClass<T>
) : BaseTypeAdapter<Set<T>>(Set::class as kotlin.reflect.KClass<Set<T>>) {

    override fun serialize(value: Set<T>): JsonElement {
        val arr = JsonArray()
        for (item in value) {
            arr.add(if (item != null && elementAdapter != null) elementAdapter.toJson(item) else if (item != null) serializeAny(item) else JsonNull.INSTANCE)
        }
        return arr
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(element: JsonElement): Set<T>? {
        if (!element.isJsonArray) return null
        val set = mutableSetOf<T>()
        for (item in element.asJsonArray()) {
            val value: T? = if (item.isJsonNull) null else if (elementAdapter != null) elementAdapter.fromJson(item) else deserializeAny(item) as? T
            value?.let { set.add(it) }
        }
        return set
    }

    override fun nullValue(): Set<T>? {
        return emptySet()
    }
}

internal fun serializeAny(value: Any): JsonElement {
    return when (value) {
        is String -> JsonPrimitive.of(value)
        is Number -> JsonPrimitive.of(value)
        is Boolean -> JsonPrimitive.of(value)
        is Enum<*> -> JsonPrimitive.of(value.name)
        is Collection<*> -> {
            val arr = JsonArray()
            value.forEach { arr.add(if (it != null) serializeAny(it) else JsonNull.INSTANCE) }
            arr
        }
        else -> JsonPrimitive.ofAny(value)
    }
}

@Suppress("UNCHECKED_CAST")
internal fun deserializeAny(element: JsonElement): Any? {
    val result: Any? = when {
        element.isJsonNull -> null
        element.isString -> element.asString()
        element.isBoolean -> element.asBoolean()
        element.isNumber -> {
            val s = element.asString()
            if (s.contains('.') || s.contains('e') || s.contains('E')) element.asDouble()
            else {
                val l = element.asLong()
                if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l
            }
        }
        element.isJsonArray -> {
            val list = mutableListOf<Any?>()
            element.asJsonArray().forEach { list.add(deserializeAny(it)) }
            list
        }
        element.isJsonObject -> {
            val m = mutableMapOf<String, Any?>()
            element.asJsonObject().forEach { (k, v) -> m[k] = deserializeAny(v) }
            m
        }
        else -> null
    }
    return result
}