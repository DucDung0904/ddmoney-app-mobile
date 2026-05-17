package com.dung.ddmoney.ui.home.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.analytics.CategoryExpense
import com.dung.ddmoney.ui.analytics.ExpenseBreakdownDialog
import com.dung.ddmoney.ui.analytics.ReportPeriod
import com.dung.ddmoney.ui.analytics.buildExpenseReport
import com.dung.ddmoney.ui.analytics.comparisonChartAxisMax
import com.dung.ddmoney.ui.analytics.comparisonChartVisualRatio
import com.dung.ddmoney.ui.analytics.compactMoney
import com.dung.ddmoney.ui.analytics.formatVnd
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.theme.*

@Composable
fun HomeReportSection(
    transactions: List<Transaction> = emptyList(),
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        // ── Section Label ─────────────────────────────────────────────────────
        Text(
            text       = "BÁO CÁO CHI TIÊU",
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            color      = NeutralGray600,
            modifier   = Modifier.padding(bottom = 12.dp)
        )

        // ── Main Card ─────────────────────────────────────────────────────────
        Surface(
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(28.dp),
            color           = HomeFrameSurface,
            border          = BorderStroke(1.dp, HomeFrameBorder.copy(alpha = 0.55f)),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // ── Period Selector ───────────────────────────────────────────
                PeriodSelector(
                    selected = selectedPeriod,
                    onSelect = { selectedPeriod = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Total Amount ──────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = formatVnd(report.currentTotal),
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Black,
                            color      = LuminousOnSurface
                        )
                        Text(
                            text       = report.summaryLabel,
                            fontSize   = 13.sp,
                            color      = NeutralGray600,
                            modifier   = Modifier.padding(top = 2.dp)
                        )
                    }

                    SpendingDeltaBadge(report = report)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Comparison Chart ──────────────────────────────────────────
                ComparisonBarChart(
                    previousAmount = report.previousTotal,
                    currentAmount = report.currentTotal,
                    previousLabel = report.previousLabel,
                    currentLabel = report.currentLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(216.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Top Categories List ───────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text       = "Chi tiêu nhiều nhất",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = LuminousOnSurface,
                        modifier   = Modifier.padding(bottom = 12.dp)
                    )

                    TopCategoriesPreview(
                        categories = report.topCategories.take(3),
                        onClick = { showBreakdown = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: ReportPeriod,
    onSelect: (ReportPeriod) -> Unit
) {
    val periods = ReportPeriod.values()
    val density = LocalDensity.current
    var selectorWidthPx by remember { mutableStateOf(0) }
    val segmentWidth =
        with(density) { (selectorWidthPx.toFloat() / periods.size).toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .onSizeChanged { selectorWidthPx = it.width }
            .clip(RoundedCornerShape(18.dp))
            .background(LuminousSurfaceContainerLow)
            .padding(4.dp)
    ) {
        val selectedIndex = periods.indexOf(selected).coerceAtLeast(0)
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "home_period_selector_offset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .fillMaxHeight()
                .width(segmentWidth)
                .clip(RoundedCornerShape(15.dp))
                .background(OceanBlue600)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            periods.forEach { period ->
                val isActive = selected == period
                val interactionSource = remember(period) { MutableInteractionSource() }
                val textColor by animateColorAsState(
                    targetValue = if (isActive) Color.White else NeutralGray600,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                    label = "home_period_${period.name}_text_color"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(15.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelect(period) }
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = period.label,
                        fontSize   = 14.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color      = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonBarChart(
    previousAmount: Double,
    currentAmount: Double,
    previousLabel: String,
    currentLabel: String,
    modifier: Modifier = Modifier
) {
    val safeAxisMaxAmount = comparisonChartAxisMax(previousAmount, currentAmount)
    val previousRatio by animateFloatAsState(
        targetValue = comparisonChartVisualRatio(previousAmount, safeAxisMaxAmount),
        animationSpec = tween(500),
        label = "home_previous_expense_ratio"
    )
    val currentRatio by animateFloatAsState(
        targetValue = comparisonChartVisualRatio(currentAmount, safeAxisMaxAmount),
        animationSpec = tween(500),
        label = "home_current_expense_ratio"
    )
    val chartSidePadding = 44.dp
    val barWidth = 54.dp
    val barSpacing = 44.dp

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = chartSidePadding, end = chartSidePadding)
            ) {
                val width = size.width
                val height = size.height
                val barWidthPx = barWidth.toPx()
                val spacingPx = barSpacing.toPx()
                val groundY = height * 0.94f
                val chartTop = height * 0.06f
                val chartHeight = groundY - chartTop
                val minBarHeight = 10.dp.toPx()

                drawLine(
                    color = NeutralGray100,
                    start = Offset(0f, groundY),
                    end = Offset(width, groundY),
                    strokeWidth = 1.dp.toPx()
                )

                val centerX = width / 2f

                val bar1Height =
                    if (previousAmount > 0.0) {
                        (chartHeight * previousRatio).coerceAtLeast(minBarHeight)
                    } else {
                        0f
                    }
                drawRoundRect(
                    color = OceanBlue400,
                    topLeft = Offset(centerX - barWidthPx - spacingPx / 2f, groundY - bar1Height),
                    size = Size(barWidthPx, bar1Height),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                val bar2Height =
                    if (currentAmount > 0.0) {
                        (chartHeight * currentRatio).coerceAtLeast(minBarHeight)
                    } else {
                        0f
                    }
                drawRoundRect(
                    color = InvestAmber400,
                    topLeft = Offset(centerX + spacingPx / 2f, groundY - bar2Height),
                    size = Size(barWidthPx, bar2Height),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                val labelGap = 6.dp.toPx()
                val labelTopGap = 8.dp.toPx()
                val labelTextSize = 10.sp.toPx()
                val labelTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val previousPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = OceanBlue600.toArgb()
                    textAlign = Paint.Align.RIGHT
                    textSize = labelTextSize
                    typeface = labelTypeface
                }
                val currentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = InvestAmber600.toArgb()
                    textAlign = Paint.Align.LEFT
                    textSize = labelTextSize
                    typeface = labelTypeface
                }
                val canvas = drawContext.canvas.nativeCanvas

                if (previousAmount > 0.0) {
                    val labelY = (groundY - bar1Height - labelTopGap).coerceAtLeast(chartTop + labelTextSize)
                    canvas.drawText(
                        compactMoney(previousAmount),
                        centerX - barWidthPx - spacingPx / 2f - labelGap,
                        labelY,
                        previousPaint
                    )
                }

                if (currentAmount > 0.0) {
                    val labelY = (groundY - bar2Height - labelTopGap).coerceAtLeast(chartTop + labelTextSize)
                    canvas.drawText(
                        compactMoney(currentAmount),
                        centerX + spacingPx / 2f + barWidthPx + labelGap,
                        labelY,
                        currentPaint
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, start = chartSidePadding, end = chartSidePadding),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.width(barWidth), contentAlignment = Alignment.Center) {
                Text(
                    text = previousLabel,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = NeutralGray600,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(barSpacing))
            Box(modifier = Modifier.width(barWidth), contentAlignment = Alignment.Center) {
                Text(
                    text = currentLabel,
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
private fun TopCategoriesPreview(
    categories: List<CategoryExpense>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) {
            OceanBlue50.copy(alpha = 0.88f)
        } else {
            LuminousSurfaceContainerLow.copy(alpha = 0.64f)
        },
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "top_categories_press_color"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.992f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "top_categories_press_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(22.dp))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (categories.isEmpty()) {
            Text(
                text = "Chưa có chi tiêu trong kỳ này",
                fontSize = 13.sp,
                color = NeutralGray600,
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 12.dp)
            )
        } else {
            categories.forEach { category ->
                TopCategoryItem(category = category)
            }
        }
    }
}

@Composable
private fun TopCategoryItem(
    category: CategoryExpense
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SavingsTeal50),
            contentAlignment = Alignment.Center
        ) {
            CategoryIcon(
                icon = category.icon,
                modifier = Modifier.size(24.dp),
                tint = category.color,
                fallbackFontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name and Amount
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = category.name,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = LuminousOnSurface
            )
            Text(
                text       = formatVnd(category.amount),
                fontSize   = 13.sp,
                color      = NeutralGray600
            )
        }

        // Percentage
        Text(
            text       = "${(category.percentage * 100).toInt()}%",
            fontSize   = 15.sp,
            fontWeight = FontWeight.Bold,
            color      = ExpenseRed600
        )
    }
}

@Composable
private fun SpendingDeltaBadge(report: com.dung.ddmoney.ui.analytics.ExpenseReport) {
    val difference = report.difference
    val isHigher = difference > 0.0
    val isLower = difference < 0.0
    val valueColor =
        when {
            !report.hasPreviousPeriodData -> NeutralGray600
            isHigher -> ExpenseRed600
            isLower -> SavingsTeal600
            else -> NeutralGray600
        }
    val backgroundColor =
        when {
            !report.hasPreviousPeriodData -> NeutralGray50
            isHigher -> ExpenseRed50
            isLower -> SavingsTeal50
            else -> NeutralGray50
        }
    val valueText =
        when {
            !report.hasPreviousPeriodData -> "Chưa có"
            isHigher -> "+${compactMoney(difference)}"
            isLower -> "-${compactMoney(-difference)}"
            else -> "0"
        }
    val label =
        if (report.hasPreviousPeriodData) {
            "so với ${report.previousLabel.lowercase()}"
        } else {
            "dữ liệu ${report.previousLabel.lowercase()}"
        }

    Column(
        modifier = Modifier
            .padding(start = 12.dp)
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

