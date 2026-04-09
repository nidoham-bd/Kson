package com.nidoham.kson.annotation

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Expose(
    val serialize: Boolean = true,
    val deserialize: Boolean = true
)