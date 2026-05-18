package com.dung.ddmoney.ui.wallets

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.network.dto.WalletRequest
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.dashboard.model.WalletType
import com.dung.ddmoney.ui.theme.*

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
    var isIncludedInTotal by remember(wallet?.id) { mutableStateOf(wallet?.isIncludedInTotal ?: true) }
    var creditLimitText by remember(wallet?.id) { mutableStateOf(wallet?.creditLimit?.toAmountInput().orEmpty()) }
    var currentDebtText by remember(wallet?.id) { mutableStateOf(wallet?.currentDebt?.toAmountInput().orEmpty()) }
    var billingDayText by remember(wallet?.id) { mutableStateOf(wallet?.billingDay?.toString().orEmpty()) }
    var paymentDueDayText by remember(wallet?.id) { mutableStateOf(wallet?.paymentDueDay?.toString().orEmpty()) }
    var colorHex by remember(wallet?.id) {
        mutableStateOf(wallet?.colorHex ?: WalletIconMap.colorHexFor(type))
    }
    var iconKey by remember(wallet?.id) {
        mutableStateOf(wallet?.icon?.takeIf { it.isNotBlank() } ?: WalletIconMap.defaultKeyFor(type))
    }
    var transferAmountText by remember(wallet?.id) { mutableStateOf("") }
    var selectedTransferWalletId by remember(wallet?.id, wallets) {
        mutableStateOf(wallets.firstOrNull { it.id != wallet?.id && !it.isArchived }?.id)
    }

    val submitWallet = {
        val request = WalletRequest(
            name = name.trim(),
            balance = balanceText.toAmount(),
            type = type.name,
            bankName = bankName.trim().ifBlank { null },
            cardNumber = cardNumber.trim().ifBlank { null },
            colorHex = colorHex,
            icon = iconKey,
            currency = currency.ifBlank { "VND" },
            isDefault = isDefault,
            isIncludedInTotal = isIncludedInTotal,
            sortOrder = wallet?.sortOrder ?: 0,
            creditLimit = creditLimitText.toNullableAmount(),
            currentDebt = currentDebtText.toNullableAmount(),
            billingDay = billingDayText.toDayOrNull(),
            paymentDueDay = paymentDueDayText.toDayOrNull()
        )
        onSave(request)
    }

    BackHandler(onBack = onDismiss)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HomeBackgroundMid,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEdit) "Chỉnh sửa ví" else "Thêm ví mới",
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
                        enabled = name.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600)
                    ) {
                        Text(
                            text = if (isEdit) "Lưu thay đổi" else "Thêm ví",
                            fontWeight = FontWeight.Bold
                        )
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
            WalletEditorPreview(
                type = type,
                iconKey = iconKey,
                name = name,
                balanceText = balanceText,
                isEdit = isEdit
            )

            if (!isEdit) {
                WalletTypePicker(
                    selected = type,
                    onSelect = { selected ->
                        type = selected
                        iconKey = WalletIconMap.defaultKeyFor(selected)
                        colorHex = WalletIconMap.colorHexFor(selected)
                        if (selected != WalletType.CREDIT_CARD) {
                            creditLimitText = ""
                            currentDebtText = ""
                            billingDayText = ""
                            paymentDueDayText = ""
                        }
                    }
                )
            }

            WalletTextField(
                label = "Tên ví",
                value = name,
                onValueChange = { name = it },
                placeholder = "Tiền mặt, Vietcombank..."
            )

            WalletTextField(
                label = if (type == WalletType.CREDIT_CARD) "Số dư hiện tại" else "Số dư",
                value = balanceText,
                onValueChange = { balanceText = it.digitsOnly() },
                placeholder = "0",
                keyboardType = KeyboardType.Number,
                suffix = "đ"
            )

            if (!isEdit) {
                if (type == WalletType.BANK || type == WalletType.CREDIT_CARD) {
                    WalletTextField(
                        label = "Tên ngân hàng",
                        value = bankName,
                        onValueChange = { bankName = it },
                        placeholder = "Vietcombank, ACB..."
                    )
                }

                if (type == WalletType.CREDIT_CARD) {
                    WalletTextField(
                        label = "Số thẻ",
                        value = cardNumber,
                        onValueChange = { cardNumber = it.take(20) },
                        placeholder = "**** 1234"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WalletTextField(
                            label = "Hạn mức",
                            value = creditLimitText,
                            onValueChange = { creditLimitText = it.digitsOnly() },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            suffix = "đ",
                            modifier = Modifier.weight(1f)
                        )
                        WalletTextField(
                            label = "Dư nợ",
                            value = currentDebtText,
                            onValueChange = { currentDebtText = it.digitsOnly() },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            suffix = "đ",
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
                            label = "Ngày đến hạn",
                            value = paymentDueDayText,
                            onValueChange = { paymentDueDayText = it.digitsOnly().take(2) },
                            placeholder = "25",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WalletTextField(
                        label = "Tiền tệ",
                        value = currency,
                        onValueChange = { currency = it.uppercase().take(10) },
                        placeholder = "VND",
                        modifier = Modifier.weight(1f)
                    )
                    DefaultWalletToggle(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                val editingWallet = wallet!!
                EditWalletManagementSection(
                    wallet = editingWallet,
                    wallets = wallets,
                    isIncludedInTotal = isIncludedInTotal,
                    onIncludedInTotalChange = { isIncludedInTotal = it },
                    transferAmountText = transferAmountText,
                    onTransferAmountChange = { transferAmountText = it.digitsOnly() },
                    selectedTransferWalletId = selectedTransferWalletId,
                    onSelectedTransferWallet = { selectedTransferWalletId = it },
                    onTransfer = { destination ->
                        onTransfer(editingWallet, destination, transferAmountText.toAmount())
                    },
                    onArchive = { onArchive(editingWallet) }
                )
            }

        }
    }
}

@Composable
private fun EditWalletManagementSection(
    wallet: Wallet,
    wallets: List<Wallet>,
    isIncludedInTotal: Boolean,
    onIncludedInTotalChange: (Boolean) -> Unit,
    transferAmountText: String,
    onTransferAmountChange: (String) -> Unit,
    selectedTransferWalletId: String?,
    onSelectedTransferWallet: (String) -> Unit,
    onTransfer: (Wallet) -> Unit,
    onArchive: () -> Unit
) {
    val destinationWallets = wallets.filter { it.id != wallet.id && !it.isArchived }
    val selectedDestination = destinationWallets.firstOrNull { it.id == selectedTransferWalletId }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        WalletSettingSwitch(
            title = "Tính vào tổng số dư",
            subtitle =
                if (isIncludedInTotal) "Ví này đang được cộng vào tổng số dư"
                else "Ví này không ảnh hưởng tổng số dư trên trang chủ",
            checked = isIncludedInTotal,
            onCheckedChange = onIncludedInTotalChange
        )

        TransferWalletPanel(
            wallets = destinationWallets,
            selectedWallet = selectedDestination,
            amountText = transferAmountText,
            onAmountChange = onTransferAmountChange,
            onWalletSelected = onSelectedTransferWallet,
            onTransfer = {
                selectedDestination?.let(onTransfer)
            }
        )

        ArchiveWalletPanel(onArchive = onArchive)
    }
}

@Composable
private fun WalletSettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LuminousSurfaceContainerLowest)
            .clickable { onCheckedChange(!checked) }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = LuminousOnSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = LuminousOnSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LuminousSurfaceContainerLowest,
                checkedTrackColor = OceanBlue600
            )
        )
    }
}

