package com.nidoham.kson.adapter

import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull

interface TypeAdapter<T : Any> {
    fun toJson(value: T?): JsonElement
    fun fromJson(element: JsonElement): T?
    fun getType(): kotlin.reflect.KClass<T>
    fun isNullSafe(): Boolean = true
}

abstract class BaseTypeAdapter<T : Any>(
    private val type: kotlin.reflect.KClass<T>,
    private val nullSafe: Boolean = true
) : TypeAdapter<T> {

    override fun getType(): kotlin.reflect.KClass<T> {
        return type
    }

    override fun isNullSafe(): Boolean {
        return nullSafe
    }

    protected open fun nullValue(): T? {
        return null
    }

    override fun toJson(value: T?): JsonElement {
        if (value == null) {
            return JsonNull.INSTANCE
        }
        return serialize(value)
    }

    override fun fromJson(element: JsonElement): T? {
        if (element.isJsonNull) {
            return if (nullSafe) nullValue() else null
        }
        return deserialize(element)
    }

    protected abstract fun serialize(value: T): JsonElement
    protected abstract fun deserialize(element: JsonElement): T?
}