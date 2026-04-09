package com.nidoham.kson.reflection

import com.nidoham.kson.annotation.*
import com.nidoham.kson.logging.KsonLogger
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class ClassInfoCache {
    private val cache = mutableMapOf<kotlin.reflect.KClass<*>, ClassInfo>()
    private val logger = KsonLogger.getLogger(ClassInfoCache::class)

    fun getClassInfo(kClass: kotlin.reflect.KClass<*>): ClassInfo = cache.getOrPut(kClass) {
        logger.debug("Creating ClassInfo for ${kClass.simpleName}")
        ClassInfo(kClass, extractFields(kClass), kClass.isSealed, if (kClass.isSealed) kClass.sealedSubclasses else emptyList())
    }

    fun clear() { cache.clear(); logger.debug("Class info cache cleared") }
    fun size(): Int = cache.size

    private fun extractFields(kClass: kotlin.reflect.KClass<*>): List<FieldInfo> = kClass.memberProperties.mapNotNull { property ->
        try { createFieldInfo(property) } catch (e: Exception) { logger.warn("Failed to extract field ${property.name}: ${e.message}"); null }
    }

    private fun createFieldInfo(property: KProperty1<*, *>): FieldInfo {
        val serializedNameAnn = property.findAnnotation<SerializedName>()
        val transientAnn = property.findAnnotation<Transient>()
        val exposeAnn = property.findAnnotation<Expose>()
        val sinceAnn = property.findAnnotation<Since>()
        val untilAnn = property.findAnnotation<Until>()
        val defaultValAnn = property.findAnnotation<DefaultValue>()
        val jsonAdapterAnn = property.findAnnotation<JsonAdapter>()
        val isKotlinTransient = property.annotations.any { it.annotationClass.simpleName == "Transient" }

        @Suppress("UNCHECKED_CAST")
        val getter: (Any) -> Any? = { instance -> try { (property as KProperty1<Any, Any?>).get(instance) } catch (e: Exception) { logger.debug("Getter failed for ${property.name}: ${e.message}"); null } }

        @Suppress("UNCHECKED_CAST")
        val setter: ((Any, Any?) -> Unit)? = if (property is KMutableProperty1) {
            { instance, value -> try { (property as KMutableProperty1<Any, Any?>).set(instance, value) } catch (e: Exception) { logger.debug("Setter failed for ${property.name}: ${e.message}") } }
        } else null

        return FieldInfo(property.name, property.returnType, serializedNameAnn?.value, serializedNameAnn?.alternate?.toList() ?: emptyList(),
            transientAnn != null || isKotlinTransient, exposeAnn, sinceAnn?.value, untilAnn?.value, defaultValAnn?.value, jsonAdapterAnn, getter, setter)
    }
}