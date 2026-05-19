package com.dung.ddmoney.ui.expensebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dung.ddmoney.repository.ExpenseBookRepository
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpenseBookViewModel(
    private val repository: ExpenseBookRepository = ExpenseBookRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseBookUiState())
    val uiState: StateFlow<ExpenseBookUiState> = _uiState.asStateFlow()

    init {
        loadExpenseBook()
    }

    fun selectPeriod(period: ExpenseBookPeriod) {
        val current = _uiState.value.filter
        val (fromDate, toDate) = dateRangeForPeriod(period, currentFilter = current)
        _uiState.update {
            it.copy(
                filter =
                    current.copy(
                        period = period,
                        fromDate = fromDate,
                        toDate = toDate
                    )
            )
        }
        loadExpenseBook()
    }

    fun applyCustomRange(fromDate: LocalDate, toDate: LocalDate) {
        if (toDate.isBefore(fromDate)) {
            _uiState.update {
                it.copy(
                    status = ExpenseBookLoadStatus.ERROR,
                    errorMessage = "Ngày kết thúc phải sau ngày bắt đầu"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                filter =
                    it.filter.copy(
                        period = ExpenseBookPeriod.CUSTOM,
                        fromDate = fromDate,
                        toDate = toDate
                    ),
                errorMessage = null
            )
        }
        loadExpenseBook()
    }

    fun selectType(type: ExpenseBookTransactionType?) {
        _uiState.update { it.copy(filter = it.filter.copy(type = type)) }
        loadExpenseBook()
    }

    fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(filter = it.filter.copy(categoryId = categoryId)) }
        loadExpenseBook()
    }

    fun selectWallet(walletId: Long?) {
        _uiState.update { it.copy(filter = it.filter.copy(walletId = walletId)) }
        loadExpenseBook()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filter = state.filter.copy(query = query)
            val visibleTransactions = applySearch(state.transactions, query)
            state.copy(
                filter = filter,
                visibleTransactions = visibleTransactions,
                status = if (visibleTransactions.isEmpty()) ExpenseBookLoadStatus.EMPTY else ExpenseBookLoadStatus.SUCCESS
            )
        }
    }

    fun setFilterOptions(
        categoryOptions: List<ExpenseBookFilterOption>,
        walletOptions: List<ExpenseBookFilterOption>
    ) {
        _uiState.update { state ->
            state.copy(
                categoryOptions =
                    (state.categoryOptions + categoryOptions)
                        .filter { it.id != null }
                        .distinctBy { it.id }
                        .sortedBy { it.label },
                walletOptions =
                    (state.walletOptions + walletOptions)
                        .filter { it.id != null }
                        .distinctBy { it.id }
                        .sortedBy { it.label }
            )
        }
    }

    fun clearSecondaryFilters() {
        _uiState.update {
            it.copy(
                filter =
                    it.filter.copy(
                        type = null,
                        categoryId = null,
                        walletId = null,
                        query = ""
                    )
            )
        }
        loadExpenseBook()
    }

    fun refresh() {
        loadExpenseBook(isRefresh = true)
    }

    fun retry() {
        loadExpenseBook()
    }

    private fun loadExpenseBook(isRefresh: Boolean = false) {
        val filter = _uiState.value.filter

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = if (isRefresh) it.status else ExpenseBookLoadStatus.LOADING,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            val result =
                coroutineScope {
                    val summary = async { repository.getSummary(filter.fromDate, filter.toDate) }
                    val transactions =
                        async {
                            repository.getTransactions(
                                fromDate = filter.fromDate,
                                toDate = filter.toDate,
                                type = filter.type,
                                categoryId = filter.categoryId,
                                walletId = filter.walletId
                            )
                        }
                    val categoryStatistics =
                        async { repository.getCategoryStatistics(filter.fromDate, filter.toDate) }
                    val dailySummary =
                        async { repository.getDailySummary(filter.fromDate, filter.toDate) }

                    ExpenseBookLoadResult(
                        summary = summary.await(),
                        transactions = transactions.await(),
                        categoryStatistics = categoryStatistics.await(),
                        dailySummaries = dailySummary.await()
                    )
                }

            val firstError =
                listOf(
                        result.summary.exceptionOrNull(),
                        result.transactions.exceptionOrNull(),
                        result.categoryStatistics.exceptionOrNull(),
                        result.dailySummaries.exceptionOrNull()
                    )
                    .firstOrNull()

            if (firstError != null) {
                _uiState.update {
                    it.copy(
                        status = ExpenseBookLoadStatus.ERROR,
                        isRefreshing = false,
                        errorMessage = firstError.message ?: "Không thể tải sổ chi tiêu"
                    )
                }
                return@launch
            }

            val transactions = result.transactions.getOrDefault(emptyList())
            val visibleTransactions = applySearch(transactions, filter.query)
            val categoryStatistics = result.categoryStatistics.getOrDefault(emptyList())

            _uiState.update { current ->
                current.copy(
                    status =
                        if (visibleTransactions.isEmpty()) {
                            ExpenseBookLoadStatus.EMPTY
                        } else {
                            ExpenseBookLoadStatus.SUCCESS
                        },
                    isRefreshing = false,
                    summary = result.summary.getOrDefault(ExpenseBookSummary()),
                    transactions = transactions,
                    visibleTransactions = visibleTransactions,
                    categoryStatistics = categoryStatistics,
                    dailySummaries = result.dailySummaries.getOrDefault(emptyList()),
                    categoryOptions =
                        mergeCategoryOptions(
                            existing = current.categoryOptions,
                            transactions = transactions,
                            statistics = categoryStatistics
                        ),
                    walletOptions =
                        mergeWalletOptions(
                            existing = current.walletOptions,
                            transactions = transactions
                        ),
                    errorMessage = null
                )
            }
        }
    }

    private fun applySearch(
        transactions: List<TransactionItem>,
        query: String
    ): List<TransactionItem> {
        val keyword = query.trim()
        if (keyword.isBlank()) return transactions

        return transactions.filter { transaction ->
            transaction.categoryName.contains(keyword, ignoreCase = true) ||
                transaction.note.orEmpty().contains(keyword, ignoreCase = true)
        }
    }

    private fun mergeCategoryOptions(
        existing: List<ExpenseBookFilterOption>,
        transactions: List<TransactionItem>,
        statistics: List<CategoryStatistic>
    ): List<ExpenseBookFilterOption> {
        val fromTransactions =
            transactions.mapNotNull { transaction ->
                transaction.categoryId?.let {
                    ExpenseBookFilterOption(
                        id = it,
                        label = transaction.categoryName,
                        icon = transaction.categoryIcon,
                        colorHex = transaction.categoryColor
                    )
                }
            }
        val fromStatistics =
            statistics.map {
                ExpenseBookFilterOption(
                    id = it.categoryId,
                    label = it.categoryName,
                    icon = it.categoryIcon,
                    colorHex = it.categoryColor
                )
            }

        return (existing + fromStatistics + fromTransactions)
            .filter { it.id != null }
            .distinctBy { it.id }
            .sortedBy { it.label }
    }

    private fun mergeWalletOptions(
        existing: List<ExpenseBookFilterOption>,
        transactions: List<TransactionItem>
    ): List<ExpenseBookFilterOption> {
        val fromTransactions =
            transactions.mapNotNull { transaction ->
                transaction.walletId?.let {
                    ExpenseBookFilterOption(
                        id = it,
                        label = transaction.walletName
                    )
                }
            }

        return (existing + fromTransactions)
            .filter { it.id != null }
            .distinctBy { it.id }
            .sortedBy { it.label }
    }
}

private data class ExpenseBookLoadResult(
    val summary: Result<ExpenseBookSummary>,
    val transactions: Result<List<TransactionItem>>,
    val categoryStatistics: Result<List<CategoryStatistic>>,
    val dailySummaries: Result<List<DailySummary>>
)
