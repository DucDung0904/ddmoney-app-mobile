package com.dung.ddmoney.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    userName: String = "Người dùng",
    totalBalance: Double = 0.0,
    wallets: List<Wallet> = emptyList(),
    recentTransactions: List<Transaction> = emptyList(),
    onSeeAllWallets: () -> Unit = {}
) {
    var isBalanceVisible by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuminousBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item { 
                HomeHeaderWithBalance(
                    name = userName, 
                    balance = totalBalance,
                    isBalanceVisible = isBalanceVisible,
                    onToggleBalance = { isBalanceVisible = !isBalanceVisible }
                ) 
            }
            
            item { 
                WalletsListCard(
                    wallets = wallets.take(3), 
                    isVisible = isBalanceVisible,
                    onSeeAll = onSeeAllWallets
                ) 
            }

            // Phần Báo cáo hiện tại nằm dưới Ví của tôi
            item { 
                SpendingAnalysisCard(
                    income = 0.0,
                    expense = 0.0
                ) 
            }
            
            item { SectionHeader("Giao dịch gần đây") }
            
            if (recentTransactions.isEmpty()) {
                item { EmptyTransactions() }
            } else {
                items(recentTransactions) { transaction ->
                    TransactionListItem(transaction, isBalanceVisible)
                }
            }
        }
    }
}

@Composable
private fun SpendingAnalysisCard(income: Double, expense: Double) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Chi tiêu & Thu nhập
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tổng đã chi", fontSize = 12.sp, color = NeutralGray600)
                    Text(
                        "${String.format("%,.0f", expense)}", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Black, 
                        color = ExpenseRed600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(4.dp).background(ExpenseRed600, RoundedCornerShape(2.dp)))
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tổng thu", fontSize = 12.sp, color = NeutralGray600)
                    Text(
                        "${String.format("%,.0f", income)}", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Black, 
                        color = IncomeGreen600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(4.dp).background(IncomeGreen600, RoundedCornerShape(2.dp)))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Biểu đồ Line Chart (Thay thế bằng thông báo Đang phát triển)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LuminousSurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.AutoGraph,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = NeutralGray400
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tính năng đang phát triển", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Medium,
                        color = NeutralGray600
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chú thích (Legend)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed600))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tháng này", fontSize = 11.sp, color = NeutralGray600)
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeutralGray400))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Trung bình 3 tháng trước", fontSize = 11.sp, color = NeutralGray600)
            }
        }
    }
}

@Composable
private fun HomeHeaderWithBalance(
    name: String, 
    balance: Double, 
    isBalanceVisible: Boolean,
    onToggleBalance: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = OceanBlue50,
                    border = androidx.compose.foundation.BorderStroke(2.dp, LuminousSurfaceContainerLowest)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = name.take(1).uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = OceanBlue600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Chào, $name",
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Black,
                        color = LuminousOnSurface
                    )
                    Text(
                        text = "Chúc bạn một ngày tốt lành!",
                        fontSize = 14.sp,
                        color = NeutralGray600
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeaderIcon(Icons.Outlined.Search)
                HeaderIcon(Icons.Outlined.Notifications)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "TỔNG SỐ DƯ", 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Black,
            color = NeutralGray600,
            letterSpacing = 1.2.sp
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = if (isBalanceVisible) "${String.format("%,.0f", balance)} đ" else "•••••••• đ",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = OceanBlue600
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Icon(
                imageVector = if (isBalanceVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                contentDescription = "Toggle Balance",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onToggleBalance() },
                tint = NeutralGray400
            )
        }
    }
}

@Composable
private fun WalletsListCard(wallets: List<Wallet>, isVisible: Boolean, onSeeAll: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ví của tôi", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Black, 
                    color = LuminousOnSurface
                )
                Text(
                    text = "Xem tất cả", 
                    fontSize = 14.sp, 
                    color = OceanBlue600, 
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onSeeAll() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            HorizontalDivider(color = LuminousSurfaceContainerLow, thickness = 1.dp)

            wallets.forEachIndexed { index, wallet ->
                WalletRowItem(wallet, isVisible)
                if (index < wallets.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = LuminousSurfaceContainerLow,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletRowItem(wallet: Wallet, isVisible: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = OceanBlue50
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = OceanBlue600
                )
            }
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Text(
            text = wallet.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = LuminousOnSurface,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = if (isVisible) "${String.format("%,.0f", wallet.balance)} đ" else "•••• đ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = LuminousOnSurface
        )
    }
}

@Composable
private fun HeaderIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = LuminousSurfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, LuminousSurfaceContainerLow)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = LuminousOnSurface)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = Color(0xFFF1F3F9).copy(alpha = 0.5f),
            thickness = 1.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = LuminousOnSurface)
            Text(
                text = "Xem tất cả", 
                fontSize = 13.sp, 
                fontWeight = FontWeight.Black, 
                color = OceanBlue600,
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
private fun TransactionListItem(transaction: Transaction, isVisible: Boolean) {
    val cardShape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .graphicsLayer {
                shape = cardShape
                clip = true
            },
        shape = cardShape,
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = LuminousSurfaceContainerLow
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = transaction.categoryIcon, fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title ?: transaction.categoryName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuminousOnSurface
                )
                Text(
                    text = transaction.date.format(DateTimeFormatter.ofPattern("dd MMM, yyyy")),
                    fontSize = 11.sp,
                    color = NeutralGray600
                )
            }
            
            Text(
                text = if (isVisible) {
                    (if (transaction.type == TransactionType.INCOME) "+" else "-") + 
                        " ${String.format("%,.0f", transaction.amount)} đ"
                } else "•••••• đ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = if (transaction.type == TransactionType.INCOME) IncomeGreen600 else ExpenseRed600
            )
        }
    }
}

@Composable
private fun EmptyTransactions() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong, 
            contentDescription = null, 
            modifier = Modifier.size(56.dp),
            tint = LuminousSurfaceContainerHigh
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Chưa có giao dịch nào", color = NeutralGray600, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
