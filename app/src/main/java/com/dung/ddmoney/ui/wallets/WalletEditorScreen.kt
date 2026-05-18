package com.dung.ddmoney.ui.wallets

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.network.dto.WalletRequest
import com.dung.ddmoney.ui.components.AppMoneyNumberPadSheet
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.dashboard.model.WalletType
import com.dung.ddmoney.ui.theme.*
import com.dung.ddmoney.ui.components.formatMoneyAmount
import com.dung.ddmoney.ui.components.formatMoneyDisplay
import com.dung.ddmoney.ui.components.formatMoneyInput
import com.dung.ddmoney.ui.components.parseMoneyInput
import com.dung.ddmoney.ui.components.parseNullableMoneyInput
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletEditorScreen(
    wallet: Wallet?,
    wallets: List<Wallet> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (WalletRequest) -> Unit,
    onArchive: (Wallet) -> Unit = {},
    onTransfer: (Wallet, Wallet, Double) -> Unit = { _, _, _ -> }
) {
    val isEdit = wallet != null
    var name by remember(wallet?.id) { mutableStateOf(wallet?.name.orEmpty()) }
    var balanceText by remember(wallet?.id) { mutableStateOf(wallet?.balance?.toAmountInput().orEmpty()) }
    var type by remember(wallet?.id) { mutableStateOf(wallet?.type ?: WalletType.CASH) }
    var bankName by remember(wallet?.id) { mutableStateOf(wallet?.bank.orEmpty()) }
    var cardNumber by remember(wallet?.id) { mutableStateOf(wallet?.cardNumber.orEmpty()) }
    var currency by remember(wallet?.id) { mutableStateOf(wallet?.currency ?: "VND") }
    var isDefault by remember(wallet?.id) { mutableStateOf(wallet?.isDefault ?: false) }
    var isIncludedInTotal by remember(wallet?.id) {
        mutableStateOf(if (wallet?.isDefault == true) true else wallet?.isIncludedInTotal ?: true)
    }
    var creditLimitText by remember(wallet?.id) { mutableStateOf(wallet?.creditLimit?.toAmountInput().orEmpty()) }
    var currentDebtText by remember(wallet?.id) { mutableStateOf(wallet?.currentDebt?.toAmountInput().orEmpty()) }
    var billingDayText by remember(wallet?.id) { mutableStateOf(wallet?.billingDay?.toString().orEmpty()) }
    var paymentDueDayText by remember(wallet?.id) { mutableStateOf(wallet?.paymentDueDay?.toString().orEmpty()) }
    var targetAmountText by remember(wallet?.id) { mutableStateOf(wallet?.targetAmount?.toAmountInput().orEmpty()) }
    var targetDate by remember(wallet?.id) { mutableStateOf(wallet?.targetDate) }
    var showTargetDatePicker by remember(wallet?.id) { mutableStateOf(false) }
    var selectedAddKind by remember(wallet?.id) {
        mutableStateOf(if (wallet == null) null else WalletAddKind.fromWalletType(wallet.type))
    }
    var colorHex by remember(wallet?.id) {
        mutableStateOf(wallet?.colorHex ?: WalletIconMap.colorHexFor(type))
    }
    var iconKey by remember(wallet?.id) {
        val savedIcon = wallet?.icon?.takeIf {
            it.isNotBlank() && it != WalletIconMap.DEFAULT_KEY
        }
        mutableStateOf(savedIcon ?: WalletIconMap.defaultKeyFor(type))
    }
    var showIconPicker by remember(wallet?.id) { mutableStateOf(false) }
    var transferAmountText by remember(wallet?.id) { mutableStateOf("") }
    var selectedTransferWalletId by remember(wallet?.id, wallets) {
        mutableStateOf(wallets.firstOrNull { it.id != wallet?.id && !it.isArchived }?.id)
    }
    var editMode by remember(wallet?.id) { mutableStateOf(WalletEditMode.Overview) }
    val activeType = if (isEdit || selectedAddKind == WalletAddKind.Basic) {
        type
    } else {
        selectedAddKind?.walletType ?: type
    }
    val canSubmit = if (isEdit) {
        name.isNotBlank()
    } else {
        selectedAddKind != null && isAddWalletFormValid(
            type = activeType,
            name = name,
            balanceText = balanceText,
            creditLimitText = creditLimitText,
            currentDebtText = currentDebtText,
            billingDayText = billingDayText,
            paymentDueDayText = paymentDueDayText,
            targetAmountText = targetAmountText
        )
    }

    val submitWallet = {
        val requestType = activeType
        val request = WalletRequest(
            name = name.trim(),
            balance = if (requestType == WalletType.CREDIT_CARD) 0.0 else balanceText.toAmount(),
            type = requestType.name,
            bankName = if (isEdit) bankName.trim().ifBlank { null } else null,
            cardNumber = if (isEdit) cardNumber.trim().ifBlank { null } else null,
            colorHex = colorHex,
            icon = iconKey,
            currency = currency.ifBlank { "VND" },
            isDefault = isDefault,
            isIncludedInTotal = if (isDefault) true else isIncludedInTotal,
            sortOrder = wallet?.sortOrder ?: 0,
            creditLimit = if (requestType == WalletType.CREDIT_CARD) creditLimitText.toNullableAmount() else null,
            currentDebt = if (requestType == WalletType.CREDIT_CARD) currentDebtText.toNullableAmount() else null,
            billingDay = if (requestType == WalletType.CREDIT_CARD) billingDayText.toDayOrNull() else null,
            paymentDueDay = if (requestType == WalletType.CREDIT_CARD) paymentDueDayText.toDayOrNull() else null,
            targetAmount = if (requestType == WalletType.SAVINGS) targetAmountText.toNullableAmount() else null,
            targetDate = if (requestType == WalletType.SAVINGS) targetDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) else null
        )
        onSave(request)
    }

    val applyAddKind: (WalletAddKind) -> Unit = { kind ->
        selectedAddKind = kind
        type = kind.walletType
        colorHex = WalletIconMap.colorHexFor(kind.walletType)
        iconKey = WalletIconMap.defaultKeyFor(kind.walletType)
        when (kind) {
            WalletAddKind.Basic -> {
                if (type !in BASIC_WALLET_TYPES) type = WalletType.CASH
                creditLimitText = ""
                currentDebtText = ""
                billingDayText = ""
                paymentDueDayText = ""
                targetAmountText = ""
                targetDate = null
            }

            WalletAddKind.Credit -> {
                balanceText = ""
                creditLimitText = creditLimitText.ifBlank { "0" }
                currentDebtText = currentDebtText.ifBlank { "0" }
                billingDayText = billingDayText.ifBlank { "15" }
                paymentDueDayText = paymentDueDayText.ifBlank { "25" }
                targetAmountText = ""
                targetDate = null
            }

            WalletAddKind.Savings -> {
                creditLimitText = ""
                currentDebtText = ""
                billingDayText = ""
                paymentDueDayText = ""
            }
        }
    }

    if (showIconPicker) {
        ModalBottomSheet(
            onDismissRequest = { showIconPicker = false },
            containerColor = HomeBackgroundMid
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                WalletIconPicker(
                    type = type,
                    selectedKey = iconKey,
                    onSelect = {
                        iconKey = it
                        showIconPicker = false
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    if (showTargetDatePicker) {
        val initialSelectedDate = targetDate
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDate)

        DatePickerDialog(
            onDismissRequest = { showTargetDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            targetDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showTargetDatePicker = false
                    }
                ) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (wallet != null) {
        EditWalletFlow(
            wallet = wallet,
            wallets = wallets,
            mode = editMode,
            onModeChange = { editMode = it },
            name = name,
            onNameChange = { name = it },
            balanceText = balanceText,
            onBalanceChange = { balanceText = formatMoneyInput(it) },
            iconKey = iconKey,
            onIconSelect = { iconKey = it },
            onOpenIconPicker = { showIconPicker = true },
            isDefault = isDefault,
            onDefaultChange = {
                isDefault = it
                if (it) isIncludedInTotal = true
            },
            isIncludedInTotal = isIncludedInTotal,
            onIncludedInTotalChange = { isIncludedInTotal = it },
            transferAmountText = transferAmountText,
            onTransferAmountChange = { transferAmountText = formatMoneyInput(it) },
            selectedTransferWalletId = selectedTransferWalletId,
            onSelectedTransferWallet = { selectedTransferWalletId = it },
            canSave = name.isNotBlank(),
            onSave = submitWallet,
            onDismiss = onDismiss,
            onArchive = { onArchive(wallet) },
            onTransfer = { destination, amount -> onTransfer(wallet, destination, amount) }
        )
        return
    }

    BackHandler {
        if (selectedAddKind == null) {
            onDismiss()
        } else {
            selectedAddKind = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HomeBackgroundMid,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = selectedAddKind?.title ?: "Thêm Ví",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = LuminousOnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Đóng",
                            tint = LuminousOnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LuminousSurfaceContainerLowest,
                    scrolledContainerColor = LuminousSurfaceContainerLowest
                )
            )
        },
        bottomBar = {
            if (selectedAddKind != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LuminousSurfaceContainerLowest,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = submitWallet,
                            enabled = canSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600)
                        ) {
                            Text(
                                text = "Thêm ví",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedAddKind == null) {
                WalletAddTypeSelection(onSelect = applyAddKind)
            } else {
                WalletEditorPreview(
                    type = activeType,
                    iconKey = iconKey,
                    name = name,
                    balanceText = if (activeType == WalletType.CREDIT_CARD) currentDebtText else balanceText,
                    isEdit = false,
                    onIconClick = { showIconPicker = true }
                )

                if (selectedAddKind == WalletAddKind.Basic) {
                    WalletTypePicker(
                        selected = type,
                        options = BASIC_WALLET_TYPE_OPTIONS,
                        onSelect = { selected ->
                            type = selected
                            iconKey = WalletIconMap.defaultKeyFor(selected)
                            colorHex = WalletIconMap.colorHexFor(selected)
                        }
                    )
                }

                WalletTextField(
                    label = "Tên ví",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = when (activeType) {
                        WalletType.CREDIT_CARD -> "Thẻ tín dụng"
                        WalletType.SAVINGS -> "Quỹ tiết kiệm"
                        else -> "Tiền mặt, Vietcombank..."
                    }
                )

                when (activeType) {
                    WalletType.CREDIT_CARD -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            WalletTextField(
                                label = "Hạn mức",
                                value = creditLimitText,
                                onValueChange = { creditLimitText = formatMoneyInput(it) },
                                placeholder = "0",
                                keyboardType = KeyboardType.Number,
                                suffix = "đ",
                                useMoneyPad = true,
                                modifier = Modifier.weight(1f)
                            )
                            WalletTextField(
                                label = "Dư nợ hiện tại",
                                value = currentDebtText,
                                onValueChange = { currentDebtText = formatMoneyInput(it) },
                                placeholder = "0",
                                keyboardType = KeyboardType.Number,
                                suffix = "đ",
                                useMoneyPad = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            WalletTextField(
                                label = "Ngày sao kê",
                                value = billingDayText,
                                onValueChange = { billingDayText = it.digitsOnly().take(2) },
                                placeholder = "15",
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                            WalletTextField(
                                label = "Ngày thanh toán",
                                value = paymentDueDayText,
                                onValueChange = { paymentDueDayText = it.digitsOnly().take(2) },
                                placeholder = "25",
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    WalletType.SAVINGS -> {
                        WalletTextField(
                            label = "Số dư",
                            value = balanceText,
                            onValueChange = { balanceText = formatMoneyInput(it) },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            suffix = "đ",
                            useMoneyPad = true
                        )
                        WalletTextField(
                            label = "Mục tiêu tiết kiệm",
                            value = targetAmountText,
                            onValueChange = { targetAmountText = formatMoneyInput(it) },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            suffix = "đ",
                            useMoneyPad = true
                        )
                        WalletDateField(
                            label = "Ngày mục tiêu",
                            value = targetDate?.format(WALLET_DATE_DISPLAY_FORMATTER) ?: "Chọn ngày",
                            onClick = { showTargetDatePicker = true }
                        )
                        SavingsGoalProgress(
                            balanceText = balanceText,
                            targetAmountText = targetAmountText
                        )
                    }

                    else -> {
                        WalletTextField(
                            label = "Số dư",
                            value = balanceText,
                            onValueChange = { balanceText = formatMoneyInput(it) },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            suffix = "đ",
                            useMoneyPad = true
                        )
                    }
                }

                WalletTextField(
                    label = "Tiền tệ",
                    value = currency,
                    onValueChange = { currency = it.uppercase().take(10) },
                    placeholder = "VND"
                )

                if (selectedAddKind == WalletAddKind.Basic) {
                    DefaultWalletToggle(
                        checked = isDefault,
                        onCheckedChange = {
                            isDefault = it
                            if (it) isIncludedInTotal = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private enum class WalletAddKind(
    val title: String,
    val walletType: WalletType,
    val color: Color,
    val icon: ImageVector
) {
    Basic("Ví cơ bản", WalletType.CASH, Color(0xFF2FBF5A), Icons.Outlined.Payments),
    Credit("Ví tín dụng", WalletType.CREDIT_CARD, Color(0xFFE84E9B), Icons.Outlined.CreditCard),
    Savings("Ví tiết kiệm", WalletType.SAVINGS, Color(0xFFFF5A57), Icons.Outlined.Savings);

    companion object {
        fun fromWalletType(type: WalletType): WalletAddKind = when (type) {
            WalletType.CREDIT_CARD -> Credit
            WalletType.SAVINGS -> Savings
            else -> Basic
        }
    }
}

private val BASIC_WALLET_TYPES = setOf(WalletType.CASH, WalletType.BANK, WalletType.EWALLET)

private val BASIC_WALLET_TYPE_OPTIONS = listOf(
    WalletTypeOption(WalletType.CASH, "Tiền mặt", Icons.Outlined.Payments),
    WalletTypeOption(WalletType.BANK, "Ngân hàng", Icons.Outlined.AccountBalance),
    WalletTypeOption(WalletType.EWALLET, "Ví điện tử", Icons.Outlined.PhoneAndroid)
)

private val DEFAULT_WALLET_TYPE_OPTIONS = BASIC_WALLET_TYPE_OPTIONS + listOf(
    WalletTypeOption(WalletType.CREDIT_CARD, "Tín dụng", Icons.Outlined.CreditCard),
    WalletTypeOption(WalletType.SAVINGS, "Tiết kiệm", Icons.Outlined.Savings),
    WalletTypeOption(WalletType.INVESTMENT, "Đầu tư", WalletIconMap.toVector("investment"))
)

private val WALLET_DATE_DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
private fun WalletAddTypeSelection(
    onSelect: (WalletAddKind) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            WalletAddKindCard(
                kind = WalletAddKind.Basic,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(WalletAddKind.Basic) }
            )
            WalletAddKindCard(
                kind = WalletAddKind.Credit,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(WalletAddKind.Credit) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            WalletAddKindCard(
                kind = WalletAddKind.Savings,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(WalletAddKind.Savings) }
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun WalletAddKindCard(
    kind: WalletAddKind,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(kind.color)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = kind.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            lineHeight = 26.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Icon(
            imageVector = kind.icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.24f),
            modifier = Modifier
                .size(92.dp)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun WalletDateField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LuminousOnSurfaceVariant
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(6.dp),
            color = LuminousSurfaceContainerLowest,
            border = BorderStroke(1.dp, LuminousOutlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuminousOnSurface
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                    contentDescription = null,
                    tint = LuminousOnSurfaceVariant.copy(alpha = 0.52f)
                )
            }
        }
    }
}

@Composable
private fun SavingsGoalProgress(
    balanceText: String,
    targetAmountText: String
) {
    val balance = balanceText.toAmount()
    val target = targetAmountText.toNullableAmount()
    if (target == null || target <= 0.0) return

    val progress = (balance / target).toFloat().coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = LuminousSurfaceContainerLowest,
        border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiến độ mục tiêu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuminousOnSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = OceanBlue600
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = OceanBlue600,
                trackColor = LuminousSurfaceContainerHigh
            )
        }
    }
}

private fun isAddWalletFormValid(
    type: WalletType,
    name: String,
    balanceText: String,
    creditLimitText: String,
    currentDebtText: String,
    billingDayText: String,
    paymentDueDayText: String,
    targetAmountText: String
): Boolean {
    if (name.isBlank()) return false

    return when (type) {
        WalletType.CREDIT_CARD -> {
            val limit = creditLimitText.toAmount()
            val debt = currentDebtText.toAmount()
            limit >= debt &&
                billingDayText.toDayOrNull() != null &&
                paymentDueDayText.toDayOrNull() != null
        }

        WalletType.SAVINGS -> {
            val balance = balanceText.toAmount()
            val target = targetAmountText.toNullableAmount()
            target == null || target > balance
        }

        else -> balanceText.toAmount() >= 0.0
    }
}

private enum class WalletEditMode {
    Overview,
    AdjustBalance,
    Transfer
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun EditWalletFlow(
    wallet: Wallet,
    wallets: List<Wallet>,
    mode: WalletEditMode,
    onModeChange: (WalletEditMode) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    balanceText: String,
    onBalanceChange: (String) -> Unit,
    iconKey: String,
    onIconSelect: (String) -> Unit,
    onOpenIconPicker: () -> Unit,
    isDefault: Boolean,
    onDefaultChange: (Boolean) -> Unit,
    isIncludedInTotal: Boolean,
    onIncludedInTotalChange: (Boolean) -> Unit,
    transferAmountText: String,
    onTransferAmountChange: (String) -> Unit,
    selectedTransferWalletId: String?,
    onSelectedTransferWallet: (String) -> Unit,
    canSave: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onArchive: () -> Unit,
    onTransfer: (Wallet, Double) -> Unit
) {
    val destinationWallets = wallets.filter { it.id != wallet.id && !it.isArchived }
    val selectedDestination = destinationWallets.firstOrNull { it.id == selectedTransferWalletId }
    val canTransfer = selectedDestination != null && transferAmountText.toAmount() > 0.0
    val title = when (mode) {
        WalletEditMode.Overview -> "Sửa Ví"
        WalletEditMode.AdjustBalance -> "Điều chỉnh số dư"
        WalletEditMode.Transfer -> "Chuyển tiền"
    }
    val rightText = if (mode == WalletEditMode.Transfer) "Chuyển" else "Lưu"
    val rightEnabled = if (mode == WalletEditMode.Transfer) canTransfer else canSave
    val onRightClick = {
        if (mode == WalletEditMode.Transfer) {
            selectedDestination?.let { onTransfer(it, transferAmountText.toAmount()) }
        } else {
            onSave()
        }
        Unit
    }

    BackHandler {
        if (mode == WalletEditMode.Overview) onDismiss() else onModeChange(WalletEditMode.Overview)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HomeBackgroundMid,
        topBar = {
            WalletEditTopBar(
                title = title,
                leftText = if (mode == WalletEditMode.Overview) "Hủy" else "Quay lại",
                rightText = rightText,
                rightEnabled = rightEnabled,
                onLeftClick = {
                    if (mode == WalletEditMode.Overview) {
                        onDismiss()
                    } else {
                        onModeChange(WalletEditMode.Overview)
                    }
                },
                onRightClick = onRightClick
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                fadeIn(tween(180, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(tween(140, easing = FastOutSlowInEasing))
            },
            label = "WalletEditMode",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { targetMode ->
            when (targetMode) {
                WalletEditMode.Overview -> WalletEditOverview(
                    wallet = wallet,
                    name = name,
                    onNameChange = onNameChange,
                    balanceText = balanceText,
                    iconKey = iconKey,
                    onIconSelect = onIconSelect,
                    onOpenIconPicker = onOpenIconPicker,
                    isDefault = isDefault,
                    onDefaultChange = onDefaultChange,
                    isIncludedInTotal = isIncludedInTotal,
                    onIncludedInTotalChange = onIncludedInTotalChange,
                    onOpenAdjustBalance = { onModeChange(WalletEditMode.AdjustBalance) },
                    onOpenTransfer = { onModeChange(WalletEditMode.Transfer) },
                    onArchive = onArchive
                )

                WalletEditMode.AdjustBalance -> WalletAdjustBalanceScreen(
                    wallet = wallet,
                    name = name,
                    onNameChange = onNameChange,
                    balanceText = balanceText,
                    onBalanceChange = onBalanceChange,
                    isDefault = isDefault,
                    isIncludedInTotal = isIncludedInTotal,
                    onIncludedInTotalChange = onIncludedInTotalChange
                )

                WalletEditMode.Transfer -> WalletTransferScreen(
                    wallets = destinationWallets,
                    selectedWallet = selectedDestination,
                    amountText = transferAmountText,
                    onAmountChange = onTransferAmountChange,
                    onWalletSelected = onSelectedTransferWallet
                )
            }
        }
    }
}

@Composable
private fun WalletEditTopBar(
    title: String,
    leftText: String,
    rightText: String,
    rightEnabled: Boolean,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Surface(color = HomeBackgroundMid) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp)
        ) {
            AssistChip(
                onClick = onLeftClick,
                label = {
                    Text(
                        text = leftText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = LuminousSurfaceContainerLowest,
                    labelColor = LuminousOnSurface
                ),
                border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = LuminousOnSurface,
                modifier = Modifier.align(Alignment.Center)
            )
            AssistChip(
                onClick = onRightClick,
                enabled = rightEnabled,
                label = {
                    Text(
                        text = rightText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = LuminousSurfaceContainerLowest,
                    labelColor = OceanBlue600,
                    disabledContainerColor = LuminousSurfaceContainerLow,
                    disabledLabelColor = LuminousOnSurfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun WalletEditOverview(
    wallet: Wallet,
    name: String,
    onNameChange: (String) -> Unit,
    balanceText: String,
    iconKey: String,
    onIconSelect: (String) -> Unit,
    onOpenIconPicker: () -> Unit,
    isDefault: Boolean,
    onDefaultChange: (Boolean) -> Unit,
    isIncludedInTotal: Boolean,
    onIncludedInTotalChange: (Boolean) -> Unit,
    onOpenAdjustBalance: () -> Unit,
    onOpenTransfer: () -> Unit,
    onArchive: () -> Unit
) {
    val defaultLocked = wallet.isDefault || isDefault

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        WalletEditPrimaryCard(
            wallet = wallet,
            name = name,
            onNameChange = onNameChange,
            balanceText = balanceText,
            iconKey = iconKey,
            onIconClick = onOpenIconPicker,
            onOpenAdjustBalance = onOpenAdjustBalance
        )

        DefaultWalletToggle(
            checked = isDefault,
            onCheckedChange = { if (!wallet.isDefault || it) onDefaultChange(it) },
            enabled = !wallet.isDefault,
            modifier = Modifier.fillMaxWidth()
        )

        WalletToggleBlock(
            title = "Không tính vào tổng",
            description =
                if (defaultLocked) {
                    "Ví mặc định luôn được tính vào tổng."
                } else {
                    "Bỏ qua ví này và số dư khỏi \"Tổng\"."
                },
            checked = !isIncludedInTotal,
            onCheckedChange = { onIncludedInTotalChange(!it) },
            enabled = !defaultLocked
        )

        WalletActionGroup(
            onOpenTransfer = onOpenTransfer,
            onOpenAdjustBalance = onOpenAdjustBalance,
            onArchive = onArchive,
            archiveEnabled = !defaultLocked
        )
    }
}

@Composable
private fun WalletAdjustBalanceScreen(
    wallet: Wallet,
    name: String,
    onNameChange: (String) -> Unit,
    balanceText: String,
    onBalanceChange: (String) -> Unit,
    isDefault: Boolean,
    isIncludedInTotal: Boolean,
    onIncludedInTotalChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            color = LuminousSurfaceContainerLowest,
            border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.32f)),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                WalletNameRow(wallet = wallet, name = name, onNameChange = onNameChange)
                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp),
                    color = LuminousOutlineVariant.copy(alpha = 0.6f)
                )
                WalletAmountEditRow(
                    label = "Nhập số dư hiện tại của ví",
                    balanceText = balanceText,
                    onBalanceChange = onBalanceChange
                )
            }
        }

        WalletToggleBlock(
            title = "Không tính vào tổng",
            description =
                if (isDefault) {
                    "Ví mặc định luôn được tính vào tổng."
                } else {
                    "Bỏ qua ví này và số dư khỏi \"Tổng\"."
                },
            checked = !isIncludedInTotal,
            onCheckedChange = { onIncludedInTotalChange(!it) },
            enabled = !isDefault
        )
    }
}

@Composable
private fun WalletTransferScreen(
    wallets: List<Wallet>,
    selectedWallet: Wallet?,
    amountText: String,
    onAmountChange: (String) -> Unit,
    onWalletSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            color = LuminousSurfaceContainerLowest,
            border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.32f)),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                WalletActionRow(
                    title = selectedWallet?.name ?: "Chọn ví nhận tiền",
                    value = selectedWallet?.let { formatDisplayAmount(it.balance) },
                    onClick = { if (wallets.isNotEmpty()) expanded = true }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(wallet.name, fontWeight = FontWeight.SemiBold)
                                    Text(formatDisplayAmount(wallet.balance), fontSize = 12.sp, color = LuminousOnSurfaceVariant)
                                }
                            },
                            onClick = {
                                onWalletSelected(wallet.id)
                                expanded = false
                            }
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 18.dp),
                    color = LuminousOutlineVariant.copy(alpha = 0.6f)
                )
                WalletAmountEditRow(
                    label = "Số tiền chuyển",
                    balanceText = amountText,
                    onBalanceChange = onAmountChange
                )
            }
        }

        if (wallets.isEmpty()) {
            Text(
                text = "Cần ít nhất một ví đang hoạt động khác để chuyển tiền.",
                fontSize = 13.sp,
                color = LuminousOnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun WalletEditPrimaryCard(
    wallet: Wallet,
    name: String,
    onNameChange: (String) -> Unit,
    balanceText: String,
    iconKey: String,
    onIconClick: () -> Unit,
    onOpenAdjustBalance: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = LuminousSurfaceContainerLowest,
        border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.32f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            WalletNameRow(
                wallet = wallet,
                iconKey = iconKey,
                name = name,
                onNameChange = onNameChange,
                onIconClick = onIconClick
            )
            HorizontalDivider(color = LuminousOutlineVariant.copy(alpha = 0.6f))
            WalletInfoRow(
                title = "Việt Nam Đồng",
                value = wallet.currency,
                onClick = {}
            )
            HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = LuminousOutlineVariant.copy(alpha = 0.6f))
            WalletInfoRow(
                title = "Nhập số dư hiện tại của ví",
                value = "${balanceText.ifBlank { "0" }} đ",
                onClick = onOpenAdjustBalance
            )
        }
    }
}

