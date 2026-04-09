package com.nidoham.kson.naming

object FieldNamingPolicies {

    val IDENTITY: FieldNamingStrategy = FieldNamingStrategy { it }

    val LOWER_CASE_WITH_UNDERSCORES: FieldNamingStrategy = FieldNamingStrategy { fieldName ->
        buildString {
            for ((index, c) in fieldName.withIndex()) {
                if (c.isUpperCase()) { if (index > 0) append('_'); append(c.lowercaseChar()) }
                else append(c)
            }
        }
    }

    val UPPER_CASE_WITH_UNDERSCORES: FieldNamingStrategy = FieldNamingStrategy {
        LOWER_CASE_WITH_UNDERSCORES.translateName(it).uppercase()
    }

    val LOWER_CASE_WITH_DASHES: FieldNamingStrategy = FieldNamingStrategy {
        LOWER_CASE_WITH_UNDERSCORES.translateName(it).replace('_', '-')
    }

    val UPPER_CASE_WITH_DASHES: FieldNamingStrategy = FieldNamingStrategy {
        UPPER_CASE_WITH_UNDERSCORES.translateName(it).replace('_', '-')
    }

    val LOWER_CASE_FIRST_LETTER: FieldNamingStrategy = FieldNamingStrategy { fieldName ->
        if (fieldName.isEmpty()) fieldName else fieldName[0].lowercaseChar() + fieldName.substring(1)
    }

    val UPPER_CASE_FIRST_LETTER: FieldNamingStrategy = FieldNamingStrategy { fieldName ->
        if (fieldName.isEmpty()) fieldName else fieldName[0].uppercaseChar() + fieldName.substring(1)
    }

    val CAMEL_CASE: FieldNamingStrategy = FieldNamingStrategy { fieldName ->
        buildString {
            var capitalizeNext = false
            for (c in fieldName) {
                when {
                    c == '_' || c == '-' || c == ' ' -> capitalizeNext = true
                    capitalizeNext -> { append(c.uppercaseChar()); capitalizeNext = false }
                    else -> append(c.lowercaseChar())
                }
            }
        }
    }
}