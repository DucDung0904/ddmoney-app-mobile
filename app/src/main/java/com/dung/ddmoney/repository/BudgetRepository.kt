
package com.dung.ddmoney.repository

import com.dung.ddmoney.local.dao.BudgetDao
import com.dung.ddmoney.local.dao.TransactionDao
import com.dung.ddmoney.local.entity.BudgetCategoryEntity
import com.dung.ddmoney.local.entity.BudgetEntity
import com.dung.ddmoney.network.ApiService
import com.dung.ddmoney.network.dto.BudgetRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

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
    private val categoryDao: com.dung.ddmoney.local.dao.CategoryDao
) {
    /**
     * Lấy danh sách ngân sách kèm theo số tiền đã chi tiêu được tính toán từ các giao dịch.
     * spent_amount = tổng transactions có category_id nằm trong danh sách category của ngân sách đó, 
     * cùng tháng/năm, và type là expense.
     */
    fun getBudgetsWithCalculatedSpent(): Flow<List<BudgetDisplayModel>> {
        val budgetsFlow = budgetDao.getBudgetsWithCategories()
        val transactionsFlow = transactionDao.observeAll()
        val allCategoriesFlow = categoryDao.observeAll()

        return combine(budgetsFlow, transactionsFlow, allCategoriesFlow) { budgets, transactions, allCats ->
            budgets.map { budgetWithCats ->
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

    suspend fun sync(): Result<Unit> =
        safeCall {
            val localMetadata = budgetDao.getAllBudgets().associateBy { it.id }
            val response = api.getCurrentBudgets()
            budgetDao.deleteAllCategories()
            budgetDao.deleteAll()
            
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
                    userId = 0L, // Assuming it's handled server side
                    name = apiBudget.name,
                    amount = apiBudget.budgetAmount,
                    month = apiBudget.month,
                    year = apiBudget.year,
                    periodType = period.type.name,
                    startDate = period.startDate.toString(),
                    endDate = period.endDate.toString(),
                    walletId = apiBudget.walletId ?: existing?.walletId
                )
                budgetDao.insertBudget(budget)
                
                val budgetCategories = apiBudget.categories.map { 
                    BudgetCategoryEntity(budgetId = budgetId, categoryId = it.id) 
                }
                budgetDao.insertBudgetCategories(budgetCategories)
            }
            Unit
        }

    suspend fun createBudget(
        userId: Long,
        name: String,
        amount: Double,
        period: BudgetPeriod,
        walletId: Long?,
        categoryIds: List<Long>
    ) {
        val req = BudgetRequest(
            name = name,
            categoryIds = categoryIds,
            amount = amount,
            month = period.month,
            year = period.year,
            periodType = period.type.name,
            startDate = period.startDate.toString(),
            endDate = period.endDate.toString(),
            walletId = walletId,
            walletScope = if (walletId == null) "ALL_WALLETS" else "ONE_WALLET"
        )
        val response = api.createBudget(req)
        
        val budgetId = response.id.toString()
        val budget = BudgetEntity(
            id = budgetId,
            userId = userId,
            name = name,
            amount = amount,
            month = period.month,
            year = period.year,
            periodType = period.type.name,
            startDate = period.startDate.toString(),
            endDate = period.endDate.toString(),
            walletId = walletId
        )
        budgetDao.insertBudget(budget)
        
        budgetDao.insertBudgetCategories(
            categoryIds.distinct().map {
                BudgetCategoryEntity(budgetId = budgetId, categoryId = it)
            }
        )
    }

    suspend fun updateBudget(
        budgetId: String,
        userId: Long,
        name: String,
        amount: Double,
        period: BudgetPeriod,
        walletId: Long?,
        categoryIds: List<Long>
    ) {
        val req = BudgetRequest(
            name = name,
            categoryIds = categoryIds,
            amount = amount,
            month = period.month,
            year = period.year,
            periodType = period.type.name,
            startDate = period.startDate.toString(),
            endDate = period.endDate.toString(),
            walletId = walletId,
            walletScope = if (walletId == null) "ALL_WALLETS" else "ONE_WALLET"
        )
        api.updateBudget(budgetId.toLong(), req)
        
        val budget = BudgetEntity(
            id = budgetId,
            userId = userId,
            name = name,
            amount = amount,
            month = period.month,
            year = period.year,
            periodType = period.type.name,
            startDate = period.startDate.toString(),
            endDate = period.endDate.toString(),
            walletId = walletId
        )
        budgetDao.insertBudget(budget)
        
        budgetDao.deleteBudgetCategories(budgetId)
        budgetDao.insertBudgetCategories(
            categoryIds.distinct().map {
                BudgetCategoryEntity(budgetId = budgetId, categoryId = it)
            }
        )
    }

    suspend fun deleteBudget(budgetId: String) {
        api.deleteBudget(budgetId.toLong())
        budgetDao.deleteBudgetCategories(budgetId)
        budgetDao.deleteBudget(budgetId)
    }
}
