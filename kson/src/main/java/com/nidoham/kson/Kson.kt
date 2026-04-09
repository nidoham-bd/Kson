package com.nidoham.kson

import com.nidoham.kson.adapter.TypeAdapter
import com.nidoham.kson.config.KsonBuilder
import com.nidoham.kson.config.KsonConfig
import com.nidoham.kson.core.JsonArray
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull
import com.nidoham.kson.core.JsonObject
import com.nidoham.kson.logging.KsonLogger
import com.nidoham.kson.parser.JsonParser
import com.nidoham.kson.parser.JsonReader
import com.nidoham.kson.reflection.ClassInfoCache
import com.nidoham.kson.serializer.JsonSerializer
import com.nidoham.kson.token.TypeToken
import java.io.File
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class Kson(
    private val config: KsonConfig = KsonConfig(),
    private val classInfoCache: ClassInfoCache = ClassInfoCache()
) {
    private val logger = KsonLogger.getLogger(Kson::class)
    private val parser = JsonParser(
        config.lenient,
        if (config.lenient) JsonReader.DuplicateKeyPolicy.REPLACE else JsonReader.DuplicateKeyPolicy.FAIL
    )
    private val serializer = JsonSerializer(config, classInfoCache)

    private val writer: com.nidoham.kson.serializer.JsonWriter by lazy {
        com.nidoham.kson.serializer.JsonWriter(
            config.prettyPrint,
            config.indent,
            config.serializeNulls,
            config.escapeHtmlChars
        )
    }

    fun toJson(src: Any?): String {
        return try {
            logger.debug("Serializing: ${src?.let { it::class.simpleName }}")
            serializer.serialize(src)
        } catch (e: KsonException) {
            throw e
        } catch (e: Exception) {
            throw KsonSerializationException(e.message ?: "Unknown error", src?.let { it::class }, e)
        }
    }

    fun toJsonTree(src: Any?): JsonElement {
        return try {
            if (src == null) JsonNull.INSTANCE else serializer.toJsonElement(src, src::class)
        } catch (e: KsonException) {
            throw e
        } catch (e: Exception) {
            throw KsonSerializationException(e.message ?: "Unknown error", src?.let { it::class }, e)
        }
    }

    fun <T : Any> toJson(src: T?, typeToken: TypeToken<T>): String {
        return if (src == null) "null" else writer.write(serializer.toJsonElement(src, typeToken))
    }

    fun <T : Any> toJsonTree(src: T?, typeToken: TypeToken<T>): JsonElement {
        return if (src == null) JsonNull.INSTANCE else serializer.toJsonElement(src, typeToken)
    }

    // ✅ FIX: Added `T : Any` bound
    inline fun <reified T : Any> fromJson(json: String): T {
        return fromJson(json, T::class)
    }

    fun <T : Any> fromJson(json: String, clazz: KClass<T>): T {
        return try {
            logger.debug("Deserializing to ${clazz.simpleName}")
            val element = parser.parse(json)
            fromJsonTree(element, clazz)
        } catch (e: KsonException) {
            throw e
        } catch (e: Exception) {
            throw KsonDeserializationException(e.message ?: "Unknown error", clazz, cause = e)
        }
    }

    // ✅ FIX: Added `T : Any` bound
    fun <T : Any> fromJson(json: String, typeToken: TypeToken<T>): T {
        return try {
            logger.debug("Deserializing to ${typeToken.simpleTypeName()}")
            val element = parser.parse(json)
            fromJsonTree(element, typeToken)
        } catch (e: KsonException) {
            throw e
        } catch (e: Exception) {
            throw KsonDeserializationException(e.message ?: "Unknown error", cause = e)
        }
    }

    fun <T : Any> fromJson(element: JsonElement, clazz: KClass<T>): T {
        return fromJsonTree(element, clazz)
    }

    fun <T : Any> fromJson(element: JsonElement, typeToken: TypeToken<T>): T {
        return fromJsonTree(element, typeToken)
    }

    fun <T : Any> fromJsonTree(element: JsonElement, clazz: KClass<T>): T {
        return try {
            val result = deserializeElement(element, clazz)
                ?: throw KsonDeserializationException("Failed to deserialize to ${clazz.simpleName}", clazz)
            result
        } catch (e: KsonException) {
            throw e
        } catch (e: Exception) {
            throw KsonDeserializationException(e.message ?: "Unknown error", clazz, cause = e)
        }
    }

    // ✅ FIX: Added `T : Any` bound and explicit type argument
    fun <T : Any> fromJsonTree(element: JsonElement, typeToken: TypeToken<T>): T {
        return try {
            val result = deserializeElement<T>(element, typeToken.rawType)
                ?: throw KsonDeserializationException("Failed to deserialize to ${typeToken.simpleTypeName()}")
            result
        } catch (e: KsonException) {
            throw e
        } catch (e: Exception) {
            throw KsonDeserializationException(e.message ?: "Unknown error", cause = e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> deserializeElement(element: JsonElement, type: KClass<*>): T? {
        if (element.isJsonNull) return null

        val adapter = findAdapter(type)
        if (adapter != null) return adapter.fromJson(element) as? T

        return when {
            type == String::class -> element.asString() as T
            type == Int::class || type == Integer::class -> element.asInt() as T
            type == Long::class -> element.asLong() as T
            type == Double::class -> element.asDouble() as T
            type == Float::class -> element.asFloat() as T
            type == Boolean::class -> element.asBoolean() as T
            type == Short::class -> element.asInt().toShort() as T
            type == Byte::class -> element.asInt().toByte() as T
            type == Char::class -> element.asString().firstOrNull() as? T
            type == BigDecimal::class -> BigDecimal(element.asString()) as T
            type == BigInteger::class -> BigInteger(element.asString()) as T
            type == Number::class -> element.asDouble() as T
            type == JsonElement::class -> element as T
            type == JsonObject::class -> element.asJsonObject() as T
            type == JsonArray::class -> element.asJsonArray() as T
            type == Any::class -> deserializeAny(element) as T
            type.java.isEnum -> deserializeEnum(element, type) as T
            Collection::class.java.isAssignableFrom(type.java) -> deserializeCollection(element, type) as T
            Map::class.java.isAssignableFrom(type.java) -> deserializeMap(element, type) as T
            Pair::class.java.isAssignableFrom(type.java) -> deserializePair(element, type) as T
            type.isSealed -> deserializeSealed(element, type) as T
            else -> deserializeObject(element, type) as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun deserializeEnum(element: JsonElement, type: KClass<*>): Enum<*> {
        val name = element.asString()
        val enumConstants = type.java.enumConstants as Array<Enum<*>>

        return enumConstants.firstOrNull { it.name == name }
            ?: enumConstants.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: throw KsonDeserializationException(
                "Unknown enum value '$name' for ${type.simpleName}. Valid: ${enumConstants.joinToString { it.name }}",
                type
            )
    }

    private fun deserializeCollection(element: JsonElement, type: KClass<*>): Collection<*> {
        if (!element.isJsonArray) {
            throw TypeMismatchException(
                expectedType = "JsonArray",
                actualType = if (element.isJsonObject) "JsonObject" else "JsonPrimitive",
                fieldName = type.simpleName
            )
        }
        val list: MutableList<Any?> = mutableListOf()
        for (item in element.asJsonArray()) {
            list.add(if (item.isJsonNull) null else deserializeAny(item))
        }
        return list
    }

    private fun deserializeMap(element: JsonElement, type: KClass<*>): Map<*, *> {
        if (!element.isJsonObject) {
            throw TypeMismatchException(
                expectedType = "JsonObject",
                actualType = if (element.isJsonArray) "JsonArray" else "JsonPrimitive",
                fieldName = type.simpleName
            )
        }
        val map = linkedMapOf<String, Any?>()
        element.asJsonObject().forEach { (k, v) ->
            map[k] = if (v.isJsonNull) null else deserializeAny(v)
        }
        return map
    }

    private fun deserializePair(element: JsonElement, type: KClass<*>): Pair<*, *> {
        if (!element.isJsonArray || element.asJsonArray().size() < 2) {
            throw TypeMismatchException(
                expectedType = "JsonArray with 2 elements",
                actualType = "JsonArray with ${element.asJsonArray().size()} elements",
                fieldName = type.simpleName
            )
        }
        val arr = element.asJsonArray()
        return Pair(deserializeAny(arr[0]), deserializeAny(arr[1]))
    }

    // ✅ FIX: Properly access sealedSubclasses via KClass extension
    private fun deserializeSealed(element: JsonElement, type: KClass<*>): Any {
        if (!element.isJsonObject) {
            throw TypeMismatchException(
                expectedType = "JsonObject with discriminator",
                actualType = if (element.isJsonArray) "JsonArray" else "JsonPrimitive",
                fieldName = type.simpleName
            )
        }
        val obj = element.asJsonObject()
        val discriminatorKey = "type"
        val discriminatorValue = obj.getAsString(discriminatorKey)
            ?: throw MissingFieldException(discriminatorKey, type)

        // ✅ FIX: Access sealedSubclasses properly
        val subclasses: List<KClass<out Any>> = type.sealedSubclasses
        val subclass = subclasses.find { subclass ->
            val name = subclass.simpleName?.removeSuffix("Impl")
            name.equals(discriminatorValue, ignoreCase = true)
        } ?: throw KsonDeserializationException("Unknown subclass '$discriminatorValue' for ${type.simpleName}", type)

        return deserializeObject(obj.deepCopy().apply { remove(discriminatorKey) }, subclass)
            ?: throw KsonDeserializationException("Failed to deserialize sealed subclass", subclass)
    }

    private fun deserializeObject(element: JsonElement, type: KClass<*>): Any? {
        if (!element.isJsonObject) {
            throw TypeMismatchException(
                expectedType = "JsonObject",
                actualType = if (element.isJsonArray) "JsonArray" else "JsonPrimitive",
                fieldName = type.simpleName
            )
        }
        val jsonObject = element.asJsonObject()
        val classInfo = classInfoCache.getClassInfo(type)
        val constructor = type.primaryConstructor
            ?: throw KsonDeserializationException("No primary constructor for ${type.simpleName}", type)

        val params = constructor.parameters.associateWith { param ->
            val fieldInfo = classInfo.findField(param.name ?: return@associateWith null)
            val serializedName = fieldInfo?.serializedName
                ?: config.fieldNamingStrategy?.translateName(param.name ?: "")
                ?: param.name
            val valueElement = jsonObject.get(serializedName ?: "")

            if (valueElement == null || valueElement.isJsonNull) {
                if (param.isOptional) return@associateWith null
                if (config.useDefaultValues && fieldInfo?.defaultValue != null) {
                    try {
                        return@associateWith parser.parse(fieldInfo.defaultValue).let { deserializeAny(it) }
                    } catch (e: Exception) { }
                }
                if (param.type.isMarkedNullable) return@associateWith null
                if (config.requireNonNullFields) throw MissingFieldException(param.name ?: "", type)
                return@associateWith getDefaultForType(param.type)
            }

            try {
                // ✅ FIX: Explicit type argument for inference
                deserializeElement<Any?>(valueElement, param.type.jvmErasure)
            } catch (e: Exception) {
                if (param.isOptional) null else throw e
            }
        }

        return try {
            constructor.callBy(params)
        } catch (e: Exception) {
            throw KsonDeserializationException("Failed to create instance of ${type.simpleName}: ${e.message}", type, cause = e)
        }
    }

    private fun deserializeAny(element: JsonElement): Any? {
        return when {
            element.isJsonNull -> null
            element.isString -> element.asString()
            element.isBoolean -> element.asBoolean()
            element.isNumber -> {
                val s = element.asString()
                if (!s.contains('.') && !s.contains('e') && !s.contains('E')) {
                    val l = element.asLong()
                    if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l
                } else {
                    element.asDouble()
                }
            }
            element.isJsonArray -> {
                val list = mutableListOf<Any?>()
                element.asJsonArray().forEach { list.add(deserializeAny(it)) }
                list
            }
            element.isJsonObject -> {
                val m = linkedMapOf<String, Any?>()
                element.asJsonObject().forEach { (k, v) -> m[k] = deserializeAny(v) }
                m
            }
            else -> null
        }
    }

    private fun getDefaultForType(type: kotlin.reflect.KType): Any? {
        return when (type.jvmErasure) {
            String::class -> ""
            Int::class -> 0
            Long::class -> 0L
            Double::class -> 0.0
            Float::class -> 0f
            Boolean::class -> false
            Short::class -> 0.toShort()
            Byte::class -> 0.toByte()
            Collection::class -> emptyList<Any>()
            Map::class -> emptyMap<String, Any>()
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun findAdapter(type: KClass<*>): TypeAdapter<Any>? {
        for (factory in config.typeAdapterFactories) {
            factory.create(type)?.let { return it as TypeAdapter<Any> }
        }
        return config.typeAdapters[type] as? TypeAdapter<Any>
    }

    fun parseJson(json: String): JsonElement {
        return parser.parse(json)
    }

    fun parseJsonObject(json: String): JsonObject {
        return parser.parseObject(json)
    }

    fun parseJsonArray(json: String): JsonArray {
        return parser.parseArray(json)
    }

    fun parseJsonOrNull(json: String): JsonElement? {
        return parser.parseOrNull(json)
    }

    // ✅ FIX: Added `T : Any` bound
    fun <T : Any> fromJson(file: File, clazz: KClass<T>): T {
        return fromJson(file.readText(), clazz)
    }

    // ✅ FIX: Added `T : Any` bound
    inline fun <reified T : Any> fromJson(file: File): T {
        return fromJson(file.readText(), T::class)
    }

    // ✅ FIX: Added `T : Any` bound
    inline fun <reified T : Any> fromJson(file: File, charset: Charset): T {
        return fromJson(file.readText(charset), T::class)
    }

    // ✅ FIX: Added `T : Any` bound
    fun <T : Any> fromJson(url: URL, clazz: KClass<T>): T {
        return fromJson(url.readText(), clazz)
    }

    // ✅ FIX: Added `T : Any` bound
    inline fun <reified T : Any> fromJson(url: URL): T {
        return fromJson(url.readText(), T::class)
    }

    // ✅ FIX: Added `T : Any` bound
    fun <T : Any> fromJson(inputStream: InputStream, charset: Charset = Charsets.UTF_8, clazz: KClass<T>): T {
        return fromJson(inputStream.bufferedReader(charset).readText(), clazz)
    }

    // ✅ FIX: Added `T : Any` bound
    inline fun <reified T : Any> fromJson(inputStream: InputStream, charset: Charset = Charsets.UTF_8): T {
        return fromJson(inputStream.bufferedReader(charset).readText(), T::class)
    }

    fun toJsonFile(src: Any?, file: File) {
        file.writeText(toJson(src))
    }

    fun toJsonFile(src: Any?, file: File, charset: Charset) {
        file.writeText(toJson(src), charset)
    }

    fun isJsonValid(json: String): Boolean {
        return try {
            parser.parse(json)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAdapter(type: KClass<*>): TypeAdapter<*>? {
        return findAdapter(type)
    }

    fun newBuilder(): KsonBuilder {
        return KsonBuilder().apply {
            if (config.prettyPrint) setPrettyPrinting()
            setIndent(config.indent)
            if (config.serializeNulls) serializeNulls()
            if (config.escapeHtmlChars) enableHtmlEscaping()
            if (config.lenient) setLenient()
            config.fieldNamingStrategy?.let { setFieldNamingPolicyLowerCaseUnderscores() }
            if (config.version != 0.0) setVersion(config.version)
            if (config.excludeFieldsWithoutExposeAnnotation) excludeFieldsWithoutExposeAnnotation()
            if (config.requireNonNullFields) requireNonNullFields()
            registerTypeAdapters(config.typeAdapters)
            config.typeAdapterFactories.forEach { registerTypeAdapterFactory(it) }
        }
    }

    fun clearCache() {
        classInfoCache.clear()
    }

    companion object {
        @JvmField
        val INSTANCE: Kson = Kson()

        @JvmStatic
        fun create(): Kson {
            return Kson()
        }

        @JvmStatic
        fun builder(): KsonBuilder {
            return KsonBuilder()
        }
    }
}