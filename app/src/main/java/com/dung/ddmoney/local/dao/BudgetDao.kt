package com.dung.ddmoney.local.dao

import androidx.room.*
import com.dung.ddmoney.local.SyncStatus
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

    // ─── Offline sync queries ─────────────────────────────────────────────

    /** Lấy tất cả budget chưa đồng bộ với server */
    @Query("SELECT * FROM budgets WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingBudgets(): List<BudgetEntity>

    /** Cập nhật syncStatus sau khi Worker xử lý xong */
    @Query("UPDATE budgets SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    /** Gán serverId khi server phản hồi ID thật sau khi tạo */
    @Query("UPDATE budgets SET serverId = :serverId, syncStatus = 'SYNCED' WHERE id = :localId")
    suspend fun confirmInsert(localId: String, serverId: Long)

    /** Lấy danh sách categoryId theo budgetId — dùng trong SyncWorker */
    @Query("SELECT categoryId FROM budget_categories WHERE budgetId = :budgetId")
    suspend fun getBudgetCategoryIds(budgetId: String): List<Long>

    // ─── Write operations ─────────────────────────────────────────────────

    @Upsert
    suspend fun upsertBudget(budget: BudgetEntity)

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
