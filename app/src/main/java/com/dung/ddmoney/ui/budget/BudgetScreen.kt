package com.dung.ddmoney.ui.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.parseColor
import com.dung.ddmoney.repository.BudgetDisplayModel
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.components.formatMoneyDisplay
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import com.dung.ddmoney.ui.theme.ExpenseRed600
import com.dung.ddmoney.ui.theme.InvestAmber600
import com.dung.ddmoney.ui.theme.LuminousBackground
import com.dung.ddmoney.ui.theme.LuminousOnBackground
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerHigh
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLow
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLowest
import com.dung.ddmoney.ui.theme.NeutralGray100
import com.dung.ddmoney.ui.theme.NeutralGray400
import com.dung.ddmoney.ui.theme.NeutralGray600
import com.dung.ddmoney.ui.theme.OceanBlue50
import com.dung.ddmoney.ui.theme.OceanBlue600
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private sealed interface BudgetDetailTarget {
    data object Overall : BudgetDetailTarget
    data class Single(val budgetId: String) : BudgetDetailTarget
}

@Composable
fun BudgetScreen(viewModel: com.dung.ddmoney.AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var showAddBudget by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetDisplayModel?>(null) }
    var detailTarget by remember { mutableStateOf<BudgetDetailTarget?>(null) }
    val state by viewModel.state.collectAsState()
    val budgets = state.budgets
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val totalLimit = budgets.sumOf { it.amount }
    val totalSpent = budgets.sumOf { it.spentAmount }
    val overallProgress = if (totalLimit > 0) (totalSpent / totalLimit).toFloat() else 0f
    val today = remember { LocalDate.now() }
    val daysRemaining =
        remember(today) {
            (YearMonth.from(today).lengthOfMonth() - today.dayOfMonth).coerceAtLeast(0)
        }
    val monthProgress =
        remember(today) {
            (today.dayOfMonth.toFloat() / YearMonth.from(today).lengthOfMonth().toFloat())
                .coerceIn(0f, 1f)
        }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 104.dp)
            )
        },
        containerColor = LuminousBackground
    ) { paddingValues ->
        val currentDetailTarget = detailTarget
        if (currentDetailTarget != null) {
            BudgetDetailScreen(
                target = currentDetailTarget,
                budgets = budgets,
                transactions = state.transactions,
                categories = state.categories,
                monthProgress = monthProgress,
                onBack = { detailTarget = null },
                onEdit = { budget ->
                    editingBudget = budget
                    showAddBudget = true
                },
                onDelete = { budget ->
                    viewModel.deleteBudget(budget.id)
                    detailTarget = null
                }
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 132.dp)
                ) {
                    item { BudgetTopBar() }

                    item { MonthTabHeader() }

                    if (budgets.isEmpty()) {
                        item {
                            EmptyBudgetState(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(470.dp),
                                onAddClick = {
                                    editingBudget = null
                                    showAddBudget = true
                                }
                            )
                        }
                    } else {
                        item {
                            OverallBudgetCard(
                                limit = totalLimit,
                                spent = totalSpent,
                                progress = overallProgress,
                                daysRemaining = daysRemaining,
                                onClick = { detailTarget = BudgetDetailTarget.Overall },
                                onAddClick = {
                                    editingBudget = null
                                    showAddBudget = true
                                }
                            )
                        }

                        items(budgets, key = { it.id }) { budget ->
                            BudgetTimelineCard(
                                budget = budget,
                                monthProgress = monthProgress,
                                onClick = { detailTarget = BudgetDetailTarget.Single(budget.id) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddBudget) {
            AddBudgetScreen(
                categories = state.categories,
                wallets = state.wallets,
                initialBudget = editingBudget,
                onSave = { name, amount, categoryIds, month, year ->
                    val budgetBeingEdited = editingBudget
                    if (budgetBeingEdited == null) {
                        viewModel.createBudget(name, amount, month, year, categoryIds)
                    } else {
                        viewModel.updateBudget(
                            budgetBeingEdited.id,
                            name,
                            amount,
                            month,
                            year,
                            categoryIds
                        )
                    }
                    editingBudget = null
                    showAddBudget = false
                },
                onDismiss = {
                    editingBudget = null
                    showAddBudget = false
                }
            )
        }
    }
}

@Composable
private fun BudgetTopBar() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ngân sách Đang áp dụng",
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LuminousOnBackground
        )
    }
}

