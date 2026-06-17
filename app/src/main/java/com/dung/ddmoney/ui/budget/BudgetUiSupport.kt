package com.dung.ddmoney.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.R
import com.dung.ddmoney.repository.BudgetDisplayModel
import com.dung.ddmoney.repository.BudgetPeriod
import com.dung.ddmoney.repository.BudgetPeriodType
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import com.dung.ddmoney.ui.theme.LuminousOnBackground
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLowest
import com.dung.ddmoney.ui.theme.NeutralGray400
import com.dung.ddmoney.ui.theme.NeutralGray600
import com.dung.ddmoney.ui.theme.OceanBlue50
import com.dung.ddmoney.ui.theme.OceanBlue600
import com.dung.ddmoney.ui.theme.OceanBlue800
import com.dung.ddmoney.ui.wallets.WalletIconMap
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

internal val budgetPeriodTypes =
    listOf(
        BudgetPeriodType.MONTH,
        BudgetPeriodType.WEEK,
        BudgetPeriodType.QUARTER,
        BudgetPeriodType.YEAR
    )

internal fun findConflictingCategoryIdsForPeriod(
    budgets: List<BudgetDisplayModel>,
    categoryIds: Set<Long>,
    period: BudgetPeriod,
    excludedBudgetId: String? = null
): Set<Long> =
    budgets
        .asSequence()
        .filter { budget ->
            budget.id != excludedBudgetId &&
            budget.periodType == period.type &&
            budget.startDate == period.startDate &&
            budget.endDate == period.endDate
        }
        .flatMap { budget -> budget.categories.asSequence().map { it.id } }
        .filter { it in categoryIds }
        .toSet()

internal fun calculateSpentForBudgetDraft(
    transactions: List<Transaction>,
    categories: List<Category>,
    selectedCategoryIds: Set<Long>,
    period: BudgetPeriod,
    walletId: Long?
): Double {
    if (selectedCategoryIds.isEmpty()) return 0.0

    val selectedIds = selectedCategoryIds.map(Long::toString).toSet()
    val childIds =
        categories
            .filter { category -> category.parentId in selectedIds }
            .map { category -> category.id }
            .toSet()
    val effectiveCategoryIds = selectedIds + childIds

    return transactions
        .asSequence()
        .filter { transaction -> transaction.type == TransactionType.EXPENSE }
        .filter { transaction -> period.contains(transaction.date) }
        .filter { transaction -> transaction.categoryId in effectiveCategoryIds }
        .filter { transaction ->
            walletId == null || transaction.walletId.toLongOrNull() == walletId
        }
        .sumOf(Transaction::amount)
}

@Composable
internal fun BudgetPeriodTabRow(
    selectedPosition: Float,
    useCurrentLabels: Boolean,
    onSelect: (BudgetPeriodType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(LuminousSurfaceContainerLowest)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            budgetPeriodTypes.forEach { type ->
                val index = budgetPeriodTypes.indexOf(type)
                val selected = kotlin.math.abs(selectedPosition - index) < 0.5f
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clickable(
                                interactionSource = remember(type) { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelect(type) }
                            ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = budgetPeriodTypeLabel(type, useCurrentLabels),
                        color = if (selected) LuminousOnBackground else NeutralGray400,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart)
        ) {
            val segmentWidth = maxWidth / budgetPeriodTypes.size
            Box(
                modifier =
                    Modifier
                        .offset(x = segmentWidth * selectedPosition.coerceIn(0f, 3f))
                        .width(segmentWidth)
                        .height(3.dp)
                        .background(LuminousOnBackground, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            )
        }
    }
}

@Composable
internal fun BudgetWalletIcon(
    wallet: Wallet?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 36.dp
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (wallet == null) {
            Image(
                painter = painterResource(R.drawable.convert),
                contentDescription = null,
                modifier = Modifier.size(size)
            )
        } else {
            WalletIconMap.WalletIcon(
                key = wallet.icon,
                walletType = wallet.type,
                contentDescription = null,
                modifier = Modifier.size(size)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BudgetWalletPickerSheet(
    visible: Boolean,
    wallets: List<Wallet>,
    selectedWalletId: Long?,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LuminousSurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = 0.2f)
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "Áp dụng cho ví",
                color = LuminousOnBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            BudgetWalletPickerRow(
                wallet = null,
                selected = selectedWalletId == null,
                onClick = { onSelect(null) }
            )
            wallets.forEach { wallet ->
                val walletId = wallet.id.toLongOrNull() ?: return@forEach
                BudgetWalletPickerRow(
                    wallet = wallet,
                    selected = selectedWalletId == walletId,
                    onClick = { onSelect(walletId) }
                )
            }
        }
    }
}

@Composable
private fun BudgetWalletPickerRow(
    wallet: Wallet?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (selected) OceanBlue50 else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BudgetWalletIcon(wallet = wallet)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = wallet?.name ?: "Tổng cộng",
                color = if (selected) OceanBlue800 else LuminousOnBackground,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (wallet == null) {
                Text(
                    text = "Tính giao dịch từ tất cả ví",
                    color = NeutralGray600,
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (selected) OceanBlue600 else Color.Transparent,
            modifier = Modifier.size(20.dp)
        )
    }
}

internal fun budgetPeriodTypeLabel(
    type: BudgetPeriodType,
    current: Boolean = false
): String {
    val base =
        when (type) {
            BudgetPeriodType.WEEK -> "Tuần"
            BudgetPeriodType.MONTH -> "Tháng"
            BudgetPeriodType.QUARTER -> "Quý"
            BudgetPeriodType.YEAR -> "Năm"
        }
    return if (current) "$base này" else base
}

internal fun formatBudgetPeriodLabel(period: BudgetPeriod): String {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
    return when (period.type) {
        BudgetPeriodType.WEEK -> {
            val week = period.startDate.get(WeekFields.ISO.weekOfWeekBasedYear())
            "Tuần $week (${period.startDate.format(dateFormatter)} - ${period.endDate.format(dateFormatter)})"
        }

        BudgetPeriodType.MONTH ->
            "Tháng ${period.startDate.monthValue} (${period.startDate.format(dateFormatter)} - ${period.endDate.format(dateFormatter)})"

        BudgetPeriodType.QUARTER -> {
            val quarter = (period.startDate.monthValue - 1) / 3 + 1
            "Quý $quarter (${period.startDate.format(dateFormatter)} - ${period.endDate.format(dateFormatter)})"
        }

        BudgetPeriodType.YEAR -> "Năm ${period.startDate.year}"
    }
}

internal fun budgetPeriodRangeLabel(period: BudgetPeriod): String =
    when (period.type) {
        BudgetPeriodType.WEEK,
        BudgetPeriodType.MONTH,
        BudgetPeriodType.QUARTER ->
            "${budgetPeriodTypeLabel(period.type, current = true)}, " +
                "${period.startDate.format(DateTimeFormatter.ofPattern("dd/MM"))} - " +
                period.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        BudgetPeriodType.YEAR -> "Năm ${period.startDate.year}"
    }
