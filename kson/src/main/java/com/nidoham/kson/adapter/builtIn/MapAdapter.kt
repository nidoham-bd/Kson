package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull
import com.nidoham.kson.core.JsonObject
import com.nidoham.kson.core.JsonPrimitive

class MapAdapter<K : Any, V : Any>(
    private val keyAdapter: TypeAdapter<K>?,
    private val valueAdapter: TypeAdapter<V>?,
    private val keyType: kotlin.reflect.KClass<K>,
    private val valueType: kotlin.reflect.KClass<V>
) : BaseTypeAdapter<Map<K, V>>(Map::class as kotlin.reflect.KClass<Map<K, V>>) {

    override fun serialize(value: Map<K, V>): JsonElement {
        val jsonObject = JsonObject()
        for ((key, val_) in value) {
            val keyStr = when (key) {
                is String -> key; is Number -> key.toString(); is Boolean -> key.toString(); is Enum<*> -> key.name; else -> key.toString()
            }
            jsonObject.add(keyStr, if (val_ != null && valueAdapter != null) valueAdapter.toJson(val_) else if (val_ != null) JsonPrimitive.ofAny(val_) else JsonNull.INSTANCE)
        }
        return jsonObject
    }

    override fun deserialize(element: JsonElement): Map<K, V>? {
        if (!element.isJsonObject) return null
        val map = mutableMapOf<K, V>()
        for ((keyStr, valueElement) in element.asJsonObject().entrySet()) {
            val key = deserializeKey(keyStr) ?: continue
            val value: V? = if (valueElement.isJsonNull) null else if (valueAdapter != null) valueAdapter.fromJson(valueElement) else null
            if (value != null) map[key] = value
        }
        return map
    }

    override fun nullValue(): Map<K, V>? {
        return emptyMap()
    }

    @Suppress("UNCHECKED_CAST")
    private fun deserializeKey(keyStr: String): K? {
        return when (keyType) {
            String::class -> keyStr as K
            Int::class -> keyStr.toIntOrNull() as? K
            Long::class -> keyStr.toLongOrNull() as? K
            Double::class -> keyStr.toDoubleOrNull() as? K
            Boolean::class -> when (keyStr.lowercase()) { "true" -> true as K; "false" -> false as K; else -> null }
            else -> null
        }
    }
}