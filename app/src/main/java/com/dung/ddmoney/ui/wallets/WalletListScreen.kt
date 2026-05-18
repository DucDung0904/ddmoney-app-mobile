package com.dung.ddmoney.ui.wallets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

private val WALLET_ICON_BOX_SIZE = 38.dp
private val WALLET_ICON_SIZE = 23.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletListScreen(
    wallets: List<Wallet>,
    onBack: () -> Unit,
    onAddWallet: () -> Unit,
    onWalletClick: (Wallet) -> Unit,
    onUnarchiveWallet: (Wallet) -> Unit = {}
) {
    val activeWallets = wallets.filter { !it.isArchived }
    val archivedWallets = wallets.filter { it.isArchived }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Ví của tôi",
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold,
                        color = LuminousOnSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    ) 
                },
                navigationIcon = {
                    LiquidIconButton(
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                },
                actions = {
                    LiquidIconButton(
                        onClick = onAddWallet,
                        icon = Icons.Outlined.Add,
                        contentDescription = "Add"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuminousBackground,
                    titleContentColor = LuminousOnSurface
                )
            )
        },
        containerColor = LuminousBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TotalBalanceSummaryCard(activeWallets.filter { it.isIncludedInTotal }.sumOf { it.balance })
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Danh sách ví",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LuminousOnSurface
                    )
                    Text(
                        text = "${activeWallets.size} ví",
                        fontSize = 13.sp,
                        color = LuminousOnSurfaceVariant
                    )
                }
            }

            itemsIndexed(activeWallets) { _, wallet ->
                WalletDetailItem(wallet = wallet, onClick = { onWalletClick(wallet) })
            }

            if (archivedWallets.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ví lưu trữ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LuminousOnSurface
                        )
                        Text(
                            text = "${archivedWallets.size} ví",
                            fontSize = 13.sp,
                            color = LuminousOnSurfaceVariant
                        )
                    }
                }

                itemsIndexed(archivedWallets) { _, wallet ->
                    WalletDetailItem(
                        wallet = wallet,
                        isArchived = true,
                        onClick = {},
                        onUnarchive = { onUnarchiveWallet(wallet) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun LiquidIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Hiệu ứng Liquid - Co giãn mượt mà khi nhấn
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(durationMillis = 160),
        label = "LiquidScale"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .size(42.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(OceanBlue50)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = OceanBlue600),
                 onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = OceanBlue600,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun TotalBalanceSummaryCard(total: Double) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp),
        shape = RoundedCornerShape(6.dp),
        color = LuminousSurfaceContainerLowest,
        border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.32f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tổng số dư",
                    color = LuminousOnSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatWalletAmount(total),
                    color = LuminousOnSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OceanBlue50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = OceanBlue600,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun WalletDetailItem(
    wallet: Wallet,
    isArchived: Boolean = false,
    onClick: () -> Unit,
    onUnarchive: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .then(if (isArchived) Modifier else Modifier.clickable(onClick = onClick)),
        shape = RoundedCornerShape(6.dp),
        color = if (isArchived) LuminousSurfaceContainerLow else LuminousSurfaceContainerLowest,
        border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.32f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(WALLET_ICON_BOX_SIZE)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isArchived) LuminousSurfaceContainerHigh
                        else WalletIconMap.backgroundFor(wallet.type)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = WalletIconMap.toVector(wallet.icon, wallet.type),
                    contentDescription = null,
                    modifier = Modifier.size(WALLET_ICON_SIZE),
                    tint = if (isArchived) LuminousOnSurfaceVariant else WalletIconMap.tintFor(wallet.type)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = wallet.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isArchived) LuminousOnSurfaceVariant else LuminousOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isArchived) {
                    Text(
                        text = "Đã lưu trữ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuminousOnSurfaceVariant
                    )
                }
            }

            if (isArchived) {
                TextButton(
                    onClick = onUnarchive,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Unarchive,
                        contentDescription = null,
                        tint = OceanBlue600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bỏ lưu trữ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue600
                    )
                }
            } else {
                Text(
                    text = formatWalletAmount(wallet.balance),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = LuminousOnSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

private fun formatWalletAmount(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    return "${formatter.format(amount)} đ"
}
