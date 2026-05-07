package com.dung.ddmoney.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.dung.ddmoney.AppState
import com.dung.ddmoney.ui.dashboard.components.*
import com.dung.ddmoney.ui.dashboard.model.DashboardState
import com.dung.ddmoney.ui.dashboard.model.MonthlySummary
import com.dung.ddmoney.ui.dashboard.model.SampleData
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import com.dung.ddmoney.ui.navigation.Routes
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── Dashboard Screen ─────────────────────────────────────────────────
@Composable
fun DashboardScreen(
        appState: AppState,
        navController: NavController,
        viewModel: com.dung.ddmoney.AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
        modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Show loading spinner while initial data loads
    if (appState.isLoading && appState.wallets.isEmpty()) {
        Box(
                modifier = modifier.fillMaxSize().background(Color(0xFF1A3B8B)),
                contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = Color.White) }
        return
    }

    val selectedMonth = remember {
        val now = LocalDate.now()
        val monthName = now.format(DateTimeFormatter.ofPattern("MMMM", Locale("vi")))
        "${monthName.replaceFirstChar { it.uppercaseChar() }} ${now.year}"
    }

    val monthlyData =
            remember(appState.transactions) {
                val months = 7
                (0 until months)
                        .map { offset ->
                            val d = LocalDate.now().minusDays(offset.toLong())
                            val expense =
                                    appState.transactions
                                            .filter {
                                                it.type == TransactionType.EXPENSE &&
                                                        it.date.dayOfMonth == d.dayOfMonth &&
                                                        it.date.month == d.month &&
                                                        it.date.year == d.year
                                            }
                                            .sumOf { it.amount }
                            val label =
                                    when (offset) {
                                        0 -> "CN"
                                        1 -> "T7"
                                        2 -> "T6"
                                        3 -> "T5"
                                        4 -> "T4"
                                        5 -> "T3"
                                        else -> "T2"
                                    }
                            MonthlySummary(label, 0.0, expense)
                        }
                        .reversed()
            }

    val dashboardState =
            remember(appState, monthlyData) {
                DashboardState(
                        totalBalance = appState.totalBalance,
                        totalIncome = appState.currentMonthIncome,
                        totalExpense = appState.currentMonthExpense,
                        selectedMonth = selectedMonth,
                        wallets = appState.wallets,
                        recentTransactions = appState.transactions.take(10),
                        categories =
                                if (appState.categorySpending().isEmpty()) SampleData.categories
                                else appState.categorySpending(),
                        monthlyData = monthlyData
                )
            }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── 1. Blue Header (greeting + balance + buttons) ─────────
            item {
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clip(
                                                RoundedCornerShape(
                                                        bottomStart = 28.dp,
                                                        bottomEnd = 28.dp
                                                )
                                        )
                                        .background(
                                                Brush.verticalGradient(
                                                        colors =
                                                                listOf(
                                                                        Color(0xFF1A3B8B),
                                                                        Color(0xFF2755C8)
                                                                )
                                                )
                                        )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Greeting row
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Avatar
                                Box(
                                        modifier =
                                                Modifier.size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                                Brush.linearGradient(
                                                                        listOf(
                                                                                LuminousSecondary,
                                                                                LuminousSecondaryContainer
                                                                        )
                                                                )
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                    if (appState.userInfo.avatarUrl.isNotBlank()) {
                                        AsyncImage(
                                                model = appState.userInfo.avatarUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                                text = appState.userInfo.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                            text = "Chào buổi sáng,",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 11.sp
                                    )
                                    Text(
                                            text = appState.userInfo.name.ifBlank { "Người dùng" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                    )
                                }
                            }
                            // Bell icon
                            Box(
                                    modifier =
                                            Modifier.size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Thông báo",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Balance section
                        BalanceCard(
                                totalBalance = dashboardState.totalBalance,
                                totalIncome = dashboardState.totalIncome,
                                totalExpense = dashboardState.totalExpense,
                                selectedMonth = dashboardState.selectedMonth,
                                onAddIncome = {
                                    navController.navigate(Routes.addTransaction("INCOME"))
                                },
                                onAddExpense = {
                                    navController.navigate(Routes.addTransaction("EXPENSE"))
                                }
                        )
                    }
                }
            }

            // ── 2. Quick Actions ──────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                QuickActionsRow(onTransfer = { navController.navigate(Routes.TRANSFER) })
            }

            // ── 3. Spending chart (last 7 days) ───────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                WeeklySpendingCard(
                        totalExpense = dashboardState.totalExpense,
                        weeklyData = dashboardState.monthlyData
                )
            }

            // ── 4. Recent Transactions ────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                TransactionSection(
                        transactions = dashboardState.recentTransactions,
                        onSeeAll = { navController.navigate(Routes.STATS) },
                        onDeleteTransaction = { id -> viewModel.deleteTransaction(id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ─── Weekly Spending Card ─────────────────────────────────────────────
@Composable
fun WeeklySpendingCard(
        totalExpense: Double,
        weeklyData: List<MonthlySummary>,
        modifier: Modifier = Modifier
) {
    Column(
            modifier =
                    modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
    ) {
        // Header row
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                        text = "Chi tiêu 7 ngày",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                )
                Text(
                        text = "- ${formatCurrency(totalExpense)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = "•••",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mini bar chart
        val maxVal = weeklyData.maxOfOrNull { it.expense }?.takeIf { it > 0 } ?: 1.0
        Row(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
        ) {
            weeklyData.forEach { day ->
                val fraction = (day.expense / maxVal).coerceIn(0.0, 1.0).toFloat()
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                ) {
                    Box(
                            modifier =
                                    Modifier.width(18.dp)
                                            .fillMaxHeight(
                                                    if (fraction > 0f) fraction.coerceAtLeast(0.06f)
                                                    else 0.06f
                                            )
                                            .clip(
                                                    RoundedCornerShape(
                                                            topStart = 6.dp,
                                                            topEnd = 6.dp
                                                    )
                                            )
                                            .background(
                                                    if (fraction >= 0.9f) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.primaryContainer
                                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = day.month, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFFF5F7FA)
@Composable
fun DashboardScreenPreview() {
    DDMoneyTheme(darkTheme = false) {
        DashboardScreen(appState = AppState(), navController = rememberNavController())
    }
}