@Composable
private fun WalletNameRow(
    wallet: Wallet,
    iconKey: String = wallet.icon,
    name: String,
    onNameChange: (String) -> Unit,
    onIconClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(WalletIconMap.backgroundFor(wallet.type))
                .then(
                    if (onIconClick != null) Modifier.clickable(onClick = onIconClick)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = WalletIconMap.toVector(iconKey, wallet.type),
                contentDescription = null,
                tint = WalletIconMap.tintFor(wallet.type),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        TextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = LuminousSurfaceContainerLowest,
                unfocusedContainerColor = LuminousSurfaceContainerLowest,
                disabledContainerColor = LuminousSurfaceContainerLowest,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
            contentDescription = null,
            tint = LuminousOnSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun WalletInfoRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = LuminousOnSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 19.sp,
                color = LuminousOnSurface,
                fontWeight = FontWeight.Bold
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
            contentDescription = null,
            tint = LuminousOnSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun WalletAmountEditRow(
    label: String,
    balanceText: String,
    onBalanceChange: (String) -> Unit
) {
    var showMoneyPad by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = LuminousOnSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = balanceText,
                onValueChange = { _: String -> },
                readOnly = true,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("đ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LuminousOnSurface) },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuminousOnSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LuminousSurfaceContainerLowest,
                    unfocusedContainerColor = LuminousSurfaceContainerLowest,
                    disabledContainerColor = LuminousSurfaceContainerLowest,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showMoneyPad = true }
            )
        }

        AppMoneyNumberPadSheet(
            visible = showMoneyPad,
            amountText = balanceText,
            onAmountChange = { onBalanceChange(formatMoneyInput(it)) },
            onDismiss = { showMoneyPad = false },
            title = label
        )
    }
}

