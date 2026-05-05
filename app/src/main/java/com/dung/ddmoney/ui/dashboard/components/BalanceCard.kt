package com.dung.ddmoney.ui.dashboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

// ─── Formatter helper ─────────────────────────────────────────────────
fun formatCurrency(amount: Double): String {
    val formatted =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
                    .format(Math.abs(amount.toLong()))
    return "${formatted}đ"
}

fun formatCurrencyVND(amount: Double): String {
    val formatted =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount.toLong())
    return "${formatted} VND"
}

// ─── Hero Balance Card (Blue header matching design) ──────────────────
@Composable
fun BalanceCard(
        totalBalance: Double,
        totalIncome: Double,
        totalExpense: Double,
        selectedMonth: String,
        onAddIncome: () -> Unit = {},
        onAddExpense: () -> Unit = {},
        modifier: Modifier = Modifier
) {
    Box(
            modifier =
                    modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                            .background(
                                    Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF1A3C8A),
                                                Color(0xFF2B63D9)
                                            )
                                    )
                            )
                            .padding(horizontal = 20.dp)
                            .padding(top = 8.dp, bottom = 28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            // Label
            Text(
                    text = "Tổng số dư",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Big balance number
            Text(
                    text = formatCurrencyVND(totalBalance),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Income & Expense buttons
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add Income button
                Row(
                        modifier =
                                Modifier.weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1A7A4A).copy(alpha = 0.85f))
                                        .clickable(
                                                interactionSource =
                                                        remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = onAddIncome
                                        )
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                            modifier =
                                    Modifier.size(24.dp)
                                            .background(
                                                    Color.White.copy(alpha = 0.25f),
                                                    CircleShape
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = "Thêm Thu\nNhập",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                    )
                }

                // Add Expense button
                Row(
                        modifier =
                                Modifier.weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF7A1E30).copy(alpha = 0.85f))
                                        .clickable(
                                                interactionSource =
                                                        remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = onAddExpense
                                        )
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                            modifier =
                                    Modifier.size(24.dp)
                                            .background(
                                                    Color.White.copy(alpha = 0.25f),
                                                    CircleShape
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = "Thêm Chi\nTiêu",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ─── Summary Chip (kept for backward compat) ──────────────────────────
@Composable
fun SummaryChip(label: String, amount: Double, isIncome: Boolean, modifier: Modifier = Modifier) {
    val color = if (isIncome) IncomeGreen else ExpenseRed
    val icon = if (isIncome) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
    val bgColor = color.copy(alpha = 0.08f)

    Row(
            modifier =
                    modifier.clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
                modifier = Modifier.size(36.dp).background(bgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                    text = formatCurrency(amount),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── Quick Action Shortcuts Row ───────────────────────────────────────
data class QuickAction(val emoji: String, val label: String, val onClick: () -> Unit = {})

@Composable
fun QuickActionsRow(
        onScanQR: () -> Unit = {},
        onTransfer: () -> Unit = {},
        onScheduled: () -> Unit = {},
        onSavings: () -> Unit = {},
        modifier: Modifier = Modifier
) {
    val actions =
            listOf(
                    QuickAction("🧾", "Quét hóa\nđơn", onScanQR),
                    QuickAction("↔️", "Chuyển\ntiền", onTransfer),
                    QuickAction("📅", "Định kỳ", onScheduled),
                    QuickAction("🐷", "Tiết\nkiệm", onSavings),
            )

    Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        actions.forEach { action ->
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                            Modifier.weight(1f)
                                    .clickable(
                                            interactionSource =
                                                    remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = action.onClick
                                    )
                                    .padding(vertical = 8.dp)
            ) {
                Box(
                        modifier =
                                Modifier.size(52.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                ) { Text(text = action.emoji, fontSize = 22.sp) }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                        text = action.label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF7F9FF)
@Composable
private fun BalanceCardPreview() {
    DDMoneyTheme(darkTheme = false) {
        Column {
            BalanceCard(
                    totalBalance = 124_500_000.0,
                    totalIncome = 42_000_000.0,
                    totalExpense = 17_420_000.0,
                    selectedMonth = "Tháng 5, 2026"
            )
            Spacer(modifier = Modifier.height(16.dp))
            QuickActionsRow()
        }
    }
}
