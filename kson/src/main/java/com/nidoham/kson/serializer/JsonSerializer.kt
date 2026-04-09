package com.nidoham.kson.serializer

import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.config.KsonConfig
import com.nidoham.kson.core.JsonArray
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull
import com.nidoham.kson.core.JsonObject
import com.nidoham.kson.core.JsonPrimitive
import com.nidoham.kson.logging.KsonLogger
import com.nidoham.kson.reflection.ClassInfoCache
import com.nidoham.kson.token.TypeToken
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

class JsonSerializer(
    private val config: KsonConfig,
    private val classInfoCache: ClassInfoCache
) {
    private val logger = KsonLogger.getLogger(JsonSerializer::class)
    private val writer = JsonWriter(config.prettyPrint, config.indent, config.serializeNulls, config.escapeHtmlChars)

    fun <T> serialize(value: T?): String {
        return if (value == null) "null" else writer.write(toJsonElement(value, value!!::class))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> toJsonElement(value: T?, type: KClass<*>): JsonElement {
        if (value == null) return JsonNull.INSTANCE
        logger.debug("Serializing ${type.simpleName}")

        val adapter = findAdapter(type)
        if (adapter != null) return adapter.toJson(value)

        return when {
            type == String::class -> JsonPrimitive.of(value as String)
            type == Int::class || type == Integer::class -> JsonPrimitive.of(value as Int)
            type == Long::class -> JsonPrimitive.of(value as Long)
            type == Double::class -> JsonPrimitive.of(value as Double)
            type == Float::class -> JsonPrimitive.of(value as Float)
            type == Boolean::class -> JsonPrimitive.of(value as Boolean)
            type == Short::class -> JsonPrimitive.of((value as Short).toInt())
            type == Byte::class -> JsonPrimitive.of((value as Byte).toInt())
            type == Char::class -> JsonPrimitive.of((value as Char).toString())
            type == BigDecimal::class -> JsonPrimitive.of(value as BigDecimal)
            type == BigInteger::class -> JsonPrimitive.of(value as BigInteger)
            type == JsonElement::class -> value as JsonElement
            type == Any::class -> serializeAny(value)
            type.java.isArray -> serializeArray(value)
            type.java.isEnum -> JsonPrimitive.of((value as Enum<*>).name)
            Collection::class.java.isAssignableFrom(type.java) -> serializeCollection(value as Collection<*>)
            Map::class.java.isAssignableFrom(type.java) -> serializeMap(value as Map<*, *>)
            Pair::class.java.isAssignableFrom(type.java) -> serializePair(value as Pair<*, *>)
            Triple::class.java.isAssignableFrom(type.java) -> serializeTriple(value as Triple<*, *, *>)
            else -> serializeObject(value, type)
        }
    }

    fun <T> toJsonElement(value: T?, typeToken: TypeToken<T>): JsonElement {
        return toJsonElement(value, typeToken.rawType)
    }

    private fun serializeObject(value: Any, type: KClass<*>): JsonObject {
        val jsonObject = JsonObject()
        val classInfo = classInfoCache.getClassInfo(type)
        for (fieldInfo in classInfo.fields) {
            if (fieldInfo.isTransient) continue
            if (fieldInfo.exposeAnnotation != null && !fieldInfo.exposeAnnotation.serialize) continue
            if (!checkVersion(fieldInfo)) continue

            val fieldValue = fieldInfo.getter(value)
            if (fieldValue == null && !config.serializeNulls) continue

            val serializedName = fieldInfo.serializedName ?: run {
                config.fieldNamingStrategy?.translateName(fieldInfo.name) ?: fieldInfo.name
            }

            val fieldElement = if (fieldValue != null) toJsonElement(fieldValue, fieldInfo.type.jvmErasure) else JsonNull.INSTANCE
            jsonObject.add(serializedName, fieldElement)
        }
        return jsonObject
    }

    private fun serializeCollection(collection: Collection<*>): JsonArray {
        val arr = JsonArray()
        for (item in collection) {
            arr.add(if (item != null) toJsonElement(item, item::class) else JsonNull.INSTANCE)
        }
        return arr
    }

    private fun serializeMap(map: Map<*, *>): JsonObject {
        val jsonObject = JsonObject()
        for ((key, value) in map) {
            val keyStr = when (key) {
                is String -> key
                is Number -> key.toString()
                is Boolean -> key.toString()
                is Enum<*> -> key.name
                else -> key?.toString() ?: continue
            }
            jsonObject.add(keyStr, if (value != null) toJsonElement(value, value::class) else JsonNull.INSTANCE)
        }
        return jsonObject
    }

    private fun serializePair(pair: Pair<*, *>): JsonArray {
        val arr = JsonArray()
        arr.add(if (pair.first != null) toJsonElement(pair.first, pair.first!!::class) else JsonNull.INSTANCE)
        arr.add(if (pair.second != null) toJsonElement(pair.second, pair.second!!::class) else JsonNull.INSTANCE)
        return arr
    }

    private fun serializeTriple(triple: Triple<*, *, *>): JsonArray {
        val arr = JsonArray()
        arr.add(if (triple.first != null) toJsonElement(triple.first, triple.first!!::class) else JsonNull.INSTANCE)
        arr.add(if (triple.second != null) toJsonElement(triple.second, triple.second!!::class) else JsonNull.INSTANCE)
        arr.add(if (triple.third != null) toJsonElement(triple.third, triple.third!!::class) else JsonNull.INSTANCE)
        return arr
    }

    @Suppress("UNCHECKED_CAST")
    private fun serializeArray(value: Any): JsonArray {
        val arr = JsonArray()
        for (item in value as Array<*>) {
            arr.add(if (item != null) toJsonElement(item, item::class) else JsonNull.INSTANCE)
        }
        return arr
    }

    @Suppress("UNCHECKED_CAST")
    private fun serializeAny(value: Any): JsonElement {
        return when (value) {
            is String -> JsonPrimitive.of(value)
            is Number -> JsonPrimitive.of(value)
            is Boolean -> JsonPrimitive.of(value)
            is Char -> JsonPrimitive.of(value.toString())
            is Enum<*> -> JsonPrimitive.of(value.name)
            is Collection<*> -> serializeCollection(value)
            is Map<*, *> -> serializeMap(value)
            is Array<*> -> serializeArray(value)
            is JsonElement -> value
            else -> serializeObject(value, value::class)
        }
    }

    private fun checkVersion(fieldInfo: com.nidoham.kson.reflection.FieldInfo): Boolean {
        if (config.version == 0.0) return true
        fieldInfo.since?.let { if (config.version < it) return false }
        fieldInfo.until?.let { if (config.version >= it) return false }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun findAdapter(type: KClass<*>): TypeAdapter<Any>? {
        for (factory in config.typeAdapterFactories) {
            factory.create(type)?.let { return it as TypeAdapter<Any> }
        }
        return config.typeAdapters[type] as? TypeAdapter<Any>
    }
}