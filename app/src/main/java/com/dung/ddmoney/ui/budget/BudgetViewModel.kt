package com.dung.ddmoney.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dung.ddmoney.network.RetrofitClient
import com.dung.ddmoney.network.dto.BudgetRequest
import com.dung.ddmoney.network.dto.BudgetResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class BudgetState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val budgets: List<BudgetResponse> = emptyList(),
    val currentMonth: Int = LocalDate.now().monthValue,
    val currentYear: Int = LocalDate.now().year
)

class BudgetViewModel : ViewModel() {
    private val api = RetrofitClient.instance

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    init {
        loadBudgets()
    }

    fun loadBudgets(month: Int = _state.value.currentMonth, year: Int = _state.value.currentYear) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, currentMonth = month, currentYear = year)
            try {
                val budgets = api.getBudgets(month, year)
                _state.value = _state.value.copy(isLoading = false, budgets = budgets)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Lỗi tải ngân sách: ${e.message}")
            }
        }
    }

    fun saveBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val req = BudgetRequest(
                    categoryId = categoryId,
                    amount = amount,
                    month = _state.value.currentMonth,
                    year = _state.value.currentYear
                )
                api.createBudget(req)
                loadBudgets() // Reload after saving
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Lỗi lưu ngân sách: ${e.message}")
            }
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                api.deleteBudget(budgetId)
                loadBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Lỗi xóa ngân sách: ${e.message}")
            }
        }
    }
}
