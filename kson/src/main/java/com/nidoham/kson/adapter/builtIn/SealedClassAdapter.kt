package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.annotation.SerializedName
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull
import com.nidoham.kson.core.JsonObject
import com.nidoham.kson.core.JsonPrimitive
import com.nidoham.kson.logging.KsonLogger
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class SealedClassAdapter<T : Any>(
    private val sealedClass: kotlin.reflect.KClass<T>,
    private val discriminatorKey: String = "type",
    private val discriminatorCaseSensitive: Boolean = false
) : BaseTypeAdapter<T>(sealedClass) {

    private val logger = KsonLogger.getLogger(SealedClassAdapter::class)

    private val subclassMap: Map<String, kotlin.reflect.KClass<out T>> by lazy {
        sealedClass.sealedSubclasses.associateBy { subclass ->
            val name = subclass.findAnnotation<SerializedName>()?.value ?: subclass.simpleName?.removeSuffix("Impl") ?: "Unknown"
            if (discriminatorCaseSensitive) name else name.lowercase()
        }
    }

    override fun serialize(value: T): JsonElement {
        val valueClass = value::class
        val serializedName = valueClass.findAnnotation<SerializedName>()?.value ?: valueClass.simpleName?.removeSuffix("Impl") ?: "Unknown"
        val result = serializeObjectToJsonObject(value)
        result.add(discriminatorKey, JsonPrimitive.of(serializedName))
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(element: JsonElement): T? {
        if (element.isJsonNull) return null
        val jsonObject = element.asJsonObject()
        val discriminatorValue = jsonObject.getAsString(discriminatorKey) ?: throw IllegalArgumentException("Missing discriminator '$discriminatorKey'")
        val lookupKey = if (discriminatorCaseSensitive) discriminatorValue else discriminatorValue.lowercase()
        val subclass = subclassMap[lookupKey] ?: throw IllegalArgumentException("Unknown subclass '$discriminatorValue' for ${sealedClass.simpleName}")
        val cleanJson = jsonObject.deepCopy().apply { remove(discriminatorKey) }
        return deserializeFromJsonObject(cleanJson, subclass)
    }

    private fun serializeObjectToJsonObject(value: Any): JsonObject {
        val obj = JsonObject()
        for (property in value::class.memberProperties) {
            try {
                val propValue = property.getter.call(value)
                val propName = property.findAnnotation<com.nidoham.kson.annotation.SerializedName>()?.value ?: property.name
                obj.add(propName, when (propValue) {
                    null -> JsonNull.INSTANCE; is String -> JsonPrimitive.of(propValue); is Number -> JsonPrimitive.of(propValue)
                    is Boolean -> JsonPrimitive.of(propValue); is Enum<*> -> JsonPrimitive.of(propValue.name); else -> JsonPrimitive.ofAny(propValue)
                })
            } catch (e: Exception) { logger.warn("Failed to serialize ${property.name}: ${e.message}") }
        }
        return obj
    }

    @Suppress("UNCHECKED_CAST")
    private fun <S : T> deserializeFromJsonObject(jsonObject: JsonObject, subclass: kotlin.reflect.KClass<S>): T? {
        val constructor = subclass.primaryConstructor ?: throw IllegalArgumentException("No primary constructor for ${subclass.simpleName}")
        val params = constructor.parameters.associateWith { param ->
            val name = param.findAnnotation<com.nidoham.kson.annotation.SerializedName>()?.value ?: param.name ?: return null
            val elem = jsonObject.get(name) ?: if (param.isOptional) null else return null
            when {
                elem?.isJsonNull ?: false -> null
                param.type.classifier == String::class -> elem?.asString() ?: ""
                param.type.classifier == Int::class -> elem?.asInt() ?: 0
                param.type.classifier == Long::class -> elem?.asLong() ?: 0
                param.type.classifier == Double::class -> elem?.asDouble() ?: 0
                param.type.classifier == Float::class -> elem?.asFloat() ?: 0
                param.type.classifier == Boolean::class -> elem?.asBoolean() ?: false
                else -> elem.toString()
            }
        }
        return constructor.callBy(params)
    }
}