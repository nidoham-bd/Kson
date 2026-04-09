package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.core.*

class AnyAdapter : BaseTypeAdapter<Any>(Any::class) {

    override fun serialize(value: Any): JsonElement = when (value) {
        is JsonElement -> value; is String -> JsonPrimitive(value); is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value); is Char -> JsonPrimitive(value.toString()); is Enum<*> -> JsonPrimitive(value.name)
        is Map<*, *> -> JsonObject().apply { value.forEach { (k, v) -> add(k?.toString() ?: return@apply, if (v != null) serialize(v) else JsonNull.INSTANCE) } }
        is Collection<*> -> JsonArray().apply { value.forEach { add(if (it != null) serialize(it) else JsonNull.INSTANCE) } }
        is Array<*> -> JsonArray().apply { value.forEach { add(if (it != null) serialize(it) else JsonNull.INSTANCE) } }
        else -> JsonPrimitive(value.toString())
    }

    override fun deserialize(element: JsonElement): Any? = when {
        element.isJsonNull -> null
        element.isString -> element.asString
        element.isBoolean -> element.asBoolean
        element.isNumber -> { val s = element.asString; if (!s.contains('.') && !s.contains('e') && !s.contains('E')) { val l = element.asLong; if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l } else element.asDouble }
        element.isJsonArray -> element.asJsonArray().map { deserialize(it) }
        element.isJsonObject -> { val m = linkedMapOf<String, Any?>(); element.asJsonObject().forEach { (k, v) -> m[k] = deserialize(v) }; m }
        else -> null
    }

    override fun nullValue(): Any? = null
}