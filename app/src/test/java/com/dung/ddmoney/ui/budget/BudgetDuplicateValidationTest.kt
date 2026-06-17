package com.dung.ddmoney.ui.budget

import com.dung.ddmoney.repository.BudgetDisplayModel
import com.dung.ddmoney.repository.BudgetPeriod
import com.dung.ddmoney.repository.BudgetPeriodType
import com.dung.ddmoney.repository.CategoryInfo
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetDuplicateValidationTest {

    @Test
    fun `returns every overlapping category in the same period`() {
        val existing = budget(categoryIds = listOf(1001L, 1002L), walletId = 24L)
        val period = monthPeriod()

        val conflicts =
            findConflictingCategoryIdsForPeriod(
                budgets = listOf(existing),
                categoryIds = setOf(1002L, 1003L),
                period = period
            )

        assertEquals(setOf(1002L), conflicts)
    }

    @Test
    fun `different standard period has no category conflicts`() {
        val existing = budget(categoryIds = listOf(1001L, 1002L), walletId = null)
        val quarter =
            BudgetPeriod(
                BudgetPeriodType.QUARTER,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 6, 30)
            )

        val conflicts =
            findConflictingCategoryIdsForPeriod(
                budgets = listOf(existing),
                categoryIds = setOf(1001L, 1002L),
                period = quarter
            )

        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `editing budget excludes all of its own categories`() {
        val existing = budget(categoryIds = listOf(1001L, 1002L), walletId = null)

        val conflicts =
            findConflictingCategoryIdsForPeriod(
                budgets = listOf(existing),
                categoryIds = setOf(1001L, 1002L),
                period = monthPeriod(),
                excludedBudgetId = existing.id
            )

        assertTrue(conflicts.isEmpty())
    }

    private fun monthPeriod() =
        BudgetPeriod(
            BudgetPeriodType.MONTH,
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30)
        )

    private fun budget(categoryIds: List<Long>, walletId: Long?): BudgetDisplayModel =
        BudgetDisplayModel(
            id = "12",
            name = "Ăn uống",
            amount = 2_000_000.0,
            spentAmount = 0.0,
            month = 6,
            year = 2026,
            periodType = BudgetPeriodType.MONTH,
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 30),
            walletId = walletId,
            categories =
                categoryIds.map { id ->
                    CategoryInfo(
                        id = id,
                        name = "Danh mục $id",
                        icon = "category",
                        colorHex = "#EB7070"
                    )
                }
        )
}
