package com.dung.ddmoney.ui.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import kotlinx.coroutines.launch

// --- Ocean Blue Palette ---
private val PrimaryOceanBlue = Color(0xFF003CC7)
private val OceanBlueLight = Color(0xFFEAF1FF)
private val BackgroundColor = Color(0xFFF4F6FB)
private val CardSurface = Color(0xFFFAFBFF)
private val DividerColor = Color(0xFFE6E8EF)
private val PrimaryText = Color(0xFF111318)
private val SecondaryText = Color(0xFF8D93A1)
private val DisabledButton = Color(0xFFCFCFCF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    wallets: List<com.dung.ddmoney.ui.dashboard.model.Wallet> = emptyList(),
    categories: List<com.dung.ddmoney.ui.dashboard.model.Category> = emptyList(),
    onSave: (amount: Double, walletId: String, categoryId: String, type: String, note: String, date: LocalDate) -> Unit = { _, _, _, _, _, _ -> },
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Khoản chi, 1: Khoản thu, 2: Vay/Nợ
    var amountText by remember { mutableStateOf("0") }
    var selectedWallet by remember { mutableStateOf(wallets.firstOrNull()) }
    var selectedCategory by remember { mutableStateOf<com.dung.ddmoney.ui.dashboard.model.Category?>(null) }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showNumPad by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showWalletPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    val filteredCategories = remember(selectedTab, categories) {
        val type = when(selectedTab) {
            0 -> com.dung.ddmoney.ui.dashboard.model.CategoryType.EXPENSE
            1 -> com.dung.ddmoney.ui.dashboard.model.CategoryType.INCOME
            else -> com.dung.ddmoney.ui.dashboard.model.CategoryType.DEBT
        }
        categories.filter { it.type == type }
    }
    
    // Reset category when tab changes to keep it empty by default
    LaunchedEffect(selectedTab) {
        selectedCategory = null
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
                }) { Text("Chọn", color = PrimaryOceanBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Huỷ", color = SecondaryText) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundColor,
        scrimColor = Color.Black.copy(alpha = 0.6f), // Làm mờ nền sau đậm hơn để làm nổi bật khung Modal
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null // Bỏ thanh kéo ngang để giống hệt giao diện được yêu cầu
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.95f) // 90% màn hình
                .fillMaxWidth()
        ) {
            Header(onDismiss = onDismiss)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = CardSurface,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        SegmentedTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                        Spacer(modifier = Modifier.height(16.dp))

                        // Wallet Row
                        FormRow(
                            onClick = { showWalletPicker = true },
                            icon = {
                                Box(
                                    modifier = Modifier.size(28.dp).background(selectedWallet?.color ?: SecondaryText, CircleShape), 
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.size(10.dp).background(Color.White, RoundedCornerShape(2.dp)))
                                }
                            },
                            content = {
                                Text(text = selectedWallet?.name ?: "Ví nguồn", fontSize = 16.sp, color = PrimaryText, fontWeight = FontWeight.Medium)
                            }
                        )
                        HorizontalDivider(color = DividerColor, thickness = 1.dp)

                        // Amount Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showNumPad = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, DividerColor),
                                    color = Color.White
                                ) {
                                    Text("VND", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 10.sp, color = SecondaryText, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Số tiền", fontSize = 13.sp, color = SecondaryText)
                                Text(text = amountText, fontSize = 34.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                            }
                        }
                        HorizontalDivider(color = DividerColor, thickness = 1.dp)

                        // Category Row
                        FormRow(
                            onClick = { showCategoryPicker = true },
                            icon = {
                                Box(
                                    modifier = Modifier.size(28.dp).background(if (selectedCategory != null) OceanBlueLight else Color(0xFFD9D9D9), CircleShape), 
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedCategory != null) {
                                        Text(selectedCategory!!.icon, fontSize = 14.sp)
                                    }
                                }
                            },
                            content = {
                                Text(
                                    text = selectedCategory?.name ?: "Chọn nhóm", 
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedCategory != null) PrimaryText else SecondaryText
                                )
                            }
                        )
                        HorizontalDivider(color = DividerColor, thickness = 1.dp)

                        // Note Row
                        FormRow(
                            onClick = { },
                            icon = {
                                Icon(Icons.AutoMirrored.Outlined.Subject, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(24.dp))
                            },
                            content = {
                                BasicTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = PrimaryText, fontWeight = FontWeight.Medium),
                                    decorationBox = { innerTextField ->
                                        if (note.isEmpty()) {
                                            Text("Ghi chú", color = SecondaryText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            showChevron = true
                        )
                        HorizontalDivider(color = DividerColor, thickness = 1.dp)

                        // Date Row
                        DateRow(
                            selectedDate = selectedDate,
                            onPrevious = { selectedDate = selectedDate.minusDays(1) },
                            onNext = { selectedDate = selectedDate.plusDays(1) },
                            onClick = { showDatePicker = true }
                        )
                    }
                }

                // "Thêm chi tiết" Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .clickable { },
                    shape = RoundedCornerShape(50),
                    color = CardSurface,
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = "Thêm chi tiết",
                        modifier = Modifier.padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        color = PrimaryOceanBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                
                // Error Message
                androidx.compose.animation.AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            val amountValue = amountText.toDoubleOrNull() ?: 0.0
            val canSave = amountValue > 0 && selectedWallet != null && selectedCategory != null
            
            SaveButton(
                canSave = canSave,
                onClick = {
                    val typeStr = when(selectedTab) {
                        0 -> "EXPENSE"
                        1 -> "INCOME"
                        else -> "DEBT"
                    }
                    onSave(amountValue, selectedWallet!!.id, selectedCategory!!.id, typeStr, note, selectedDate)
                }
            )

            NumPadOverlay(
                showNumPad = showNumPad,
                amountText = amountText,
                onAmountChange = { amountText = it },
                onDone = { showNumPad = false }
            )
        }
    }

    WalletPickerSheet(showWalletPicker, wallets, onDismiss = { showWalletPicker = false }, onSelect = { selectedWallet = it })
    CategoryPickerSheet(showCategoryPicker, filteredCategories, onDismiss = { showCategoryPicker = false }, onSelect = { selectedCategory = it })
}

