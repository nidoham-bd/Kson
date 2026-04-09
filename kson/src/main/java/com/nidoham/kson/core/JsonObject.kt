package com.nidoham.kson.core

import com.nidoham.kson.logging.KsonLogger

/**
 * Represents a JSON object as a map of string keys to JsonElement values.
 * Maintains insertion order for consistency.
 */
class JsonObject : JsonElement(), Iterable<Map.Entry<String, JsonElement>> {

    private val members: LinkedHashMap<String, JsonElement> = LinkedHashMap()

    private val logger = KsonLogger.getLogger(JsonObject::class)

    fun add(property: String, value: JsonElement?): JsonObject {
        members[property] = value ?: JsonNull.INSTANCE
        logger.debug("Added property: $property")
        return this
    }

    fun addProperty(property: String, value: String?): JsonObject {
        members[property] = if (value != null) JsonPrimitive(value) else JsonNull.INSTANCE
        return this
    }

    fun addProperty(property: String, value: Number?): JsonObject {
        members[property] = if (value != null) JsonPrimitive(value) else JsonNull.INSTANCE
        return this
    }

    fun addProperty(property: String, value: Boolean?): JsonObject {
        members[property] = if (value != null) JsonPrimitive(value) else JsonNull.INSTANCE
        return this
    }

    fun addProperty(property: String, value: Char?): JsonObject {
        members[property] = if (value != null) JsonPrimitive(value.toString()) else JsonNull.INSTANCE
        return this
    }

    fun remove(property: String): JsonElement? {
        logger.debug("Removed property: $property")
        return members.remove(property)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : JsonElement> removeAs(property: String): T? = remove(property) as? T

    fun keySet(): Set<String> = members.keys

    fun size(): Int = members.size

    fun isEmpty(): Boolean = members.isEmpty()

    fun isNotEmpty(): Boolean = members.isNotEmpty()

    fun has(property: String): Boolean = members.containsKey(property)

    fun get(property: String): JsonElement? = members[property]

    fun getOrDefault(property: String, default: JsonElement): JsonElement =
        members.getOrDefault(property, default)

    fun entrySet(): Set<Map.Entry<String, JsonElement>> = members.entries

    fun getAsJsonObject(property: String): JsonObject =
        get(property)?.asJsonObject() ?: throw IllegalStateException("Property '$property' not found")

    fun getAsJsonArray(property: String): JsonArray =
        get(property)?.asJsonArray() ?: throw IllegalStateException("Property '$property' not found")

    fun getAsJsonPrimitive(property: String): JsonPrimitive =
        get(property)?.asJsonPrimitive() ?: throw IllegalStateException("Property '$property' not found")

    fun getAsString(property: String): String? = get(property)?.asString
    fun getAsInt(property: String): Int? = get(property)?.asInt
    fun getAsLong(property: String): Long? = get(property)?.asLong
    fun getAsDouble(property: String): Double? = get(property)?.asDouble
    fun getAsFloat(property: String): Float? = get(property)?.asFloat
    fun getAsBoolean(property: String): Boolean? = get(property)?.asBoolean

    fun getStringOrDefault(property: String, default: String): String =
        get(property)?.takeIf { !it.isJsonNull }?.asString ?: default

    fun getIntOrDefault(property: String, default: Int): Int =
        get(property)?.takeIf { !it.isJsonNull }?.asInt ?: default

    fun getLongOrDefault(property: String, default: Long): Long =
        get(property)?.takeIf { !it.isJsonNull }?.asLong ?: default

    fun getDoubleOrDefault(property: String, default: Double): Double =
        get(property)?.takeIf { !it.isJsonNull }?.asDouble ?: default

    fun getBooleanOrDefault(property: String, default: Boolean): Boolean =
        get(property)?.takeIf { !it.isJsonNull }?.asBoolean ?: default

    fun getFloatOrDefault(property: String, default: Float): Float =
        get(property)?.takeIf { !it.isJsonNull }?.asFloat ?: default

    fun hasNonNull(property: String): Boolean {
        val element = members[property] ?: return false
        return !element.isJsonNull
    }

    fun addAll(other: JsonObject): JsonObject {
        members.putAll(other.members)
        return this
    }

    fun subset(vararg properties: String): JsonObject {
        val result = JsonObject()
        properties.forEach { prop -> members[prop]?.let { result.add(prop, it) } }
        return result
    }

    fun exclude(vararg properties: String): JsonObject {
        val excludeSet = properties.toSet()
        val result = JsonObject()
        members.forEach { (key, value) -> if (key !in excludeSet) result.add(key, value) }
        return result
    }

    fun containsKey(key: String): Boolean = members.containsKey(key)

    fun containsValue(value: JsonElement): Boolean = members.containsValue(value)

    fun forEach(action: (String, JsonElement) -> Unit) {
        members.forEach { (k, v) -> action(k, v) }
    }

    fun filter(predicate: (Map.Entry<String, JsonElement>) -> Boolean): JsonObject {
        val result = JsonObject()
        members.filter(predicate).forEach { (k, v) -> result.add(k, v) }
        return result
    }

    fun mapKeys(transform: (String) -> String): JsonObject {
        val result = JsonObject()
        members.forEach { (k, v) -> result.add(transform(k), v) }
        return result
    }

    fun mapValues(transform: (JsonElement) -> JsonElement): JsonObject {
        val result = JsonObject()
        members.forEach { (k, v) -> result.add(k, transform(v)) }
        return result
    }

    override fun iterator(): Iterator<Map.Entry<String, JsonElement>> = members.iterator()

    override fun deepCopy(): JsonObject {
        val result = JsonObject()
        members.forEach { (key, value) -> result.add(key, value.deepCopy()) }
        return result
    }

    override fun toJsonString(): String = buildString {
        append('{')
        var first = true
        for ((key, value) in members) {
            if (!first) append(',')
            append('"')
            append(escapeString(key))
            append('"')
            append(':')
            append(value.toJsonString())
            first = false
        }
        append('}')
    }

    override fun toPrettyJsonString(indent: Int): String = buildString {
        append("{\n")
        var first = true
        val innerIndent = " ".repeat(indent)
        for ((key, value) in members) {
            if (!first) append(",\n")
            append(innerIndent)
            append('"')
            append(escapeString(key))
            append('"')
            append(": ")
            append(value.toPrettyJsonString(indent))
            first = false
        }
        append("\n}")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonObject) return false
        return members == other.members
    }

    override fun hashCode(): Int = members.hashCode()

    internal fun escapeString(s: String): String = buildString {
        for (c in s) {
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '\u000C' -> append("\\f")
                else -> if (c.code < 32) append("\\u${c.code.toString(16).padStart(4, '0')}") else append(c)
            }
        }
    }
}