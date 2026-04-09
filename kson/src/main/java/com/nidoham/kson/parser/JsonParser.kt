package com.nidoham.kson.parser

import com.nidoham.kson.core.*
import com.nidoham.kson.logging.KsonLogger

class JsonParser(
    private var lenient: Boolean = false,
    private var duplicateKeyPolicy: JsonReader.DuplicateKeyPolicy = JsonReader.DuplicateKeyPolicy.FAIL
) {
    private val logger = KsonLogger.getLogger(JsonParser::class)

    fun parse(json: String): JsonElement {
        logger.debug("Starting JSON parsing, length: ${json.length}")
        val startTime = System.nanoTime()
        try {
            val reader = JsonReader(json).apply { this.lenient = this@JsonParser.lenient; this.duplicateKeyPolicy = this@JsonParser.duplicateKeyPolicy }
            val element = reader.nextElement()
            reader.close()
            val elapsed = (System.nanoTime() - startTime) / 1_000_000.0
            logger.debug("JSON parsing completed in ${elapsed}ms")
            return element
        } catch (e: ParseException) { logger.error("Parse error: ${e.message}"); throw e }
    }

    fun parseObject(json: String): JsonObject = parse(json).asJsonObject()
    fun parseArray(json: String): JsonArray = parse(json).asJsonArray()

    fun parseOrNull(json: String): JsonElement? = try { parse(json) } catch (e: ParseException) { logger.warn("Parse failed: ${e.message}"); null }

    fun parseWithDetails(json: String): ParseResult = try { ParseResult.Success(parse(json)) }
    catch (e: ParseException) { ParseResult.Error(e.message ?: "Unknown error", e.line, e.column, e.path) }

    fun setLenient(lenient: Boolean): JsonParser { this.lenient = lenient; return this }
    fun setDuplicateKeyPolicy(policy: JsonReader.DuplicateKeyPolicy): JsonParser { this.duplicateKeyPolicy = policy; return this }

    sealed class ParseResult {
        data class Success(val element: JsonElement) : ParseResult()
        data class Error(val message: String, val line: Int, val column: Int, val path: String) : ParseResult() {
            val isSuccess: Boolean get() = false
            val isError: Boolean get() = true
        }
    }

    companion object {
        @JvmStatic fun strict(): JsonParser = JsonParser(lenient = false)
        @JvmStatic fun lenient(): JsonParser = JsonParser(lenient = true)
        @JvmStatic fun parseString(json: String): JsonElement = strict().parse(json)
        @JvmStatic fun parseStringLenient(json: String): JsonElement = lenient().parse(json)
    }
}