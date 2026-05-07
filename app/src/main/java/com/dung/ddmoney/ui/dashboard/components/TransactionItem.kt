package com.dung.ddmoney.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.dung.ddmoney.ui.dashboard.model.SampleData
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import androidx.compose.ui.graphics.luminance
import com.dung.ddmoney.ui.theme.*

// ─── Activity Section (Transaction Feed) ─────────────────────────────
@Composable
fun TransactionSection(
    transactions: List<Transaction>,
    onSeeAll: () -> Unit = {},
    onDeleteTransaction: ((String) -> Unit)? = null,
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
                text = "Giao dịch gần đây",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAll) {
                Text(
                    text = "Xem tất cả",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "💸", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có giao dịch nào",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nhấn + để thêm giao dịch đầu tiên",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        } else {
            // Activity feed in a unified container with spacing between items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                transactions.forEachIndexed { index, transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { onDeleteTransaction?.invoke(transaction.id) }
                    )
                    if (index < transactions.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─── Single Transaction Item (Activity Feed style) ────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.1f
    val amountColor = when (transaction.type) {
        TransactionType.INCOME   -> if (isDark) DarkIncomeGreen else IncomeGreen
        TransactionType.EXPENSE  -> if (isDark) DarkExpenseRed else ExpenseRed
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
        TransactionType.DEBT     -> androidx.compose.ui.graphics.Color(0xFF7C4DFF)
    }
    val amountPrefix = when (transaction.type) {
        TransactionType.INCOME   -> "+"
        TransactionType.EXPENSE  -> "-"
        TransactionType.TRANSFER -> "↔"
        TransactionType.DEBT     -> ""
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa giao dịch") },
            text = { Text("Bạn có chắc chắn muốn xóa giao dịch này không?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null || onDelete != null) {
                    Modifier.combinedClickable(
                        onClick = { onClick?.invoke() },
                        onLongClick = { if (onDelete != null) showDeleteDialog = true }
                    )
                } else Modifier
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon box - rounded square style like image
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    transaction.categoryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = transaction.categoryIcon, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title & subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${transaction.displayDate} · ${transaction.categoryName}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Amount
        Text(
            text = "$amountPrefix${formatCurrency(transaction.amount)}đ",
            color = if (transaction.type == TransactionType.EXPENSE) MaterialTheme.colorScheme.onSurface else amountColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF7F9FF)
@Composable
private fun TransactionSectionPreview() {
    DDMoneyTheme(darkTheme = false) {
        TransactionSection(
            transactions = SampleData.sampleTransactions,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FF)
@Composable
private fun TransactionSectionEmptyPreview() {
    DDMoneyTheme(darkTheme = false) {
        TransactionSection(transactions = emptyList())
    }
}
