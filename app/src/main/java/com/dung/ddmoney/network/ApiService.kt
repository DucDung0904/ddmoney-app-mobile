package com.dung.ddmoney.network

import com.dung.ddmoney.network.dto.*
import com.dung.ddmoney.ui.expensebook.CategoryStatistic
import com.dung.ddmoney.ui.expensebook.DailySummary
import com.dung.ddmoney.ui.expensebook.ExpenseBookSummary
import com.dung.ddmoney.ui.expensebook.TransactionItem
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────────────

    @POST("api/auth/login")
    suspend fun login(@Body req: AuthRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): Map<String, String>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body req: GoogleLoginRequest): GoogleAuthResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body req: RefreshTokenRequest): TokenRefreshResponse


    // ─── Users ────────────────────────────────────────────────────────

    @GET("api/users/me")
    suspend fun getCurrentUser(): UserResponse

    @PUT("api/users/avatar")
    suspend fun updateAvatar(@Body req: AvatarUpdateRequest): UserResponse

    // ─── Wallets ──────────────────────────────────────────────────────

    @GET("api/wallets")
    suspend fun getWallets(): List<WalletResponse>

    @GET("api/wallets/{id}")
    suspend fun getWalletById(@Path("id") id: Long): WalletResponse

    @GET("api/wallets/total-balance")
    suspend fun getTotalBalance(): Map<String, Double>

    @POST("api/wallets")
    suspend fun createWallet(@Body req: WalletRequest): WalletResponse

    @PUT("api/wallets/{id}")
    suspend fun updateWallet(@Path("id") id: Long, @Body req: WalletRequest): WalletResponse

    @DELETE("api/wallets/{id}")
    suspend fun deleteWallet(@Path("id") id: Long): Response<Void>

    @POST("api/wallets/{id}/restore")
    suspend fun restoreWallet(@Path("id") id: Long): WalletResponse

    @POST("api/wallets/transfer")
    suspend fun transfer(@Body req: TransferRequest): Map<String, String>

    // ─── Categories ───────────────────────────────────────────────────

    @GET("api/categories")
    suspend fun getCategories(): List<CategoryResponse>

    @GET("api/categories/expense")
    suspend fun getExpenseCategories(): List<CategoryResponse>

    @GET("api/categories/income")
    suspend fun getIncomeCategories(): List<CategoryResponse>

    @GET("api/categories/debt")
    suspend fun getDebtCategories(): List<CategoryResponse>

    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Long): CategoryResponse

    @POST("api/categories")
    suspend fun createCategory(@Body req: CategoryRequest): CategoryResponse

    @PUT("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body req: CategoryRequest): CategoryResponse

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long): Response<Void>

    // ─── Transactions ─────────────────────────────────────────────────

    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): List<TransactionResponse>

    @GET("api/transactions/{id}")
    suspend fun getTransactionById(@Path("id") id: Long): TransactionResponse

    @GET("api/analytics/summary")
    suspend fun getTransactionSummary(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): TransactionSummary

    @GET("api/analytics/monthly-chart")
    suspend fun getMonthlyChart(
        @Query("months") months: Int = 4
    ): List<MonthlyChart>

    @GET("api/analytics/category-spending")
    suspend fun getCategorySpending(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): List<CategorySpending>

    @POST("api/transactions")
    suspend fun createTransaction(@Body req: TransactionRequest): TransactionResponse

    @PUT("api/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: Long,
        @Body req: TransactionRequest
    ): TransactionResponse

    @DELETE("api/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Long): Response<Void>

    // ─── Budgets ──────────────────────────────────────────────────────

    @GET("api/budgets")
    suspend fun getBudgets(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): List<BudgetResponse>

    @GET("api/budgets/current")
    suspend fun getCurrentBudgets(): List<BudgetResponse>

    @POST("api/budgets")
    suspend fun createBudget(@Body req: BudgetRequest): BudgetResponse

    @PUT("api/budgets/{id}")
    suspend fun updateBudget(@Path("id") id: Long, @Body req: BudgetRequest): BudgetResponse

    @DELETE("api/budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: Long): Response<Void>
}

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
