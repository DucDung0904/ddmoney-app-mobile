package com.dung.ddmoney.ui.analytics

import androidx.compose.ui.graphics.Color
import com.dung.ddmoney.ui.home.components.DefaultCategorySeed
import com.dung.ddmoney.ui.home.components.DefaultCategorySpec
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import com.dung.ddmoney.ui.components.formatMoneyDisplay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.roundToLong

enum class ReportPeriod(val label: String) {
    WEEK("Tuần"),
    MONTH("Tháng")
}

data class CategoryExpense(
        val id: String,
        val name: String,
        val icon: String,
        val color: Color,
        val amount: Double,
        val percentage: Float
)

data class CategoryExpenseGroup(
        val parent: CategoryExpense,
        val children: List<CategoryExpense>
)

data class ExpenseReport(
        val period: ReportPeriod,
        val currentTotal: Double,
        val previousTotal: Double,
        val transactionCount: Int,
        val previousTransactionCount: Int = 0,
        val currentLabel: String,
        val previousLabel: String,
        val rangeLabel: String,
        val summaryLabel: String,
        val topCategories: List<CategoryExpense>,
        val breakdownCategories: List<CategoryExpense> = topCategories,
        val categoryBreakdowns: List<CategoryExpenseGroup> =
                topCategories.map { CategoryExpenseGroup(parent = it, children = emptyList()) }
) {
    val hasCurrentPeriodData: Boolean
        get() = transactionCount > 0 || currentTotal > 0.0

    val hasPreviousPeriodData: Boolean
        get() = previousTransactionCount > 0 || previousTotal > 0.0

    val hasComparisonContext: Boolean
        get() = hasCurrentPeriodData || hasPreviousPeriodData

    val difference: Double
        get() = currentTotal - previousTotal

    val differencePercentage: Float
        get() =
                if (hasCurrentPeriodData && previousTotal > 0.0) {
                    ((difference / previousTotal) * 100.0).toFloat()
                } else {
                    0f
                }
}

fun buildExpenseReport(
        transactions: List<Transaction>,
        period: ReportPeriod,
        today: LocalDate = LocalDate.now(),
        categories: List<Category> = emptyList()
): ExpenseReport {
    val range = currentRange(period, today)
    val previousRange = previousRange(period, range.first)
    val resolver = ReportCategoryResolver(categories)

    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
    val currentTransactions = expenseTransactions.filter { it.date.isWithin(range) }
    val previousTransactions =
            expenseTransactions.filter { it.date.isWithin(previousRange) }

    val currentTotal = currentTransactions.sumOf { it.amount }
    val previousTotal = previousTransactions.sumOf { it.amount }

    val topCategories =
            currentTransactions.toCategoryExpenses(currentTotal) { resolver.parentReportCategory(it) }
    val categoryBreakdowns =
            topCategories.map { parentCategory ->
                val parentTransactions =
                        currentTransactions.filter {
                            resolver.parentReportCategory(it).key == parentCategory.id
                        }
                val childExpenses =
                        parentTransactions
                                .toCategoryExpenses(parentCategory.amount) {
                                    resolver.childReportCategory(it)
                                }
                val hasSubcategoryRows = childExpenses.any { it.id != parentCategory.id }
                val children =
                        childExpenses
                                .map { child ->
                                    if (child.id == parentCategory.id && hasSubcategoryRows) {
                                        child.copy(
                                                id = "${parentCategory.id}:direct",
                                                name = "Chi trực tiếp"
                                        )
                                    } else {
                                        child
                                    }
                                }
                CategoryExpenseGroup(parent = parentCategory, children = children)
            }

    return ExpenseReport(
            period = period,
            currentTotal = currentTotal,
            previousTotal = previousTotal,
            transactionCount = currentTransactions.size,
            previousTransactionCount = previousTransactions.size,
            currentLabel = if (period == ReportPeriod.WEEK) "Tuần này" else "Tháng này",
            previousLabel = if (period == ReportPeriod.WEEK) "Tuần trước" else "Tháng trước",
            rangeLabel = rangeLabel(period, range.first, range.second),
            summaryLabel =
                    if (period == ReportPeriod.WEEK) {
                        "Tổng đã chi tuần này"
                    } else {
                        "Tổng đã chi tháng này"
                    },
            topCategories = topCategories,
            breakdownCategories = topCategories,
            categoryBreakdowns = categoryBreakdowns
    )
}

