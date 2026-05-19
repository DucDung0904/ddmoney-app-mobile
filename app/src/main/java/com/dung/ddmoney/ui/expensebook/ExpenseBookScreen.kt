package com.dung.ddmoney.ui.expensebook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.components.formatMoneyDisplay
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.theme.DDMoneyTheme
import com.dung.ddmoney.ui.theme.ExpenseRed50
import com.dung.ddmoney.ui.theme.ExpenseRed600
import com.dung.ddmoney.ui.theme.IncomeGreen50
import com.dung.ddmoney.ui.theme.IncomeGreen600
import com.dung.ddmoney.ui.theme.InvestAmber50
import com.dung.ddmoney.ui.theme.LuminousBackground
import com.dung.ddmoney.ui.theme.LuminousOnPrimary
import com.dung.ddmoney.ui.theme.LuminousOnSurface
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLow
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLowest
import com.dung.ddmoney.ui.theme.NeutralGray100
import com.dung.ddmoney.ui.theme.NeutralGray50
import com.dung.ddmoney.ui.theme.NeutralGray600
import com.dung.ddmoney.ui.theme.OceanBlue50
import com.dung.ddmoney.ui.theme.OceanBlue100
import com.dung.ddmoney.ui.theme.OceanBlue400
import com.dung.ddmoney.ui.theme.OceanBlue600
import com.dung.ddmoney.ui.theme.OceanBlue800
import com.dung.ddmoney.ui.theme.SavingsTeal50
import com.dung.ddmoney.ui.theme.SavingsTeal600
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBookScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseBookViewModel = viewModel(),
    categories: List<Category> = emptyList(),
    wallets: List<Wallet> = emptyList(),
    onTransactionClick: (TransactionItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionItem?>(null) }

    LaunchedEffect(categories, wallets) {
        viewModel.setFilterOptions(
            categoryOptions =
                categories.mapNotNull { category ->
                    category.id.toLongOrNull()?.let { id ->
                        ExpenseBookFilterOption(
                            id = id,
                            label = category.name,
                            icon = category.icon
                        )
                    }
                },
            walletOptions =
                wallets.mapNotNull { wallet ->
                    wallet.id.toLongOrNull()?.let { id ->
                        ExpenseBookFilterOption(id = id, label = wallet.name)
                    }
                }
        )
    }

    ExpenseBookScreenContent(
        modifier = modifier,
        uiState = uiState,
        showSearch = showSearch,
        onSearchVisibilityChange = { showSearch = it },
        onSearchQueryChange = viewModel::updateSearchQuery,
        onPeriodSelected = viewModel::selectPeriod,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onOpenFilters = { showFilters = true },
        onTransactionClick = { transaction ->
            selectedTransaction = transaction
            onTransactionClick(transaction)
        }
    )

    if (showFilters) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState,
            containerColor = LuminousSurfaceContainerLowest
        ) {
            ExpenseBookFilterSheet(
                uiState = uiState,
                onTypeSelected = viewModel::selectType,
                onCategorySelected = viewModel::selectCategory,
                onWalletSelected = viewModel::selectWallet,
                onApplyCustomRange = viewModel::applyCustomRange,
                onClearFilters = viewModel::clearSecondaryFilters,
                onDismiss = { showFilters = false }
            )
        }
    }

    selectedTransaction?.let { transaction ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedTransaction = null },
            sheetState = sheetState,
            containerColor = LuminousSurfaceContainerLowest
        ) {
            TransactionDetailSheet(
                transaction = transaction,
                onDismiss = { selectedTransaction = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseBookScreenContent(
    modifier: Modifier = Modifier,
    uiState: ExpenseBookUiState,
    showSearch: Boolean,
    onSearchVisibilityChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPeriodSelected: (ExpenseBookPeriod) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onOpenFilters: () -> Unit,
    onTransactionClick: (TransactionItem) -> Unit
) {
    val pullRefreshState = rememberPullToRefreshState()

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(LuminousBackground)
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            state = pullRefreshState
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 148.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    ExpenseBookHeader(
                        hasActiveFilters = uiState.hasActiveFilters,
                        onSearchClick = { onSearchVisibilityChange(!showSearch) },
                        onFilterClick = onOpenFilters
                    )
                }

                item {
                    AnimatedVisibility(visible = showSearch) {
                        ExpenseBookSearchField(
                            query = uiState.filter.query,
                            onQueryChange = onSearchQueryChange,
                            onClose = {
                                onSearchQueryChange("")
                                onSearchVisibilityChange(false)
                            }
                        )
                    }
                }

                item {
                    FilterChipRow(
                        selectedPeriod = uiState.filter.period,
                        onPeriodSelected = onPeriodSelected
                    )
                }

                when (uiState.status) {
                    ExpenseBookLoadStatus.LOADING -> {
                        item { ExpenseBookLoadingState() }
                    }

                    ExpenseBookLoadStatus.ERROR -> {
                        item {
                            ExpenseBookErrorState(
                                message = uiState.errorMessage ?: "Không thể tải dữ liệu sổ chi tiêu",
                                onRetry = onRetry
                            )
                        }
                    }

                    ExpenseBookLoadStatus.EMPTY,
                    ExpenseBookLoadStatus.SUCCESS -> {
                        item { SummaryCard(summary = uiState.summary) }
                        item {
                            CategoryStatisticSection(
                                statistics = uiState.categoryStatistics
                            )
                        }
                        item {
                            SectionHeader(
                                title = "Nhật ký giao dịch",
                                subtitle = rangeLabel(uiState.filter.fromDate, uiState.filter.toDate)
                            )
                        }

                        if (uiState.groupedTransactions.isEmpty()) {
                            item { EmptyState() }
                        } else {
                            uiState.groupedTransactions.forEach { group ->
                                item(key = "expense_group_${group.date}") {
                                    TransactionGroupItem(
                                        group = group,
                                        onTransactionClick = onTransactionClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseBookHeader(
    hasActiveFilters: Boolean,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Sổ chi tiêu",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LuminousOnSurface,
                letterSpacing = 0.sp
            )
            Text(
                text = "Nhật ký tiền ra, tiền vào và các dấu hiệu chi tiêu nổi bật",
                fontSize = 13.sp,
                color = NeutralGray600,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderIconButton(
                icon = Icons.Outlined.Search,
                contentDescription = "Tìm kiếm giao dịch",
                onClick = onSearchClick
            )
            HeaderIconButton(
                icon = Icons.Outlined.Tune,
                contentDescription = "Bộ lọc",
                isActive = hasActiveFilters,
                onClick = onFilterClick
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = if (isActive) OceanBlue600 else LuminousSurfaceContainerLowest,
        shadowElevation = if (isActive) 4.dp else 1.dp
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isActive) LuminousOnPrimary else OceanBlue600
            )
        }
    }
}

@Composable
private fun ExpenseBookSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp)),
        placeholder = {
            Text(
                text = "Tìm theo danh mục hoặc ghi chú",
                color = NeutralGray600,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = OceanBlue600
            )
        },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Đóng tìm kiếm",
                    tint = NeutralGray600
                )
            }
        },
        singleLine = true,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = LuminousSurfaceContainerLowest,
                unfocusedContainerColor = LuminousSurfaceContainerLowest,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = OceanBlue600
            )
    )
}