@Composable
private fun WalletToggleBlock(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val contentAlpha = if (enabled) 1f else 0.58f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(LuminousSurfaceContainerLowest)
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuminousOnSurface.copy(alpha = contentAlpha)
            )
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = LuminousSurfaceContainerLowest,
                    checkedTrackColor = OceanBlue600,
                    uncheckedThumbColor = LuminousSurfaceContainerLowest,
                    uncheckedTrackColor = LuminousSurfaceContainerHighest,
                    disabledCheckedThumbColor = LuminousSurfaceContainerLowest,
                    disabledCheckedTrackColor = OceanBlue600.copy(alpha = 0.38f),
                    disabledUncheckedThumbColor = LuminousSurfaceContainerLowest,
                    disabledUncheckedTrackColor = LuminousSurfaceContainerHighest.copy(alpha = 0.78f)
                )
            )
        }
        Text(
            text = description,
            fontSize = 13.sp,
            color = LuminousOnSurfaceVariant.copy(alpha = contentAlpha),
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}

@Composable
private fun WalletActionGroup(
    onOpenTransfer: () -> Unit,
    onOpenAdjustBalance: () -> Unit,
    onArchive: () -> Unit,
    archiveEnabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            color = LuminousSurfaceContainerLowest,
            border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.32f))
        ) {
            Column {
                WalletActionRow(
                    title = "Chuyển tiền đến ví khác",
                    value = null,
                    tint = OceanBlue600,
                    onClick = onOpenTransfer
                )
                HorizontalDivider(color = LuminousOutlineVariant.copy(alpha = 0.6f))
                WalletActionRow(
                    title = "Điều chỉnh số dư",
                    value = null,
                    tint = OceanBlue600,
                    onClick = onOpenAdjustBalance
                )
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            color = LuminousSurfaceContainerLowest,
            border = BorderStroke(1.dp, LuminousOutlineVariant.copy(alpha = 0.32f))
        ) {
            WalletActionRow(
                title = "Lưu trữ",
                value =
                    if (archiveEnabled) {
                        "Đóng băng ví và ngừng tạo giao dịch"
                    } else {
                        "Ví mặc định không thể lưu trữ"
                    },
                tint = if (archiveEnabled) LuminousError else LuminousOnSurfaceVariant,
                enabled = archiveEnabled,
                onClick = onArchive
            )
        }
    }
}

