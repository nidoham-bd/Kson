package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.annotation.SerializedName
import com.nidoham.kson.core.*
import com.nidoham.kson.logging.KsonLogger
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class SealedClassAdapter<T : Any>(
    private val sealedClass: KClass<T>,
    private val discriminatorKey: String = "type",
    private val discriminatorCaseSensitive: Boolean = false
) : BaseTypeAdapter<T>(sealedClass) {

    private val logger = KsonLogger.getLogger(SealedClassAdapter::class)

    private val subclassMap: Map<String, KClass<out T>> by lazy {
        sealedClass.sealedSubclasses.associateBy { subclass ->
            val name = subclass.findAnnotation<SerializedName>()?.value ?: subclass.simpleName?.removeSuffix("Impl") ?: "Unknown"
            if (discriminatorCaseSensitive) name else name.lowercase()
        }
    }

    override fun serialize(value: T): JsonElement {
        val valueClass = value::class
        val serializedName = valueClass.findAnnotation<SerializedName>()?.value ?: valueClass.simpleName?.removeSuffix("Impl") ?: "Unknown"
        val result = serializeObjectToJsonObject(value)
        result.add(discriminatorKey, JsonPrimitive(serializedName))
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

    private fun serializeObjectToJsonObject(value: Any): JsonObject = JsonObject().apply {
        for (property in value::class.memberProperties) {
            try {
                val propValue = property.getter.call(value)
                val propName = property.findAnnotation<com.nidoham.kson.annotation.SerializedName>()?.value ?: property.name
                add(propName, when (propValue) {
                    null -> JsonNull.INSTANCE; is String -> JsonPrimitive(propValue); is Number -> JsonPrimitive(propValue)
                    is Boolean -> JsonPrimitive(propValue); is Enum<*> -> JsonPrimitive(propValue.name); else -> JsonPrimitive(propValue.toString())
                })
            } catch (e: Exception) { logger.warn("Failed to serialize ${property.name}: ${e.message}") }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <S : T> deserializeFromJsonObject(jsonObject: JsonObject, subclass: KClass<S>): T? {
        val constructor = subclass.primaryConstructor ?: throw IllegalArgumentException("No primary constructor for ${subclass.simpleName}")
        val params = constructor.parameters.associateWith { param ->
            val name = param.findAnnotation<com.nidoham.kson.annotation.SerializedName>()?.value ?: param.name ?: return null
            val element = jsonObject.get(name) ?: if (param.isOptional) null else return null
            when {
                element.isJsonNull -> null
                param.type.classifier == String::class -> element.asString
                param.type.classifier == Int::class -> element.asInt
                param.type.classifier == Long::class -> element.asLong
                param.type.classifier == Double::class -> element.asDouble
                param.type.classifier == Float::class -> element.asFloat
                param.type.classifier == Boolean::class -> element.asBoolean
                else -> element.toString()
            }
        }
        return constructor.callBy(params)
    }
}