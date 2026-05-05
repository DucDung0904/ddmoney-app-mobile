package com.dung.ddmoney.ui.wallets

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.dung.ddmoney.AppState
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import com.dung.ddmoney.ui.transactions.walletTypeIcon
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    appState: AppState,
    onTransfer: (fromId: String, toId: String, amount: Double, date: LocalDate, note: String) -> Unit,
    onBack: () -> Unit
) {
    var fromWalletId by remember { mutableStateOf(appState.wallets.getOrNull(0)?.id) }
    var toWalletId by remember { mutableStateOf(appState.wallets.getOrNull(1)?.id) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val fromWallet = appState.wallets.find { it.id == fromWalletId }
    val toWallet = appState.wallets.find { it.id == toWalletId }
    val amount = amountText.toLongOrNull()?.toDouble() ?: 0.0
    val isValid = fromWalletId != null && toWalletId != null
            && fromWalletId != toWalletId && amount > 0
            && (fromWallet?.balance ?: 0.0) >= amount

    // Animated arrow rotation
    val arrowRotation by animateFloatAsState(
        targetValue = if (fromWalletId != null && toWalletId != null) 0f else 0f,
        animationSpec = tween(300),
        label = "arrow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
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
                "Chuyển tiền",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Transfer flow visualization ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // From wallet
                        WalletSelector(
                            label = "Từ ví",
                            wallet = fromWallet,
                            onClick = { showFromPicker = true },
                            modifier = Modifier.weight(1f)
                        )

                        // Arrow + swap button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .rotate(arrowRotation)
                            )
                            Spacer(Modifier.height(8.dp))
                            // Swap button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable {
                                        val tmp = fromWalletId
                                        fromWalletId = toWalletId
                                        toWalletId = tmp
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // To wallet
                        WalletSelector(
                            label = "Đến ví",
                            wallet = toWallet,
                            onClick = { showToPicker = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Error: same wallet
                    AnimatedVisibility(visible = fromWalletId == toWalletId && fromWalletId != null) {
                        Text(
                            "⚠️ Ví nguồn và đích không được trùng nhau",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Amount input ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Số tiền chuyển",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))

                    // Amount display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = if (amountText.isEmpty()) "0" else "%,d".format(
                                amountText.toLongOrNull() ?: 0
                            ).replace(',', '.'),
                            color = if (amountText.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "VND",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }

                    // Insufficient balance warning
                    val fromBalance = fromWallet?.balance ?: 0.0
                    AnimatedVisibility(visible = amount > 0 && amount > fromBalance) {
                        Text(
                            "⚠️ Số dư không đủ (còn ${formatVnd(fromBalance)})",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    // Number Pad
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("000", "0", "⌫"),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                row.forEach { key ->
                                    val isDelete = key == "⌫"
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isDelete) MaterialTheme.colorScheme.secondary.copy(
                                                    0.1f
                                                ) else MaterialTheme.colorScheme.background
                                            )
                                            .clickable {
                                                if (isDelete) {
                                                    amountText =
                                                        if (amountText.length <= 1) "" else amountText.dropLast(
                                                            1
                                                        )
                                                } else {
                                                    if (amountText.replace(",", "").length < 12) {
                                                        amountText =
                                                            if (amountText == "0") key else amountText + key
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDelete) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Backspace,
                                                null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Text(
                                                key,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Date + Note ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Ngày chuyển",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text(
                    text = if (selectedDate == LocalDate.now()) "Hôm nay" else selectedDate.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = {
                    Text(
                        "Ghi chú (tuỳ chọn)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 2,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )

            // ── Transfer Summary (when valid) ────────────────────────
            AnimatedVisibility(visible = isValid) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Xác nhận chuyển",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${fromWallet?.name} → ${toWallet?.name}",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            formatVnd(amount),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Confirm button ───────────────────────────────────────
            Button(
                onClick = {
                    onTransfer(fromWalletId!!, toWalletId!!, amount, selectedDate, note)
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "Xác nhận chuyển tiền",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Date Picker ──────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 86400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = LocalDate.ofEpochDay(it / 86400_000L)
                    }
                    showDatePicker = false
                }) {
                    Text(
                        "Chọn",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        "Huỷ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            DatePicker(
                state = datePickerState, colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    dayContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                    todayDateBorderColor = MaterialTheme.colorScheme.secondary,
                    todayContentColor = MaterialTheme.colorScheme.secondary,
                    navigationContentColor = MaterialTheme.colorScheme.onSurface,
                    yearContentColor = MaterialTheme.colorScheme.onSurface,
                    currentYearContentColor = MaterialTheme.colorScheme.secondary,
                    selectedYearContainerColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }

    // ── From wallet picker ───────────────────────────────────────────
    if (showFromPicker) {
        WalletPickerDialog(
            wallets = appState.wallets,
            selectedId = fromWalletId,
            title = "Chọn ví nguồn",
            onSelect = { fromWalletId = it; showFromPicker = false },
            onDismiss = { showFromPicker = false }
        )
    }

    // ── To wallet picker ─────────────────────────────────────────────
    if (showToPicker) {
        WalletPickerDialog(
            wallets = appState.wallets,
            selectedId = toWalletId,
            title = "Chọn ví đích",
            onSelect = { toWalletId = it; showToPicker = false },
            onDismiss = { showToPicker = false }
        )
    }
}

// ─── Wallet selector box ──────────────────────────────────────────────
@Composable
private fun WalletSelector(
    label: String,
    wallet: Wallet?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(12.dp))
        if (wallet != null) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(wallet.color.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(walletTypeIcon(wallet.type), fontSize = 24.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                wallet.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                formatVnd(wallet.balance),
                color = wallet.color,
                style = MaterialTheme.typography.labelSmall
            )
        } else {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Chọn ví",
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

// ─── Wallet picker dialog ─────────────────────────────────────────────
@Composable
private fun WalletPickerDialog(
    wallets: List<Wallet>,
    selectedId: String?,
    title: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                wallets.forEach { wallet ->
                    val isSelected = wallet.id == selectedId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) wallet.color.copy(0.15f) else MaterialTheme.colorScheme.surfaceContainerLowest)
                            .border(
                                1.dp,
                                if (isSelected) wallet.color else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelect(wallet.id) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(wallet.color.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(walletTypeIcon(wallet.type), fontSize = 24.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                wallet.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                formatVnd(wallet.balance),
                                color = wallet.color,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = wallet.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Huỷ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
