package com.dung.ddmoney.local.entity

import androidx.room.*

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val userId: Long,
    val name: String,
    val amount: Double,
    val month: Int,
    val year: Int,
    val periodType: String = "MONTH",
    val startDate: String? = null,
    val endDate: String? = null,
    val walletId: Long? = null
)

@Entity(tableName = "budget_categories", primaryKeys = ["budgetId", "categoryId"])
data class BudgetCategoryEntity(
    val budgetId: String,
    val categoryId: Long
)

data class BudgetWithCategories(
    @Embedded val budget: BudgetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "budgetId"
    )
    val categories: List<BudgetCategoryEntity>
)
