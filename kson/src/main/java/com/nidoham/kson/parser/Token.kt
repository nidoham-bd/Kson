package com.nidoham.kson.parser

enum class Token(val value: String? = null) {
    BEGIN_OBJECT("{"),
    END_OBJECT("}"),
    BEGIN_ARRAY("["),
    END_ARRAY("]"),
    COLON(":"),
    COMMA(","),
    STRING(null),
    NUMBER(null),
    BOOLEAN(null),
    NULL("null"),
    END_DOCUMENT(null),
    ERROR(null);

    val isStructural: Boolean get() = this in listOf(BEGIN_OBJECT, END_OBJECT, BEGIN_ARRAY, END_ARRAY, COLON, COMMA)
    val isValue: Boolean get() = this in listOf(STRING, NUMBER, BOOLEAN, NULL)
}