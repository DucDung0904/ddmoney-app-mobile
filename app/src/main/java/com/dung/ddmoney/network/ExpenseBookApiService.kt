package com.dung.ddmoney.network

import com.dung.ddmoney.ui.expensebook.CategoryStatistic
import com.dung.ddmoney.ui.expensebook.DailySummary
import com.dung.ddmoney.ui.expensebook.ExpenseBookSummary
import com.dung.ddmoney.ui.expensebook.TransactionItem
import retrofit2.http.GET
import retrofit2.http.Query

interface ExpenseBookApiService {
    @GET("api/expense-book/summary")
    suspend fun getSummary(
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): ExpenseBookSummary

    @GET("api/expense-book/transactions")
    suspend fun getTransactions(
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String,
        @Query("type") type: String? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("walletId") walletId: Long? = null
    ): List<TransactionItem>

    @GET("api/expense-book/category-statistics")
    suspend fun getCategoryStatistics(
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): List<CategoryStatistic>

    @GET("api/expense-book/daily-summary")
    suspend fun getDailySummary(
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): List<DailySummary>
}
