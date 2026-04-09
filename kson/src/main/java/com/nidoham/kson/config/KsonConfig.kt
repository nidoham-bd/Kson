package com.nidoham.kson.config

import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.adapter.TypeAdapterFactory
import com.nidoham.kson.naming.FieldNamingStrategy
import kotlin.reflect.KClass

data class KsonConfig(
    val prettyPrint: Boolean = false,
    val indent: String = "  ",
    val serializeNulls: Boolean = false,
    val escapeHtmlChars: Boolean = false,
    val lenient: Boolean = false,
    val fieldNamingStrategy: FieldNamingStrategy? = null,
    val version: Double = 0.0,
    val excludeFieldsWithoutExposeAnnotation: Boolean = false,
    val requireNonNullFields: Boolean = false,
    val typeAdapters: Map<KClass<*>, TypeAdapter<*>> = emptyMap(),
    val typeAdapterFactories: List<TypeAdapterFactory> = emptyList(),
    val complexMapKeySerialization: Boolean = false,
    val useDefaultValues: Boolean = true
)