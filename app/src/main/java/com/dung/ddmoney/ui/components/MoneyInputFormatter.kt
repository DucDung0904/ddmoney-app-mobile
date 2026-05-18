package com.dung.ddmoney.ui.components

import kotlin.math.abs
import kotlin.math.roundToLong

private const val MONEY_GROUP_SEPARATOR = "."

fun formatMoneyInput(value: String, emptyAsZero: Boolean = false, maxDigits: Int = 15): String {
    val rawDigits = value.filter { it.isDigit() }
    if (rawDigits.isEmpty()) return if (emptyAsZero) "0" else ""

    val digits = rawDigits.trimStart('0').take(maxDigits)
    val normalized = when {
        digits.isNotEmpty() -> digits
        else -> "0"
    }
    return formatMoneyDigits(normalized)
}

fun formatMoneyAmount(value: Double): String {
    if (value == 0.0) return ""
    val amount = value.roundToLong()
    val sign = if (amount < 0) "-" else ""
    return sign + formatMoneyDigits(abs(amount).toString())
}

fun formatMoneyDisplay(value: Double, suffix: String = " đ"): String {
    return "${formatMoneyAmount(value).ifBlank { "0" }}$suffix"
}

fun parseMoneyInput(value: String): Double {
    return value.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
}

fun parseNullableMoneyInput(value: String): Double? {
    return value.takeIf { it.isNotBlank() }?.let { parseMoneyInput(it) }
}

fun applyMoneyInputKey(current: String, key: String, maxDigits: Int = 15): String {
    val digits = current.filter { it.isDigit() }
    val nextDigits = when (key) {
        "C" -> ""
        "DEL" -> digits.dropLast(1)
        "000" -> if (digits.isBlank() || digits == "0") "0" else digits + key
        in "0".."9" -> digits + key
        else -> digits
    }
    return formatMoneyInput(nextDigits, emptyAsZero = true, maxDigits = maxDigits)
}

private fun formatMoneyDigits(digits: String): String {
    return digits.reversed().chunked(3).joinToString(MONEY_GROUP_SEPARATOR).reversed()
}
