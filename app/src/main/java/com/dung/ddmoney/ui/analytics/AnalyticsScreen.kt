package com.dung.ddmoney.ui.analytics

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.theme.ExpenseRed50
import com.dung.ddmoney.ui.theme.ExpenseRed600
import com.dung.ddmoney.ui.theme.InvestAmber400
import com.dung.ddmoney.ui.theme.InvestAmber600
import com.dung.ddmoney.ui.theme.LuminousBackground
import com.dung.ddmoney.ui.theme.LuminousOnSurface
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLow
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLowest
import com.dung.ddmoney.ui.theme.NeutralGray50
import com.dung.ddmoney.ui.theme.NeutralGray100
import com.dung.ddmoney.ui.theme.NeutralGray600
import com.dung.ddmoney.ui.theme.NeutralGray800
import com.dung.ddmoney.ui.theme.OceanBlue50
import com.dung.ddmoney.ui.theme.OceanBlue400
import com.dung.ddmoney.ui.theme.OceanBlue600
import com.dung.ddmoney.ui.theme.SavingsTeal50
import com.dung.ddmoney.ui.theme.SavingsTeal600
import kotlin.math.abs

@Composable
fun AnalyticsScreen(
        transactions: List<Transaction>,
        categories: List<Category> = emptyList(),
        modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.MONTH) }
    var showBreakdown by remember { mutableStateOf(false) }
    val weeklyReport = remember(transactions, categories) {
        buildExpenseReport(transactions, ReportPeriod.WEEK, categories = categories)
    }
    val monthlyReport = remember(transactions, categories) {
        buildExpenseReport(transactions, ReportPeriod.MONTH, categories = categories)
    }
    val report = if (selectedPeriod == ReportPeriod.WEEK) weeklyReport else monthlyReport

    if (showBreakdown) {
        ExpenseBreakdownDialog(report = report, onDismiss = { showBreakdown = false })
    }

    Box(modifier = modifier.fillMaxSize().background(LuminousBackground)) {
        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { AnalyticsHeader(report.rangeLabel) }

            item {
                AnalyticsSummaryCard(
                        report = report,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it }
                )
            }

            item {
                TopSpendingHeader(
                        categoryCount = report.topCategories.size,
                        onClick = { showBreakdown = true }
                )
            }

            if (report.topCategories.isEmpty()) {
                item { EmptyReportState() }
            } else {
                items(report.topCategories.take(8), key = { it.id }) { category ->
                    CategorySpendingRow(category = category)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeader(rangeLabel: String) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                    text = "BÁO CÁO CHI TIÊU",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralGray600
            )
            Text(
                    text = "Báo cáo",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LuminousOnSurface
            )
        }

        Surface(shape = CircleShape, color = OceanBlue50) {
            Icon(
                    imageVector = Icons.Outlined.Insights,
                    contentDescription = "Báo cáo",
                    tint = OceanBlue600,
                    modifier = Modifier.padding(12.dp).size(24.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(text = rangeLabel, fontSize = 14.sp, color = NeutralGray600)
}

@Composable
private fun AnalyticsSummaryCard(
        report: ExpenseReport,
        selectedPeriod: ReportPeriod,
        onPeriodSelected: (ReportPeriod) -> Unit
) {
    Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = LuminousSurfaceContainerLowest,
            shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            ReportPeriodSelector(selected = selectedPeriod, onSelect = onPeriodSelected)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = formatVnd(report.currentTotal),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LuminousOnSurface
                    )
                    Text(text = report.summaryLabel, fontSize = 13.sp, color = NeutralGray600)
                }

                SpendingDeltaBadge(report = report)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricPill(
                        label = report.previousLabel,
                        value = formatVnd(report.previousTotal),
                        modifier = Modifier.weight(1f)
                )
                MetricPill(
                        label = differenceLabel(report),
                        value = differenceValue(report),
                        modifier = Modifier.weight(1f),
                        valueColor = if (report.difference <= 0.0) SavingsTeal600 else ExpenseRed600
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ExpenseComparisonChart(
                    report = report,
                    modifier = Modifier.fillMaxWidth().height(210.dp)
            )
        }
    }
}

