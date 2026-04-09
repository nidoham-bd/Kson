package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.core.JsonArray
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull
import com.nidoham.kson.core.JsonObject
import com.nidoham.kson.core.JsonPrimitive

class AnyAdapter : BaseTypeAdapter<Any>(Any::class) {

    override fun serialize(value: Any): JsonElement {
        return when (value) {
            is JsonElement -> value
            is String -> JsonPrimitive.of(value)
            is Number -> JsonPrimitive.of(value)
            is Boolean -> JsonPrimitive.of(value)
            is Char -> JsonPrimitive.of(value.toString())
            is Enum<*> -> JsonPrimitive.of(value.name)
            is Map<*, *> -> {
                val obj = JsonObject()
                value.forEach { (k, v) -> obj.add(k?.toString() ?: return@forEach, if (v != null) serialize(v) else JsonNull.INSTANCE) }
                obj
            }
            is Collection<*> -> {
                val arr = JsonArray()
                value.forEach { arr.add(if (it != null) serialize(it) else JsonNull.INSTANCE) }
                arr
            }
            is Array<*> -> {
                val arr = JsonArray()
                value.forEach { arr.add(if (it != null) serialize(it) else JsonNull.INSTANCE) }
                arr
            }
            else -> JsonPrimitive.ofAny(value)
        }
    }

    override fun deserialize(element: JsonElement): Any? {
        val result: Any? = when {
            element.isJsonNull -> null
            element.isString -> element.asString()
            element.isBoolean -> element.asBoolean()
            element.isNumber -> {
                val s = element.asString()
                if (!s.contains('.') && !s.contains('e') && !s.contains('E')) {
                    val l = element.asLong()
                    if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l
                } else {
                    element.asDouble()
                }
            }
            element.isJsonArray -> {
                val list = mutableListOf<Any?>()
                element.asJsonArray().forEach { list.add(deserializeAny(it)) }
                list
            }
            element.isJsonObject -> {
                val m = linkedMapOf<String, Any?>()
                element.asJsonObject().forEach { (k, v) -> m[k] = deserializeAny(v) }
                m
            }
            else -> null
        }
        return result
    }

    override fun nullValue(): Any? {
        return null
    }
}