@Composable
private fun MonthTabHeader() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(LuminousSurfaceContainerLowest)
                .padding(top = 16.dp)
    ) {
        Text(
            text = "Tháng này",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LuminousOnBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier =
                Modifier
                    .padding(start = 18.dp)
                    .width(92.dp)
                    .height(2.dp)
                    .background(LuminousOnBackground, RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun EmptyBudgetState(modifier: Modifier, onAddClick: () -> Unit) {
    Box(
        modifier = modifier.padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier =
                    Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(OceanBlue50.copy(alpha = 0.68f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = OceanBlue600,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Bạn chưa có ngân sách",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LuminousOnBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bắt đầu tiết kiệm bằng cách tạo ngân sách và chúng tôi sẽ giúp bạn kiểm soát ngân sách",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = NeutralGray600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onAddClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600)
            ) {
                Text(
                    text = "Tạo ngân sách",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LuminousSurfaceContainerLowest
                )
            }
        }
    }
}

@Composable
private fun OverallBudgetCard(
    limit: Double,
    spent: Double,
    progress: Float,
    daysRemaining: Int,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val remaining = (limit - spent).coerceAtLeast(0.0)

    Surface(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BudgetGauge(
                progress = progress,
                amount = formatBudgetMoney(remaining)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BudgetMetric(
                    value = formatCompactMoney(limit),
                    label = "Tổng ngân sách",
                    modifier = Modifier.weight(1f)
                )
                VerticalMetricDivider()
                BudgetMetric(
                    value = formatCompactMoney(spent),
                    label = "Tổng đã chi",
                    modifier = Modifier.weight(1f)
                )
                VerticalMetricDivider()
                BudgetMetric(
                    value = "$daysRemaining ngày",
                    label = "Đến cuối tháng",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                modifier =
                    Modifier
                        .width(190.dp)
                        .height(46.dp),
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600)
            ) {
                Text(
                    text = "Tạo Ngân sách",
                    color = LuminousSurfaceContainerLowest,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun BudgetGauge(progress: Float, amount: String) {
    val animatedProgress by
        animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(900),
            label = "budget_gauge"
        )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(164.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = (size.width - 44.dp.toPx()).coerceAtMost(size.height * 1.66f)
            val top = 12.dp.toPx()
            val left = (size.width - diameter) / 2f
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = LuminousSurfaceContainerHigh.copy(alpha = 0.72f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(left, top),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = OceanBlue600,
                startAngle = 180f,
                sweepAngle = animatedProgress * 180f,
                useCenter = false,
                topLeft = Offset(left, top),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val radius = diameter / 2f
            val center = Offset(left + radius, top + radius)
            val angle = (180f + animatedProgress * 180f) * PI.toFloat() / 180f
            val knobCenter = Offset(
                x = center.x + radius * cos(angle),
                y = center.y + radius * sin(angle)
            )

            drawCircle(
                color = LuminousSurfaceContainerLowest,
                radius = 8.dp.toPx(),
                center = knobCenter
            )
            drawCircle(
                color = OceanBlue600,
                radius = 5.dp.toPx(),
                center = knobCenter
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 22.dp)
        ) {
            Text(
                text = "Số tiền bạn có thể chi",
                color = NeutralGray600,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = amount,
                color = OceanBlue600,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BudgetMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = LuminousOnBackground,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = NeutralGray600,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun VerticalMetricDivider() {
    Box(
        modifier =
            Modifier
                .height(26.dp)
                .width(1.dp)
                .background(NeutralGray400)
    )
}

@Composable
private fun BudgetTimelineCard(
    budget: BudgetDisplayModel,
    monthProgress: Float,
    onClick: () -> Unit
) {
    val isOverBudget = budget.spentAmount > budget.amount
    val progress = (budget.percentUsed / 100.0).toFloat().coerceIn(0f, 1f)
    val progressColor =
        when {
            isOverBudget -> ExpenseRed600
            progress > 0.85f -> InvestAmber600
            else -> OceanBlue600
        }
    val animatedProgress by
        animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(650),
            label = "budget_timeline_progress"
        )
    val firstCategory = budget.categories.firstOrNull()
    val iconTint = firstCategory?.colorHex?.let { parseColor(it) } ?: OceanBlue600
    val iconBackground = iconTint.copy(alpha = 0.12f)

    Surface(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    if (firstCategory != null) {
                        CategoryIcon(
                            icon = firstCategory.icon,
                            modifier = Modifier.size(24.dp),
                            tint = iconTint
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Category,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = budget.name,
                    modifier = Modifier.weight(1f),
                    color = LuminousOnBackground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatBudgetMoney(budget.amount),
                        color = LuminousOnBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text =
                            if (isOverBudget) {
                                "Vượt ${formatBudgetMoney(budget.spentAmount - budget.amount)}"
                            } else {
                                "Còn lại ${formatBudgetMoney(budget.remaining)}"
                            },
                        color = if (isOverBudget) ExpenseRed600 else NeutralGray600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            BudgetProgressRail(
                progress = animatedProgress,
                markerProgress = monthProgress,
                color = progressColor
            )
        }
    }
}

@Composable
private fun BudgetProgressRail(
    progress: Float,
    markerProgress: Float,
    color: Color
) {
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(42.dp)
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .background(NeutralGray100)
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .clip(CircleShape)
                        .background(color)
            )
        }

        val markerX = (maxWidth - 2.dp) * markerProgress.coerceIn(0f, 1f)
        Box(
            modifier =
                Modifier
                    .offset(x = markerX)
                    .width(2.dp)
                    .height(15.dp)
                    .background(NeutralGray400.copy(alpha = 0.72f))
        )

        val labelOffset = (maxWidth - 66.dp) * markerProgress.coerceIn(0f, 1f)
        Box(
            modifier =
                Modifier
                    .offset(x = labelOffset, y = 14.dp)
                    .width(66.dp)
                    .height(25.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(LuminousSurfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Hôm nay",
                color = NeutralGray600,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BudgetDetailScreen(
    target: BudgetDetailTarget,
    budgets: List<BudgetDisplayModel>,
    transactions: List<Transaction>,
    categories: List<Category>,
    monthProgress: Float,
    onBack: () -> Unit,
    onEdit: (BudgetDisplayModel) -> Unit,
    onDelete: (BudgetDisplayModel) -> Unit
) {
    val selectedBudgets =
        when (target) {
            BudgetDetailTarget.Overall -> budgets
            is BudgetDetailTarget.Single -> budgets.filter { it.id == target.budgetId }
        }
    val primaryBudget = selectedBudgets.firstOrNull()

    if (selectedBudgets.isEmpty()) {
        LaunchedEffect(target) { onBack() }
        return
    }

    val period =
        primaryBudget?.let { YearMonth.of(it.year, it.month) } ?: YearMonth.from(LocalDate.now())
    val startDate = period.atDay(1)
    val endDate = period.atEndOfMonth()
    val today = LocalDate.now()
    val clampedToday = today.coerceIn(startDate, endDate)
    val totalDays = period.lengthOfMonth().toDouble()
    val elapsedDays = (ChronoUnit.DAYS.between(startDate, clampedToday) + 1).coerceAtLeast(1)
    val remainingDays = (ChronoUnit.DAYS.between(clampedToday, endDate) + 1).coerceAtLeast(0)
    val totalLimit = selectedBudgets.sumOf { it.amount }
    val totalSpent = selectedBudgets.sumOf { it.spentAmount }
    val remaining = (totalLimit - totalSpent).coerceAtLeast(0.0)
    val progress = if (totalLimit > 0.0) (totalSpent / totalLimit).toFloat().coerceIn(0f, 1f) else 0f
    val title =
        when (target) {
            BudgetDetailTarget.Overall -> "Tổng ngân sách"
            is BudgetDetailTarget.Single -> primaryBudget?.name.orEmpty()
        }
    val effectiveCategoryIds = remember(selectedBudgets, categories) {
        effectiveBudgetCategoryIds(selectedBudgets, categories)
    }
    val periodTransactions =
        remember(transactions, effectiveCategoryIds, period) {
            transactions.filter { transaction ->
                transaction.type == TransactionType.EXPENSE &&
                    YearMonth.from(transaction.date) == period &&
                    (effectiveCategoryIds.isEmpty() || transaction.categoryId in effectiveCategoryIds)
            }
        }
    val categoryRows =
        remember(selectedBudgets, categories, periodTransactions) {
            buildBudgetCategoryRows(selectedBudgets, categories, periodTransactions)
        }
    val dailySuggested = if (remainingDays > 0) remaining / remainingDays else 0.0
    val actualDaily = totalSpent / elapsedDays
    val projected = actualDaily * totalDays
    val singleBudget = (target as? BudgetDetailTarget.Single)?.let { targetId ->
        budgets.firstOrNull { it.id == targetId.budgetId }
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(LuminousBackground),
        contentPadding = PaddingValues(bottom = 132.dp)
    ) {
        item {
            BudgetDetailHeader(
                canEdit = singleBudget != null,
                onBack = onBack,
                onEdit = { singleBudget?.let(onEdit) }
            )
        }

        item {
            BudgetDetailSummaryCard(
                title = title,
                budget = primaryBudget,
                limit = totalLimit,
                spent = totalSpent,
                remaining = remaining,
                progress = progress,
                monthProgress = monthProgress,
                startDate = startDate,
                endDate = endDate,
                remainingDays = remainingDays
            )
        }

        item {
            BudgetDetailStatsCard(
                limit = totalLimit,
                spent = totalSpent,
                dailySuggested = dailySuggested,
                projected = projected,
                actualDaily = actualDaily,
                startDate = startDate,
                endDate = endDate
            )
        }

        item { BudgetCategorySpendCard(rows = categoryRows) }

        item {
            DetailActionButton(
                text = "Danh sách giao dịch",
                color = OceanBlue600,
                onClick = {}
            )
        }

        if (singleBudget != null) {
            item {
                DetailActionButton(
                    text = "Xóa",
                    color = ExpenseRed600,
                    onClick = { onDelete(singleBudget) }
                )
            }
        }
    }
}

@Composable
private fun BudgetDetailHeader(
    canEdit: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = LuminousSurfaceContainerLowest,
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBackIosNew,
                    contentDescription = "Quay lại",
                    tint = LuminousOnBackground,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (canEdit) {
            Surface(
                onClick = onEdit,
                shape = RoundedCornerShape(28.dp),
                color = LuminousSurfaceContainerLowest,
                shadowElevation = 10.dp
            ) {
                Text(
                    text = "Sửa",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = LuminousOnBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun BudgetDetailSummaryCard(
    title: String,
    budget: BudgetDisplayModel?,
    limit: Double,
    spent: Double,
    remaining: Double,
    progress: Float,
    monthProgress: Float,
    startDate: LocalDate,
    endDate: LocalDate,
    remainingDays: Long
) {
    val firstCategory = budget?.categories?.firstOrNull()
    val iconTint = firstCategory?.colorHex?.let { parseColor(it) } ?: OceanBlue600

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (firstCategory != null) {
                        CategoryIcon(
                            icon = firstCategory.icon,
                            modifier = Modifier.size(24.dp),
                            tint = iconTint
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = LuminousOnBackground,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = formatBudgetMoney(limit),
                color = LuminousOnBackground,
                fontSize = 31.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Đã chi", color = NeutralGray600, fontSize = 14.sp)
                    Text(
                        formatBudgetMoney(spent),
                        color = LuminousOnBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Còn lại", color = NeutralGray600, fontSize = 14.sp)
                    Text(
                        formatBudgetMoney(remaining),
                        color = LuminousOnBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            BudgetProgressRail(
                progress = progress,
                markerProgress = monthProgress,
                color = if (spent > limit) ExpenseRed600 else OceanBlue600
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(
                icon = Icons.Outlined.CalendarToday,
                title = "${startDate.format(shortDateFormatter)} - ${endDate.format(shortDateFormatter)}",
                subtitle = "Còn $remainingDays ngày"
            )
            DetailInfoRow(
                icon = Icons.Outlined.Language,
                title = "Tổng cộng",
                subtitle = null
            )
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?
) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LuminousOnBackground,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                color = LuminousOnBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = NeutralGray600,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun BudgetDetailStatsCard(
    limit: Double,
    spent: Double,
    dailySuggested: Double,
    projected: Double,
    actualDaily: Double,
    startDate: LocalDate,
    endDate: LocalDate
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(26.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            BudgetMiniChart(
                limit = limit,
                spent = spent,
                startDate = startDate,
                endDate = endDate
            )
            DetailStatRow("Nên chi hằng ngày", dailySuggested)
            DetailStatRow("Dự kiến chi tiêu", projected)
            DetailStatRow("Thực tế chi tiêu hằng ngày", actualDaily)
        }
    }
}

@Composable
private fun BudgetMiniChart(
    limit: Double,
    spent: Double,
    startDate: LocalDate,
    endDate: LocalDate
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(176.dp)
                .padding(horizontal = 18.dp)
    ) {
        val labelColor = NeutralGray600.copy(alpha = 0.82f)
        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val chartLeft = 64.dp.toPx()
                val chartRight = size.width
                val chartTop = 4.dp.toPx()
                val chartBottom = size.height - 22.dp.toPx()
                val chartHeight = chartBottom - chartTop

                repeat(5) { index ->
                    val y = chartTop + chartHeight * index / 4f
                    drawLine(
                        color = NeutralGray100,
                        start = Offset(chartLeft, y),
                        end = Offset(chartRight, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                drawLine(
                    color = ExpenseRed600.copy(alpha = 0.74f),
                    start = Offset(chartLeft, chartTop),
                    end = Offset(chartRight, chartTop),
                    strokeWidth = 1.4.dp.toPx()
                )
                val spentY =
                    if (limit <= 0.0) chartBottom
                    else chartBottom - (spent / limit).toFloat().coerceIn(0f, 1f) * chartHeight
                drawLine(
                    color = OceanBlue600.copy(alpha = 0.9f),
                    start = Offset(chartLeft, spentY),
                    end = Offset(chartRight, spentY),
                    strokeWidth = 1.6.dp.toPx()
                )
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val steps = listOf(limit, limit * 0.8, limit * 0.6, limit * 0.4, 0.0)
                steps.forEach { value ->
                    Text(
                        text = formatBudgetMoney(value),
                        color = if (value == limit) ExpenseRed600.copy(alpha = 0.8f) else labelColor,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }

        Row(
            modifier = Modifier.padding(start = 68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = startDate.format(fullDateFormatter),
                color = LuminousOnBackground,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = endDate.format(fullDateFormatter),
                color = LuminousOnBackground,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DetailStatRow(label: String, value: Double) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(LuminousSurfaceContainerLowest)
                .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = NeutralGray600,
            fontSize = 16.sp
        )
        Text(
            text = formatBudgetMoney(value),
            color = LuminousOnBackground,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BudgetCategorySpendCard(rows: List<CategorySpendRow>) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(26.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Chi theo danh mục",
                color = LuminousOnBackground,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (rows.isEmpty()) {
                Text(
                    text = "Chưa có chi tiêu trong kỳ này",
                    color = NeutralGray600,
                    fontSize = 15.sp
                )
            } else {
                rows.forEach { row ->
                    CategorySpendItem(row = row)
                }
            }
        }
    }
}

@Composable
private fun CategorySpendItem(row: CategorySpendRow) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(row.color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            CategoryIcon(
                icon = row.icon,
                modifier = Modifier.size(23.dp),
                tint = row.color
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.name,
                color = LuminousOnBackground,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (row.childCount > 0) {
                Text(
                    text = "và ${row.childCount} nhóm con",
                    color = NeutralGray600,
                    fontSize = 13.sp
                )
            }
        }
        Text(
            text = formatBudgetMoney(row.spent),
            color = NeutralGray600,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = NeutralGray400,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun DetailActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .height(54.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
        shape = RoundedCornerShape(27.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = color,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class CategorySpendRow(
    val id: String,
    val name: String,
    val icon: String,
    val color: Color,
    val spent: Double,
    val childCount: Int
)

private fun effectiveBudgetCategoryIds(
    budgets: List<BudgetDisplayModel>,
    categories: List<Category>
): Set<String> {
    val selectedIds = budgets.flatMap { budget ->
        budget.categories.map { it.id.toString() }
    }.toSet()
    val childIds = categories.filter { it.parentId in selectedIds }.map { it.id }
    return selectedIds + childIds
}

private fun buildBudgetCategoryRows(
    budgets: List<BudgetDisplayModel>,
    categories: List<Category>,
    transactions: List<Transaction>
): List<CategorySpendRow> {
    val selectedCategories =
        budgets.flatMap { it.categories }.distinctBy { it.id }

    if (selectedCategories.isEmpty()) {
        return transactions
            .groupBy { it.categoryId }
            .map { (categoryId, categoryTransactions) ->
                val first = categoryTransactions.first()
                CategorySpendRow(
                    id = categoryId,
                    name = first.categoryName,
                    icon = first.categoryIcon,
                    color = first.categoryColor,
                    spent = categoryTransactions.sumOf { it.amount },
                    childCount = 0
                )
            }
            .sortedByDescending { it.spent }
    }

    return selectedCategories.map { category ->
        val categoryId = category.id.toString()
        val childIds = categories.filter { it.parentId == categoryId }.map { it.id }
        val effectiveIds = setOf(categoryId) + childIds
        val spent = transactions.filter { it.categoryId in effectiveIds }.sumOf { it.amount }
        CategorySpendRow(
            id = categoryId,
            name = category.name,
            icon = category.icon,
            color = parseColor(category.colorHex),
            spent = spent,
            childCount = childIds.size
        )
    }.sortedByDescending { it.spent }
}

private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")
private val fullDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun formatBudgetMoney(value: Double): String = formatMoneyDisplay(value, suffix = "")

private fun formatCompactMoney(value: Double): String {
    val absValue = kotlin.math.abs(value)
    return when {
        absValue >= 1_000_000 -> {
            val compact = value / 1_000_000.0
            if (compact.roundToInt().toDouble() == compact) "${compact.roundToInt()} M"
            else "${String.format("%.1f", compact)} M"
        }
        absValue >= 1_000 -> "${(value / 1_000.0).roundToInt()} K"
        else -> formatBudgetMoney(value)
    }
}
