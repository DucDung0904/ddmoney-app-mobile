package com.dung.ddmoney.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dung.ddmoney.network.RetrofitClient
import com.dung.ddmoney.network.dto.CategorySpending
import com.dung.ddmoney.network.dto.MonthlyChart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AnalyticsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val categorySpending: List<CategorySpending> = emptyList(),
    val monthlyCharts: List<MonthlyChart> = emptyList()
)

class AnalyticsViewModel : ViewModel() {
    private val api = RetrofitClient.instance

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    private var currentMonth = LocalDate.now().monthValue
    private var currentYear = LocalDate.now().year

    init {
        loadData()
    }

    fun loadData(month: Int = currentMonth, year: Int = currentYear) {
        currentMonth = month
        currentYear = year
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val spending = api.getCategorySpending(month, year)
                val charts = api.getMonthlyChart(6) // Lấy 6 tháng gần nhất
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    categorySpending = spending,
                    monthlyCharts = charts
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Lỗi tải dữ liệu báo cáo: ${e.message}"
                )
            }
        }
    }
}
