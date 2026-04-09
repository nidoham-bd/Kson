package com.nidoham.kson.parser

import com.nidoham.kson.core.*
import com.nidoham.kson.logging.KsonLogger

class JsonReader(private val input: String) {
    private var position = 0
    private var line = 1
    private var column = 1
    private var previousChar = '\u0000'
    private val stack = mutableListOf<Token>()
    private val pathNames = mutableListOf<String?>()
    private val pathIndices = mutableListOf<Int>()

    var lenient = false
    var duplicateKeyPolicy = DuplicateKeyPolicy.FAIL
    private var duplicateKeys = mutableSetOf<String>()

    enum class DuplicateKeyPolicy { FAIL, REPLACE, IGNORE }

    private val length: Int get() = input.length
    private val hasMore: Boolean get() = position < length
    private val peekChar: Char get() = if (position < length) input[position] else '\u0000'

    private val logger = KsonLogger.getLogger(JsonReader::class)

    private val peekedString = StringBuilder()
    private var peekedToken: Token? = null
    private var peekedNumber: Number? = null
    private var peekedBoolean: Boolean? = null

    fun getPath(): String = buildString {
        append("$")
        for (i in stack.indices) {
            val name = pathNames.getOrNull(i)
            if (name != null) { append("."); append(name) }
            else { append("["); append(pathIndices.getOrNull(i) ?: 0); append("]") }
        }
    }

    fun peek(): Token {
        peekedToken?.let { return it }
        skipWhitespace()
        if (!hasMore) return Token.END_DOCUMENT

        peekedToken = when (val c = peekChar) {
            '{' -> Token.BEGIN_OBJECT
            '}' -> Token.END_OBJECT
            '[' -> Token.BEGIN_ARRAY
            ']' -> Token.END_ARRAY
            ':' -> Token.COLON
            ',' -> Token.COMMA
            '"', '\'' -> { readStringInternal(); Token.STRING }
            't', 'f' -> { readBooleanInternal(); Token.BOOLEAN }
            'n' -> { readNullInternal(); Token.NULL }
            else -> {
                if (c == '-' || c in '0'..'9') { readNumberInternal(); Token.NUMBER }
                else if (lenient) { readUnquotedStringInternal(); Token.STRING }
                else throw syntaxError("Unexpected character: '$c'")
            }
        }
        return peekedToken!!
    }

    fun nextToken(): Token { val token = peek(); peekedToken = null; return token }

    fun beginObject() {
        expect(Token.BEGIN_OBJECT)
        stack.add(Token.BEGIN_OBJECT)
        pathNames.add(null)
        pathIndices.add(0)
        duplicateKeys.clear()
        logger.debug("Begin object at ${getPath()}")
    }

    fun endObject() {
        expect(Token.END_OBJECT)
        stack.removeAt(stack.lastIndex)
        pathNames.removeAt(pathNames.lastIndex)
        pathIndices.removeAt(pathIndices.lastIndex)
        logger.debug("End object at ${getPath()}")
    }

    fun beginArray() {
        expect(Token.BEGIN_ARRAY)
        stack.add(Token.BEGIN_ARRAY)
        pathNames.add(null)
        pathIndices.add(0)
        logger.debug("Begin array at ${getPath()}")
    }

    fun endArray() {
        expect(Token.END_ARRAY)
        stack.removeAt(stack.lastIndex)
        pathNames.removeAt(pathNames.lastIndex)
        pathIndices.removeAt(pathIndices.lastIndex)
        logger.debug("End array at ${getPath()}")
    }

    fun hasNext(): Boolean {
        peek()
        return peekedToken != Token.END_OBJECT && peekedToken != Token.END_ARRAY && peekedToken != Token.END_DOCUMENT
    }

    fun nextName(): String {
        expect(Token.STRING)
        val name = peekedString.toString()
        peekedString.clear()
        peekedToken = null

        if (pathNames.isNotEmpty()) pathNames[pathNames.lastIndex] = name

        if (duplicateKeyPolicy == DuplicateKeyPolicy.FAIL && !duplicateKeys.add(name)) {
            throw DuplicateKeyException(name, line, column)
        }

        logger.debug("Next name: $name at ${getPath()}")
        return name
    }

