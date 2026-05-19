package com.dung.ddmoney.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.theme.*
import com.dung.ddmoney.ui.wallets.WalletIconMap
import com.dung.ddmoney.ui.components.formatMoneyDisplay

// ─── Dimensions ───────────────────────────────────────────────────────────────
private val WALLET_PANEL_SHAPE = RoundedCornerShape(24.dp)
private val WALLET_ICON_SHAPE = RoundedCornerShape(14.dp)
private val WALLET_ICON_BOX_SIZE = 38.dp
private val WALLET_ICON_SIZE = 23.dp
private val WALLET_ROW_HEIGHT = 56.dp

// ─── Display-ordering logic ───────────────────────────────────────────────────
/**
 * Returns at most 3 wallets to show on Home:
 *   slot 0 = default wallet (isDefault == true), or first if none marked
 *   slot 1,2 = common daily wallets first, then by balance
 */
internal fun selectDisplayWallets(wallets: List<Wallet>): List<Wallet> {
    if (wallets.isEmpty()) return emptyList()
    val default = wallets.firstOrNull { it.isDefault } ?: wallets.first()
    val others  = wallets.filter { it.id != default.id }
        .sortedWith(
            compareByDescending<Wallet> { it.type.isQuickAccessDefault }
                .thenByDescending { it.balance }
        )
        .take(2)
    return listOf(default) + others
}

// ─────────────────────────────────────────────────────────────────────────────
//  WalletCardsSection
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun WalletCardsSection(
    wallets:      List<Wallet>,
    isVisible:    Boolean,
    onSeeAll:     () -> Unit,
    onAddWallet:  () -> Unit
) {
    val displayWallets = selectDisplayWallets(wallets)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = WALLET_PANEL_SHAPE,
        color = LuminousSurfaceContainerLowest,
        border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.32f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            WalletSectionHeader(
                walletCount = wallets.size,
                onSeeAll = onSeeAll,
                onAdd = onAddWallet
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = LuminousOutlineVariant.copy(alpha = 0.48f),
                thickness = 0.7.dp
            )

            if (displayWallets.isEmpty()) {
                EmptyWalletRow(onClick = onAddWallet)
            } else {
                displayWallets.forEachIndexed { index, wallet ->
                    WalletSummaryRow(
                        wallet = wallet,
                        isVisible = isVisible,
                        isLast = index == displayWallets.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletSectionHeader(
    walletCount: Int,
    onSeeAll: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 10.dp, top = 14.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ví của tôi",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LuminousOnSurface
            )
            if (walletCount > 1) {
                Text(
                    text = "$walletCount ví",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuminousOnSurfaceVariant
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LuminousSurfaceContainerLow)
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Thêm ví",
                    tint = OceanBlue600,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Tất cả",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OceanBlue600,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSeeAll() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun WalletSummaryRow(
    wallet: Wallet,
    isVisible: Boolean,
    isLast: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = WALLET_ROW_HEIGHT)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(WALLET_ICON_BOX_SIZE)
                    .clip(WALLET_ICON_SHAPE)
                    .background(WalletIconMap.backgroundFor(wallet.type)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = WalletIconMap.toVector(wallet.icon, wallet.type),
                    contentDescription = null,
                    tint = WalletIconMap.tintFor(wallet.type),
                    modifier = Modifier.size(WALLET_ICON_SIZE)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = wallet.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,

                    )

                    if (wallet.isDefault) {
                        Text(
                            text = "Mặc định",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue600,
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = if (isVisible) formatWalletAmount(wallet.balance) else "•••••• đ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = LuminousOnSurface,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 68.dp, end = 18.dp),
                color = LuminousOutlineVariant.copy(alpha = 0.32f),
                thickness = 0.6.dp
            )
        }
    }
}

@Composable
private fun EmptyWalletRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = WALLET_ROW_HEIGHT)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(WALLET_ICON_BOX_SIZE)
                .clip(WALLET_ICON_SHAPE)
                ,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = OceanBlue600,
                modifier = Modifier.size(WALLET_ICON_SIZE)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Thêm ví đầu tiên",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LuminousOnSurfaceVariant
        )
    }
}

private fun formatWalletAmount(amount: Double): String {
    return formatMoneyDisplay(amount)
}
