package com.nidoham.kson.adapter.builtIn

import com.nidoham.kson.adapter.BaseTypeAdapter
import com.nidoham.kson.core.JsonElement
import com.nidoham.kson.core.JsonNull
import com.nidoham.kson.core.JsonPrimitive
import com.nidoham.kson.logging.KsonLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DateAdapter(
    private val dateFormat: String = ISO_8601,
    private val timeZone: TimeZone = TimeZone.getTimeZone("UTC"),
    private val lenient: Boolean = false,
    private val serializeAsTimestamp: Boolean = false
) : BaseTypeAdapter<Date>(Date::class) {

    private val logger = KsonLogger.getLogger(DateAdapter::class)
    private val formatter: SimpleDateFormat by lazy { SimpleDateFormat(dateFormat, Locale.US).apply { this.timeZone = this@DateAdapter.timeZone; this.isLenient = this@DateAdapter.lenient } }

    override fun serialize(value: Date): JsonElement = if (serializeAsTimestamp) JsonPrimitive(value.time) else synchronized(formatter) { JsonPrimitive(formatter.format(value)) }

    override fun deserialize(element: JsonElement): Date? {
        if (element.isJsonNull) return null
        return if (element.isJsonPrimitive && element.isNumber) Date(element.asLong)
        else if (element.isJsonPrimitive && element.isString) {
            synchronized(formatter) {
                try { formatter.parse(element.asString) }
                catch (e: Exception) { logger.error("Failed to parse date: ${element.asString}"); if (lenient) tryParseAlternativeFormats(element.asString) else throw IllegalArgumentException("Failed to parse date: '${element.asString}' with format: $dateFormat", e) }
            }
        } else throw IllegalArgumentException("Cannot parse Date from: $element")
    }

    override fun nullValue(): Date? = null

    private fun tryParseAlternativeFormats(dateString: String): Date? {
        val formats = listOf(ISO_8601_NO_MILLIS, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy")
        for (format in formats) try { return SimpleDateFormat(format, Locale.US).apply { timeZone = this@DateAdapter.timeZone }.parse(dateString) } catch (e: Exception) {}
        return dateString.toLongOrNull()?.let { Date(it) }
    }

    companion object {
        const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val ISO_8601_NO_MILLIS = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        const val DATE_ONLY = "yyyy-MM-dd"
        const val DATE_TIME = "yyyy-MM-dd HH:mm:ss"
        @JvmStatic fun iso8601(): DateAdapter = DateAdapter(ISO_8601)
        @JvmStatic fun timestamp(): DateAdapter = DateAdapter(serializeAsTimestamp = true)
    }
}