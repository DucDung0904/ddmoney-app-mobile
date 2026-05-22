package com.dung.ddmoney.repository

import com.dung.ddmoney.local.dao.TransactionDao
import com.dung.ddmoney.local.entity.TransactionEntity
import com.dung.ddmoney.network.ExpenseBookApiService
import com.dung.ddmoney.network.RetrofitClient
import com.dung.ddmoney.ui.expensebook.CategoryStatistic
import com.dung.ddmoney.ui.expensebook.DailySummary
import com.dung.ddmoney.ui.expensebook.ExpenseBookSummary
import com.dung.ddmoney.ui.expensebook.ExpenseBookTransactionType
import com.dung.ddmoney.ui.expensebook.TransactionItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpenseBookRepository(
    private val api: ExpenseBookApiService = RetrofitClient.expenseBookService,
    private val transactionDao: TransactionDao? = null
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun getSummary(fromDate: LocalDate, toDate: LocalDate): Result<ExpenseBookSummary> =
        remoteOrLocal(
            remote = {
                api.getSummary(fromDate.format(formatter), toDate.format(formatter))
            },
            local = { dao ->
                dao.getBetweenDates(fromDate.format(formatter), toDate.format(formatter))
                    .toExpenseBookSummary()
            }
        )

    suspend fun getTransactions(
        fromDate: LocalDate,
        toDate: LocalDate,
        type: ExpenseBookTransactionType?,
        categoryId: Long?,
        walletId: Long?
    ): Result<List<TransactionItem>> =
        remoteOrLocal(
            remote = {
                api.getTransactions(
                    fromDate = fromDate.format(formatter),
                    toDate = toDate.format(formatter),
                    type = type?.name,
                    categoryId = categoryId,
                    walletId = walletId
                )
            },
            local = { dao ->
                dao.getForExpenseBook(
                        fromDate = fromDate.format(formatter),
                        toDate = toDate.format(formatter),
                        type = type?.name,
                        categoryId = categoryId,
                        walletId = walletId
                    )
                    .map { it.toExpenseBookItem() }
            }
        )

    suspend fun getCategoryStatistics(
        fromDate: LocalDate,
        toDate: LocalDate
    ): Result<List<CategoryStatistic>> =
        remoteOrLocal(
            remote = {
                api.getCategoryStatistics(fromDate.format(formatter), toDate.format(formatter))
            },
            local = { dao ->
                dao.getBetweenDates(fromDate.format(formatter), toDate.format(formatter))
                    .toCategoryStatistics()
            }
        )

    suspend fun getDailySummary(fromDate: LocalDate, toDate: LocalDate): Result<List<DailySummary>> =
        remoteOrLocal(
            remote = {
                api.getDailySummary(fromDate.format(formatter), toDate.format(formatter))
            },
            local = { dao ->
                dao.getBetweenDates(fromDate.format(formatter), toDate.format(formatter))
                    .toDailySummaries()
            }
        )

    private suspend fun <T> remoteOrLocal(
        remote: suspend () -> T,
        local: suspend (TransactionDao) -> T
    ): Result<T> {
        transactionDao?.let { dao ->
            return try {
                Result.success(local(dao))
            } catch (localError: Exception) {
                try {
                    Result.success(remote())
                } catch (remoteError: Exception) {
                    Result.failure(localError)
                }
            }
        }

        return try {
            Result.success(remote())
        } catch (remoteError: Exception) {
            Result.failure(remoteError)
        }
    }
}

private fun List<TransactionEntity>.toExpenseBookSummary(): ExpenseBookSummary {
    val totalExpense = filter { it.isType(ExpenseBookTransactionType.EXPENSE) }.sumOf { it.amount }
    val totalIncome = filter { it.isType(ExpenseBookTransactionType.INCOME) }.sumOf { it.amount }
    return ExpenseBookSummary(
        totalExpense = totalExpense,
        totalIncome = totalIncome,
        balance = totalIncome - totalExpense,
        transactionCount = size
    )
}

private fun List<TransactionEntity>.toCategoryStatistics(): List<CategoryStatistic> {
    val expenses = filter { it.isType(ExpenseBookTransactionType.EXPENSE) }
    val totalExpense = expenses.sumOf { it.amount }
    if (expenses.isEmpty() || totalExpense <= 0.0) return emptyList()

    return expenses
        .groupBy { it.categoryId }
        .map { (categoryId, transactions) ->
            val first = transactions.first()
            val categoryTotal = transactions.sumOf { it.amount }
            CategoryStatistic(
                categoryId = categoryId,
                categoryName = first.categoryName ?: "Khác",
                categoryIcon = first.categoryIcon ?: "category",
                categoryColor = first.categoryColor,
                totalAmount = categoryTotal,
                percentage = (categoryTotal / totalExpense).toFloat()
            )
        }
        .sortedByDescending { it.totalAmount }
}

private fun List<TransactionEntity>.toDailySummaries(): List<DailySummary> =
    groupBy { it.date.take(10) }
        .map { (date, transactions) ->
            val totalExpense =
                transactions.filter { it.isType(ExpenseBookTransactionType.EXPENSE) }.sumOf { it.amount }
            val totalIncome =
                transactions.filter { it.isType(ExpenseBookTransactionType.INCOME) }.sumOf { it.amount }
            DailySummary(
                date = date,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                balance = totalIncome - totalExpense,
                transactionCount = transactions.size
            )
        }
        .sortedByDescending { it.date }

private fun TransactionEntity.toExpenseBookItem(): TransactionItem =
    TransactionItem(
        id = serverId ?: stableLocalLongId(id),
        categoryId = categoryId,
        categoryName = categoryName ?: "Khác",
        categoryIcon = categoryIcon ?: "category",
        categoryColor = categoryColor,
        walletId = walletId,
        walletName = walletName ?: "Ví",
        amount = amount,
        type = type,
        note = note,
        transactionDate = date
    )

private fun TransactionEntity.isType(type: ExpenseBookTransactionType): Boolean =
    this.type.equals(type.name, ignoreCase = true)

private fun stableLocalLongId(localId: String): Long {
    val positive =
        localId.hashCode().toLong().let { hash ->
            if (hash < 0L) -hash else hash
        }.coerceAtLeast(1L)
    return -positive
}
