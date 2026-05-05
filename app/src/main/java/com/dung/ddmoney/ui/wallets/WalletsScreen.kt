package com.dung.ddmoney.ui.wallets

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.dung.ddmoney.AppState
import com.dung.ddmoney.ui.categories.COLOR_OPTIONS
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import com.dung.ddmoney.ui.transactions.walletTypeIcon
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    appState: AppState,
    onAddWallet: (name: String, balance: Double, type: WalletType, bank: String, color: Color) -> Unit,
    onEditWallet: (Wallet) -> Unit,
    onDeleteWallet: (String) -> Unit,
    onTransfer: () -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingWallet by remember { mutableStateOf<Wallet?>(null) }
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 16.dp, bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    "Quản lý Ví",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                // Transfer button
                IconButton(onClick = onTransfer) {
                    Icon(Icons.Default.SwapHoriz, null, tint = MaterialTheme.colorScheme.secondary)
                }
                // Add wallet button
                IconButton(onClick = { showAddDialog = true }) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total balance summary
            item {
                TotalBalanceCard(totalBalance = appState.totalBalance)
            }

            // Wallet cards
            items(appState.wallets, key = { it.id }) { wallet ->
                WalletCard(
                    wallet = wallet,
                    onEdit = { editingWallet = wallet },
                    onDelete = { deleteConfirmId = wallet.id }
                )
            }

            // Transfer shortcut
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onTransfer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Chuyển tiền giữa ví",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Thêm ví mới",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // ── Add/Edit Wallet Dialog ───────────────────────────────────────
    if (showAddDialog || editingWallet != null) {
        WalletFormDialog(
            existing = editingWallet,
            onDismiss = { showAddDialog = false; editingWallet = null },
            onSave = { name, balance, type, bank, color ->
                if (editingWallet != null) {
                    onEditWallet(
                        editingWallet!!.copy(
                            name = name,
                            balance = balance,
                            type = type,
                            bank = bank,
                            color = color
                        )
                    )
                } else {
                    onAddWallet(name, balance, type, bank, color)
                }
                showAddDialog = false
                editingWallet = null
            }
        )
    }

    // ── Delete Confirm ───────────────────────────────────────────────
    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            title = {
                Text(
                    "Xóa ví?",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Ví này và tất cả dữ liệu liên quan sẽ bị xóa vĩnh viễn.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { onDeleteWallet(id); deleteConfirmId = null }) {
                    Text(
                        "Xóa",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) {
                    Text(
                        "Huỷ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

// ─── Total balance summary ────────────────────────────────────────────
@Composable
private fun TotalBalanceCard(totalBalance: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(32.dp)
    ) {
        Column {
            Text(
                "Tổng tài sản",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatVnd(totalBalance),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

// ─── Single wallet card ───────────────────────────────────────────────
@Composable
private fun WalletCard(wallet: Wallet, onEdit: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        wallet.color.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )
            )
            .border(1.dp, wallet.color.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(wallet.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(walletTypeIcon(wallet.type), fontSize = 26.sp)
                    }
                    Column {
                        Text(
                            wallet.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (wallet.bank.isNotBlank()) {
                            Text(
                                text = buildString {
                                    append(wallet.bank)
                                    if (wallet.cardNumber.isNotBlank()) append(" ${wallet.cardNumber}")
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = when (wallet.type) {
                                    WalletType.CASH -> "Tiền mặt"
                                    WalletType.BANK -> "Ngân hàng"
                                    WalletType.EWALLET -> "Ví điện tử"
                                    WalletType.CREDIT -> "Thẻ tín dụng"
                                },
                                color = wallet.color,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = formatVnd(wallet.balance),
                color = if (wallet.balance >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── Wallet form dialog ───────────────────────────────────────────────
@Composable
private fun WalletFormDialog(
    existing: Wallet?,
    onDismiss: () -> Unit,
    onSave: (name: String, balance: Double, type: WalletType, bank: String, color: Color) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var balanceText by remember { mutableStateOf(existing?.balance?.toLong()?.toString() ?: "") }
    var selectedType by remember { mutableStateOf(existing?.type ?: WalletType.CASH) }
    var bank by remember { mutableStateOf(existing?.bank ?: "") }
    var selectedColor by remember { mutableStateOf(existing?.color ?: LuminousPrimary) }

    val walletTypes = listOf(
        WalletType.CASH to ("Tiền mặt" to "💵"),
        WalletType.BANK to ("Ngân hàng" to "🏦"),
        WalletType.EWALLET to ("Ví điện tử" to "📱"),
        WalletType.CREDIT to ("Thẻ tín dụng" to "💳"),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                if (existing != null) "Chỉnh sửa ví" else "Thêm ví mới",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên ví", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )

                // Balance
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) balanceText = it },
                    label = {
                        Text(
                            "Số dư ban đầu (VND)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )

                // Wallet type
                Text(
                    "Loại ví",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(walletTypes) { (type, info) ->
                        val (label, icon) = info
                        val isSelected = selectedType == type
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) selectedColor.copy(0.15f) else MaterialTheme.colorScheme.surfaceContainerLow)
                                .clickable { selectedType = type }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(icon, fontSize = 24.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                label,
                                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Bank name (optional)
                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it },
                    label = {
                        Text(
                            "Tên ngân hàng / dịch vụ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )

                // Color picker
                Text(
                    "Màu thẻ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(COLOR_OPTIONS) { color ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    3.dp,
                                    if (selectedColor == color) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    onSave(name, balance, selectedType, bank, selectedColor)
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Lưu",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        dismissButton = {}
    )
}

// ─── Helper ───────────────────────────────────────────────────────────
fun formatVnd(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    return "${nf.format(amount)} đ"
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
)
