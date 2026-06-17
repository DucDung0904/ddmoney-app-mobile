package com.dung.ddmoney.repository

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetPeriodTest {

    @Test
    fun `week starts on Monday and ends on Sunday`() {
        val period = BudgetPeriod.current(BudgetPeriodType.WEEK, LocalDate.of(2026, 6, 11))

        assertEquals(LocalDate.of(2026, 6, 8), period.startDate)
        assertEquals(LocalDate.of(2026, 6, 14), period.endDate)
        assertTrue(period.contains(LocalDate.of(2026, 6, 11)))
        assertFalse(period.contains(LocalDate.of(2026, 6, 15)))
    }

    @Test
    fun `quarter uses calendar quarter boundaries`() {
        val period = BudgetPeriod.current(BudgetPeriodType.QUARTER, LocalDate.of(2026, 5, 20))

        assertEquals(LocalDate.of(2026, 4, 1), period.startDate)
        assertEquals(LocalDate.of(2026, 6, 30), period.endDate)
    }

    @Test
    fun `year covers the full calendar year`() {
        val period = BudgetPeriod.current(BudgetPeriodType.YEAR, LocalDate.of(2026, 6, 11))

        assertEquals(LocalDate.of(2026, 1, 1), period.startDate)
        assertEquals(LocalDate.of(2026, 12, 31), period.endDate)
    }

    @Test
    fun `legacy budget falls back to its stored month`() {
        val period =
            BudgetPeriod.fromStorage(
                typeValue = null,
                startValue = null,
                endValue = null,
                month = 2,
                year = 2024
            )

        assertEquals(BudgetPeriodType.MONTH, period.type)
        assertEquals(LocalDate.of(2024, 2, 1), period.startDate)
        assertEquals(LocalDate.of(2024, 2, 29), period.endDate)
    }

    @Test
    fun `period options stay within the selected calendar year`() {
        val months = BudgetPeriod.periodsInYear(BudgetPeriodType.MONTH, 2026)
        val quarters = BudgetPeriod.periodsInYear(BudgetPeriodType.QUARTER, 2026)
        val weeks = BudgetPeriod.periodsInYear(BudgetPeriodType.WEEK, 2026)

        assertEquals(12, months.size)
        assertEquals(4, quarters.size)
        assertTrue(weeks.all { it.startDate.year == 2025 || it.startDate.year == 2026 })
        assertTrue(weeks.all {
            it.startDate.get(java.time.temporal.WeekFields.ISO.weekBasedYear()) == 2026
        })
    }
}