@Composable
private fun Header(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Surface(
            onClick = onDismiss,
            shape = CircleShape,
            color = CardSurface,
            modifier = Modifier.align(Alignment.CenterStart),
            shadowElevation = 1.dp
        ) {
            Text(
                "Huỷ", 
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), 
                fontWeight = FontWeight.Medium,
                color = PrimaryText
            )
        }
        Text(
            text = "Thêm Giao Dịch",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun SegmentedTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Khoản chi", "Khoản thu", "Vay/Nợ")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F1F4), RoundedCornerShape(50))
            .padding(2.dp)
    ) {
        val animatedWeightBefore by androidx.compose.animation.core.animateFloatAsState(
            targetValue = selectedTab.toFloat(),
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
            ),
            label = "tab_indicator"
        )

        // Animated Active Tab Indicator
        Row(modifier = Modifier.matchParentSize()) {
            if (animatedWeightBefore > 0f) {
                Spacer(modifier = Modifier.weight(animatedWeightBefore))
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(50),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.04f))
            ) {}
            
            val weightAfter = (tabs.size - 1).toFloat() - animatedWeightBefore
            if (weightAfter > 0f) {
                Spacer(modifier = Modifier.weight(weightAfter))
            }
        }

        // Tab Texts
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) }
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = PrimaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun FormRow(onClick: () -> Unit = {}, icon: @Composable () -> Unit, content: @Composable () -> Unit, showChevron: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 16.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) {
            icon()
        }
        Box(modifier = Modifier.weight(1f)) { content() }
        if (showChevron) { 
            Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = SecondaryText, modifier = Modifier.size(20.dp)) 
        }
    }
}

@Composable
private fun DateRow(selectedDate: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) {
            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(24.dp))
        }
        
        Surface(
            shape = CircleShape, 
            color = BackgroundColor, 
            modifier = Modifier.size(32.dp).clickable { onPrevious() }
        ) {
            Icon(Icons.Outlined.ChevronLeft, contentDescription = null, modifier = Modifier.padding(4.dp), tint = PrimaryOceanBlue)
        }
        
        Surface(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            shape = RoundedCornerShape(50),
            color = BackgroundColor
        ) {
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")),
                modifier = Modifier.padding(vertical = 10.dp).clickable { onClick() },
                textAlign = TextAlign.Center,
                color = PrimaryOceanBlue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        
        Surface(
            shape = CircleShape, 
            color = BackgroundColor, 
            modifier = Modifier.size(32.dp).clickable { onNext() }
        ) {
            Icon(Icons.AutoMirrored.Outlined.NavigateNext, contentDescription = null, modifier = Modifier.padding(4.dp), tint = PrimaryOceanBlue)
        }
    }
}

