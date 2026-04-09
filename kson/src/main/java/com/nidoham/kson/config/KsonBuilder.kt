package com.nidoham.kson.config

import com.nidoham.kson.Kson
import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.adapter.TypeAdapterFactory
import com.nidoham.kson.adapter.builtIn.DateAdapter
import com.nidoham.kson.adapter.builtIn.EnumAdapter
import com.nidoham.kson.naming.FieldNamingPolicies
import com.nidoham.kson.reflection.ClassInfoCache
import kotlin.reflect.KClass

class KsonBuilder {
    private var prettyPrint = false
    private var indent = "  "
    private var serializeNulls = false
    private var escapeHtmlChars = false
    private var lenient = false
    private var fieldNamingStrategy: com.nidoham.kson.naming.FieldNamingStrategy? = null
    private var version = 0.0
    private var excludeFieldsWithoutExposeAnnotation = false
    private var requireNonNullFields = false
    private var complexMapKeySerialization = false
    private var useDefaultValues = true
    private val typeAdapters = mutableMapOf<KClass<*>, TypeAdapter<*>>()
    private val typeAdapterFactories = mutableListOf<TypeAdapterFactory>()
    private val classInfoCache = ClassInfoCache()

    fun setPrettyPrinting(): KsonBuilder { prettyPrint = true; return this }
    fun setIndent(indent: String): KsonBuilder { this.indent = indent; if (indent.isNotEmpty()) prettyPrint = true; return this }
    fun serializeNulls(): KsonBuilder { serializeNulls = true; return this }
    fun enableHtmlEscaping(): KsonBuilder { escapeHtmlChars = true; return this }
    fun disableHtmlEscaping(): KsonBuilder { escapeHtmlChars = false; return this }
    fun setLenient(): KsonBuilder { lenient = true; return this }
    fun setFieldNamingPolicyLowerCaseUnderscores(): KsonBuilder { fieldNamingStrategy = FieldNamingPolicies.LOWER_CASE_WITH_UNDERSCORES; return this }
    fun setFieldNamingPolicyUpperCaseUnderscores(): KsonBuilder { fieldNamingStrategy = FieldNamingPolicies.UPPER_CASE_WITH_UNDERSCORES; return this }
    fun setFieldNamingPolicyLowerCaseDashes(): KsonBuilder { fieldNamingStrategy = FieldNamingPolicies.LOWER_CASE_WITH_DASHES; return this }
    fun setVersion(version: Double): KsonBuilder { this.version = version; return this }
    fun excludeFieldsWithoutExposeAnnotation(): KsonBuilder { excludeFieldsWithoutExposeAnnotation = true; return this }
    fun requireNonNullFields(): KsonBuilder { requireNonNullFields = true; return this }
    fun enableComplexMapKeySerialization(): KsonBuilder { complexMapKeySerialization = true; return this }
    fun disableDefaultValues(): KsonBuilder { useDefaultValues = false; return this }

    fun <T : Any> registerTypeAdapter(type: KClass<T>, adapter: TypeAdapter<T>): KsonBuilder { typeAdapters[type] = adapter; return this }
    fun registerTypeAdapterFactory(factory: TypeAdapterFactory): KsonBuilder { typeAdapterFactories.add(factory); return this }
    fun registerTypeAdapters(adapters: Map<KClass<*>, TypeAdapter<*>>): KsonBuilder { typeAdapters.putAll(adapters); return this }

    inline fun <reified T : Enum<T>> registerEnumAdapter(caseSensitive: Boolean = false, policy: EnumAdapter.UnknownValuePolicy = EnumAdapter.UnknownValuePolicy.THROW_EXCEPTION): KsonBuilder {
        return registerTypeAdapter(T::class, EnumAdapter(T::class, caseSensitive, policy))
    }

    fun registerDateAdapter(format: String = DateAdapter.ISO_8601, serializeAsTimestamp: Boolean = false): KsonBuilder {
        return registerTypeAdapter(java.util.Date::class, DateAdapter(format, java.util.TimeZone.getTimeZone("UTC"), false, serializeAsTimestamp))
    }

    fun registerIso8601DateAdapter(): KsonBuilder { return registerDateAdapter(DateAdapter.ISO_8601) }
    fun registerTimestampDateAdapter(): KsonBuilder { return registerDateAdapter(serializeAsTimestamp = true) }
    fun clearTypeAdapters(): KsonBuilder { typeAdapters.clear(); typeAdapterFactories.clear(); return this }
    fun clearCache(): KsonBuilder { classInfoCache.clear(); return this }

    fun create(): Kson {
        val config = KsonConfig(prettyPrint, indent, serializeNulls, escapeHtmlChars, lenient, fieldNamingStrategy, version, excludeFieldsWithoutExposeAnnotation, requireNonNullFields, typeAdapters.toMap(), typeAdapterFactories.toList(), complexMapKeySerialization, useDefaultValues)
        return Kson(config, classInfoCache)
    }

    companion object {
        @JvmStatic
        fun create(): KsonBuilder {
            return KsonBuilder()
        }
    }
}

inline fun kson(block: KsonBuilder.() -> Unit): Kson {
    return KsonBuilder().apply(block).create()
}