    fun nextString(): String { expect(Token.STRING); val v = peekedString.toString(); peekedString.clear(); peekedToken = null; return v }
    fun nextBoolean(): Boolean { expect(Token.BOOLEAN); val v = peekedBoolean!!; peekedBoolean = null; peekedToken = null; return v }
    fun nextNull() { expect(Token.NULL); peekedToken = null }
    fun nextNumber(): Number { expect(Token.NUMBER); val v = peekedNumber!!; peekedNumber = null; peekedToken = null; return v }

    fun nextElement(): JsonElement = when (peek()) {
        Token.BEGIN_OBJECT -> {
            beginObject()
            val obj = JsonObject()
            while (hasNext()) { val name = nextName(); obj.add(name, nextElement()) }
            endObject()
            obj
        }
        Token.BEGIN_ARRAY -> {
            beginArray()
            val arr = JsonArray()
            while (hasNext()) { arr.add(nextElement()); incrementPathIndex() }
            endArray()
            arr
        }
        Token.STRING -> JsonPrimitive(nextString())
        Token.NUMBER -> JsonPrimitive(nextNumber())
        Token.BOOLEAN -> JsonPrimitive(nextBoolean())
        Token.NULL -> { nextNull(); JsonNull.INSTANCE }
        Token.END_DOCUMENT -> throw syntaxError("Unexpected end of document")
        else -> throw syntaxError("Unexpected token: ${peek()}")
    }

    fun skipValue() {
        when (peek()) {
            Token.BEGIN_OBJECT -> { beginObject(); while (hasNext()) { nextName(); skipValue() }; endObject() }
            Token.BEGIN_ARRAY -> { beginArray(); while (hasNext()) { skipValue(); incrementPathIndex() }; endArray() }
            Token.STRING, Token.NUMBER, Token.BOOLEAN, Token.NULL -> nextToken()
            Token.END_DOCUMENT -> throw syntaxError("Unexpected end of document")
            else -> nextToken()
        }
    }

    fun close() {
        skipWhitespace()
        if (hasMore && !lenient) throw syntaxError("Unexpected characters at end of document")
        logger.debug("Reader closed successfully")
    }

    private fun expect(expected: Token) {
        val actual = peek()
        if (actual != expected) throw UnexpectedTokenException(expected.name, actual.name, line, column)
        peekedToken = null
    }

    private fun skipWhitespace() {
        while (hasMore) {
            val c = peekChar
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') { advance() }
            else if (lenient && c == '/' && position + 1 < length) {
                when (input[position + 1]) {
                    '/' -> { skipLineComment(); continue }
                    '*' -> { skipBlockComment(); continue }
                    else -> break
                }
            } else break
        }
    }

    private fun skipLineComment() { advance(); advance(); while (hasMore && peekChar != '\n') advance() }

    private fun skipBlockComment() {
        advance(); advance()
        while (hasMore) {
            if (peekChar == '*' && position + 1 < length && input[position + 1] == '/') { advance(); advance(); return }
            advance()
        }
        throw syntaxError("Unterminated block comment")
    }

    private fun advance() {
        if (position < length) {
            if (input[position] == '\n') { line++; column = 1 } else column++
            previousChar = input[position]
            position++
        }
    }