@Composable
fun FilterChipRow(
    selectedPeriod: ExpenseBookPeriod,
    onPeriodSelected: (ExpenseBookPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(ExpenseBookPeriod.values().size) { index ->
            val period = ExpenseBookPeriod.values()[index]
            SelectablePill(
                label = period.label,
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) }
            )
        }
    }
}

@Composable
fun SummaryCard(summary: ExpenseBookSummary, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(OceanBlue50.copy(alpha = 0.9f), LuminousSurfaceContainerLowest)
                        )
                    )
                    .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tổng quan giai đoạn",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuminousOnSurface
                    )
                    Text(
                        text = "Số liệu đã tính theo bộ lọc hiện tại",
                        fontSize = 12.sp,
                        color = NeutralGray600
                    )
                }
                Surface(shape = CircleShape, color = OceanBlue600) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = LuminousOnPrimary,
                        modifier = Modifier.padding(10.dp).size(20.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Tổng chi",
                    amount = summary.totalExpense,
                    color = ExpenseRed600,
                    icon = Icons.Outlined.TrendingDown,
                    background = ExpenseRed50
                )
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Tổng thu",
                    amount = summary.totalIncome,
                    color = IncomeGreen600,
                    icon = Icons.Outlined.TrendingUp,
                    background = IncomeGreen50
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Số dư",
                    amount = summary.balance,
                    color = if (summary.balance < 0) ExpenseRed600 else OceanBlue600,
                    icon = Icons.Outlined.AccountBalanceWallet,
                    background = OceanBlue50
                )
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Giao dịch",
                    value = summary.transactionCount.toString(),
                    color = OceanBlue800,
                    icon = Icons.Outlined.ReceiptLong,
                    background = SavingsTeal50
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    color: Color,
    icon: ImageVector,
    background: Color,
    modifier: Modifier = Modifier,
    amount: Double? = null,
    value: String? = null
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(20.dp))
                .background(background.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = LuminousSurfaceContainerLowest.copy(alpha = 0.82f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(8.dp).size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = NeutralGray600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value ?: formatMoneyDisplay(amount ?: 0.0),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CategoryStatisticSection(
    statistics: List<CategoryStatistic>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = "Phân tích chi tiêu",
                subtitle = "Top danh mục chi nhiều nhất",
                compact = true
            )

            if (statistics.isEmpty()) {
                Text(
                    text = "Chưa đủ dữ liệu để phân tích danh mục.",
                    fontSize = 13.sp,
                    color = NeutralGray600
                )
            } else {
                statistics.take(5).forEach { statistic ->
                    CategoryStatisticRow(statistic = statistic)
                }
            }
        }
    }
}