private fun List<Transaction>.toCategoryExpenses(
        currentTotal: Double,
        descriptor: (Transaction) -> ReportCategoryDescriptor
): List<CategoryExpense> =
        groupBy { descriptor(it).key }
                .map { (_, items) ->
                    val amount = items.sumOf { it.amount }
                    val category = descriptor(items.maxBy { it.amount })
                    CategoryExpense(
                            id = category.key,
                            name = category.name,
                            icon = category.icon,
                            color = category.color,
                            amount = amount,
                            percentage =
                                    if (currentTotal > 0.0) {
                                        (amount / currentTotal).toFloat()
                                    } else {
                                        0f
                                    }
                    )
                }
                .sortedByDescending { it.amount }

private data class ReportCategoryDescriptor(
        val key: String,
        val name: String,
        val icon: String,
        val color: Color
)

private class ReportCategoryResolver(categories: List<Category>) {
    private val categoriesById = categories.associateBy { it.id }

    fun parentReportCategory(transaction: Transaction): ReportCategoryDescriptor {
        val appCategory = categoriesById[transaction.categoryId]
        val seedCategory = transaction.seedCategory()
        val appParent =
                appCategory?.parentId?.let { parentId ->
                    categoriesById[parentId]?.toReportDescriptor()
                            ?: DefaultCategorySeed.findById(parentId)?.toReportDescriptor()
                }
        if (appParent != null) return appParent
        if (appCategory != null && seedCategory?.parentId == null) {
            return appCategory.toReportDescriptor()
        }

        val seedParent = seedCategory?.let(DefaultCategorySeed::parentOf)
        return seedParent?.let(::descriptorForSeed) ?: childReportCategory(transaction)
    }

    fun childReportCategory(transaction: Transaction): ReportCategoryDescriptor {
        val appCategory = categoriesById[transaction.categoryId]
        if (appCategory != null) return appCategory.toReportDescriptor()

        return transaction.seedCategory()?.toReportDescriptor()
                ?: ReportCategoryDescriptor(
                        key = transaction.fallbackCategoryKey(),
                        name = transaction.reportCategoryName(),
                        icon = transaction.categoryIcon.ifBlank { "📦" },
                        color = transaction.categoryColor
                )
    }

    private fun descriptorForSeed(seed: DefaultCategorySpec): ReportCategoryDescriptor {
        return categoriesById[seed.id.toString()]?.toReportDescriptor()
                ?: seed.toReportDescriptor()
    }
}

private fun Transaction.seedCategory(): DefaultCategorySpec? {
    return DefaultCategorySeed.findById(categoryId)
            ?: DefaultCategorySeed.findByName(categoryName)
}

private fun DefaultCategorySpec.toReportDescriptor(): ReportCategoryDescriptor =
        ReportCategoryDescriptor(
                key = "default:$id",
                name = name,
                icon = icon,
                color = parseReportColor(colorHex)
        )

private fun Category.toReportDescriptor(): ReportCategoryDescriptor =
        ReportCategoryDescriptor(
                key = "category:$id",
                name = name,
                icon = icon,
                color = color
        )

private fun Transaction.fallbackCategoryKey(): String {
    val normalizedName = categoryName.trim().lowercase(Locale.ROOT)
    return when {
        normalizedName.isNotBlank() -> "name:$normalizedName"
        categoryId.isNotBlank() -> "id:${categoryId.trim()}"
        else -> "unknown"
    }
}

private fun Transaction.reportCategoryName(): String {
    return categoryName.trim().ifBlank { title.trim().ifBlank { "Khác" } }
}

private fun parseReportColor(hex: String): Color =
        runCatching { Color(android.graphics.Color.parseColor(hex)) }
                .getOrDefault(Color(0xFF4659A6))

fun formatVnd(amount: Double): String {
    return formatMoneyDisplay(amount)
}

