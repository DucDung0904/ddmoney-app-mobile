package com.dung.ddmoney.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import com.dung.ddmoney.ui.theme.*
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
//  TransactionSectionHeader
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun TransactionSectionHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            "Giao dịch gần đây",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Black,
            color      = LuminousOnSurface
        )
        Text(
            "Tất cả",
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

// ─────────────────────────────────────────────────────────────────────────────
//  TransactionPill  — single transaction row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun TransactionPill(
    transaction: Transaction,
    isVisible:   Boolean,
    isLast:      Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
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
                            " ${String.format("%,.0f", transaction.amount)} đ"
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

// ─────────────────────────────────────────────────────────────────────────────
//  EmptyTransactions
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun EmptyTransactions() {
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
            text       = "Chưa có giao dịch nào",
            color      = NeutralGray600,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
