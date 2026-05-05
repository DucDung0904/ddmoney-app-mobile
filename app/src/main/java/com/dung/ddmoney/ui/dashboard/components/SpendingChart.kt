package com.dung.ddmoney.ui.dashboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.dung.ddmoney.ui.dashboard.model.MonthlySummary
import com.dung.ddmoney.ui.dashboard.model.SampleData
import com.dung.ddmoney.ui.theme.*

// ─── Spending Insights Section (Luminous Finance style) ───────────────
@Composable
fun SpendingChartSection(
    monthlyData: List<MonthlySummary>,
    totalSavings: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spending Insights",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "4 tháng gần đây",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(20.dp)
        ) {
            Column {
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = MaterialTheme.colorScheme.primary, label = "Thu nhập")
                    LegendDot(color = MaterialTheme.colorScheme.surfaceContainerHigh, label = "Chi tiêu")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Luminous-style bar chart
                LuminousBarChart(monthlyData = monthlyData)

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Savings summary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tiết kiệm tháng này",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(totalSavings),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    SavingsRing(
                        savings = totalSavings,
                        income = monthlyData.lastOrNull()?.income ?: 1.0
                    )
                }
            }
        }
    }
}

// ─── Luminous Bar Chart ───────────────────────────────────────────────
@Composable
fun LuminousBarChart(monthlyData: List<MonthlySummary>) {
    val maxValue = monthlyData.maxOf { maxOf(it.income, it.expense) }
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "bar_anim"
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val bgBarColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        monthlyData.forEachIndexed { index, summary ->
            val isCurrentMonth = index == monthlyData.lastIndex
            LuminousBarGroup(
                summary = summary,
                maxValue = maxValue,
                animationProgress = animationProgress,
                isHighlighted = isCurrentMonth,
                primaryColor = primaryColor,
                bgBarColor = bgBarColor
            )
        }
    }
}

@Composable
fun LuminousBarGroup(
    summary: MonthlySummary,
    maxValue: Double,
    animationProgress: Float,
    isHighlighted: Boolean,
    primaryColor: Color,
    bgBarColor: Color
) {
    val incomeHeight = ((summary.income / maxValue) * 120 * animationProgress).dp
    val expenseHeight = ((summary.expense / maxValue) * 120 * animationProgress).dp

    val incomeBarColor = if (isHighlighted) primaryColor else primaryColor.copy(alpha = 0.25f)
    val expenseBarColor = bgBarColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.width(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Income bar (taller, primary colored)
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(incomeHeight)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(incomeBarColor)
            )
            // Expense bar (shorter, muted)
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(expenseHeight)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(expenseBarColor)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = summary.month,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ─── Savings Ring ─────────────────────────────────────────────────────
@Composable
fun SavingsRing(savings: Double, income: Double) {
    val percentage = (savings / income).coerceIn(0.0, 1.0).toFloat()
    val animatedSweep by animateFloatAsState(
        targetValue = percentage * 360f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "ring"
    )
    val ringColor = MaterialTheme.colorScheme.primary

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(56.dp)) {
            val strokeWidth = 7.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)

            drawArc(
                color = ringColor.copy(alpha = 0.1f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(ringColor.copy(alpha = 0.5f), ringColor)),
                startAngle = -90f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
        androidx.compose.material3.Text(
            text = "${(percentage * 100).toInt()}%",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Legend Dot ──────────────────────────────────────────────────────
@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF7F9FF)
@Composable
private fun SpendingChartPreview() {
    DDMoneyTheme(darkTheme = false) {
        SpendingChartSection(
            monthlyData = SampleData.monthlyData,
            totalSavings = 24_580_000.0,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
