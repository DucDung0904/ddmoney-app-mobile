package com.dung.ddmoney.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dung.ddmoney.network.dto.CategorySpending
import com.dung.ddmoney.network.dto.MonthlyChart
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phân tích", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                item {
                    Text("Xu hướng thu chi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(16.dp))
                    MonthlyBarChart(data = state.monthlyCharts)
                }

                item {
                    Text("Cơ cấu chi tiêu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(16.dp))
                    if (state.categorySpending.isEmpty()) {
                        Text("Chưa có giao dịch chi tiêu nào trong tháng.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        DonutChart(data = state.categorySpending)
                    }
                }

                items(state.categorySpending) { item ->
                    CategorySpendingItem(item)
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun MonthlyBarChart(data: List<MonthlyChart>) {
    if (data.isEmpty()) return
    

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val maxAmount = data.maxOfOrNull { maxOf(it.income, it.expense) }?.toFloat() ?: 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(10.dp).background(primaryColor, RoundedCornerShape(3.dp)))
                    Text("Thu nhập", style = MaterialTheme.typography.labelSmall, color = labelColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(10.dp).background(errorColor, RoundedCornerShape(3.dp)))
                    Text("Chi tiêu", style = MaterialTheme.typography.labelSmall, color = labelColor)
                }
            }
            Spacer(Modifier.height(16.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val colWidth = size.width / data.size
                val barWidth = colWidth * 0.28f
                val gap = barWidth * 0.3f
                val chartHeight = size.height

                data.forEachIndexed { i, item ->
                    val baseX = i * colWidth + (colWidth - barWidth * 2 - gap) / 2

                    val incomeH = if (maxAmount > 0) (item.income.toFloat() / maxAmount) * chartHeight else 0f
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(baseX, chartHeight - incomeH),
                        size = Size(barWidth, incomeH),
                        cornerRadius = CornerRadius(barWidth / 2)
                    )

                    val expenseH = if (maxAmount > 0) (item.expense.toFloat() / maxAmount) * chartHeight else 0f
                    drawRoundRect(
                        color = errorColor,
                        topLeft = Offset(baseX + barWidth + gap, chartHeight - expenseH),
                        size = Size(barWidth, expenseH),
                        cornerRadius = CornerRadius(barWidth / 2)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                data.forEach { item ->
                    Text(
                        text = item.monthLabel,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DonutChart(data: List<CategorySpending>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                var startAngle = -90f
                val strokeWidth = 32.dp.toPx()

                data.forEach { item ->
                    val sweepAngle = item.percentage * 360f
                    val cleanHex = item.categoryColor?.trimStart('#') ?: "4659A6"
                    val color = try { Color(android.graphics.Color.parseColor("#$cleanHex")) } catch(e: Exception) { Color.Gray }

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    )
                    startAngle += sweepAngle
                }
            }
        }
    }
}

@Composable
fun CategorySpendingItem(item: CategorySpending) {
    val cleanHex = item.categoryColor?.trimStart('#') ?: "4659A6"
    val color = try { Color(android.graphics.Color.parseColor("#$cleanHex")) } catch(e: Exception) { Color.Gray }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(item.categoryIcon, fontSize = 24.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.categoryName, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { item.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(currencyFormat.format(item.amount), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("${(item.percentage * 100).toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
