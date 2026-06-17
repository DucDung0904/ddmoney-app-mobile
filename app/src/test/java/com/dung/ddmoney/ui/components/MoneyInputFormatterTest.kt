package com.dung.ddmoney.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyInputFormatterTest {

    @Test
    fun formatMoneyInput_groupsDigitsAndRemovesLeadingZeros() {
        assertEquals("1.234.567", formatMoneyInput("0001234567"))
    }

    @Test
    fun formatMoneyInput_respectsMaximumDigits() {
        assertEquals("123.456", formatMoneyInput("123456789", maxDigits = 6))
    }

    @Test
    fun formatMoneyInput_handlesEmptyValues() {
        assertEquals("", formatMoneyInput(""))
        assertEquals("0", formatMoneyInput("", emptyAsZero = true))
    }

    @Test
    fun applyMoneyInputKey_supportsAppendDeleteAndClear() {
        assertEquals("12.000", applyMoneyInputKey("12", "000"))
        assertEquals("1.200", applyMoneyInputKey("12.000", "DEL"))
        assertEquals("0", applyMoneyInputKey("1.200", "C"))
    }

    @Test
    fun parseMoneyInput_ignoresGroupingCharacters() {
        assertEquals(1_234_567.0, parseMoneyInput("1.234.567"), 0.0)
        assertEquals(0.0, parseMoneyInput(""), 0.0)
    }

    @Test
    fun parseNullableMoneyInput_distinguishesBlankFromZero() {
        assertNull(parseNullableMoneyInput(""))
        assertEquals(0.0, parseNullableMoneyInput("0") ?: -1.0, 0.0)
    }

    @Test
    fun formatMoneyDisplay_usesVietnameseGrouping() {
        assertEquals("1.250.000 đ", formatMoneyDisplay(1_250_000.0))
        assertEquals("-50.000 đ", formatMoneyDisplay(-50_000.0))
    }
}