@Composable
private fun WalletActionRow(
    title: String,
    value: String?,
    tint: Color = LuminousOnSurface,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentAlpha = if (enabled) 1f else 0.58f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = tint.copy(alpha = contentAlpha)
            )
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    color = LuminousOnSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
            contentDescription = null,
            tint = LuminousOnSurfaceVariant.copy(alpha = 0.45f * contentAlpha),
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun formatDisplayAmount(amount: Double): String = formatMoneyDisplay(amount)

@Composable
private fun WalletEditorPreview(
    type: WalletType,
    iconKey: String,
    name: String,
    balanceText: String,
    isEdit: Boolean,
    onIconClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LuminousSurfaceContainerLowest)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(WalletIconMap.backgroundFor(type))
                    .clickable(onClick = onIconClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = WalletIconMap.toVector(iconKey, type),
                    contentDescription = null,
                    tint = WalletIconMap.tintFor(type),
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name.ifBlank { if (isEdit) "Ví của bạn" else "Ví mới" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = LuminousOnSurface,
                    maxLines = 1
                )
                Text(
                    text = "${balanceText.ifBlank { "0" }} đ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuminousOnSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun WalletTypePicker(
    selected: WalletType,
    options: List<WalletTypeOption> = DEFAULT_WALLET_TYPE_OPTIONS,
    onSelect: (WalletType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Loại ví",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LuminousOnSurfaceVariant
        )
        options.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    val isSelected = selected == option.type
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) WalletIconMap.backgroundFor(option.type) else LuminousSurfaceContainerLow)
                            .clickable { onSelect(option.type) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(LuminousSurfaceContainerLowest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.label,
                                tint = WalletIconMap.tintFor(option.type),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = option.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) WalletIconMap.tintFor(option.type) else LuminousOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletIconPicker(
    type: WalletType,
    selectedKey: String,
    onSelect: (String) -> Unit
) {
    val options = WalletIconMap.optionsFor(type)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Biểu tượng ví",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LuminousOnSurfaceVariant
        )
        options.chunked(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { option ->
                    val isSelected = selectedKey == option.key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) WalletIconMap.backgroundFor(type) else LuminousSurfaceContainerLow)
                            .clickable { onSelect(option.key) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(LuminousSurfaceContainerLowest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = WalletIconMap.toVector(option.key, type),
                                contentDescription = option.label,
                                tint = if (isSelected) WalletIconMap.tintFor(type) else LuminousOnSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                repeat(5 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WalletTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: String? = null,
    useMoneyPad: Boolean = false
) {
    var showMoneyPad by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LuminousOnSurfaceVariant
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = if (useMoneyPad) ({ _: String -> }) else onValueChange,
                readOnly = useMoneyPad,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                trailingIcon = suffix?.let {
                    { Text(it, color = LuminousOnSurfaceVariant, fontWeight = FontWeight.Bold) }
                },
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OceanBlue600,
                    unfocusedBorderColor = LuminousOutlineVariant,
                    focusedContainerColor = LuminousSurfaceContainerLowest,
                    unfocusedContainerColor = LuminousSurfaceContainerLowest
                )
            )
            if (useMoneyPad) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showMoneyPad = true }
                )
            }
        }

        if (useMoneyPad) {
            AppMoneyNumberPadSheet(
                visible = showMoneyPad,
                amountText = value,
                onAmountChange = { onValueChange(formatMoneyInput(it)) },
                onDismiss = { showMoneyPad = false },
                title = label
            )
        }
    }
}

