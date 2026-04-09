package com.nidoham.kson.naming

fun interface FieldNamingStrategy {
    fun translateName(fieldName: String): String
}