package com.dung.ddmoney.repository

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
    private val api: ExpenseBookApiService = RetrofitClient.expenseBookService
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun getSummary(fromDate: LocalDate, toDate: LocalDate): Result<ExpenseBookSummary> =
        safeCall {
            api.getSummary(fromDate.format(formatter), toDate.format(formatter))
        }

    suspend fun getTransactions(
        fromDate: LocalDate,
        toDate: LocalDate,
        type: ExpenseBookTransactionType?,
        categoryId: Long?,
        walletId: Long?
    ): Result<List<TransactionItem>> =
        safeCall {
            api.getTransactions(
                fromDate = fromDate.format(formatter),
                toDate = toDate.format(formatter),
                type = type?.name,
                categoryId = categoryId,
                walletId = walletId
            )
        }

    suspend fun getCategoryStatistics(
        fromDate: LocalDate,
        toDate: LocalDate
    ): Result<List<CategoryStatistic>> =
        safeCall {
            api.getCategoryStatistics(fromDate.format(formatter), toDate.format(formatter))
        }

    suspend fun getDailySummary(fromDate: LocalDate, toDate: LocalDate): Result<List<DailySummary>> =
        safeCall {
            api.getDailySummary(fromDate.format(formatter), toDate.format(formatter))
        }
}
