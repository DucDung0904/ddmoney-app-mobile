package com.dung.ddmoney.ui.expensebook

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.util.Locale

enum class ExpenseBookTransactionType(val label: String) {
    EXPENSE("Chi tiêu"),
    INCOME("Thu nhập"),
    TRANSFER("Chuyển ví")
}

enum class ExpenseBookPeriod(val label: String) {
    TODAY("Hôm nay"),
    WEEK("Tuần này"),
    MONTH("Tháng này"),
    CUSTOM("Tùy chỉnh")
}

data class ExpenseBookSummary(
    @SerializedName(value = "totalExpense", alternate = ["total_expense"])
    val totalExpense: Double = 0.0,
    @SerializedName(value = "totalIncome", alternate = ["total_income"])
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0,
    @SerializedName(value = "transactionCount", alternate = ["transaction_count"])
    val transactionCount: Int = 0
)

data class TransactionItem(
    val id: Long,
    @SerializedName(value = "categoryId", alternate = ["category_id"])
    val categoryId: Long? = null,
    val categoryName: String = "Khác",
    val categoryIcon: String = "category",
    val categoryColor: String? = null,
    @SerializedName(value = "walletId", alternate = ["wallet_id"])
    val walletId: Long? = null,
    val walletName: String = "Ví",
    val amount: Double = 0.0,
    val type: String = ExpenseBookTransactionType.EXPENSE.name,
    val note: String? = null,
    @SerializedName(value = "transactionDate", alternate = ["transaction_date", "date"])
    val transactionDate: String
) {
    val parsedDate: LocalDate
        get() = parseExpenseBookDate(transactionDate)

    val normalizedType: ExpenseBookTransactionType?
        get() =
            runCatching {
                ExpenseBookTransactionType.valueOf(type.trim().uppercase(Locale.ROOT))
            }.getOrNull()
}

data class CategoryStatistic(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String = "category",
    val categoryColor: String? = null,
    val totalAmount: Double = 0.0,
    val percentage: Float = 0f
)

data class DailySummary(
    val date: String,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0,
    val transactionCount: Int = 0
) {
    val parsedDate: LocalDate
        get() = parseExpenseBookDate(date)
}

data class ExpenseBookFilterOption(
    val id: Long?,
    val label: String,
    val icon: String? = null,
    val colorHex: String? = null
)

data class TransactionDayGroup(
    val date: LocalDate,
    val transactions: List<TransactionItem>,
    val dailySummary: DailySummary? = null
) {
    val totalExpense: Double =
        dailySummary?.totalExpense
            ?: transactions
                .filter { it.normalizedType == ExpenseBookTransactionType.EXPENSE }
                .sumOf { it.amount }

    val totalIncome: Double =
        dailySummary?.totalIncome
            ?: transactions
                .filter { it.normalizedType == ExpenseBookTransactionType.INCOME }
                .sumOf { it.amount }
}

fun parseExpenseBookDate(raw: String): LocalDate {
    val normalized = raw.trim()
    if (normalized.length >= 10) {
        runCatching { return LocalDate.parse(normalized.take(10)) }
    }
    return runCatching { LocalDate.parse(normalized) }.getOrDefault(LocalDate.now())
}
