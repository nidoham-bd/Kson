package com.nidoham.kson.adapter

import kotlin.reflect.KClass

interface TypeAdapterFactory {
    fun <T> create(type: KClass<T>): TypeAdapter<T>?
}

class LambdaTypeAdapterFactory(private val factory: (KClass<*>) -> TypeAdapter<*>?) : TypeAdapterFactory {
    @Suppress("UNCHECKED_CAST")
    override fun <T> create(type: KClass<T>): TypeAdapter<T>? = factory(type) as? TypeAdapter<T>
}

fun typeAdapterFactory(factory: (KClass<*>) -> TypeAdapter<*>?): TypeAdapterFactory = LambdaTypeAdapterFactory(factory)