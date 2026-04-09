package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.core.*

class ListAdapter<T>(
    private val elementAdapter: TypeAdapter<T>?,
    private val elementType: kotlin.reflect.KClass<T>
) : BaseTypeAdapter<List<T>>(List::class) {

    override fun serialize(value: List<T>): JsonElement = JsonArray().apply {
        for (item in value) add(if (item != null && elementAdapter != null) elementAdapter.toJson(item) else if (item != null) serializeAny(item) else JsonNull.INSTANCE)
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

    override fun nullValue(): List<T>? = emptyList()
}

class SetAdapter<T>(
    private val elementAdapter: TypeAdapter<T>?,
    private val elementType: kotlin.reflect.KClass<T>
) : BaseTypeAdapter<Set<T>>(Set::class) {

    override fun serialize(value: Set<T>): JsonElement = JsonArray().apply {
        for (item in value) add(if (item != null && elementAdapter != null) elementAdapter.toJson(item) else if (item != null) serializeAny(item) else JsonNull.INSTANCE)
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

    override fun nullValue(): Set<T>? = emptySet()
}

internal fun serializeAny(value: Any): JsonElement = when (value) {
    is String -> JsonPrimitive(value); is Number -> JsonPrimitive(value); is Boolean -> JsonPrimitive(value)
    is Enum<*> -> JsonPrimitive(value.name)
    is Collection<*> -> JsonArray().apply { value.forEach { add(if (it != null) serializeAny(it) else JsonNull.INSTANCE) } }
    else -> JsonPrimitive(value.toString())
}

@Suppress("UNCHECKED_CAST")
internal fun deserializeAny(element: JsonElement): Any? = when {
    element.isJsonNull -> null
    element.isString -> element.asString
    element.isBoolean -> element.asBoolean
    element.isNumber -> { val s = element.asString; if (s.contains('.') || s.contains('e') || s.contains('E')) element.asDouble else { val l = element.asLong; if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l } }
    element.isJsonArray -> element.asJsonArray().map { deserializeAny(it) }.toMutableList()
    element.isJsonObject -> { val m = mutableMapOf<String, Any?>(); element.asJsonObject().forEach { (k, v) -> m[k] = deserializeAny(v) }; m }
    else -> null
}