@Composable
private fun DefaultWalletToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val contentAlpha = if (enabled) 1f else 0.72f

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Ví mặc định",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LuminousOnSurfaceVariant
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .clickable(enabled = enabled) { onCheckedChange(!checked) },
            shape = RoundedCornerShape(6.dp),
            color = LuminousSurfaceContainerLowest,
            border = BorderStroke(
                width = 1.dp,
                color = if (checked) OceanBlue600.copy(alpha = 0.22f)
                else LuminousOutlineVariant.copy(alpha = 0.32f)
            ),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (checked) "Đang mặc định" else "Đặt mặc định",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (checked) OceanBlue600.copy(alpha = contentAlpha)
                    else LuminousOnSurface.copy(alpha = contentAlpha)
                )
                Switch(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = LuminousSurfaceContainerLowest,
                        checkedTrackColor = OceanBlue600,
                        uncheckedThumbColor = LuminousSurfaceContainerLowest,
                        uncheckedTrackColor = LuminousSurfaceContainerHighest,
                        disabledCheckedThumbColor = LuminousSurfaceContainerLowest,
                        disabledCheckedTrackColor = OceanBlue600.copy(alpha = 0.58f)
                    )
                )
            }
        }
    }
}

private data class WalletTypeOption(
    val type: WalletType,
    val label: String,
    val icon: ImageVector
)

private fun String.digitsOnly(): String = filter { it.isDigit() }

private fun String.toAmount(): Double = parseMoneyInput(this)

private fun String.toNullableAmount(): Double? = parseNullableMoneyInput(this)

private fun String.toDayOrNull(): Int? = toIntOrNull()?.takeIf { it in 1..31 }

private fun Double.toAmountInput(): String =
    formatMoneyAmount(this)
