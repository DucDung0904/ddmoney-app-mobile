package com.dung.ddmoney.ui.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.parseColor
import com.dung.ddmoney.ui.theme.*
import com.dung.ddmoney.ui.components.formatMoneyDisplay


@Composable
fun BudgetScreen(viewModel: com.dung.ddmoney.AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var showAddBudget by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val budgets = state.budgets
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị thông báo lỗi nếu có
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    val totalLimit = budgets.sumOf { it.amount }
    val totalSpent = budgets.sumOf { it.spentAmount }
    val overallProgress =
            if (totalLimit > 0) (totalSpent / totalLimit).toFloat().coerceIn(0f, 1f) else 0f


    Scaffold(
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp) // Đẩy lên trên Nav Bar
            ) 
        },
        containerColor = LuminousBackground
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            BudgetHeader(onAddClick = { showAddBudget = true })

            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
            ) {
            item { OverallBudgetCard(totalLimit, totalSpent, overallProgress) }

            item {
                Text(
                        text = "Chi tiết ngân sách",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            if (budgets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Chưa có ngân sách nào", color = NeutralGray600)
                    }
                }
            } else {
                items(budgets) { budget -> BudgetCategoryItem(budget) }
            }
        }
    }

        if (showAddBudget) {
            AddBudgetScreen(
                    categories = state.categories,
                    onSave = { name, amount, categoryIds, month, year ->
                        viewModel.createBudget(name, amount, month, year, categoryIds)
                        showAddBudget = false
                    },
                    onDismiss = { showAddBudget = false }
            )
        }
    }
}

@Composable
private fun BudgetHeader(onAddClick: () -> Unit) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Tháng này", color = NeutralGray600, fontSize = 14.sp)
            Text(
                    "Ngân sách",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
            )
        }

        Surface(
                shape = CircleShape,
                color = LuminousSurfaceContainerLowest,
                shadowElevation = 2.dp,
                onClick = onAddClick
        ) {
            Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Tạo ngân sách",
                    tint = OceanBlue600,
                    modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun OverallBudgetCard(limit: Double, spent: Double, progress: Float) {
    Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = LuminousSurfaceContainerLowest,
            shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tổng ngân sách", color = NeutralGray600, fontSize = 15.sp)
                Text(
                        formatMoneyDisplay(limit),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Đã chi tiêu", color = NeutralGray600, fontSize = 13.sp)
            Text(
                    formatMoneyDisplay(spent),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = OceanBlue600
            )

            Spacer(modifier = Modifier.height(20.dp))

            val animatedProgress by
                    animateFloatAsState(
                            targetValue = progress,
                            animationSpec = tween(1000),
                            label = "overall_progress"
                    )

            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(12.dp)
                                    .clip(CircleShape)
                                    .background(OceanBlue50)
            ) {
                Box(
                        modifier =
                                Modifier.fillMaxHeight()
                                        .fillMaxWidth(animatedProgress)
                                        .clip(CircleShape)
                                        .background(OceanBlue600)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val remaining = (limit - spent).coerceAtLeast(0.0)
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                        if (spent > limit) "Vượt tổng ngân sách!" else "Còn lại",
                        color = if (spent > limit) ExpenseRed600 else NeutralGray600,
                        fontSize = 13.sp
                )
                Text(
                        formatMoneyDisplay(remaining),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (spent > limit) ExpenseRed600 else SavingsTeal600
                )
            }
        }
    }
}

@Composable
private fun BudgetCategoryItem(budget: com.dung.ddmoney.repository.BudgetDisplayModel) {
    val isOverBudget = budget.spentAmount > budget.amount
    val progress = (budget.percentUsed / 100.0).toFloat().coerceIn(0f, 1f)
    
    val progressColor =
            when {
                isOverBudget -> ExpenseRed600
                progress > 0.85f -> InvestAmber600
                else -> SavingsTeal600
            }

    val animatedProgress by
            animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(800),
                    label = "cat_progress"
            )

    // Lấy icon từ danh mục đầu tiên hoặc icon mặc định
    val firstCategory = budget.categories.firstOrNull()
    val iconBg = if (firstCategory != null) parseColor(firstCategory.colorHex).copy(alpha = 0.1f) else OceanBlue50
    val iconTint = if (firstCategory != null) parseColor(firstCategory.colorHex) else OceanBlue600
    
    Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = LuminousSurfaceContainerLowest,
            shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(iconBg),
                        contentAlignment = Alignment.Center
                ) {
                    // Logic map icon string to ImageVector — đơn giản hoá ở đây
                    Icon(
                            imageVector = Icons.Outlined.Category,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            budget.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                            "${formatMoneyDisplay(budget.spentAmount)} / ${formatMoneyDisplay(budget.amount)}",
                            color = NeutralGray600,
                            fontSize = 13.sp
                    )
                }

                Text(
                        text = "${budget.percentUsed.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(NeutralGray100)
            ) {
                Box(
                        modifier =
                                Modifier.fillMaxHeight()
                                        .fillMaxWidth(animatedProgress)
                                        .clip(CircleShape)
                                        .background(progressColor)
                )
            }

            if (isOverBudget) {
                Text(
                        text =
                                "Đã vượt quá ngân sách ${formatMoneyDisplay(budget.spentAmount - budget.amount)}",
                        color = ExpenseRed600,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                        text = "Còn lại ${formatMoneyDisplay(budget.remaining)}",
                        color = NeutralGray600,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
