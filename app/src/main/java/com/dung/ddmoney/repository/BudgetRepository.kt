
package com.dung.ddmoney.repository

import android.content.Context
import com.dung.ddmoney.local.SyncStatus
import com.dung.ddmoney.local.SyncWorker
import com.dung.ddmoney.local.dao.BudgetDao
import com.dung.ddmoney.local.dao.CategoryDao
import com.dung.ddmoney.local.dao.TransactionDao
import com.dung.ddmoney.local.entity.BudgetCategoryEntity
import com.dung.ddmoney.local.entity.BudgetEntity
import com.dung.ddmoney.network.ApiService
import com.dung.ddmoney.network.dto.BudgetRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.util.UUID

data class BudgetDisplayModel(
    val id: String,
    val name: String,
    val amount: Double,
    val spentAmount: Double,
    val month: Int,
    val year: Int,
    val periodType: BudgetPeriodType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val walletId: Long?,
    val categories: List<CategoryInfo>
) {
    val remaining: Double = amount - spentAmount
    val percentUsed: Double = if (amount > 0) (spentAmount / amount * 100) else 0.0
}

data class CategoryInfo(
    val id: Long,
    val name: String,
    val icon: String,
    val colorHex: String
)


class BudgetRepository(
    private val api: ApiService,
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    fun getBudgetsWithCalculatedSpent(): Flow<List<BudgetDisplayModel>> {
        val budgetsFlow = budgetDao.getBudgetsWithCategories()
        val transactionsFlow = transactionDao.observeAll()
        val allCategoriesFlow = categoryDao.observeAll()

        return combine(budgetsFlow, transactionsFlow, allCategoriesFlow) { budgets, transactions, allCats ->
            // Lọc bỏ budget chờ xóa khỏi UI
            budgets.filter { it.budget.syncStatus != SyncStatus.PENDING_DELETE }.map { budgetWithCats ->
                val budgetEntity = budgetWithCats.budget
                val period =
                    BudgetPeriod.fromStorage(
                        typeValue = budgetEntity.periodType,
                        startValue = budgetEntity.startDate,
                        endValue = budgetEntity.endDate,
                        month = budgetEntity.month,
                        year = budgetEntity.year
                    )
                val catIds = budgetWithCats.categories.map { it.categoryId }
                val childIds =
                    allCats
                        .filter { it.parentId != null && it.parentId in catIds }
                        .mapNotNull { it.serverId }
                val effectiveCatIds = (catIds + childIds).toSet()
                val spent = transactions
                    .filter { transaction ->
                        val transactionDate =
                            runCatching { LocalDate.parse(transaction.date.take(10)) }.getOrNull()
                        transaction.type == "EXPENSE" &&
                            transaction.categoryId in effectiveCatIds &&
                            transactionDate != null &&
                            period.contains(transactionDate) &&
                            (budgetEntity.walletId == null || transaction.walletId == budgetEntity.walletId)
                    }
                    .sumOf { it.amount }
                
                val categoryInfos = allCats.filter { 
                    val serverId = it.serverId
                    serverId != null && serverId in catIds 
                }.map { 
                    CategoryInfo(it.serverId!!, it.name, it.icon, it.colorHex)
                }

                BudgetDisplayModel(
                    id = budgetEntity.id,
                    name = budgetEntity.name,
                    amount = budgetEntity.amount,
                    spentAmount = spent,
                    month = budgetEntity.month,
                    year = budgetEntity.year,
                    periodType = period.type,
                    startDate = period.startDate,
                    endDate = period.endDate,
                    walletId = budgetEntity.walletId,
                    categories = categoryInfos
                )
            }
        }
    }

    /**
     * Pull toàn bộ budget từ server → ghi đè Room.
     * Chỉ xóa budget đã SYNCED; giữ lại bản pending chưa được đẩy lên.
     */
    suspend fun sync(): Result<Unit> =
        safeCall {
            val response = api.getCurrentBudgets()

            // Chỉ xóa các budget đã synced, giữ lại pending
            val pending = budgetDao.getPendingBudgets().map { it.id }.toSet()
            val localMetadata = budgetDao.getAllBudgets().associateBy { it.id }

            // Xóa categories của budget đã synced trước
            localMetadata.keys
                .filter { it !in pending }
                .forEach { budgetDao.deleteBudgetCategories(it) }

            response.forEach { apiBudget ->
                val budgetId = apiBudget.id.toString()
                val existing = localMetadata[budgetId]
                val period =
                    BudgetPeriod.fromStorage(
                        typeValue = apiBudget.periodType ?: existing?.periodType,
                        startValue = apiBudget.startDate ?: existing?.startDate,
                        endValue = apiBudget.endDate ?: existing?.endDate,
                        month = apiBudget.month,
                        year = apiBudget.year
                    )
                val budget = BudgetEntity(
                    id = budgetId,
                    serverId = apiBudget.id,
                    userId = existing?.userId ?: 0L,
                    name = apiBudget.name,
                    amount = apiBudget.amount,
                    month = apiBudget.month,
                    year = apiBudget.year,
                    periodType = period.type.name,
                    startDate = period.startDate.toString(),
                    endDate = period.endDate.toString(),
                    walletId = apiBudget.walletId ?: existing?.walletId,
                    syncStatus = SyncStatus.SYNCED
                )
                budgetDao.upsertBudget(budget)
                
                val budgetCategories = apiBudget.categoryIds?.map {
                    BudgetCategoryEntity(budgetId = budgetId, categoryId = it)
                } ?: apiBudget.categories.map { 
                    BudgetCategoryEntity(budgetId = budgetId, categoryId = it.id) 
                }
                budgetDao.insertBudgetCategories(budgetCategories)
            }
            Unit
        }

    /**
     * Tạo budget mới theo kiểu offline-first:
     * Ghi local với UUID + PENDING_INSERT
     * Trigger SyncWorker để đẩy lên server
     */
    suspend fun createBudget(
        context: Context,
        userId: Long,
        name: String,
        amount: Double,
        period: BudgetPeriod,
        walletId: Long?,
        categoryIds: List<Long>
    ) {
        val localId = UUID.randomUUID().toString()
        val budget = BudgetEntity(
            id = localId,
            serverId = null,
            userId = userId,
            name = name,
            amount = amount,
            month = period.month,
            year = period.year,
            periodType = period.type.name,
            startDate = period.startDate.toString(),
            endDate = period.endDate.toString(),
            walletId = walletId,
            syncStatus = SyncStatus.PENDING_INSERT
        )
        budgetDao.upsertBudget(budget)
        budgetDao.insertBudgetCategories(
            categoryIds.distinct().map {
                BudgetCategoryEntity(budgetId = localId, categoryId = it)
            }
        )
        SyncWorker.enqueue(context)
    }

    /**
     * Cập nhật budget theo offline-first:
     * 1. Ghi local với PENDING_UPDATE (nếu đã có serverId) hoặc giữ PENDING_INSERT
     * 2. Trigger SyncWorker
     */
    suspend fun updateBudget(
        context: Context,
        budgetId: String,
        userId: Long,
        name: String,
        amount: Double,
        period: BudgetPeriod,
        walletId: Long?,
        categoryIds: List<Long>
    ) {
        val existing = budgetDao.getAllBudgets().find { it.id == budgetId }
        // Nếu đang PENDING_INSERT thì giữ nguyên status (chưa lên server nên không cần UPDATE)
        val newStatus = when (existing?.syncStatus) {
            SyncStatus.PENDING_INSERT -> SyncStatus.PENDING_INSERT
            else -> SyncStatus.PENDING_UPDATE
        }
        val budget = BudgetEntity(
            id = budgetId,
            serverId = existing?.serverId,
            userId = userId,
            name = name,
            amount = amount,
            month = period.month,
            year = period.year,
            periodType = period.type.name,
            startDate = period.startDate.toString(),
            endDate = period.endDate.toString(),
            walletId = walletId,
            syncStatus = newStatus
        )
        budgetDao.upsertBudget(budget)
        budgetDao.deleteBudgetCategories(budgetId)
        budgetDao.insertBudgetCategories(
            categoryIds.distinct().map {
                BudgetCategoryEntity(budgetId = budgetId, categoryId = it)
            }
        )
        SyncWorker.enqueue(context)
    }

    /**
     * Xóa budget theo offline-first:
     * - Nếu chưa có serverId (PENDING_INSERT): xóa thẳng khỏi local (chưa lên server)
     * - Nếu đã có serverId: đánh PENDING_DELETE, để Worker xóa trên server
     */
    suspend fun deleteBudget(context: Context, budgetId: String) {
        val existing = budgetDao.getAllBudgets().find { it.id == budgetId }
        if (existing?.serverId == null) {
            // Chưa lên server → xóa thẳng local
            budgetDao.deleteBudgetCategories(budgetId)
            budgetDao.deleteBudget(budgetId)
        } else {
            // Đã có trên server → đánh pending, worker sẽ xóa
            budgetDao.updateSyncStatus(budgetId, SyncStatus.PENDING_DELETE)
            SyncWorker.enqueue(context)
        }
    }
}
