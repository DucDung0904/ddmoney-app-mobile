package com.dung.ddmoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.home.components.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.dung.ddmoney.ui.theme.*

/**
 * HomeScreen — entry point.
 * Layout is assembled here; all UI blocks live in ui/home/components/.
 */
@Composable
fun HomeScreen(
    userName: String = "Người dùng",
    totalBalance: Double = 0.0,
    wallets: List<Wallet> = emptyList(),
    categories: List<Category> = emptyList(),
    transactions: List<Transaction> = emptyList(),
    recentTransactions: List<Transaction> = emptyList(),
    onSeeAllWallets: () -> Unit = {},
    onAddWallet: ((Wallet) -> Unit)? = null
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
                HomeReportSection(transactions = transactions, categories = categories)
            }

            // ── 4. Recent transactions header + list ─────────────────────────
            item {
                TransactionSectionHeader(onSeeAll = {})
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape    = RoundedCornerShape(24.dp),
                    color    = LuminousSurfaceContainerLowest,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (recentTransactions.isEmpty()) {
                            EmptyTransactions()
                        } else {
                            recentTransactions.forEachIndexed { index, tx ->
                                TransactionPill(
                                    transaction = tx,
                                    isVisible   = isBalanceVisible,
                                    isLast      = index == recentTransactions.lastIndex
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
