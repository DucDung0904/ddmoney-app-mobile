package com.dung.ddmoney.repository

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

enum class BudgetPeriodType {
    WEEK,
    MONTH,
    QUARTER,
    YEAR;

    companion object {
        fun fromStorage(value: String?): BudgetPeriodType =
            entries.firstOrNull { it.name == value } ?: MONTH
    }
}

data class BudgetPeriod(
    val type: BudgetPeriodType,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    init {
        require(!endDate.isBefore(startDate)) { "Budget period end date must not precede start date" }
    }

    val month: Int get() = startDate.monthValue
    val year: Int get() = startDate.year

    fun contains(date: LocalDate): Boolean =
        !date.isBefore(startDate) && !date.isAfter(endDate)

    companion object {
        fun current(
            type: BudgetPeriodType,
            date: LocalDate = LocalDate.now()
        ): BudgetPeriod =
            when (type) {
                BudgetPeriodType.WEEK -> {
                    val start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    BudgetPeriod(type, start, start.plusDays(6))
                }

                BudgetPeriodType.MONTH -> {
                    val month = YearMonth.from(date)
                    BudgetPeriod(type, month.atDay(1), month.atEndOfMonth())
                }

                BudgetPeriodType.QUARTER -> {
                    val firstMonth = ((date.monthValue - 1) / 3) * 3 + 1
                    val start = LocalDate.of(date.year, firstMonth, 1)
                    BudgetPeriod(type, start, start.plusMonths(3).minusDays(1))
                }

                BudgetPeriodType.YEAR -> {
                    val start = LocalDate.of(date.year, 1, 1)
                    BudgetPeriod(type, start, start.withMonth(12).withDayOfMonth(31))
                }
            }

        fun fromStorage(
            typeValue: String?,
            startValue: String?,
            endValue: String?,
            month: Int,
            year: Int
        ): BudgetPeriod {
            val type = BudgetPeriodType.fromStorage(typeValue)
            val start = startValue?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            val end = endValue?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            if (start != null && end != null && !end.isBefore(start)) {
                return BudgetPeriod(type, start, end)
            }

            val safeMonth = month.coerceIn(1, 12)
            return current(type, LocalDate.of(year, safeMonth, 1))
        }

        fun periodsInYear(type: BudgetPeriodType, year: Int): List<BudgetPeriod> =
            when (type) {
                BudgetPeriodType.WEEK -> {
                    val weekFields = WeekFields.ISO
                    val firstWeekStart =
                        LocalDate.of(year, 1, 4)
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    generateSequence(firstWeekStart) { it.plusWeeks(1) }
                        .takeWhile { it.get(weekFields.weekBasedYear()) == year }
                        .map { BudgetPeriod(type, it, it.plusDays(6)) }
                        .toList()
                }

                BudgetPeriodType.MONTH ->
                    (1..12).map { month ->
                        val yearMonth = YearMonth.of(year, month)
                        BudgetPeriod(type, yearMonth.atDay(1), yearMonth.atEndOfMonth())
                    }

                BudgetPeriodType.QUARTER ->
                    (1..4).map { quarter ->
                        val start = LocalDate.of(year, (quarter - 1) * 3 + 1, 1)
                        BudgetPeriod(type, start, start.plusMonths(3).minusDays(1))
                    }

                BudgetPeriodType.YEAR -> {
                    val start = LocalDate.of(year, 1, 1)
                    listOf(BudgetPeriod(type, start, LocalDate.of(year, 12, 31)))
                }
            }
    }
}
