package com.nidoham.kson.annotation

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SerializedName(
    val value: String,
    val alternate: Array<String> = []
)