@Composable
private fun CategoryStatisticRow(statistic: CategoryStatistic) {
    val color = colorFromHex(statistic.categoryColor, OceanBlue600)
    val progress = normalizedPercentage(statistic.percentage)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            CategoryIcon(
                icon = statistic.categoryIcon,
                tint = color,
                fallbackFontSize = 20.sp,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statistic.categoryName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuminousOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatMoneyDisplay(statistic.totalAmount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed600,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(20.dp)),
                    color = color,
                    trackColor = LuminousSurfaceContainerLow
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralGray600
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = if (compact) 16.sp else 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LuminousOnSurface
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = NeutralGray600
        )
    }
}

@Composable
fun TransactionGroupItem(
    group: TransactionDayGroup,
    onTransactionClick: (TransactionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        DateGroupHeader(group = group)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = LuminousSurfaceContainerLowest,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                group.transactions.forEach { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateGroupHeader(group: TransactionDayGroup) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = dayLabel(group.date),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface
            )
            Text(
                text = group.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                fontSize = 12.sp,
                color = NeutralGray600
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            if (group.totalIncome > 0.0) {
                Text(
                    text = "+${formatMoneyDisplay(group.totalIncome)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen600
                )
            }
            if (group.totalExpense > 0.0) {
                Text(
                    text = "-${formatMoneyDisplay(group.totalExpense)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed600
                )
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val type = transaction.normalizedType
    val categoryColor = colorFromHex(transaction.categoryColor, OceanBlue600)
    val amountColor =
        when (type) {
            ExpenseBookTransactionType.EXPENSE -> ExpenseRed600
            ExpenseBookTransactionType.INCOME -> IncomeGreen600
            ExpenseBookTransactionType.TRANSFER -> OceanBlue600
            null -> NeutralGray600
        }
    val typeIcon =
        when (type) {
            ExpenseBookTransactionType.INCOME -> Icons.Outlined.TrendingUp
            ExpenseBookTransactionType.TRANSFER -> Icons.Outlined.SwapHoriz
            else -> Icons.Outlined.TrendingDown
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(categoryColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            CategoryIcon(
                icon = transaction.categoryIcon,
                tint = categoryColor,
                fallbackFontSize = 21.sp,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.categoryName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminousOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text =
                    listOf(transaction.note.orEmpty(), transaction.walletName)
                        .filter { it.isNotBlank() }
                        .joinToString(", "),
                fontSize = 12.sp,
                color = NeutralGray600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = signedAmountText(transaction),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor,
                    maxLines = 1
                )
            }
            Text(
                text = transaction.parsedDate.format(DateTimeFormatter.ofPattern("dd/MM")),
                fontSize = 11.sp,
                color = NeutralGray600
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = CircleShape, color = OceanBlue50) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = OceanBlue600,
                    modifier = Modifier.padding(18.dp).size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Không có giao dịch trong giai đoạn này",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Thử đổi khoảng thời gian hoặc bỏ bớt bộ lọc để xem lại nhật ký.",
                fontSize = 13.sp,
                color = NeutralGray600,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ExpenseBookLoadingState() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(172.dp),
            shape = RoundedCornerShape(28.dp),
            color = LuminousSurfaceContainerLowest
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OceanBlue600)
            }
        }
        repeat(3) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(78.dp),
                shape = RoundedCornerShape(24.dp),
                color = LuminousSurfaceContainerLowest.copy(alpha = 0.78f)
            ) {}
        }
    }
}

