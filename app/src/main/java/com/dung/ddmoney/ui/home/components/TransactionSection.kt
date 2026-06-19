package com.dung.ddmoney.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import com.dung.ddmoney.ui.theme.*
import com.dung.ddmoney.ui.components.formatMoneyDisplay
import java.time.format.DateTimeFormatter

@Composable
internal fun TransactionSectionHeader(
    title: String = "Giao dịch gần đây",
    detail: String? = null,
    actionText: String = "Tất cả",
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                title,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Black,
                color      = LuminousOnSurface
            )
            detail?.let {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeutralGray600
                )
            }
        }
        Text(
            actionText,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            color      = OceanBlue600,
            modifier   = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onSeeAll() }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
internal fun TransactionPill(
    transaction: Transaction,
    isVisible:   Boolean,
    isLast:      Boolean = false,
    onClick:     () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon bubble
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LuminousSurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                CategoryIcon(
                    icon = transaction.categoryIcon,
                    modifier = Modifier.size(20.dp),
                    tint = transaction.categoryColor,
                    fallbackFontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = transaction.title ?: transaction.categoryName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = LuminousOnSurface
                )
                Text(
                    text     = transaction.date.format(DateTimeFormatter.ofPattern("dd MMM, yyyy")),
                    fontSize = 11.sp,
                    color    = NeutralGray600
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isVisible) {
                        (if (transaction.type == TransactionType.INCOME) "+" else "-") +
                            " ${formatMoneyDisplay(transaction.amount)}"
                    } else "•••••• đ",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Black,
                    color      = if (transaction.type == TransactionType.INCOME)
                        IncomeGreen600 else ExpenseRed600
                )
                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (transaction.type == TransactionType.INCOME)
                                IncomeGreen50 else ExpenseRed50
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text       = if (transaction.type == TransactionType.INCOME) "Thu" else "Chi",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (transaction.type == TransactionType.INCOME)
                            IncomeGreen600 else ExpenseRed600
                    )
                }
            }
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(LuminousSurfaceContainerLow)
            )
        }
    }
}

@Composable
internal fun HomeTransactionDetailSheet(
    transaction: Transaction,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val amountColor = transactionAmountColor(transaction.type)
    val amountText =
        if (isVisible) {
            transactionAmountPrefix(transaction.type) + formatMoneyDisplay(transaction.amount)
        } else {
            "•••••• đ"
        }

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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(transaction.categoryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIcon(
                        icon = transaction.categoryIcon,
                        modifier = Modifier.size(25.dp),
                        tint = transaction.categoryColor,
                        fallbackFontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.categoryName,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LuminousOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = transactionTypeLabel(transaction.type),
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
                DetailLine("Số tiền", amountText, amountColor)
                DetailLine("Danh mục", transaction.categoryName, LuminousOnSurface)
                DetailLine("Ví", transaction.walletName, LuminousOnSurface)
                DetailLine(
                    "Ngày",
                    transaction.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    LuminousOnSurface
                )
                DetailLine(
                    "Ghi chú",
                    transaction.note.ifBlank { "Không có ghi chú" },
                    LuminousOnSurface
                )
                DetailLine("Mã giao dịch", transaction.id, NeutralGray600)
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = NeutralGray600
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

private fun transactionAmountColor(type: TransactionType): Color =
    when (type) {
        TransactionType.INCOME -> IncomeGreen600
        TransactionType.EXPENSE -> ExpenseRed600
        TransactionType.TRANSFER -> OceanBlue600
        TransactionType.DEBT -> InvestAmber600
    }

private fun transactionAmountPrefix(type: TransactionType): String =
    when (type) {
        TransactionType.INCOME -> "+ "
        TransactionType.EXPENSE -> "- "
        else -> ""
    }

private fun transactionTypeLabel(type: TransactionType): String =
    when (type) {
        TransactionType.INCOME -> "Thu nhập"
        TransactionType.EXPENSE -> "Chi tiêu"
        TransactionType.TRANSFER -> "Chuyển ví"
        TransactionType.DEBT -> "Nợ"
    }

// ─────────────────────────────────────────────────────────────────────────────
//  EmptyTransactions
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun EmptyTransactions(
    message: String = "Chưa có giao dịch nào",
    helperText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector    = Icons.AutoMirrored.Outlined.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint     = LuminousSurfaceContainerHigh
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text       = message,
            color      = NeutralGray600,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium
        )
        helperText?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = it,
                color = NeutralGray600.copy(alpha = 0.75f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
