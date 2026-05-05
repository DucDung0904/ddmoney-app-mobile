package com.dung.ddmoney.ui.dashboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.dashboard.model.SampleData
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.theme.*

// ─── Wallet Carousel ──────────────────────────────────────────────────
@Composable
fun WalletSection(
    wallets: List<Wallet>,
    onManageWallets: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ví của tôi",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onManageWallets) {
                Text(
                    text = "Quản lý",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(wallets) { wallet ->
                WalletCard(wallet = wallet)
            }
        }
    }
}

// ─── Single Wallet Card ───────────────────────────────────────────────
@Composable
fun WalletCard(
    wallet: Wallet,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wallet_${wallet.name}")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wallet_shimmer"
    )

    val gradient = Brush.linearGradient(
        colors = listOf(
            wallet.color.copy(alpha = 0.15f),
            wallet.color.copy(alpha = 0.05f),
            MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        start = Offset(shimmerOffset * 0.3f, 0f),
        end = Offset(shimmerOffset, 200f)
    )

    Box(
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.7f))
            .padding(20.dp)
    ) {
        // Accent border top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            wallet.color.copy(alpha = 0.0f),
                            wallet.color,
                            wallet.color.copy(alpha = 0.0f)
                        )
                    )
                )
        )

        Column {
            // Bank logo placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wallet.bank,
                    color = wallet.color,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(wallet.color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "💳", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = wallet.name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(wallet.balance),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = wallet.cardNumber,
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF7F9FF)
@Composable
private fun WalletSectionPreview() {
    DDMoneyTheme(darkTheme = false) {
        WalletSection(
            wallets = SampleData.defaultWallets,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
