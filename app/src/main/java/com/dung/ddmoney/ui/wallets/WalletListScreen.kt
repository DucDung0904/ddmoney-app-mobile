package com.dung.ddmoney.ui.wallets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import com.dung.ddmoney.ui.components.formatMoneyDisplay

private val WALLET_ICON_BOX_SIZE = 42.dp
private val CARD_RADIUS = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletListScreen(
    wallets: List<Wallet>,
    onBack: () -> Unit,
    onAddWallet: () -> Unit,
    onWalletClick: (Wallet) -> Unit,
    onUnarchiveWallet: (Wallet) -> Unit = {}
) {
    val activeWallets = wallets.filter { !it.isArchived }.defaultWalletFirst()
    val includedWallets = activeWallets.filter { it.isIncludedInTotal }
    val excludedWallets = activeWallets.filter { !it.isIncludedInTotal }
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
                        modifier = Modifier.padding(start = 4.dp)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
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

            item {
                ActiveWalletSectionBox(
                    includedWallets = includedWallets,
                    excludedWallets = excludedWallets,
                    emptyText = "Chưa có ví đang hoạt động",
                    onWalletClick = onWalletClick
                )
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

                item {
                    WalletSectionBox(
                        wallets = archivedWallets,
                        emptyText = "",
                        itemContent = { wallet ->
                            WalletDetailItem(
                                wallet = wallet,
                                isArchived = true,
                                onClick = {},
                                onUnarchive = { onUnarchiveWallet(wallet) }
                            )
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ActiveWalletSectionBox(
    includedWallets: List<Wallet>,
    excludedWallets: List<Wallet>,
    emptyText: String,
    onWalletClick: (Wallet) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        if (includedWallets.isEmpty() && excludedWallets.isEmpty()) {
            Text(
                text = emptyText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminousOnSurfaceVariant
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                WalletGroup(
                    title = "Tính vào tổng",
                    count = includedWallets.size,
                    wallets = includedWallets,
                    emptyText = "Chưa có ví tính vào tổng",
                    onWalletClick = onWalletClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    thickness = 0.5.dp,
                    color = LuminousSurfaceContainerLow
                )

                WalletGroup(
                    title = "Không tính vào tổng",
                    count = excludedWallets.size,
                    wallets = excludedWallets,
                    emptyText = "Chưa có ví không tính vào tổng",
                    onWalletClick = onWalletClick
                )
            }
        }
    }
}

@Composable
private fun WalletGroup(
    title: String,
    count: Int,
    wallets: List<Wallet>,
    emptyText: String,
    onWalletClick: (Wallet) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WalletGroupHeader(title = title, count = count)

        if (wallets.isEmpty()) {
            Text(
                text = emptyText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = LuminousOnSurfaceVariant
            )
        } else {
            wallets.forEachIndexed { index, wallet ->
                WalletDetailItem(wallet = wallet, onClick = { onWalletClick(wallet) })
                if (index < wallets.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                        thickness = 0.5.dp,
                        color = LuminousSurfaceContainerLow
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletGroupHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralGray400,
            letterSpacing = 0.5.sp
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(OceanBlue50)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count ví",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OceanBlue600
            )
        }
    }
}

@Composable
private fun WalletSectionBox(
    wallets: List<Wallet>,
    emptyText: String,
    itemContent: @Composable (Wallet) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        if (wallets.isEmpty()) {
            Text(
                text = emptyText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminousOnSurfaceVariant
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                wallets.forEachIndexed { index, wallet ->
                    itemContent(wallet)
                    if (index < wallets.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                            thickness = 0.5.dp,
                            color = LuminousSurfaceContainerLow
                        )
                    }
                }
            }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CARD_RADIUS))
            .background(
                Brush.linearGradient(
                    colors = listOf(OceanBlue600, OceanBlue400)
                )
            )
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tổng số dư",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatWalletAmount(total),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isArchived) Modifier else Modifier.clickable(onClick = onClick))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(WALLET_ICON_BOX_SIZE)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                WalletIconMap.WalletIcon(
                    key = wallet.icon,
                    walletType = wallet.type,
                    contentDescription = null,
                    modifier = Modifier
                        .size(WALLET_ICON_BOX_SIZE)
                        .alpha(if (isArchived) 0.46f else 1f)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = wallet.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
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
                } else if (wallet.isDefault) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(OceanBlue50)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Mặc định",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue600
                        )
                    }
                }
            }

            if (isArchived) {
                TextButton(
                    onClick = onUnarchive,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = OceanBlue600)
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
                    fontSize = 15.sp,
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
    return formatMoneyDisplay(amount)
}

private fun List<Wallet>.defaultWalletFirst(): List<Wallet> {
    return mapIndexed { index, wallet -> index to wallet }
        .sortedWith(
            compareByDescending<Pair<Int, Wallet>> { it.second.isDefault }
                .thenBy { it.first }
        )
        .map { it.second }
}
