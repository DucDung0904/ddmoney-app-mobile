package com.dung.ddmoney.ui.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.theme.*

// ─── Dimensions ──────────────────────────────────────────────────────────────
private val CARD_WIDTH  = 200.dp
private val CARD_HEIGHT = 120.dp

// ─── Per-card colour palette ──────────────────────────────────────────────────
private data class WalletTheme(
    val gradStart: Color,
    val gradEnd:   Color,
    val chipColor: Color
)

private val walletThemes = listOf(
    WalletTheme(Color(0xFF0C447C), Color(0xFF378ADD), Color(0xFFB5D4F4)),
    WalletTheme(Color(0xFF0F5C43), Color(0xFF3DBFA0), Color(0xFFA6E4D0)),
    WalletTheme(Color(0xFF8A5606), Color(0xFFF3BC56), Color(0xFFFCDDA0)),
    WalletTheme(Color(0xFF8E1F1F), Color(0xFFEB7070), Color(0xFFF5BABA)),
)

// ─────────────────────────────────────────────────────────────────────────────
//  WalletCardsSection
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun WalletCardsSection(
    wallets:   List<Wallet>,
    isVisible: Boolean,
    onSeeAll:  () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ─── Header moved OUTSIDE the frame ──────────────────────────────
        // Padding matches standard screen margins (24dp)
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            WalletSectionHeader(wallets = wallets, onSeeAll = onSeeAll)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─── The Rounded Frame (now smaller, only contains cards) ────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape    = RoundedCornerShape(28.dp),
            color    = LuminousSurfaceContainerLowest,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp) // Smaller vertical padding
            ) {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()

                Box(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        state          = listState,
                        modifier       = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (wallets.isEmpty()) {
                            item { EmptyWalletCard() }
                        } else {
                            items(wallets.size) { index ->
                                WalletCard(wallets[index], isVisible, index)
                            }
                            item { AddWalletGhostCard() }
                        }
                    }

                    // ─── Fade overlays (Left & Right) ─────────────────────────
                    // Left fade
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxHeight()
                            .width(40.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(LuminousSurfaceContainerLowest, Color.Transparent)
                                )
                            )
                    )
                    // Right fade
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(40.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, LuminousSurfaceContainerLowest)
                                )
                            )
                    )
                }

                // Dot indicators
                if (wallets.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyScrollDotIndicator(count = wallets.size, listState = listState)
                }
            }
        }
    }
}

@Composable
private fun LazyScrollDotIndicator(
    count: Int,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val activeIndex = listState.firstVisibleItemIndex

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val isActive = i == activeIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(6.dp)
                    .width(if (isActive) 18.dp else 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isActive) OceanBlue600 else NeutralGray100)
            )
        }
    }
}

// ─── Section header ───────────────────────────────────────────────────────────
@Composable
private fun WalletSectionHeader(wallets: List<Wallet>, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector    = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint     = OceanBlue600
            )
            Text(
                "Ví của tôi",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Black,
                color      = LuminousOnSurface
            )
            if (wallets.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(OceanBlue50)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text       = "${wallets.size}",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Black,
                        color      = OceanBlue600
                    )
                }
            }
        }
        Text(
            "Tất cả",
            fontSize   = 17.sp,
            fontWeight = FontWeight.Bold,
            color      = OceanBlue600,
            modifier   = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onSeeAll() }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ─── Individual wallet card ───────────────────────────────────────────────────
@Composable
private fun WalletCard(wallet: Wallet, isVisible: Boolean, index: Int) {
    val theme = walletThemes[index % walletThemes.size]

    Box(
        modifier = Modifier
            .width(CARD_WIDTH)
            .height(CARD_HEIGHT)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(theme.gradStart, theme.gradEnd),
                    start  = Offset(0f, 0f),
                    end    = Offset(700f, 700f)
                )
            )
            .clickable { }
    ) {
        // Depth circles
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 16.dp, y = (-16).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // EMV chip (3 bars)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(theme.chipColor)
                    )
                }
            }

            // Wallet name + balance
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text          = wallet.name.uppercase(),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text       = if (isVisible)
                        "${String.format("%,.0f", wallet.balance)} đ"
                    else
                        "•••••• đ",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Black,
                    color      = Color.White
                )
            }
        }
    }
}

// ─── "Add wallet" ghost card ─────────────────────────────────────────────────
@Composable
private fun AddWalletGhostCard() {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(CARD_HEIGHT)
            .clip(RoundedCornerShape(24.dp))
            .background(LuminousSurfaceContainerLow)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment  = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(OceanBlue50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector    = Icons.Outlined.Add,
                    contentDescription = "Thêm ví",
                    tint     = OceanBlue600,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text("Thêm", fontSize = 12.sp, color = OceanBlue600, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Empty state card ─────────────────────────────────────────────────────────
@Composable
private fun EmptyWalletCard() {
    Box(
        modifier = Modifier
            .width(CARD_WIDTH)
            .height(CARD_HEIGHT)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFE6EEF8), Color(0xFFCDD9EC))))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector    = Icons.Outlined.Add,
                    contentDescription = null,
                    tint     = OceanBlue600,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text("Thêm ví mới", fontSize = 13.sp, color = OceanBlue800, fontWeight = FontWeight.Bold)
        }
    }
}