@Composable
private fun SaveButton(canSave: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(50),
        color = if (canSave) PrimaryOceanBlue else DisabledButton,
        shadowElevation = if (canSave) 4.dp else 0.dp
    ) {
        Text(
            text = "Lưu",
            modifier = Modifier
                .clickable(enabled = canSave) { onClick() }
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NumPadOverlay(showNumPad: Boolean, amountText: String, onAmountChange: (String) -> Unit, onDone: () -> Unit) {
    androidx.compose.animation.AnimatedVisibility(
        visible = showNumPad,
        enter = androidx.compose.animation.expandVertically(expandFrom = Alignment.Bottom),
        exit = androidx.compose.animation.shrinkVertically(shrinkTowards = Alignment.Bottom)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardSurface,
            shadowElevation = 16.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val suggestions = listOf("20,000", "200,000", "2,000,000", "20m")
                    suggestions.forEach { sug ->
                        Surface(
                            modifier = Modifier.weight(1f).clickable { onAmountChange(if(sug == "20m") "20000000" else sug.replace(",", "")) },
                            shape = RoundedCornerShape(12.dp),
                            color = OceanBlueLight,
                        ) {
                            Text(text = sug, modifier = Modifier.padding(vertical = 10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryOceanBlue, textAlign = TextAlign.Center)
                        }
                    }
                }

                CustomNumPad(
                    onKeyPress = { key ->
                        when (key) {
                            "C" -> onAmountChange("0")
                            "DEL" -> if (amountText.length > 1) onAmountChange(amountText.dropLast(1)) else onAmountChange("0")
                            else -> if (amountText == "0" && key != "." && key != "000") onAmountChange(key) else if (amountText.length < 12) onAmountChange(amountText + key)
                        }
                    },
                    onDone = onDone
                )
            }
        }
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
                        NumPadButton(text = keys[i][j], modifier = Modifier.weight(1f).height(56.dp).padding(4.dp), onClick = { onKeyPress(keys[i][j]) }, isOperator = keys[i][j] in listOf("C", "÷", "×", "-", "+"))
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                NumPadButton("0", Modifier.weight(1f).height(56.dp).padding(4.dp), { onKeyPress("0") })
                NumPadButton("000", Modifier.weight(1f).height(56.dp).padding(4.dp), { onKeyPress("000") })
                NumPadButton(".", Modifier.weight(1f).height(56.dp).padding(4.dp), { onKeyPress(".") })
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            NumPadButton(text = "DEL", modifier = Modifier.fillMaxWidth().height(56.dp).padding(4.dp), onClick = { onKeyPress("DEL") }, isOperator = true, isIcon = true, icon = Icons.Default.KeyboardBackspace)
            NumPadButton("-", Modifier.fillMaxWidth().height(56.dp).padding(4.dp), { onKeyPress("-") }, true)
            NumPadButton("+", Modifier.fillMaxWidth().height(56.dp).padding(4.dp), { onKeyPress("+") }, true)
            Surface(modifier = Modifier.fillMaxWidth().height(112.dp).padding(4.dp).clickable { onDone() }, shape = RoundedCornerShape(12.dp), color = PrimaryOceanBlue, shadowElevation = 2.dp) {
                Box(contentAlignment = Alignment.Center) { Text("XONG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            }
        }
    }
}

@Composable
private fun NumPadButton(text: String, modifier: Modifier, onClick: () -> Unit, isOperator: Boolean = false, isIcon: Boolean = false, icon: ImageVector? = null) {
    Surface(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(12.dp), color = if (isOperator) BackgroundColor else CardSurface, border = if (!isOperator) BorderStroke(1.dp, DividerColor) else null, shadowElevation = if (isOperator) 0.dp else 1.dp) {
        Box(contentAlignment = Alignment.Center) {
            if (isIcon && icon != null) { Icon(imageVector = icon, contentDescription = text, tint = PrimaryOceanBlue, modifier = Modifier.size(24.dp)) }
            else { Text(text = text, fontSize = 22.sp, fontWeight = if (isOperator) FontWeight.Bold else FontWeight.Medium, color = if (isOperator) PrimaryOceanBlue else PrimaryText) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletPickerSheet(show: Boolean, wallets: List<com.dung.ddmoney.ui.dashboard.model.Wallet>, onDismiss: () -> Unit, onSelect: (com.dung.ddmoney.ui.dashboard.model.Wallet) -> Unit) {
    if (show) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = CardSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            scrimColor = Color.Black.copy(alpha = 0.2f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).fillMaxWidth()) {
                Text("Chọn ví", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryText, modifier = Modifier.padding(bottom = 16.dp))
                wallets.forEach { wallet ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onSelect(wallet); onDismiss() }.padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(wallet.color), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(wallet.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PrimaryText)
                            Text(String.format("%,.0f VND", wallet.balance), fontSize = 13.sp, color = SecondaryText)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(show: Boolean, categories: List<com.dung.ddmoney.ui.dashboard.model.Category>, onDismiss: () -> Unit, onSelect: (com.dung.ddmoney.ui.dashboard.model.Category) -> Unit) {
    if (show) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = CardSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            scrimColor = Color.Black.copy(alpha = 0.2f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).fillMaxWidth()) {
                Text("Chọn hạng mục", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryText, modifier = Modifier.padding(bottom = 16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(categories) { cat ->
                        Column(
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onSelect(cat); onDismiss() }.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(cat.color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                Text(cat.icon, fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(cat.name, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, color = PrimaryText, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