@Composable
private fun TransferWalletPanel(
    wallets: List<Wallet>,
    selectedWallet: Wallet?,
    amountText: String,
    onAmountChange: (String) -> Unit,
    onWalletSelected: (String) -> Unit,
    onTransfer: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val canTransfer = selectedWallet != null && amountText.toAmount() > 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LuminousSurfaceContainerLowest)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.SwapHoriz,
                contentDescription = null,
                tint = OceanBlue600,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Chuyển tiền sang ví khác",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = LuminousOnSurface
            )
        }

        WalletTextField(
            label = "Số tiền chuyển",
            value = amountText,
            onValueChange = onAmountChange,
            placeholder = "0",
            keyboardType = KeyboardType.Number,
            suffix = "đ"
        )

        Box {
            OutlinedButton(
                onClick = { if (wallets.isNotEmpty()) expanded = true },
                enabled = wallets.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = selectedWallet?.name ?: "Chọn ví nhận tiền",
                    modifier = Modifier.weight(1f),
                    color = if (selectedWallet == null) LuminousOnSurfaceVariant else LuminousOnSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                wallets.forEach { wallet ->
                    DropdownMenuItem(
                        text = { Text(wallet.name) },
                        onClick = {
                            onWalletSelected(wallet.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = onTransfer,
            enabled = canTransfer,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600)
        ) {
            Text("Chuyển tiền", fontWeight = FontWeight.Bold)
        }

        if (wallets.isEmpty()) {
            Text(
                text = "Cần ít nhất một ví đang hoạt động khác để chuyển tiền.",
                fontSize = 12.sp,
                color = LuminousOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArchiveWalletPanel(onArchive: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LuminousErrorContainer)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Archive,
                contentDescription = null,
                tint = LuminousError,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Lưu trữ ví",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = LuminousOnErrorContainer
                )
                Text(
                    text = "Đóng băng ví và ngừng tạo giao dịch mới từ ví này.",
                    fontSize = 12.sp,
                    color = LuminousOnErrorContainer
                )
            }
        }
        OutlinedButton(
            onClick = onArchive,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LuminousError)
        ) {
            Text("Lưu trữ ví", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WalletEditorPreview(
    type: WalletType,
    iconKey: String,
    name: String,
    balanceText: String,
    isEdit: Boolean
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
                    .background(WalletIconMap.backgroundFor(type)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = WalletIconMap.toVector(iconKey),
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
    onSelect: (WalletType) -> Unit
) {
    val options = listOf(
        WalletTypeOption(WalletType.CASH, "Tiền mặt", Icons.Outlined.Payments),
        WalletTypeOption(WalletType.BANK, "Ngân hàng", Icons.Outlined.AccountBalance),
        WalletTypeOption(WalletType.EWALLET, "Ví điện tử", Icons.Outlined.PhoneAndroid),
        WalletTypeOption(WalletType.CREDIT_CARD, "Tín dụng", Icons.Outlined.CreditCard),
        WalletTypeOption(WalletType.SAVINGS, "Tiết kiệm", Icons.Outlined.Savings),
        WalletTypeOption(WalletType.INVESTMENT, "Đầu tư", WalletIconMap.toVector("investment"))
    )

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
private fun WalletTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: String? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LuminousOnSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
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
    }
}

@Composable
private fun DefaultWalletToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Mặc định",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LuminousOnSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) OceanBlue50 else LuminousSurfaceContainerLow)
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (checked) "Đang bật" else "Tắt",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (checked) OceanBlue600 else LuminousOnSurfaceVariant
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = LuminousSurfaceContainerLowest,
                    checkedTrackColor = OceanBlue600
                )
            )
        }
    }
}

private data class WalletTypeOption(
    val type: WalletType,
    val label: String,
    val icon: ImageVector
)

private fun String.digitsOnly(): String = filter { it.isDigit() }

private fun String.toAmount(): Double = toDoubleOrNull() ?: 0.0

private fun String.toNullableAmount(): Double? = takeIf { it.isNotBlank() }?.toAmount()

private fun String.toDayOrNull(): Int? = toIntOrNull()?.takeIf { it in 1..31 }

private fun Double.toAmountInput(): String =
    takeIf { it != 0.0 }?.toLong()?.toString().orEmpty()