fun formatPercentageChange(percentage: Float): String {
    if (!percentage.isFinite()) return "0%"

    val roundedMagnitude = round(abs(percentage.toDouble()) * 10.0) / 10.0
    val magnitudeText =
            if (roundedMagnitude % 1.0 == 0.0) {
                roundedMagnitude.toInt().toString()
            } else {
                roundedMagnitude.toString()
            }
    val sign =
            when {
                roundedMagnitude == 0.0 -> ""
                percentage > 0f -> "+"
                percentage < 0f -> "-"
                else -> ""
            }

    return "$sign$magnitudeText%"
}

fun spendingDeltaAmountText(report: ExpenseReport): String {
    return if (report.hasPreviousPeriodData) {
        formatVnd(abs(report.difference))
    } else {
        "Chưa có"
    }
}

fun spendingDeltaContextLabel(report: ExpenseReport): String {
    val previousLabel = report.previousLabel.lowercase()
    return when {
        !report.hasPreviousPeriodData -> "dữ liệu $previousLabel"
        report.difference > 0.0 -> "nhiều hơn $previousLabel"
        report.difference < 0.0 -> "ít hơn $previousLabel"
        else -> "bằng $previousLabel"
    }
}

fun comparisonChartAxisMax(vararg amounts: Double): Double {
    val maxAmount = amounts.maxOrNull() ?: 0.0
    if (maxAmount <= 0.0) return 1.0

    val step =
            when {
                maxAmount >= 100_000_000.0 -> 10_000_000.0
                maxAmount >= 1_000_000.0 -> 1_000_000.0
                maxAmount >= 100_000.0 -> 100_000.0
                maxAmount >= 10_000.0 -> 10_000.0
                maxAmount >= 1_000.0 -> 1_000.0
                else -> 100.0
            }

    return ceil(maxAmount / step) * step
}

fun comparisonChartVisualRatio(amount: Double, axisMaxAmount: Double): Float {
    if (amount <= 0.0 || axisMaxAmount <= 0.0) return 0f

    val trueRatio = (amount / axisMaxAmount).toFloat().coerceIn(0f, 1f)
    val readableFloor = 0.26f
    return (readableFloor + trueRatio * (1f - readableFloor)).coerceIn(0f, 1f)
}

fun compactMoney(amount: Double): String {
    return when {
        amount >= 1_000_000_000.0 -> compactUnit(amount / 1_000_000_000.0, "B")
        amount >= 1_000_000.0 -> compactUnit(amount / 1_000_000.0, "M")
        amount >= 1_000.0 -> compactUnit(amount / 1_000.0, "K")
        else -> amount.roundToLong().toString()
    }
}

private fun compactUnit(value: Double, suffix: String): String {
    val roundedValue = round(value * 10.0) / 10.0
    return if (roundedValue % 1.0 == 0.0) {
        "${roundedValue.toInt()}$suffix"
    } else {
        "$roundedValue$suffix"
    }
}

private fun currentRange(period: ReportPeriod, today: LocalDate): Pair<LocalDate, LocalDate> {
    return when (period) {
        ReportPeriod.WEEK -> {
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            start to start.plusDays(6)
        }
        ReportPeriod.MONTH -> {
            val start = today.withDayOfMonth(1)
            start to start.plusMonths(1).minusDays(1)
        }
    }
}

private fun LocalDate.isWithin(range: Pair<LocalDate, LocalDate>): Boolean {
    return !isBefore(range.first) && !isAfter(range.second)
}

private fun previousRange(period: ReportPeriod, currentStart: LocalDate): Pair<LocalDate, LocalDate> {
    return when (period) {
        ReportPeriod.WEEK -> {
            val start = currentStart.minusWeeks(1)
            start to start.plusDays(6)
        }
        ReportPeriod.MONTH -> {
            val start = currentStart.minusMonths(1)
            start to start.plusMonths(1).minusDays(1)
        }
    }
}

private fun rangeLabel(period: ReportPeriod, start: LocalDate, end: LocalDate): String {
    return when (period) {
        ReportPeriod.WEEK -> {
            val formatter = DateTimeFormatter.ofPattern("dd/MM")
            "${start.format(formatter)} - ${end.format(formatter)}"
        }
        ReportPeriod.MONTH -> "Tháng ${start.monthValue}/${start.year}"
    }
}
