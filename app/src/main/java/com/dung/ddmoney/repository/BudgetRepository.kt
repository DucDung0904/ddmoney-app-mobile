
package com.dung.ddmoney.repository

import com.dung.ddmoney.local.dao.BudgetDao
import com.dung.ddmoney.local.dao.TransactionDao
import com.dung.ddmoney.local.entity.BudgetCategoryEntity
import com.dung.ddmoney.local.entity.BudgetEntity
import com.dung.ddmoney.network.ApiService
import com.dung.ddmoney.network.dto.BudgetRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

data class BudgetDisplayModel(
    val id: String,
    val name: String,
    val amount: Double,
    val spentAmount: Double,
    val month: Int,
    val year: Int,
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
    fun getBudgetsWithCalculatedSpent(month: Int, year: Int): Flow<List<BudgetDisplayModel>> {
        val budgetsFlow = budgetDao.getBudgetsWithCategories()
        val transactionsFlow = transactionDao.observeByMonth(month, year)
        val allCategoriesFlow = categoryDao.observeAll()

        return combine(budgetsFlow, transactionsFlow, allCategoriesFlow) { budgets, transactions, allCats ->
            budgets.map { budgetWithCats ->
                val catIds = budgetWithCats.categories.map { it.categoryId }
                val childIds =
                    allCats
                        .filter { it.parentId != null && it.parentId in catIds }
                        .mapNotNull { it.serverId }
                val effectiveCatIds = (catIds + childIds).toSet()
                val spent = transactions
                    .filter { it.type == "EXPENSE" && it.categoryId in effectiveCatIds }
                    .sumOf { it.amount }
                
                val categoryInfos = allCats.filter { 
                    val serverId = it.serverId
                    serverId != null && serverId in catIds 
                }.map { 
                    CategoryInfo(it.serverId!!, it.name, it.icon, it.colorHex)
                }

                BudgetDisplayModel(
                    id = budgetWithCats.budget.id,
                    name = budgetWithCats.budget.name,
                    amount = budgetWithCats.budget.amount,
                    spentAmount = spent,
                    month = budgetWithCats.budget.month,
                    year = budgetWithCats.budget.year,
                    categories = categoryInfos
                )
            }
        }
    }

    suspend fun sync() {
        try {
            val response = api.getBudgets()
            budgetDao.deleteAllCategories()
            budgetDao.deleteAll()
            
            response.forEach { apiBudget ->
                val budgetId = apiBudget.id.toString()
                val budget = BudgetEntity(
                    id = budgetId,
                    userId = 0L, // Assuming it's handled server side
                    name = apiBudget.name,
                    amount = apiBudget.budgetAmount,
                    month = apiBudget.month,
                    year = apiBudget.year
                )
                budgetDao.insertBudget(budget)
                
                val budgetCategories = apiBudget.categories.map { 
                    BudgetCategoryEntity(budgetId = budgetId, categoryId = it.id) 
                }
                budgetDao.insertBudgetCategories(budgetCategories)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createBudget(
        userId: Long,
        name: String,
        amount: Double,
        month: Int,
        year: Int,
        categoryIds: List<Long>
    ) {
        val req = BudgetRequest(name = name, categoryIds = categoryIds, amount = amount, month = month, year = year)
        val response = api.createBudget(req)
        
        val budgetId = response.id.toString()
        val budget = BudgetEntity(
            id = budgetId,
            userId = userId,
            name = name,
            amount = amount,
            month = month,
            year = year
        )
        budgetDao.insertBudget(budget)
        
        val budgetCategories = categoryIds.map { 
            BudgetCategoryEntity(budgetId = budgetId, categoryId = it) 
        }
        budgetDao.insertBudgetCategories(budgetCategories)
    }

    suspend fun updateBudget(
        budgetId: String,
        userId: Long,
        name: String,
        amount: Double,
        month: Int,
        year: Int,
        categoryIds: List<Long>
    ) {
        val req = BudgetRequest(name = name, categoryIds = categoryIds, amount = amount, month = month, year = year)
        api.updateBudget(budgetId.toLong(), req)
        
        val budget = BudgetEntity(
            id = budgetId,
            userId = userId,
            name = name,
            amount = amount,
            month = month,
            year = year
        )
        budgetDao.insertBudget(budget)
        
        budgetDao.deleteBudgetCategories(budgetId)
        val budgetCategories = categoryIds.map { 
            BudgetCategoryEntity(budgetId = budgetId, categoryId = it) 
        }
        budgetDao.insertBudgetCategories(budgetCategories)
    }

    suspend fun deleteBudget(budgetId: String) {
        api.deleteBudget(budgetId.toLong())
        budgetDao.deleteBudgetCategories(budgetId)
        budgetDao.deleteBudget(budgetId)
    }
}
