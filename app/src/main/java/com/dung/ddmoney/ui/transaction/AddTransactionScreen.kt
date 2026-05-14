package com.dung.ddmoney.ui.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Subject
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    wallets: List<com.dung.ddmoney.ui.dashboard.model.Wallet> = emptyList(),
    onSave: (amount: Double, walletId: String, note: String, date: LocalDate) -> Unit = { _, _, _, _ -> },
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Chi, 1: Thu, 2: Vay
    var amountText by remember { mutableStateOf("0") }
    var selectedWallet by remember { mutableStateOf(wallets.firstOrNull()) }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showNumPad by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showWalletPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    val primaryColor = when(selectedTab) {
        0 -> ExpenseRed600
        1 -> IncomeGreen600
        else -> SavingsTeal600
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = java.time.Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Chọn", color = OceanBlue600) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Huỷ") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Dim background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp) // Slightly smaller gap
                .clickable(enabled = false) { }
                .animateContentSize() // Smooth transition when NumPad appears
        ) {
            // --- Header & Form Card ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = LuminousBackground
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // --- Header ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onDismiss,
                            shape = CircleShape,
                            color = LuminousSurfaceContainerHigh,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                                Text("Huỷ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LuminousOnSurface)
                            }
                        }
                        
                        Text(
                            text = "Thêm Giao Dịch",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuminousOnSurface
                        )
                        
                        Spacer(modifier = Modifier.width(72.dp))
                    }

                    // --- Form Content ---
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(LuminousSurfaceContainerLowest)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 20.dp)
                    ) {
                        // Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(LuminousSurfaceContainerLow, RoundedCornerShape(16.dp))
                                .padding(4.dp)
                        ) {
                            val tabs = listOf("Khoản chi", "Khoản thu", "Vay/Nợ")
                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedTab == index
                                val tabColor = if (isSelected) {
                                    when(index) {
                                        0 -> ExpenseRed600
                                        1 -> IncomeGreen600
                                        else -> SavingsTeal600
                                    }
                                } else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tabColor)
                                        .clickable { selectedTab = index }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else NeutralGray600
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Wallet selector
                        FormRow(
                            onClick = { showWalletPicker = true },
                            icon = {
                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(OceanBlue50), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.AccountBalanceWallet, null, tint = OceanBlue600, modifier = Modifier.size(20.dp))
                                }
                            },
                            content = {
                                Column {
                                    Text("Ví nguồn", fontSize = 11.sp, color = NeutralGray600)
                                    Text(text = selectedWallet?.name ?: "Tiet Kiem", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuminousOnSurface)
                                }
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp, end = 24.dp), color = LuminousSurfaceContainerLow)

                        // Amount Display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showNumPad = !showNumPad }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = OceanBlue50) {
                                Text(text = "VND", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Black, color = OceanBlue600)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Số tiền", fontSize = 11.sp, color = NeutralGray600)
                                Text(text = amountText, fontSize = 36.sp, fontWeight = FontWeight.Black, color = primaryColor)
                            }
                            if (amountText != "0") {
                                IconButton(onClick = { amountText = "0" }, modifier = Modifier.size(24.dp).background(LuminousSurfaceContainerHigh, CircleShape)) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = NeutralGray600)
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp, end = 24.dp), color = LuminousSurfaceContainerLow)

                        // Category
                        FormRow(
                            onClick = { showCategoryPicker = true },
                            icon = {
                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(LuminousSurfaceContainerLow), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Category, null, tint = NeutralGray400, modifier = Modifier.size(20.dp))
                                }
                            },
                            content = { Text("Chọn hạng mục", fontSize = 16.sp, color = NeutralGray400) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp, end = 24.dp), color = LuminousSurfaceContainerLow)

                        // Note
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(LuminousSurfaceContainerLow), contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Outlined.Subject, null, tint = NeutralGray600, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                value = note,
                                onValueChange = { note = it },
                                placeholder = { Text("Ghi chú", color = NeutralGray400) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = LuminousOnSurface)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp, end = 24.dp), color = LuminousSurfaceContainerLow)

                        // Date
                        FormRow(
                            onClick = { showDatePicker = true },
                            icon = {
                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(LuminousSurfaceContainerLow), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.CalendarToday, null, tint = NeutralGray600, modifier = Modifier.size(20.dp))
                                }
                            },
                            content = {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    IconButton(onClick = { selectedDate = selectedDate.minusDays(1) }) { Icon(Icons.Outlined.ChevronLeft, null, tint = OceanBlue600, modifier = Modifier.size(24.dp)) }
                                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(OceanBlue50).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                        Text(text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OceanBlue600)
                                    }
                                    IconButton(onClick = { selectedDate = selectedDate.plusDays(1) }) { Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = OceanBlue600, modifier = Modifier.size(24.dp)) }
                                }
                            },
                            showChevron = false
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "+ Thêm chi tiết chi tiêu", modifier = Modifier.fillMaxWidth().clickable { }, textAlign = TextAlign.Center, color = OceanBlue600, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    // --- Save Button ---
                    androidx.compose.animation.AnimatedVisibility(visible = !showNumPad) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Transparent
                        ) {
                            Button(
                                onClick = { 
                                    val amountValue = amountText.toDoubleOrNull() ?: 0.0
                                    selectedWallet?.let { onSave(amountValue, it.id, note, selectedDate) }
                                },
                                enabled = amountText != "0" && (selectedWallet != null || wallets.isNotEmpty()),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600, disabledContainerColor = NeutralGray100),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("LƯU GIAO DỊCH", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
            }

            // --- NumPad Area ---
            androidx.compose.animation.AnimatedVisibility(
                visible = showNumPad,
                enter = androidx.compose.animation.expandVertically(expandFrom = Alignment.Bottom) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Bottom) + androidx.compose.animation.fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LuminousSurfaceContainerLowest,
                    shadowElevation = 32.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(bottom = 24.dp)) {
                        // Quick Suggestions
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val suggestions = listOf("20,000", "200,000", "2,000,000", "20m")
                            suggestions.forEach { sug ->
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { amountText = if(sug == "20m") "20000000" else sug.replace(",", "") },
                                    shape = RoundedCornerShape(12.dp),
                                    color = LuminousSurfaceContainerLow,
                                    border = BorderStroke(1.dp, LuminousSurfaceContainerHigh)
                                ) {
                                    Text(text = sug, modifier = Modifier.padding(vertical = 10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OceanBlue600, textAlign = TextAlign.Center)
                                }
                            }
                        }

                        // NumPad
                        CustomNumPad(
                            onKeyPress = { key ->
                                when (key) {
                                    "C" -> amountText = "0"
                                    "DEL" -> if (amountText.length > 1) amountText = amountText.dropLast(1) else amountText = "0"
                                    else -> if (amountText == "0" && key != "." && key != "000") amountText = key else if (amountText.length < 12) amountText += key
                                }
                            },
                            onDone = { showNumPad = false }
                        )
                    }
                }
            }
        }
    }

    // Dummy Wallet Picker
    if (showWalletPicker) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showWalletPicker = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Chọn ví nguồn", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    wallets.forEach { wallet ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { selectedWallet = wallet; showWalletPicker = false }.padding(vertical = 12.dp)) {
                            Text(wallet.name)
                        }
                    }
                    if (wallets.isEmpty()) {
                        Text("Không có ví nào", color = Color.Gray)
                    }
                }
            }
        }
    }

    // Dummy Category Picker
    if (showCategoryPicker) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCategoryPicker = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Chọn hạng mục", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf("Ăn uống", "Di chuyển", "Mua sắm", "Giải trí").forEach { cat ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { showCategoryPicker = false }.padding(vertical = 12.dp)) {
                            Text(cat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormRow(onClick: () -> Unit = {}, icon: @Composable () -> Unit, content: @Composable () -> Unit, showChevron: Boolean = true) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 24.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier.weight(1f)) { content() }
        if (showChevron) { Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = NeutralGray400, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
private fun CustomNumPad(onKeyPress: (String) -> Unit, onDone: () -> Unit) {
    val keys = listOf(
        listOf("C", "÷", "×", "DEL"),
        listOf("7", "8", "9", "-"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "XONG"),
        listOf("0", "000", ".", "XONG")
    )

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.weight(3f)) {
            for (i in 0..3) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (j in 0..2) {
                        NumPadButton(text = keys[i][j], modifier = Modifier.weight(1f).height(52.dp).padding(4.dp), onClick = { onKeyPress(keys[i][j]) }, isOperator = keys[i][j] in listOf("C", "÷", "×", "-", "+"))
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                NumPadButton("0", Modifier.weight(1f).height(52.dp).padding(4.dp), { onKeyPress("0") })
                NumPadButton("000", Modifier.weight(1f).height(52.dp).padding(4.dp), { onKeyPress("000") })
                NumPadButton(".", Modifier.weight(1f).height(52.dp).padding(4.dp), { onKeyPress(".") })
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            NumPadButton(text = "DEL", modifier = Modifier.fillMaxWidth().height(52.dp).padding(4.dp), onClick = { onKeyPress("DEL") }, isOperator = true, isIcon = true, icon = Icons.Default.KeyboardBackspace)
            NumPadButton("-", Modifier.fillMaxWidth().height(52.dp).padding(4.dp), { onKeyPress("-") }, true)
            NumPadButton("+", Modifier.fillMaxWidth().height(52.dp).padding(4.dp), { onKeyPress("+") }, true)
            Surface(modifier = Modifier.fillMaxWidth().height(104.dp).padding(4.dp).clickable { onDone() }, shape = RoundedCornerShape(12.dp), color = OceanBlue600, shadowElevation = 2.dp) {
                Box(contentAlignment = Alignment.Center) { Text("XONG", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp) }
            }
        }
    }
}

@Composable
private fun NumPadButton(text: String, modifier: Modifier, onClick: () -> Unit, isOperator: Boolean = false, isIcon: Boolean = false, icon: ImageVector? = null) {
    Surface(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(12.dp), color = if (isOperator) OceanBlue50 else Color.White, border = if (!isOperator) BorderStroke(1.dp, LuminousSurfaceContainerLow) else null, shadowElevation = if (isOperator) 0.dp else 1.dp) {
        Box(contentAlignment = Alignment.Center) {
            if (isIcon && icon != null) { Icon(imageVector = icon, contentDescription = text, tint = OceanBlue600, modifier = Modifier.size(20.dp)) }
            else { Text(text = text, fontSize = 20.sp, fontWeight = if (isOperator) FontWeight.Bold else FontWeight.SemiBold, color = if (isOperator) OceanBlue600 else LuminousOnSurface) }
        }
    }
}
