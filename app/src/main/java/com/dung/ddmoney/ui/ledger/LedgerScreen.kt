package com.dung.ddmoney.ui.ledger

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.analytics.formatVnd
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ── Filter tabs ───────────────────────────────────────────────────────────────
private enum class LedgerFilter(val label: String) {
    ALL("Tất cả"),
    EXPENSE("Chi tiêu"),
    INCOME("Thu nhập")
}

@Composable
fun LedgerScreen(
    transactions: List<Transaction> = emptyList(),
    categories: List<Category> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(LedgerFilter.ALL) }

    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            LedgerFilter.ALL     -> transactions
            LedgerFilter.EXPENSE -> transactions.filter { it.type == TransactionType.EXPENSE }
            LedgerFilter.INCOME  -> transactions.filter { it.type == TransactionType.INCOME }
        }
    }

    // Group by date descending
    val grouped = remember(filteredTransactions) {
        filteredTransactions
            .sortedByDescending { it.date }
            .groupBy { it.date }
            .toSortedMap(compareByDescending { it })
    }

    Box(modifier = modifier.fillMaxSize().background(LuminousBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────────────
            item { LedgerHeader() }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ── Filter Tabs ────────────────────────────────────────────────────
            item {
                LedgerFilterSelector(
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it }
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }

            if (grouped.isEmpty()) {
                item { LedgerEmptyState() }
            } else {
                grouped.forEach { (date, txList) ->
                    // Date header
                    item(key = "header_$date") {
                        DateGroupHeader(
                            date = date,
                            transactions = txList
                        )
                    }

                    // Transaction rows inside a card
                    item(key = "group_$date") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = LuminousSurfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                txList.forEachIndexed { index, tx ->
                                    val category = categoryMap[tx.categoryId]
                                    TransactionRow(
                                        transaction = tx,
                                        category = category
                                    )
                                    if (index < txList.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            thickness = 0.5.dp,
                                            color = NeutralGray100
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun LedgerHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "SỔ CHI TIÊU",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeutralGray600
            )
            Text(
                text = "Giao dịch",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LuminousOnSurface
            )
        }
        Surface(shape = CircleShape, color = OceanBlue50) {
            Icon(
                imageVector = Icons.Outlined.ReceiptLong,
                contentDescription = "Sổ chi tiêu",
                tint = OceanBlue600,
                modifier = Modifier.padding(12.dp).size(24.dp)
            )
        }
    }
}

// ── Filter Selector ───────────────────────────────────────────────────────────
@Composable
private fun LedgerFilterSelector(
    selected: LedgerFilter,
    onSelect: (LedgerFilter) -> Unit
) {
    val filters = LedgerFilter.values()
    val density = LocalDensity.current
    var selectorWidthPx by remember { mutableStateOf(0) }
    val segmentWidth = with(density) { (selectorWidthPx.toFloat() / filters.size).toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .onSizeChanged { selectorWidthPx = it.width }
            .clip(RoundedCornerShape(18.dp))
            .background(LuminousSurfaceContainerLow)
            .padding(4.dp)
    ) {
        val selectedIndex = filters.indexOf(selected).coerceAtLeast(0)
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "ledger_filter_offset"
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
            filters.forEach { filter ->
                val isActive = selected == filter
                val interactionSource = remember(filter) { MutableInteractionSource() }
                val textColor by animateColorAsState(
                    targetValue = if (isActive) Color.White else NeutralGray600,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                    label = "ledger_filter_${filter.name}_color"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(15.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelect(filter) }
                        )
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 14.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

// ── Date Group Header ─────────────────────────────────────────────────────────
@Composable
private fun DateGroupHeader(
    date: LocalDate,
    transactions: List<Transaction>
) {
    val totalExpense = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    val totalIncome = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("vi"))
    val formatted = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val isToday = date == LocalDate.now()
    val isYesterday = date == LocalDate.now().minusDays(1)
    val dayLabel = when {
        isToday     -> "Hôm nay"
        isYesterday -> "Hôm qua"
        else        -> dayOfWeek.replaceFirstChar { it.uppercase() }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = dayLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface
            )
            Text(
                text = formatted,
                fontSize = 11.sp,
                color = NeutralGray600
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (totalIncome > 0.0) {
                Text(
                    text = "+${formatVnd(totalIncome)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SavingsTeal600
                )
            }
            if (totalExpense > 0.0) {
                Text(
                    text = "-${formatVnd(totalExpense)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ExpenseRed600
                )
            }
        }
    }
}

// ── Transaction Row ───────────────────────────────────────────────────────────
@Composable
private fun TransactionRow(
    transaction: Transaction,
    category: Category?
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) ExpenseRed600 else SavingsTeal600
    val amountPrefix = if (isExpense) "-" else "+"
    val iconBg = category?.color?.copy(alpha = 0.12f) ?: NeutralGray100
    val iconTint = category?.color ?: NeutralGray600

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            if (category != null) {
                CategoryIcon(
                    icon = category.icon,
                    modifier = Modifier.size(22.dp),
                    tint = iconTint,
                    fallbackFontSize = 22.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = NeutralGray600,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Name + note
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category?.name ?: transaction.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminousOnSurface
            )
            if (transaction.note.isNotBlank()) {
                Text(
                    text = transaction.note,
                    fontSize = 12.sp,
                    color = NeutralGray600,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amount
        Text(
            text = "$amountPrefix${formatVnd(transaction.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor,
            textAlign = TextAlign.End
        )
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────
@Composable
private fun LedgerEmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.ReceiptLong,
                contentDescription = null,
                tint = NeutralGray600,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Chưa có giao dịch",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Các giao dịch của bạn sẽ xuất hiện ở đây.",
                fontSize = 13.sp,
                color = NeutralGray600,
                textAlign = TextAlign.Center
            )
        }
    }
}
