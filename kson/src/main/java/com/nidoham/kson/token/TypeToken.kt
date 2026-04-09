package com.nidoham.kson.token

import kotlin.reflect.jvm.jvmErasure

abstract class TypeToken<T> {
    val type: kotlin.reflect.KType by lazy { this::class.supertypes.firstOrNull() ?: throw IllegalStateException("Could not determine type") }
    val rawType: kotlin.reflect.KClass<*> by lazy { type.jvmErasure }
    val typeArguments: List<kotlin.reflect.KType> by lazy { type.arguments.mapNotNull { it.type } }
    val isGeneric: Boolean by lazy { typeArguments.isNotEmpty() }

    fun simpleTypeName(): String = buildString {
        append(rawType.simpleName)
        if (typeArguments.isNotEmpty()) { append("<"); append(typeArguments.joinToString(", ") { it.jvmErasure.simpleName ?: "Unknown" }); append(">") }
    }

    override fun toString(): String = "TypeToken<${simpleTypeName()}>"
}

inline fun <reified T> typeToken(): TypeToken<T> = object : TypeToken<T>() {}