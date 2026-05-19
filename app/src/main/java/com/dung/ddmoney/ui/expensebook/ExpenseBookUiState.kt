package com.dung.ddmoney.ui.expensebook

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class ExpenseBookLoadStatus {
    LOADING,
    SUCCESS,
    EMPTY,
    ERROR
}

data class ExpenseBookFilter(
    val period: ExpenseBookPeriod = ExpenseBookPeriod.MONTH,
    val fromDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    val toDate: LocalDate = LocalDate.now(),
    val type: ExpenseBookTransactionType? = null,
    val categoryId: Long? = null,
    val walletId: Long? = null,
    val query: String = ""
)

data class ExpenseBookUiState(
    val status: ExpenseBookLoadStatus = ExpenseBookLoadStatus.LOADING,
    val isRefreshing: Boolean = false,
    val summary: ExpenseBookSummary = ExpenseBookSummary(),
    val transactions: List<TransactionItem> = emptyList(),
    val visibleTransactions: List<TransactionItem> = emptyList(),
    val categoryStatistics: List<CategoryStatistic> = emptyList(),
    val dailySummaries: List<DailySummary> = emptyList(),
    val categoryOptions: List<ExpenseBookFilterOption> = emptyList(),
    val walletOptions: List<ExpenseBookFilterOption> = emptyList(),
    val filter: ExpenseBookFilter = ExpenseBookFilter(),
    val errorMessage: String? = null
) {
    val groupedTransactions: List<TransactionDayGroup>
        get() {
            val summariesByDate = dailySummaries.associateBy { it.parsedDate }
            return visibleTransactions
                .sortedWith(compareByDescending<TransactionItem> { it.parsedDate }.thenByDescending { it.id })
                .groupBy { it.parsedDate }
                .map { (date, items) ->
                    TransactionDayGroup(
                        date = date,
                        transactions = items,
                        dailySummary = summariesByDate[date]
                    )
                }
        }

    val hasActiveFilters: Boolean
        get() =
            filter.type != null ||
                filter.categoryId != null ||
                filter.walletId != null ||
                filter.query.isNotBlank() ||
                filter.period == ExpenseBookPeriod.CUSTOM
}

fun dateRangeForPeriod(
    period: ExpenseBookPeriod,
    today: LocalDate = LocalDate.now(),
    currentFilter: ExpenseBookFilter = ExpenseBookFilter()
): Pair<LocalDate, LocalDate> {
    return when (period) {
        ExpenseBookPeriod.TODAY -> today to today
        ExpenseBookPeriod.WEEK ->
            today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) to
                today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        ExpenseBookPeriod.MONTH -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
        ExpenseBookPeriod.CUSTOM -> currentFilter.fromDate to currentFilter.toDate
    }
}

object ExpenseBookPreviewData {
    private val today: LocalDate = LocalDate.now()

    val summary =
        ExpenseBookSummary(
            totalExpense = 2_845_000.0,
            totalIncome = 18_500_000.0,
            balance = 15_655_000.0,
            transactionCount = 18
        )

    val categoryStatistics =
        listOf(
            CategoryStatistic(1, "Ăn uống", "restaurant", "#E24B4A", 1_250_000.0, 0.44f),
            CategoryStatistic(2, "Di chuyển", "directions_car", "#185FA5", 620_000.0, 0.22f),
            CategoryStatistic(3, "Mua sắm", "shopping_bag", "#EF9F27", 515_000.0, 0.18f),
            CategoryStatistic(4, "Hóa đơn", "receipt_long", "#1D9E75", 460_000.0, 0.16f)
        )

    val transactions =
        listOf(
            TransactionItem(
                id = 1001,
                categoryId = 1,
                categoryName = "Ăn trưa",
                categoryIcon = "restaurant",
                categoryColor = "#E24B4A",
                walletId = 10,
                walletName = "Tiền mặt",
                amount = 75_000.0,
                type = ExpenseBookTransactionType.EXPENSE.name,
                note = "Cơm văn phòng",
                transactionDate = today.toString()
            ),
            TransactionItem(
                id = 1002,
                categoryId = 11,
                categoryName = "Lương",
                categoryIcon = "payments",
                categoryColor = "#639922",
                walletId = 11,
                walletName = "Vietcombank",
                amount = 18_500_000.0,
                type = ExpenseBookTransactionType.INCOME.name,
                note = "Lương tháng này",
                transactionDate = today.toString()
            ),
            TransactionItem(
                id = 1003,
                categoryId = 2,
                categoryName = "Taxi / Grab",
                categoryIcon = "local_taxi",
                categoryColor = "#185FA5",
                walletId = 10,
                walletName = "Tiền mặt",
                amount = 42_000.0,
                type = ExpenseBookTransactionType.EXPENSE.name,
                note = "Đi gặp khách hàng",
                transactionDate = today.minusDays(1).toString()
            ),
            TransactionItem(
                id = 1004,
                categoryId = 3,
                categoryName = "Mua sắm",
                categoryIcon = "shopping_bag",
                categoryColor = "#EF9F27",
                walletId = 12,
                walletName = "Ví MoMo",
                amount = 320_000.0,
                type = ExpenseBookTransactionType.EXPENSE.name,
                note = "Đồ dùng cá nhân",
                transactionDate = today.minusDays(1).toString()
            )
        )

    val uiState =
        ExpenseBookUiState(
            status = ExpenseBookLoadStatus.SUCCESS,
            summary = summary,
            transactions = transactions,
            visibleTransactions = transactions,
            categoryStatistics = categoryStatistics,
            dailySummaries =
                listOf(
                    DailySummary(today.toString(), totalExpense = 75_000.0, totalIncome = 18_500_000.0, balance = 18_425_000.0, transactionCount = 2),
                    DailySummary(today.minusDays(1).toString(), totalExpense = 362_000.0, totalIncome = 0.0, balance = -362_000.0, transactionCount = 2)
                ),
            categoryOptions =
                categoryStatistics.map {
                    ExpenseBookFilterOption(it.categoryId, it.categoryName, it.categoryIcon, it.categoryColor)
                },
            walletOptions =
                listOf(
                    ExpenseBookFilterOption(10, "Tiền mặt"),
                    ExpenseBookFilterOption(11, "Vietcombank"),
                    ExpenseBookFilterOption(12, "Ví MoMo")
                )
        )
}
