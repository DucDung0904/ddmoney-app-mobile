package com.dung.ddmoney.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.AppState
import com.dung.ddmoney.ui.dashboard.components.formatCurrency
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── Budget Screen ────────────────────────────────────────────────────
@Composable
fun BudgetScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember {
        val now = LocalDate.now()
        now.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi")))
            .replaceFirstChar { it.uppercaseChar() }
    }

    val totalExpense = appState.currentMonthExpense
    val totalIncome = appState.currentMonthIncome
    val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome).toFloat().coerceIn(0f, 1f) else 0f

    // Sample budget goals — trong thực tế sẽ lưu vào DB
    val budgetItems = remember(appState) {
        appState.categorySpending().map { cat ->
            val limit = cat.totalAmount * 1.2  // mocked limit = 120% spending
            BudgetItem(
                categoryName = cat.name,
                categoryIcon = cat.icon,
                color = cat.color,
                spent = cat.totalAmount,
                limit = limit
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Ngân sách",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentMonth,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Savings Overview Card ─────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(LuminousSecondary, LuminousSecondaryContainer)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LuminousOnSecondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Savings,
                                contentDescription = null,
                                tint = LuminousOnSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            "Tiết kiệm tháng này",
                            color = LuminousOnSecondary.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = formatCurrency(totalIncome - totalExpense),
                        color = LuminousOnSecondary,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(16.dp))

                    // Savings rate bar
                    Text(
                        "Tỷ lệ tiết kiệm: ${(savingsRate * 100).toInt()}%",
                        color = LuminousOnSecondary.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LuminousOnSecondary.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(savingsRate)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(LuminousOnSecondary)
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(28.dp)) }

        // ── Income vs Expense ─────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryTile(
                    label = "Thu nhập",
                    amount = totalIncome,
                    color = IncomeGreen,
                    emoji = "📈",
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    label = "Chi tiêu",
                    amount = totalExpense,
                    color = ExpenseRed,
                    emoji = "📉",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(Modifier.height(28.dp)) }

        // ── Budget by Category ────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Giới hạn theo danh mục",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        if (budgetItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💡", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Chưa có giao dịch nào tháng này",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Thêm giao dịch để xem phân tích chi tiêu",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        } else {
            items(budgetItems.size) { i ->
                BudgetCategoryRow(
                    item = budgetItems[i],
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 12.dp)
                )
            }
        }
    }
}

// ─── Data class for budget item ───────────────────────────────────────
data class BudgetItem(
    val categoryName: String,
    val categoryIcon: String,
    val color: androidx.compose.ui.graphics.Color,
    val spent: Double,
    val limit: Double
)

// ─── Summary tile ─────────────────────────────────────────────────────
@Composable
private fun SummaryTile(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(20.dp)
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ─── Budget category progress row ────────────────────────────────────
@Composable
private fun BudgetCategoryRow(
    item: BudgetItem,
    modifier: Modifier = Modifier
) {
    val progress = if (item.limit > 0) (item.spent / item.limit).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = item.spent >= item.limit * 0.9

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(item.categoryIcon, fontSize = 20.sp)
                Text(
                    item.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatCurrency(item.spent),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "/ ${formatCurrency(item.limit)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isOverBudget) ExpenseRed
                        else item.color
                    )
            )
        }
    }
}
