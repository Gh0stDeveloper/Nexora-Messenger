package com.nexora.app.util

object PhoneNumberNormalizer {
    fun normalizeInternational(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        val digits = trimmed.filter(Char::isDigit)
        if (digits.isBlank()) return ""
        if (trimmed.startsWith("+") && !digits.startsWith("52")) return "+$digits"
        return normalizeMexico(trimmed)
    }

    fun normalizeMexico(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        val digits = trimmed.filter { it.isDigit() }
        return when {
            trimmed.startsWith("+") && trimmed.startsWith("+52") -> {
                val after52 = trimmed.filter { it.isDigit() }.removePrefix("52")
                "+52${after52.removePrefix("1")}".take(13)
            }
            digits.startsWith("521") && digits.length >= 13 -> "+52${digits.removePrefix("521")}".take(13)
            digits.startsWith("52") && digits.length >= 12 -> "+52${digits.removePrefix("52").removePrefix("1")}".take(13)
            digits.length == 10 -> "+52$digits"
            digits.length == 11 && digits.startsWith("1") -> "+52${digits.removePrefix("1")}".take(13)
            else -> if (trimmed.startsWith("+")) "+$digits" else "+$digits"
        }
    }

    fun isValidInternational(phone: String): Boolean {
        val normalized = normalizeInternational(phone)
        val digits = normalized.drop(1)
        return normalized.startsWith("+") && digits.length in 8..15 && digits.all(Char::isDigit)
    }

    fun isValidMexico(phone: String): Boolean {
        val normalized = normalizeMexico(phone)
        return normalized.startsWith("+52") && normalized.length == 13 && normalized.drop(3).all { it.isDigit() }
    }
}
