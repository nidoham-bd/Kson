package com.nidoham.kson.core

import com.nidoham.kson.logging.KsonLogger

class JsonArray() : JsonElement(), Iterable<JsonElement> {

    private val elements: MutableList<JsonElement> = mutableListOf()
    private val logger = KsonLogger.getLogger(JsonArray::class)

    constructor(vararg elements: JsonElement?) : this() {
        elements.forEach { add(it) }
    }

    fun add(element: JsonElement?): JsonArray {
        elements.add(element ?: JsonNull.INSTANCE)
        logger.debug("Added element at index ${elements.size - 1}")
        return this
    }

    fun add(value: String?): JsonArray {
        return add(if (value != null) JsonPrimitive.of(value) else JsonNull.INSTANCE)
    }

    fun add(value: Number?): JsonArray {
        return add(if (value != null) JsonPrimitive.of(value) else JsonNull.INSTANCE)
    }

    fun add(value: Boolean?): JsonArray {
        return add(if (value != null) JsonPrimitive.of(value) else JsonNull.INSTANCE)
    }

    fun addAll(array: JsonArray): JsonArray {
        elements.addAll(array.elements)
        return this
    }

    fun addAll(collection: Collection<JsonElement?>): JsonArray {
        collection.forEach { add(it) }
        return this
    }

    fun add(index: Int, element: JsonElement?): JsonArray {
        elements.add(index, element ?: JsonNull.INSTANCE)
        return this
    }

    fun remove(index: Int): JsonElement {
        return elements.removeAt(index)
    }

    fun remove(element: JsonElement): Boolean {
        return elements.remove(element)
    }

    fun removeIf(predicate: (JsonElement) -> Boolean): Boolean {
        return elements.removeAll(predicate)
    }

    fun contains(element: JsonElement): Boolean {
        return elements.contains(element)
    }

    fun size(): Int {
        return elements.size
    }

    fun isEmpty(): Boolean {
        return elements.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return elements.isNotEmpty()
    }

    fun clear(): JsonArray {
        elements.clear()
        return this
    }

    operator fun get(index: Int): JsonElement {
        if (index < 0 || index >= elements.size) {
            throw IndexOutOfBoundsException("Index: $index, Size: ${elements.size}")
        }
        return elements[index]
    }

    fun getAsJsonObject(index: Int): JsonObject {
        return get(index).asJsonObject()
    }

    fun getAsJsonArray(index: Int): JsonArray {
        return get(index).asJsonArray()
    }

    fun getAsJsonPrimitive(index: Int): JsonPrimitive {
        return get(index).asJsonPrimitive()
    }

    fun getAsString(index: Int): String {
        return get(index).asString()
    }

    fun getAsInt(index: Int): Int {
        return get(index).asInt()
    }

    fun getAsLong(index: Int): Long {
        return get(index).asLong()
    }

    fun getAsDouble(index: Int): Double {
        return get(index).asDouble()
    }

    fun getAsFloat(index: Int): Float {
        return get(index).asFloat()
    }

    fun getAsBoolean(index: Int): Boolean {
        return get(index).asBoolean()
    }

    fun getStringOrDefault(index: Int, default: String): String {
        return if (index in elements.indices && !elements[index].isJsonNull) {
            elements[index].asString()
        } else {
            default
        }
    }

    fun getIntOrDefault(index: Int, default: Int): Int {
        return if (index in elements.indices && !elements[index].isJsonNull) {
            elements[index].asInt()
        } else {
            default
        }
    }

    fun getLongOrDefault(index: Int, default: Long): Long {
        return if (index in elements.indices && !elements[index].isJsonNull) {
            elements[index].asLong()
        } else {
            default
        }
    }

    fun getDoubleOrDefault(index: Int, default: Double): Double {
        return if (index in elements.indices && !elements[index].isJsonNull) {
            elements[index].asDouble()
        } else {
            default
        }
    }

    fun getBooleanOrDefault(index: Int, default: Boolean): Boolean {
        return if (index in elements.indices && !elements[index].isJsonNull) {
            elements[index].asBoolean()
        } else {
            default
        }
    }

    fun getFloatOrDefault(index: Int, default: Float): Float {
        return if (index in elements.indices && !elements[index].isJsonNull) {
            elements[index].asFloat()
        } else {
            default
        }
    }

    fun toList(): List<JsonElement> {
        return elements.toList()
    }

    operator fun set(index: Int, element: JsonElement?) {
        elements[index] = element ?: JsonNull.INSTANCE
    }

    fun indexOf(element: JsonElement): Int {
        return elements.indexOf(element)
    }

    fun lastIndexOf(element: JsonElement): Int {
        return elements.lastIndexOf(element)
    }

    fun subArray(fromIndex: Int, toIndex: Int): JsonArray {
        val result = JsonArray()
        for (i in fromIndex until toIndex) {
            result.add(elements[i])
        }
        return result
    }

    fun filter(predicate: (JsonElement) -> Boolean): JsonArray {
        val result = JsonArray()
        for (element in elements) {
            if (predicate(element)) {
                result.add(element)
            }
        }
        return result
    }

    fun map(transform: (JsonElement) -> JsonElement?): JsonArray {
        val result = JsonArray()
        for (element in elements) {
            result.add(transform(element))
        }
        return result
    }

    fun flatMap(transform: (JsonElement) -> JsonArray): JsonArray {
        val result = JsonArray()
        for (element in elements) {
            result.addAll(transform(element))
        }
        return result
    }

    fun firstOrNull(): JsonElement? {
        return elements.firstOrNull()
    }

    fun lastOrNull(): JsonElement? {
        return elements.lastOrNull()
    }

    fun forEachIndexed(action: (Int, JsonElement) -> Unit) {
        elements.forEachIndexed { index, element -> action(index, element) }
    }

    fun <T> mapToList(transform: (JsonElement) -> T): List<T> {
        return elements.map(transform)
    }

    fun <T> mapNotNullToList(transform: (JsonElement) -> T?): List<T> {
        return elements.mapNotNull(transform)
    }

    override fun iterator(): Iterator<JsonElement> {
        return elements.iterator()
    }

    override fun deepCopy(): JsonArray {
        val result = JsonArray()
        for (element in elements) {
            result.add(element.deepCopy())
        }
        return result
    }

    override fun toJsonString(): String {
        return buildString {
            append('[')
            var first = true
            for (element in elements) {
                if (!first) append(',')
                append(element.toJsonString())
                first = false
            }
            append(']')
        }
    }

    override fun toPrettyJsonString(indent: Int): String {
        return buildString {
            append("[\n")
            var first = true
            val innerIndent = " ".repeat(indent)
            for (element in elements) {
                if (!first) append(",\n")
                append(innerIndent)
                append(element.toPrettyJsonString(indent))
                first = false
            }
            append("\n]")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonArray) return false
        return elements == other.elements
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }
}