package com.dung.ddmoney.ui.expensebook

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseBookLogicTest {

    @Test
    fun dateRangeForMonth_returnsWholeSelectedMonth() {
        val range = dateRangeForMonth(YearMonth.of(2024, 2))

        assertEquals(LocalDate.of(2024, 2, 1), range.first)
        assertEquals(LocalDate.of(2024, 2, 29), range.second)
    }

    @Test
    fun expenseBookMonthOptions_areContinuousAndNewestFirst() {
        val months =
            expenseBookMonthOptions(
                transactionDates = listOf(LocalDate.of(2026, 3, 15)),
                currentMonth = YearMonth.of(2026, 6),
                minimumMonthCount = 3
            )

        assertEquals(
            listOf(
                YearMonth.of(2026, 3),
                YearMonth.of(2026, 4),
                YearMonth.of(2026, 5),
                YearMonth.of(2026, 6)
            ),
            months
        )
    }

    @Test
    fun expenseBookMonthLabel_usesCurrentMonthNameOnlyForCurrentMonth() {
        val currentMonth = YearMonth.of(2026, 6)

        assertEquals("Tháng này", expenseBookMonthLabel(currentMonth, currentMonth))
        assertEquals(
            "05/2026",
            expenseBookMonthLabel(YearMonth.of(2026, 5), currentMonth)
        )
    }

    @Test
    fun parseExpenseBookDate_acceptsIsoTimestamp() {
        assertEquals(
            LocalDate.of(2026, 6, 10),
            parseExpenseBookDate("2026-06-10T08:30:00")
        )
    }

    @Test
    fun groupedTransactions_ordersNewestDayFirst() {
        val older = transaction(1, "2026-06-08")
        val newer = transaction(2, "2026-06-10")
        val state =
            ExpenseBookUiState(
                status = ExpenseBookLoadStatus.SUCCESS,
                transactions = listOf(older, newer),
                visibleTransactions = listOf(older, newer)
            )

        assertEquals(LocalDate.of(2026, 6, 10), state.groupedTransactions.first().date)
        assertEquals(LocalDate.of(2026, 6, 8), state.groupedTransactions.last().date)
    }

    @Test
    fun hasActiveFilters_detectsAndClearsSecondaryFilters() {
        assertFalse(ExpenseBookUiState().hasActiveFilters)
        assertTrue(
            ExpenseBookUiState(
                filter = ExpenseBookFilter(query = "cafe")
            ).hasActiveFilters
        )
    }

    private fun transaction(id: Long, date: String) =
        TransactionItem(
            id = id,
            categoryName = "Ăn uống",
            walletName = "Tiền mặt",
            amount = 50_000.0,
            transactionDate = date
        )
}
