package com.nidoham.kson.reflection

import com.nidoham.kson.annotation.*

data class FieldInfo(
    val name: String,
    val type: kotlin.reflect.KType,
    val serializedName: String?,
    val alternateNames: List<String>,
    val isTransient: Boolean,
    val exposeAnnotation: Expose?,
    val since: Double?,
    val until: Double?,
    val defaultValue: String?,
    val jsonAdapterAnnotation: JsonAdapter?,
    val getter: (Any) -> Any?,
    val setter: ((Any, Any?) -> Unit)?
) {
    fun getAllNames(): List<String> = if (alternateNames.isEmpty()) listOf(serializedName ?: name) else listOf(serializedName ?: name) + alternateNames
}

data class ClassInfo(
    val kClass: kotlin.reflect.KClass<*>,
    val fields: List<FieldInfo>,
    val isSealed: Boolean,
    val sealedSubclasses: List<kotlin.reflect.KClass<*>>
) {
    private val fieldMap: Map<String, FieldInfo> by lazy { fields.associateBy { it.name } }
    private val serializedFieldMap: Map<String, FieldInfo> by lazy { fields.mapNotNull { f -> f.serializedName?.let { it to f } }.toMap() }

    fun getField(name: String): FieldInfo? = fieldMap[name]
    fun getFieldBySerializedName(name: String): FieldInfo? = serializedFieldMap[name]

    fun findField(name: String): FieldInfo? {
        fieldMap[name]?.let { return it }
        serializedFieldMap[name]?.let { return it }
        for (field in fields) if (name in field.alternateNames) return field
        return null
    }
}