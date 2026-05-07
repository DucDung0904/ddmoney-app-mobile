package com.dung.ddmoney.ui.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dung.ddmoney.AppState
import com.dung.ddmoney.network.dto.BudgetResponse
import com.dung.ddmoney.ui.dashboard.components.formatCurrency
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── Budget Screen ────────────────────────────────────────────────────
@Composable
fun BudgetScreen(
    appState: AppState,
    viewModel: BudgetViewModel = viewModel(),
    onAddBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    val currentMonthText = remember {
        val now = LocalDate.now()
        now.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi")))
            .replaceFirstChar { it.uppercaseChar() }
    }

    // Tổng từ budgets của tháng này (dùng remainingAmount từ server cho chính xác)
    val totalBudget  = state.budgets.sumOf { it.budgetAmount }
    val totalSpent   = state.budgets.sumOf { it.spentAmount }
    val totalLeft    = state.budgets.sumOf { it.remainingAmount } // có thể âm khi vượt mức
    // usedRatio thực tế (có thể > 1.0 khi vượt định mức)
    val usedRatio    = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    // fraction cho arc chỉ 0-1, nhưng giữ usedRatio thực để tô màu
    val usedPercent  = usedRatio.coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ngân sách",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Quản lý hạn mức chi tiêu định kỳ của bạn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onAddBudgetClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Thêm ngân sách",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // ── Overview card with donut chart ────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Donut chart
                    BudgetDonutChart(
                        usedFraction = usedPercent, // arc 0-1
                        usedColor = when {
                            usedRatio >= 1f   -> ExpenseRed
                            usedRatio >= 0.9f -> WarningYellow
                            else              -> MaterialTheme.colorScheme.primary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        size = 180.dp,
                        strokeWidth = 22.dp
                    )

                    Spacer(Modifier.height(20.dp))

                    // Remaining label
                    val isOverall = totalLeft < 0
                    Text(
                        text = if (isOverall) "TỔNG ĐÃ VƯỢT ĐỊNH MỨC" else "TỔNG NGÂN SÁCH CÒN LẠI",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverall) ExpenseRed.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${formatCurrency(totalLeft)} đ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOverall) ExpenseRed else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                    Spacer(Modifier.height(16.dp))

                    // Bottom stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BudgetStatCell(
                            label = "Tổng hạn mức",
                            value = "${formatCurrency(totalBudget)} đ",
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        VerticalDivider(
                            modifier = Modifier.height(36.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                        )
                        BudgetStatCell(
                            label = "Đã chi tiêu",
                            value = "${formatCurrency(totalSpent)} đ",
                            valueColor = when {
                                usedRatio >= 1f   -> ExpenseRed
                                usedRatio >= 0.9f -> WarningYellow
                                else              -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(28.dp)) }

        // ── Section title ─────────────────────────────────────────────
        item {
            Text(
                text = "Giới hạn theo danh mục",
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
        }

        // ── Budget list ───────────────────────────────────────────────
        if (state.isLoading) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        } else if (state.error != null) {
            item {
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else if (state.budgets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💡", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Chưa có ngân sách nào",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Nhấn + để tạo ngân sách đầu tiên",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        } else {
            items(state.budgets.size) { i ->
                BudgetCategoryCard(
                    item = state.budgets[i],
                    onDelete = { viewModel.deleteBudget(state.budgets[i].id) },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 12.dp)
                )
            }
        }
    }
}

// ─── Donut Chart (Canvas) ─────────────────────────────────────────────
@Composable
fun BudgetDonutChart(
    usedFraction: Float,
    usedColor: Color,
    trackColor: Color,
    size: Dp = 180.dp,
    strokeWidth: Dp = 22.dp
) {
    val animatedFraction by animateFloatAsState(
        targetValue = usedFraction,
        animationSpec = tween(durationMillis = 800),
        label = "donut"
    )
    val percent = (usedFraction * 100).toInt()
    val labelText = if (usedFraction >= 1f) "Vượt mức" else "$percent%"

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = minOf(this.size.width, this.size.height) - stroke
            val topLeft = Offset((this.size.width - diameter) / 2, (this.size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)

            // Track
            drawArc(
                color = trackColor,
                startAngle = -220f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
            // Used
            drawArc(
                color = usedColor,
                startAngle = -220f,
                sweepAngle = 260f * animatedFraction,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = labelText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = usedColor
            )
            Text(
                text = "ĐÃ DÙNG",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─── Stat cell ────────────────────────────────────────────────────────
@Composable
private fun BudgetStatCell(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Budget category card ─────────────────────────────────────────────
@Composable
private fun BudgetCategoryCard(
    item: BudgetResponse,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = item.percentage.coerceIn(0f, 1f)
    val isOverBudget = item.percentage >= 1f
    val isWarning    = item.percentage >= 0.9f && !isOverBudget

    val statusLabel = when {
        isOverBudget -> "Vượt định mức"
        isWarning    -> "Cảnh báo"
        else         -> "An toàn"
    }
    val statusColor = when {
        isOverBudget -> ExpenseRed
        isWarning    -> WarningYellow
        else         -> IncomeGreen
    }

    val catColor = try {
        val clean = item.categoryColor?.trimStart('#') ?: "4659A6"
        Color(android.graphics.Color.parseColor("#$clean"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val barColor = when {
        isOverBudget -> ExpenseRed
        isWarning    -> WarningYellow
        else         -> catColor
    }

    // Format spent: "8.5tr" shorthand
    fun shortAmount(v: Double): String = when {
        v >= 1_000_000 -> "${(v / 1_000_000).let { if (it == it.toLong().toDouble()) it.toLong().toString() else String.format("%.1f", it) }}tr"
        v >= 1_000     -> "${(v / 1_000).let { if (it == it.toLong().toDouble()) it.toLong().toString() else String.format("%.1f", it) }}k"
        else           -> v.toLong().toString()
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xóa ngân sách") },
            text = { Text("Bạn có chắc muốn xóa giới hạn ngân sách cho \"${item.categoryName}\"?") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Hủy") }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDeleteConfirm = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(catColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.categoryIcon, fontSize = 22.sp)
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = item.categoryName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Đã dùng: ${shortAmount(item.spentAmount)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "/ ${shortAmount(item.budgetAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