@Composable
private fun ExpenseBookErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = ExpenseRed600,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Không tải được sổ chi tiêu",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = NeutralGray600,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Thử lại")
            }
        }
    }
}

@Composable
private fun ExpenseBookFilterSheet(
    uiState: ExpenseBookUiState,
    onTypeSelected: (ExpenseBookTransactionType?) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onWalletSelected: (Long?) -> Unit,
    onApplyCustomRange: (LocalDate, LocalDate) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    var fromText by remember(uiState.filter.fromDate) { mutableStateOf(uiState.filter.fromDate.toString()) }
    var toText by remember(uiState.filter.toDate) { mutableStateOf(uiState.filter.toDate.toString()) }
    var dateError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.filter.fromDate, uiState.filter.toDate) {
        fromText = uiState.filter.fromDate.toString()
        toText = uiState.filter.toDate.toString()
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Bộ lọc",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LuminousOnSurface
                )
                Text(
                    text = "Tinh chỉnh nhật ký theo đúng bối cảnh cần xem",
                    fontSize = 12.sp,
                    color = NeutralGray600
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Đóng bộ lọc",
                    tint = NeutralGray600
                )
            }
        }

        FilterGroup(title = "Loại giao dịch") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    SelectablePill(
                        label = "Tất cả",
                        selected = uiState.filter.type == null,
                        onClick = { onTypeSelected(null) }
                    )
                }
                items(ExpenseBookTransactionType.values().size) { index ->
                    val type = ExpenseBookTransactionType.values()[index]
                    SelectablePill(
                        label = type.label,
                        selected = uiState.filter.type == type,
                        onClick = { onTypeSelected(type) }
                    )
                }
            }
        }

        FilterGroup(title = "Danh mục") {
            OptionPillRow(
                options = uiState.categoryOptions,
                selectedId = uiState.filter.categoryId,
                allLabel = "Tất cả danh mục",
                onSelected = onCategorySelected
            )
        }

        FilterGroup(title = "Ví") {
            OptionPillRow(
                options = uiState.walletOptions,
                selectedId = uiState.filter.walletId,
                allLabel = "Tất cả ví",
                onSelected = onWalletSelected
            )
        }

        FilterGroup(title = "Khoảng thời gian tùy chỉnh") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DateTextField(
                    label = "Từ ngày",
                    value = fromText,
                    onValueChange = {
                        fromText = it
                        dateError = null
                    },
                    modifier = Modifier.weight(1f)
                )
                DateTextField(
                    label = "Đến ngày",
                    value = toText,
                    onValueChange = {
                        toText = it
                        dateError = null
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            dateError?.let {
                Text(text = it, color = ExpenseRed600, fontSize = 12.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColors(contentColor = OceanBlue600)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterAltOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Xóa lọc")
            }
            Button(
                onClick = {
                    val fromDate = runCatching { LocalDate.parse(fromText.trim()) }.getOrNull()
                    val toDate = runCatching { LocalDate.parse(toText.trim()) }.getOrNull()
                    when {
                        fromDate == null || toDate == null -> {
                            dateError = "Định dạng ngày cần là yyyy-MM-dd"
                        }
                        toDate.isBefore(fromDate) -> {
                            dateError = "Ngày kết thúc phải sau ngày bắt đầu"
                        }
                        else -> {
                            onApplyCustomRange(fromDate, toDate)
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600)
            ) {
                Text(text = "Áp dụng")
            }
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = LuminousOnSurface
        )
        Column(content = content)
    }
}

@Composable
private fun OptionPillRow(
    options: List<ExpenseBookFilterOption>,
    selectedId: Long?,
    allLabel: String,
    onSelected: (Long?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            SelectablePill(
                label = allLabel,
                selected = selectedId == null,
                onClick = { onSelected(null) }
            )
        }
        items(options.size) { index ->
            val option = options[index]
            SelectablePill(
                label = option.label,
                selected = selectedId == option.id,
                onClick = { onSelected(option.id) }
            )
        }
    }
}

@Composable
private fun DateTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = NeutralGray600,
            modifier = Modifier.padding(start = 4.dp, bottom = 5.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = LuminousSurfaceContainerLow,
                    unfocusedContainerColor = LuminousSurfaceContainerLow,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = OceanBlue600
                )
        )
    }
}

