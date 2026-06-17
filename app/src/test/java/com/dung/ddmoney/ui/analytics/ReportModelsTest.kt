package com.dung.ddmoney.ui.analytics

import androidx.compose.ui.graphics.Color
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportModelsTest {

    @Test
    fun buildExpenseReport_includesOnlyExpensesInsideCurrentPeriod() {
        val today = LocalDate.of(2026, 6, 10)
        val transactions =
            listOf(
                transaction("current-expense", 120_000.0, TransactionType.EXPENSE, today),
                transaction("previous-expense", 80_000.0, TransactionType.EXPENSE, today.minusWeeks(1)),
                transaction("income", 5_000_000.0, TransactionType.INCOME, today)
            )

        val report = buildExpenseReport(transactions, ReportPeriod.WEEK, today)

        assertEquals(120_000.0, report.currentTotal, 0.0)
        assertEquals(80_000.0, report.previousTotal, 0.0)
        assertEquals(1, report.transactionCount)
        assertEquals(1, report.previousTransactionCount)
        assertEquals(50f, report.differencePercentage, 0.001f)
    }

    @Test
    fun buildExpenseReport_groupsTransactionsByFallbackCategoryName() {
        val today = LocalDate.of(2026, 6, 10)
        val report =
            buildExpenseReport(
                listOf(
                    transaction("a", 40_000.0, TransactionType.EXPENSE, today),
                    transaction("b", 60_000.0, TransactionType.EXPENSE, today)
                ),
                ReportPeriod.MONTH,
                today
            )

        assertEquals(1, report.topCategories.size)
        assertEquals("Ăn uống", report.topCategories.single().name)
        assertEquals(100_000.0, report.topCategories.single().amount, 0.0)
        assertEquals(1f, report.topCategories.single().percentage, 0.001f)
    }

    @Test
    fun reportPreviousDataFlag_usesCountOrAmount() {
        val noPrevious =
            ExpenseReport(
                period = ReportPeriod.MONTH,
                currentTotal = 10.0,
                previousTotal = 0.0,
                transactionCount = 1,
                currentLabel = "",
                previousLabel = "",
                rangeLabel = "",
                summaryLabel = "",
                topCategories = emptyList()
            )
        val withPrevious = noPrevious.copy(previousTransactionCount = 1)

        assertFalse(noPrevious.hasPreviousPeriodData)
        assertTrue(withPrevious.hasPreviousPeriodData)
    }

    @Test
    fun differencePercentage_comparesChangeAgainstPreviousPeriod() {
        val report =
            ExpenseReport(
                period = ReportPeriod.MONTH,
                currentTotal = 2_060_000.0,
                previousTotal = 5_500_000.0,
                transactionCount = 1,
                previousTransactionCount = 1,
                currentLabel = "",
                previousLabel = "",
                rangeLabel = "",
                summaryLabel = "",
                topCategories = emptyList()
            )

        assertEquals(-62.545456f, report.differencePercentage, 0.001f)
        assertEquals("-62.5%", formatPercentageChange(report.differencePercentage))
        assertEquals("+50%", formatPercentageChange(50f))
        assertEquals("0%", formatPercentageChange(0f))
        assertEquals("0%", formatPercentageChange(0.04f))
    }

    @Test
    fun chartHelpers_handleEmptyAndLargeValues() {
        assertEquals(1.0, comparisonChartAxisMax(0.0), 0.0)
        assertEquals(2_000_000.0, comparisonChartAxisMax(1_250_000.0), 0.0)
        assertEquals(0f, comparisonChartVisualRatio(0.0, 100.0), 0f)
        assertEquals("1.5M", compactMoney(1_500_000.0))
    }

    private fun transaction(
        id: String,
        amount: Double,
        type: TransactionType,
        date: LocalDate
    ) =
        Transaction(
            id = id,
            title = "Bữa ăn",
            categoryId = "custom-food",
            categoryName = "Ăn uống",
            categoryIcon = "restaurant",
            categoryColor = Color(0xFFE24B4A),
            amount = amount,
            type = type,
            walletId = "1",
            walletName = "Tiền mặt",
            date = date
        )
}