    private fun readStringInternal() {
        peekedString.clear()
        val quote = peekChar
        advance()
        while (hasMore) {
            val c = peekChar; advance()
            when (c) {
                quote -> return
                '\\' -> {
                    if (!hasMore) throw syntaxError("Unterminated string escape")
                    val escape = peekChar; advance()
                    when (escape) {
                        '"' -> peekedString.append('"')
                        '\\' -> peekedString.append('\\')
                        '/' -> peekedString.append('/')
                        'b' -> peekedString.append('\b')
                        'f' -> peekedString.append('\u000C')
                        'n' -> peekedString.append('\n')
                        'r' -> peekedString.append('\r')
                        't' -> peekedString.append('\t')
                        'u' -> peekedString.append(readHexDigits(4).toInt(16).toChar())
                        else -> if (lenient) { peekedString.append('\\'); peekedString.append(escape) }
                        else throw syntaxError("Invalid escape sequence: \\$escape")
                    }
                }
                '\n', '\r' -> if (!lenient) throw syntaxError("Unterminated string") else peekedString.append(c)
                else -> { if (c.code < 32 && !lenient) throw syntaxError("Unescaped control character"); peekedString.append(c) }
            }
        }
        throw syntaxError("Unterminated string")
    }

    private fun readHexDigits(count: Int): String {
        val sb = StringBuilder()
        repeat(count) {
            if (!hasMore) throw syntaxError("Unterminated hex escape")
            val c = peekChar
            if (c !in '0'..'9' && c !in 'a'..'f' && c !in 'A'..'F') throw syntaxError("Invalid hex digit: $c")
            sb.append(c); advance()
        }
        return sb.toString()
    }

    private fun readNumberInternal() {
        val sb = StringBuilder()
        if (peekChar == '-') { sb.append(peekChar); advance() }
        if (peekChar == '0') { sb.append(peekChar); advance(); if (peekChar in '0'..'9' && !lenient) throw syntaxError("Leading zeros not allowed") }
        else if (peekChar in '1'..'9') { while (peekChar in '0'..'9') { sb.append(peekChar); advance() } }
        else if (!lenient) throw syntaxError("Expected digit")

        if (peekChar == '.') {
            sb.append(peekChar); advance()
            if (peekChar !in '0'..'9' && !lenient) throw syntaxError("Expected digit after decimal point")
            while (peekChar in '0'..'9') { sb.append(peekChar); advance() }
        }

        if (peekChar == 'e' || peekChar == 'E') {
            sb.append(peekChar); advance()
            if (peekChar == '+' || peekChar == '-') { sb.append(peekChar); advance() }
            if (peekChar !in '0'..'9' && !lenient) throw syntaxError("Expected digit in exponent")
            while (peekChar in '0'..'9') { sb.append(peekChar); advance() }
        }

        peekedNumber = parseNumber(sb.toString())
    }

    private fun parseNumber(str: String): Number = try {
        if (str.contains('.') || str.contains('e') || str.contains('E')) {
            val d = str.toDouble()
            if (!str.contains('.') && d == d.toLong().toDouble() && d in Long.MIN_VALUE.toDouble()..Long.MAX_VALUE.toDouble()) d.toLong() else d
        } else {
            val l = str.toLong()
            if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l
        }
    } catch (e: NumberFormatException) {
        if (lenient) try { str.toDouble() } catch (e2: NumberFormatException) { throw syntaxError("Invalid number: $str") }
        else throw syntaxError("Invalid number: $str")
    }

    private fun readBooleanInternal() {
        if (input.startsWith("true", position)) { peekedBoolean = true; position += 4; column += 4 }
        else if (input.startsWith("false", position)) { peekedBoolean = false; position += 5; column += 5 }
        else throw syntaxError("Expected boolean (true/false)")
    }

    private fun readNullInternal() {
        if (input.startsWith("null", position)) { position += 4; column += 4 }
        else throw syntaxError("Expected 'null'")
    }

    private fun readUnquotedStringInternal() {
        peekedString.clear()
        while (hasMore) { val c = peekChar; if (c in ' '..'\u001f' || c in "{}[]:,\"\\/") break; peekedString.append(c); advance() }
    }

    private fun incrementPathIndex() { if (pathIndices.isNotEmpty()) pathIndices[pathIndices.lastIndex]++ }
    private fun syntaxError(message: String): ParseException = ParseException(message, line, column, position, getPath())

    companion object {
        @JvmStatic fun of(input: String): JsonReader = JsonReader(input)
    }
}