@Composable
private fun SelectablePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) OceanBlue600 else LuminousSurfaceContainerLowest,
        shadowElevation = if (selected) 3.dp else 1.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) LuminousOnPrimary else NeutralGray600,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TransactionDetailSheet(
    transaction: TransactionItem,
    onDismiss: () -> Unit
) {
    val type = transaction.normalizedType
    val categoryColor = colorFromHex(transaction.categoryColor, OceanBlue600)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(categoryColor.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIcon(
                        icon = transaction.categoryIcon,
                        tint = categoryColor,
                        fallbackFontSize = 24.sp,
                        modifier = Modifier.size(25.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = transaction.categoryName,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LuminousOnSurface
                    )
                    Text(
                        text = type?.label ?: transaction.type,
                        fontSize = 12.sp,
                        color = NeutralGray600
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Đóng chi tiết",
                    tint = NeutralGray600
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = LuminousSurfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DetailLine("Số tiền", signedAmountText(transaction), amountColorFor(type))
                DetailLine("Ví", transaction.walletName, LuminousOnSurface)
                DetailLine(
                    "Ngày",
                    transaction.parsedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    LuminousOnSurface
                )
                DetailLine("Ghi chú", transaction.note.orEmpty().ifBlank { "Không có ghi chú" }, LuminousOnSurface)
                DetailLine("Mã giao dịch", "#${transaction.id}", NeutralGray600)
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = NeutralGray600)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 12.dp)
        )
    }
}

private fun colorFromHex(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return runCatching {
        Color(android.graphics.Color.parseColor(hex.trim()))
    }.getOrDefault(fallback)
}

private fun normalizedPercentage(value: Float): Float {
    return if (value > 1f) (value / 100f).coerceIn(0f, 1f) else value.coerceIn(0f, 1f)
}

private fun signedAmountText(transaction: TransactionItem): String {
    val amount = formatMoneyDisplay(abs(transaction.amount))
    return when (transaction.normalizedType) {
        ExpenseBookTransactionType.EXPENSE -> "-$amount"
        ExpenseBookTransactionType.INCOME -> "+$amount"
        ExpenseBookTransactionType.TRANSFER -> amount
        null -> amount
    }
}

private fun amountColorFor(type: ExpenseBookTransactionType?): Color {
    return when (type) {
        ExpenseBookTransactionType.EXPENSE -> ExpenseRed600
        ExpenseBookTransactionType.INCOME -> IncomeGreen600
        ExpenseBookTransactionType.TRANSFER -> OceanBlue600
        null -> NeutralGray600
    }
}

private fun dayLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Hôm nay"
        today.minusDays(1) -> "Hôm qua"
        else ->
            date.dayOfWeek
                .getDisplayName(TextStyle.FULL, Locale("vi"))
                .replaceFirstChar { it.uppercase(Locale("vi")) }
    }
}

private fun rangeLabel(fromDate: LocalDate, toDate: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return "${fromDate.format(formatter)} đến ${toDate.format(formatter)}"
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun ExpenseBookScreenPreview() {
    DDMoneyTheme(darkTheme = false) {
        ExpenseBookScreenContent(
            uiState = ExpenseBookPreviewData.uiState,
            showSearch = false,
            onSearchVisibilityChange = {},
            onSearchQueryChange = {},
            onPeriodSelected = {},
            onRefresh = {},
            onRetry = {},
            onOpenFilters = {},
            onTransactionClick = {}
        )
    }
}