@Composable
private fun ReportPeriodSelector(selected: ReportPeriod, onSelect: (ReportPeriod) -> Unit) {
    BoxWithConstraints(
            modifier =
                    Modifier.fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(LuminousSurfaceContainerLow)
                            .padding(4.dp)
    ) {
        val periods = ReportPeriod.values()
        val selectedIndex = periods.indexOf(selected).coerceAtLeast(0)
        val segmentWidth = maxWidth / periods.size.toFloat()
        val indicatorOffset by
                animateDpAsState(
                        targetValue = segmentWidth * selectedIndex.toFloat(),
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                        label = "analytics_period_selector_offset"
                )

        Box(
                modifier =
                        Modifier.offset(x = indicatorOffset)
                                .fillMaxHeight()
                                .width(segmentWidth)
                                .clip(RoundedCornerShape(15.dp))
                                .background(OceanBlue600)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            periods.forEach { period ->
                val isActive = selected == period
                Box(
                    modifier =
                                    Modifier.weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(15.dp))
                                    .clickable { onSelect(period) }
                                    .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.label,
                        fontSize = 14.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) Color.White else NeutralGray600
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
        label: String,
        value: String,
        modifier: Modifier = Modifier,
        valueColor: Color = NeutralGray800
) {
    Column(
            modifier =
                    modifier.clip(RoundedCornerShape(16.dp))
                            .background(LuminousSurfaceContainerLow)
                            .padding(14.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = NeutralGray600)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun SpendingDeltaBadge(report: ExpenseReport) {
    val difference = report.difference
    val isHigher = difference > 0.0
    val isLower = difference < 0.0
    val valueColor =
            when {
                isHigher -> ExpenseRed600
                isLower -> SavingsTeal600
                else -> NeutralGray600
            }
    val backgroundColor =
            when {
                isHigher -> ExpenseRed50
                isLower -> SavingsTeal50
                else -> NeutralGray50
            }
    val valueText =
            when {
                isHigher -> "+${compactMoney(difference)}"
                isLower -> "-${compactMoney(-difference)}"
                else -> "0"
            }
    val label = "so với ${report.previousLabel.lowercase()}"

    Column(
            modifier =
                    Modifier.padding(start = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(backgroundColor)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.End
    ) {
        Text(
                text = valueText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
        )
        Text(
                text = label,
                fontSize = 10.sp,
                color = NeutralGray600,
                textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ExpenseComparisonChart(
        report: ExpenseReport,
        modifier: Modifier = Modifier
) {
    val safeAxisMaxAmount = comparisonChartAxisMax(report.previousTotal, report.currentTotal)
    val previousRatio by
            animateFloatAsState(
                    targetValue = comparisonChartVisualRatio(report.previousTotal, safeAxisMaxAmount),
                    animationSpec = tween(500),
                    label = "previous_expense_ratio"
            )
    val currentRatio by
            animateFloatAsState(
                    targetValue = comparisonChartVisualRatio(report.currentTotal, safeAxisMaxAmount),
                    animationSpec = tween(500),
                    label = "current_expense_ratio"
            )

    val chartSidePadding = 46.dp
    val barWidth = 60.dp
    val barSpacing = 54.dp

    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Canvas(
                    modifier =
                            Modifier.fillMaxSize()
                                    .padding(start = chartSidePadding, end = chartSidePadding)
            ) {
                val groundY    = size.height * 0.88f   // taller bars: more floor room removed
                val chartTop   = size.height * 0.06f   // bars can grow higher
                val chartHeight = groundY - chartTop
                val barWidthPx   = barWidth.toPx()
                val spacingPx    = barSpacing.toPx()
                val centerX    = size.width / 2f
                val minBarHeight = 10.dp.toPx()

                drawLine(
                        color = NeutralGray100,
                        start = Offset(0f, groundY),
                        end = Offset(size.width, groundY),
                        strokeWidth = 1.dp.toPx()
                )

                val previousHeight =
                        if (report.previousTotal > 0.0) {
                            (chartHeight * previousRatio).coerceAtLeast(minBarHeight)
                        } else {
                            0f
                        }
                val currentHeight =
                        if (report.currentTotal > 0.0) {
                            (chartHeight * currentRatio).coerceAtLeast(minBarHeight)
                        } else {
                            0f
                        }

                drawRoundRect(
                        color = OceanBlue400,
                        topLeft = Offset(centerX - barWidthPx - spacingPx / 2f, groundY - previousHeight),
                        size = Size(barWidthPx, previousHeight),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
                drawRoundRect(
                        color = InvestAmber400,
                        topLeft = Offset(centerX + spacingPx / 2f, groundY - currentHeight),
                        size = Size(barWidthPx, currentHeight),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )

                val labelGap = 6.dp.toPx()
                val labelTopGap = 8.dp.toPx()
                val labelTextSize = 10.sp.toPx()
                val labelTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val previousPaint =
                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = OceanBlue600.toArgb()
                            textAlign = Paint.Align.RIGHT
                            textSize = labelTextSize
                            typeface = labelTypeface
                        }
                val currentPaint =
                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = InvestAmber600.toArgb()
                            textAlign = Paint.Align.LEFT
                            textSize = labelTextSize
                            typeface = labelTypeface
                        }
                val canvas = drawContext.canvas.nativeCanvas

                if (report.previousTotal > 0.0) {
                    val labelY =
                            (groundY - previousHeight - labelTopGap)
                                    .coerceAtLeast(chartTop + labelTextSize)
                    canvas.drawText(
                            compactMoney(report.previousTotal),
                            centerX - barWidthPx - spacingPx / 2f - labelGap,
                            labelY,
                            previousPaint
                    )
                }

                if (report.currentTotal > 0.0) {
                    val labelY =
                            (groundY - currentHeight - labelTopGap)
                                    .coerceAtLeast(chartTop + labelTextSize)
                    canvas.drawText(
                            compactMoney(report.currentTotal),
                            centerX + spacingPx / 2f + barWidthPx + labelGap,
                            labelY,
                            currentPaint
                    )
                }
            }
        }

        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .padding(top = 2.dp, start = chartSidePadding, end = chartSidePadding),
                horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.width(barWidth), contentAlignment = Alignment.Center) {
                Text(
                    text = report.previousLabel,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = NeutralGray600,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(barSpacing))
            Box(modifier = Modifier.width(barWidth), contentAlignment = Alignment.Center) {
                Text(
                    text = report.currentLabel,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = NeutralGray600,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TopSpendingHeader(categoryCount: Int, onClick: () -> Unit) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onClick)
                            .padding(top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
                text = "Chi tiêu nhiều nhất",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface
        )
        Text(text = "$categoryCount danh mục", fontSize = 13.sp, color = NeutralGray600)
    }
}

@Composable
private fun CategorySpendingRow(category: CategoryExpense) {
    Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = LuminousSurfaceContainerLowest,
            shadowElevation = 1.dp
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                    modifier =
                            Modifier.size(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(category.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
            ) {
                CategoryIcon(
                        icon = category.icon,
                        modifier = Modifier.size(24.dp),
                        tint = category.color,
                        fallbackFontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = category.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LuminousOnSurface
                    )
                    Text(
                            text = "${(category.percentage * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed600
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                        progress = category.percentage.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                        color = category.color,
                        trackColor = SavingsTeal50
                )

                Spacer(modifier = Modifier.height(7.dp))
                Text(text = formatVnd(category.amount), fontSize = 13.sp, color = NeutralGray600)
            }
        }
    }
}

@Composable
private fun EmptyReportState() {
    Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = LuminousSurfaceContainerLowest,
            shadowElevation = 1.dp
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Chưa có chi tiêu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuminousOnSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Các giao dịch chi tiêu trong kỳ này sẽ xuất hiện ở đây.", fontSize = 13.sp, color = NeutralGray600)
        }
    }
}

private fun differenceLabel(report: ExpenseReport): String {
    return when {
        report.previousTotal == 0.0 -> "So với kỳ trước"
        report.difference <= 0.0 -> "Giảm chi"
        else -> "Tăng chi"
    }
}

private fun differenceValue(report: ExpenseReport): String {
    return when {
        report.previousTotal == 0.0 && report.currentTotal == 0.0 -> "0 đ"
        report.previousTotal == 0.0 -> formatVnd(report.currentTotal)
        else -> formatVnd(abs(report.difference))
    }
}

