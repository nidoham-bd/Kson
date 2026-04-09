package com.nidoham.kson.annotation

import com.nidoham.kson.adapter.TypeAdapter

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonAdapter(
    val value: kotlin.reflect.KClass<out TypeAdapter<*>>,
    val nullSafe: Boolean = true
)