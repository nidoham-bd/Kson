package com.nidoham.kson.adapter

import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull

interface TypeAdapter<T> {
    fun toJson(value: T?): JsonElement
    fun fromJson(element: JsonElement): T?
    fun getType(): kotlin.reflect.KClass<T>
    fun isNullSafe(): Boolean = true
}

abstract class BaseTypeAdapter<T>(
    private val type: kotlin.reflect.KClass<T>,
    private val nullSafe: Boolean = true
) : TypeAdapter<T> {

    override fun getType(): kotlin.reflect.KClass<T> = type
    override fun isNullSafe(): Boolean = nullSafe
    protected open fun nullValue(): T? = null

    override fun toJson(value: T?): JsonElement {
        if (value == null) return if (nullSafe) handleNullSerialization() else JsonNull.INSTANCE
        return serialize(value)
    }

    override fun fromJson(element: JsonElement): T? {
        if (element.isJsonNull) return if (nullSafe) nullValue() else null
        return deserialize(element)
    }

    protected open fun handleNullSerialization(): JsonElement = JsonNull.INSTANCE
    protected abstract fun serialize(value: T): JsonElement
    protected abstract fun deserialize(element: JsonElement): T?
}