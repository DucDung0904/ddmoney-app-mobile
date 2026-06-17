package com.dung.ddmoney.local.dao

import androidx.room.*
import com.dung.ddmoney.local.entity.BudgetCategoryEntity
import com.dung.ddmoney.local.entity.BudgetEntity
import com.dung.ddmoney.local.entity.BudgetWithCategories
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Transaction
    @Query("SELECT * FROM budgets")
    fun getBudgetsWithCategories(): Flow<List<BudgetWithCategories>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategories(categories: List<BudgetCategoryEntity>)

    @Query("DELETE FROM budget_categories WHERE budgetId = :budgetId")
    suspend fun deleteBudgetCategories(budgetId: String)

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudget(budgetId: String)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()

    @Query("DELETE FROM budget_categories")
    suspend fun deleteAllCategories()
}
