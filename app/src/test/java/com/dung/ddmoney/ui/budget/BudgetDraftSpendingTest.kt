package com.dung.ddmoney.ui.budget

import androidx.compose.ui.graphics.Color
import com.dung.ddmoney.repository.BudgetPeriod
import com.dung.ddmoney.repository.BudgetPeriodType
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.CategoryType
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetDraftSpendingTest {

    @Test
    fun `draft spending includes child categories and filters period and wallet`() {
        val parent = category(id = "1001", name = "Ăn uống")
        val child = category(id = "1002", name = "Ăn sáng", parentId = parent.id)
        val period =
            BudgetPeriod(
                type = BudgetPeriodType.MONTH,
                startDate = LocalDate.of(2026, 6, 1),
                endDate = LocalDate.of(2026, 6, 30)
            )
        val transactions =
            listOf(
                expense(categoryId = child.id, walletId = "9", amount = 120_000.0, date = LocalDate.of(2026, 6, 3)),
                expense(categoryId = child.id, walletId = "10", amount = 80_000.0, date = LocalDate.of(2026, 6, 4)),
                expense(categoryId = child.id, walletId = "9", amount = 50_000.0, date = LocalDate.of(2026, 5, 31)),
                expense(
                    categoryId = child.id,
                    walletId = "9",
                    amount = 40_000.0,
                    date = LocalDate.of(2026, 6, 5),
                    type = TransactionType.INCOME
                )
            )

        val allWallets =
            calculateSpentForBudgetDraft(
                transactions = transactions,
                categories = listOf(parent, child),
                selectedCategoryIds = setOf(1001L),
                period = period,
                walletId = null
            )
        val oneWallet =
            calculateSpentForBudgetDraft(
                transactions = transactions,
                categories = listOf(parent, child),
                selectedCategoryIds = setOf(1001L),
                period = period,
                walletId = 9L
            )

        assertEquals(200_000.0, allWallets, 0.0)
        assertEquals(120_000.0, oneWallet, 0.0)
    }

    private fun category(id: String, name: String, parentId: String? = null) =
        Category(
            id = id,
            name = name,
            icon = "category",
            color = Color.Blue,
            type = CategoryType.EXPENSE,
            parentId = parentId
        )

    private fun expense(
        categoryId: String,
        walletId: String,
        amount: Double,
        date: LocalDate,
        type: TransactionType = TransactionType.EXPENSE
    ) =
        Transaction(
            title = "Test",
            categoryId = categoryId,
            categoryName = "Test",
            categoryIcon = "category",
            categoryColor = Color.Blue,
            amount = amount,
            type = type,
            walletId = walletId,
            walletName = "Ví",
            date = date
        )
}
