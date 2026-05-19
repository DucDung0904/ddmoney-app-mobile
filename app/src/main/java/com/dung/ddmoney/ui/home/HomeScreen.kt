package com.dung.ddmoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.home.components.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.dung.ddmoney.ui.theme.*

/**
 * HomeScreen — entry point.
 * Layout is assembled here; all UI blocks live in ui/home/components/.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "Người dùng",
    totalBalance: Double = 0.0,
    wallets: List<Wallet> = emptyList(),
    categories: List<Category> = emptyList(),
    transactions: List<Transaction> = emptyList(),
    recentTransactions: List<Transaction> = emptyList(),
    onSeeAllWallets: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    onAddWallet: () -> Unit = {},
    onViewReport: () -> Unit = {}
) {
    var isBalanceVisible by remember { mutableStateOf(true) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackgroundBrush)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            // ── 1. Hero: gradient header + search bar + balance card ──────────
            item {
                HomeHeroSection(
                    name          = userName,
                    balance       = totalBalance,
                    isVisible     = isBalanceVisible,
                    onToggle      = { isBalanceVisible = !isBalanceVisible }
                )
            }

            // ── 2. Wallet cards (horizontal scroll) ──────────────────────────
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                WalletCardsSection(
                    wallets     = wallets,
                    isVisible   = isBalanceVisible,
                    onSeeAll    = onSeeAllWallets,
                    onAddWallet = onAddWallet
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ── 3. Spending Report ──────────────────────────────────────────
            item {
                HomeReportSection(
                    transactions = transactions,
                    categories = categories,
                    onViewReport = onViewReport
                )
            }

            // ── 4. Recent transactions header + list ─────────────────────────
            item {
                TransactionSectionHeader(onSeeAll = onSeeAllTransactions)
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape    = RoundedCornerShape(24.dp),
                    color    = HomeFrameSurface,
                    border   = BorderStroke(1.dp, HomeFrameBorder.copy(alpha = 0.55f)),
                    shadowElevation = 6.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (recentTransactions.isEmpty()) {
                            EmptyTransactions()
                        } else {
                            recentTransactions.forEachIndexed { index, tx ->
                                TransactionPill(
                                    transaction = tx,
                                    isVisible   = isBalanceVisible,
                                    isLast      = index == recentTransactions.lastIndex,
                                    onClick     = { selectedTransaction = tx }
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        selectedTransaction?.let { transaction ->
            ModalBottomSheet(
                onDismissRequest = { selectedTransaction = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = LuminousSurfaceContainerLowest
            ) {
                HomeTransactionDetailSheet(
                    transaction = transaction,
                    isVisible = isBalanceVisible,
                    onDismiss = { selectedTransaction = null }
                )
            }
        }
    }
}

private val HomeBackgroundBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to HomeBackgroundTop,
        0.36f to HomeBackgroundMid,
        1.0f to HomeBackgroundBottom
    )
)
