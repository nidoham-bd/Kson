package com.nidoham.kson.adapter

import kotlin.reflect.KClass

interface TypeAdapterFactory {
    fun <T : Any> create(type: KClass<T>): TypeAdapter<T>?
}

class LambdaTypeAdapterFactory(private val factory: (KClass<*>) -> TypeAdapter<*>?) : TypeAdapterFactory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> create(type: KClass<T>): TypeAdapter<T>? {
        return factory(type) as? TypeAdapter<T>
    }
}

fun typeAdapterFactory(factory: (KClass<*>) -> TypeAdapter<*>?): TypeAdapterFactory {
    return LambdaTypeAdapterFactory(factory)
}