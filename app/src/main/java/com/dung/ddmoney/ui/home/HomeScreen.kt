package com.dung.ddmoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.home.components.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.dung.ddmoney.ui.theme.*
import java.text.Normalizer
import java.util.Locale

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
    var showTransactionSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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
                    onToggle      = { isBalanceVisible = !isBalanceVisible },
                    onSearchClick = { showTransactionSearch = true }
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

        if (showTransactionSearch) {
            ModalBottomSheet(
                onDismissRequest = {
                    showTransactionSearch = false
                    searchQuery = ""
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = LuminousSurfaceContainerLowest,
                dragHandle = null
            ) {
                HomeTransactionSearchSheet(
                    transactions = transactions,
                    query = searchQuery,
                    isVisible = isBalanceVisible,
                    onQueryChange = { searchQuery = it },
                    onDismiss = {
                        showTransactionSearch = false
                        searchQuery = ""
                    },
                    onTransactionClick = { transaction ->
                        showTransactionSearch = false
                        searchQuery = ""
                        selectedTransaction = transaction
                    }
                )
            }
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

@Composable
private fun HomeTransactionSearchSheet(
    transactions: List<Transaction>,
    query: String,
    isVisible: Boolean,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val searchResults = remember(transactions, query) {
        if (query.isBlank()) emptyList() else transactions.filterByHomeSearch(query)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.95f)
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tìm kiếm giao dịch",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Black,
                color = LuminousOnSurface
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Đóng tìm kiếm",
                    tint = NeutralGray600
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = OceanBlue600
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Xóa từ khóa",
                            tint = NeutralGray600
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = "Tên danh mục hoặc ghi chú",
                    color = NeutralGray600
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() }
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LuminousSurfaceContainerLow,
                unfocusedContainerColor = LuminousSurfaceContainerLow,
                disabledContainerColor = LuminousSurfaceContainerLow,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        when {
            query.isBlank() -> {
                EmptyTransactions(
                    message = "Nhập từ khóa để tìm kiếm",
                    helperText = "Tìm theo tên danh mục hoặc ghi chú giao dịch"
                )
            }

            searchResults.isEmpty() -> {
                EmptyTransactions(
                    message = "Không tìm thấy giao dịch",
                    helperText = "Thử tên danh mục hoặc ghi chú khác"
                )
            }

            else -> {
                Text(
                    text = "${searchResults.size} giao dịch phù hợp",
                    fontWeight = FontWeight.Bold,
                    color = NeutralGray600
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    color = HomeFrameSurface,
                    border = BorderStroke(1.dp, HomeFrameBorder.copy(alpha = 0.55f))
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(
                            items = searchResults,
                            key = { _, transaction -> transaction.id }
                        ) { index, transaction ->
                            TransactionPill(
                                transaction = transaction,
                                isVisible = isVisible,
                                isLast = index == searchResults.lastIndex,
                                onClick = { onTransactionClick(transaction) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun List<Transaction>.filterByHomeSearch(query: String): List<Transaction> {
    val normalizedQuery = normalizeHomeSearchText(query)
    if (normalizedQuery.isBlank()) return this

    return filter { transaction ->
        normalizeHomeSearchText(transaction.categoryName).contains(normalizedQuery) ||
            normalizeHomeSearchText(transaction.note).contains(normalizedQuery)
    }
}

private fun normalizeHomeSearchText(value: String): String =
    Normalizer
        .normalize(value, Normalizer.Form.NFD)
        .replace(CombiningMarksRegex, "")
        .replace('đ', 'd')
        .replace('Đ', 'D')
        .lowercase(Locale.ROOT)
        .trim()

private val CombiningMarksRegex = "\\p{Mn}+".toRegex()

private val HomeBackgroundBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to HomeBackgroundTop,
        0.36f to HomeBackgroundMid,
        1.0f to HomeBackgroundBottom
    )
)
