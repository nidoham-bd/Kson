package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonPrimitive
import com.nidoham.kson.logging.KsonLogger

class EnumAdapter<T : Enum<T>>(
    private val enumClass: kotlin.reflect.KClass<T>,
    private val caseSensitive: Boolean = false,
    private val unknownValuePolicy: UnknownValuePolicy = UnknownValuePolicy.THROW_EXCEPTION
) : BaseTypeAdapter<T>(enumClass) {

    private val logger = KsonLogger.getLogger(EnumAdapter::class)
    private val enumConstants: Array<T> by lazy { enumClass.java.enumConstants as Array<T> }
    private val nameMap: Map<String, T> by lazy { enumConstants.associateBy { if (caseSensitive) it.name else it.name.lowercase() } }

    override fun serialize(value: T): JsonElement {
        return JsonPrimitive.of(value.name)
    }

    override fun deserialize(element: JsonElement): T? {
        if (element.isJsonNull) return nullValue()
        val name = if (element.isJsonPrimitive) element.asString() else return null
        val lookupName = if (caseSensitive) name else name.lowercase()
        nameMap[lookupName]?.let { return it }
        name.toIntOrNull()?.let { if (it in enumConstants.indices) return enumConstants[it] }

        return when (unknownValuePolicy) {
            UnknownValuePolicy.THROW_EXCEPTION -> throw IllegalArgumentException("Unknown enum value '$name' for ${enumClass.simpleName}. Valid: ${enumConstants.joinToString { it.name }}")
            UnknownValuePolicy.USE_NULL -> { logger.warn("Unknown enum value '$name', returning null"); null }
            UnknownValuePolicy.USE_FIRST -> { logger.warn("Unknown enum value '$name', using first"); enumConstants.firstOrNull() }
            UnknownValuePolicy.USE_DEFAULT -> { logger.warn("Unknown enum value '$name', using default"); enumConstants.firstOrNull() }
        }
    }

    override fun nullValue(): T? {
        return enumConstants.firstOrNull()
    }

    enum class UnknownValuePolicy { THROW_EXCEPTION, USE_NULL, USE_FIRST, USE_DEFAULT }
}