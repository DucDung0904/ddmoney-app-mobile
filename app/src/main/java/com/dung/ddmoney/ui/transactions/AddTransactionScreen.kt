package com.dung.ddmoney.ui.transactions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.dung.ddmoney.AppState
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Available emoji icons to pick ───────────────────────────────────
val EMOJI_OPTIONS = listOf(
    "🍜", "🍔", "☕", "🍕", "🥗", "🛒", "🍣", "🥤",
    "🚗", "🛵", "🚌", "✈️", "🚀", "🚲", "🛞", "⛽",
    "🛍️", "👗", "👟", "💍", "🎁", "🧴", "📱", "💻",
    "💊", "🏥", "💪", "🧘", "🏃", "🩺", "🦷", "🏋️",
    "🎮", "🎬", "🎵", "🎯", "🎰", "🎲", "📺", "🎤",
    "📚", "✏️", "🏫", "🔬", "📖", "🖊️", "🎓", "📝",
    "💡", "💧", "🔥", "🏠", "📞", "🌐", "🔧", "🔑",
    "💰", "💵", "💳", "📈", "🏦", "🤝", "🎉", "✨",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    initialType: String = "EXPENSE",
    appState: AppState,
    onSave: (title: String, categoryId: String, amount: Double, type: TransactionType, walletId: String, date: LocalDate, note: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember {
        mutableStateOf(
            when (initialType) {
                "INCOME" -> TransactionType.INCOME
                "DEBT" -> TransactionType.DEBT
                else -> TransactionType.EXPENSE
            }
        )
    }
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedWalletId by remember { mutableStateOf(appState.wallets.firstOrNull()?.id) }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showWalletSheet by remember { mutableStateOf(false) }
    var isNoteFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Categories for current type
    val relevantCategories = remember(selectedType, appState.categories) {
        when (selectedType) {
            TransactionType.INCOME -> appState.incomeCategories()
            TransactionType.DEBT   -> appState.debtCategories()
            else                   -> appState.expenseCategories()
        }
    }
    LaunchedEffect(selectedType, relevantCategories) {
        if (selectedCategoryId == null || relevantCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = relevantCategories.firstOrNull()?.id
        }
    }

    val amount = evaluateExpression(amountText)
    val isValid = amount > 0 && selectedCategoryId != null && selectedWalletId != null
    val hasOperator = amountText.contains('+') || amountText.contains('-')

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Đóng", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Thêm giao dịch",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ── Amount Input ─────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SỐ TIỀN", color = Color.Gray, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            val displayText = if (amountText.isEmpty()) "0" else formatAmountDisplay(amountText)
            val amountFontSize = when {
                displayText.length <= 8 -> 56.sp
                displayText.length <= 11 -> 48.sp
                displayText.length <= 14 -> 40.sp
                displayText.length <= 17 -> 32.sp
                else -> 28.sp
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = displayText,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = amountFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = " đ",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Ghi chú dưới số tiền
            TextField(
                value = note,
                onValueChange = { note = it },
                placeholder = {
                    Text(
                        text = "Thêm ghi chú...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { 
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .onFocusChanged { focusState -> 
                        isNoteFocused = focusState.isFocused 
                    }
            )
        }

        // ── Category Picker (LazyRow) ───────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(relevantCategories) { cat ->
                val isSelected = selectedCategoryId == cat.id
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(24.dp)
                        )
                        .clickable { selectedCategoryId = cat.id }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(cat.icon, fontSize = 16.sp)
                    Text(
                        text = cat.name,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

        // ── Wallet and Date row ───────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date card
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { showDatePicker = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("NGÀY", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(formatDateDisplay(selectedDate), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Wallet card
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { showWalletSheet = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("TÀI KHOẢN", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    val walletName = appState.wallets.find { it.id == selectedWalletId }?.name ?: "Tiền mặt"
                    Text(walletName, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        val isImeVisible = WindowInsets.isImeVisible

        // ── Number Pad ───────────────────────────────────────────
        AnimatedVisibility(
            visible = !isImeVisible,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            CustomNumberPad(
                onDigit = { digit ->
                    val currentDigits = amountText.count { it.isDigit() }
                    if (currentDigits + digit.length <= 13) {
                        if (amountText == "0" || amountText.isEmpty()) {
                            amountText = if (digit == "000" || digit == "0") "0" else digit
                        } else {
                            amountText += digit
                        }
                    }
                },
                onOperator = { op ->
                    if (amountText.isNotEmpty()) {
                        val lastChar = amountText.last()
                        if (lastChar == '+' || lastChar == '-') {
                            amountText = amountText.dropLast(1) + op
                        } else {
                            amountText += op
                        }
                    }
                },
                onDelete = {
                    amountText = if (amountText.length <= 1) "" else amountText.dropLast(1)
                },
                onSave = {
                    val cat = appState.categories.find { it.id == selectedCategoryId } ?: return@CustomNumberPad
                    onSave(
                        note.ifBlank { cat.name },
                        selectedCategoryId!!,
                        amount,
                        selectedType,
                        selectedWalletId!!,
                        selectedDate,
                        note
                    )
                },
                onEvaluate = {
                    val result = evaluateExpression(amountText).toLong()
                    amountText = if (result > 0) result.toString() else "0"
                },
                isValid = isValid,
                hasOperator = hasOperator
            )
        }
    }

    // Date Picker Dialog
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
                    Text("Chọn", color = Color(0xFF1A237E), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Huỷ", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Wallet Bottom Sheet
    if (showWalletSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showWalletSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text("Chọn tài khoản", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                LazyColumn {
                    items(appState.wallets) { wallet ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedWalletId = wallet.id; showWalletSheet = false }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(walletTypeIcon(wallet.type), fontSize = 24.sp)
                            Spacer(Modifier.width(16.dp))
                            Text(wallet.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                            if (selectedWalletId == wallet.id) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Number Pad ───────────────────────────────────────────────────────
@Composable
private fun CustomNumberPad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onEvaluate: () -> Unit,
    isValid: Boolean,
    hasOperator: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = 8.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Row 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            PadButton("1", Modifier.weight(1f), isAction = false, onClick = { onDigit("1") })
            PadButton("2", Modifier.weight(1f), isAction = false, onClick = { onDigit("2") })
            PadButton("3", Modifier.weight(1f), isAction = false, onClick = { onDigit("3") })
            PadButton("⌫", Modifier.weight(1f), isAction = true, onClick = onDelete)
        }
        // Row 2
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            PadButton("4", Modifier.weight(1f), isAction = false, onClick = { onDigit("4") })
            PadButton("5", Modifier.weight(1f), isAction = false, onClick = { onDigit("5") })
            PadButton("6", Modifier.weight(1f), isAction = false, onClick = { onDigit("6") })
            PadButton("+", Modifier.weight(1f), isAction = true, onClick = { onOperator("+") })
        }
        // Row 3
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            PadButton("7", Modifier.weight(1f), isAction = false, onClick = { onDigit("7") })
            PadButton("8", Modifier.weight(1f), isAction = false, onClick = { onDigit("8") })
            PadButton("9", Modifier.weight(1f), isAction = false, onClick = { onDigit("9") })
            PadButton("-", Modifier.weight(1f), isAction = true, onClick = { onOperator("-") })
        }
        // Row 4
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
            PadButton("000", Modifier.weight(1f), isAction = false, onClick = { onDigit("000") })
            PadButton("0", Modifier.weight(1f), isAction = false, onClick = { onDigit("0") })
            
            // LƯU / = button
            val actionColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isValid || hasOperator) actionColor else actionColor.copy(alpha = 0.5f))
                    .clickable(enabled = isValid || hasOperator) {
                        if (hasOperator) onEvaluate() else onSave()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasOperator) {
                        Text("=", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Lưu", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PadButton(
    text: String,
    modifier: Modifier = Modifier,
    isAction: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isAction) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val textColor = if (isAction) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "⌫") {
            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
        } else {
            Text(text, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────
fun walletTypeIcon(type: WalletType): String = when (type) {
    WalletType.CASH -> "💵"
    WalletType.BANK -> "🏦"
    WalletType.EWALLET -> "📱"
    WalletType.CREDIT -> "💳"
}

private fun formatAmountDisplay(raw: String): String {
    if (raw.isEmpty()) return ""
    var result = ""
    var currentNumber = ""
    for (char in raw) {
        if (char == '+' || char == '-') {
            if (currentNumber.isNotEmpty()) {
                val num = currentNumber.toLongOrNull()
                result += if (num != null) "%,d".format(num).replace(',', '.') else currentNumber
                currentNumber = ""
            }
            result += " $char "
        } else {
            currentNumber += char
        }
    }
    if (currentNumber.isNotEmpty()) {
        val num = currentNumber.toLongOrNull()
        result += if (num != null) "%,d".format(num).replace(',', '.') else currentNumber
    }
    return result
}

private fun evaluateExpression(expr: String): Double {
    try {
        var currentNumber = ""
        var total = 0.0
        var currentOp = '+'
        
        for (char in expr) {
            if (char == '+' || char == '-') {
                if (currentNumber.isNotEmpty()) {
                    val num = currentNumber.toDoubleOrNull() ?: 0.0
                    if (currentOp == '+') total += num else total -= num
                }
                currentOp = char
                currentNumber = ""
            } else {
                currentNumber += char
            }
        }
        if (currentNumber.isNotEmpty()) {
            val num = currentNumber.toDoubleOrNull() ?: 0.0
            if (currentOp == '+') total += num else total -= num
        }
        return total
    } catch (e: Exception) {
        return 0.0
    }
}

private fun formatDateDisplay(date: LocalDate): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    return when (date) {
        today -> "Hôm nay"
        yesterday -> "Hôm qua"
        else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}
