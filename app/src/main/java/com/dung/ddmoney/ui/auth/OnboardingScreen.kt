package com.dung.ddmoney.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.dashboard.model.WalletType
import com.dung.ddmoney.ui.wallets.WalletIconMap
import com.dung.ddmoney.ui.components.parseMoneyInput

private enum class Currency(
        val code: String,
        val flag: String,
        val label: String,
        val symbol: String
) {
        VND("VND", "🇻🇳", "Việt Nam Đồng", "₫"),
        USD("USD", "🇺🇸", "Đô la Mỹ", "$")
}

@Composable
fun OnboardingScreen(
        userName: String = "",
        isLoading: Boolean = false,
        /** walletIcon: key string lưu DB, ví dụ "wallet", "savings", "food" */
        onComplete: (currency: String, walletName: String, walletBalance: Double, walletIcon: String, walletType: WalletType) -> Unit
) {
        var step by remember { mutableIntStateOf(0) }
        var selectedCurrency by remember { mutableStateOf(Currency.VND) }
        var walletName by remember { mutableStateOf("") }
        var walletBalanceText by remember { mutableStateOf("") }
        // Khởi tạo từ default key trong WalletIconMap
        var selectedIcon by remember { mutableStateOf(WalletIconMap.toVector(WalletIconMap.DEFAULT_KEY)) }
        var selectedWalletType by remember { mutableStateOf(WalletType.CASH) }

        val focusManager = LocalFocusManager.current

        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(Color(0xFFF3F4F8)) // Matching the very light background
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 24.dp)
                                        .padding(top = 80.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        AnimatedContent(
                                targetState = step,
                                transitionSpec = {
                                        (fadeIn(tween(300)) +
                                                        slideInHorizontally(tween(300)) { it / 2 })
                                                .togetherWith(
                                                        fadeOut(tween(300)) +
                                                                slideOutHorizontally(tween(300)) {
                                                                        -it / 2
                                                                }
                                                )
                                },
                                label = "stepContent"
                        ) { currentStep ->
                                when (currentStep) {
                                        0 ->
                                                CurrencyStep(
                                                        selected = selectedCurrency,
                                                        onSelect = { selectedCurrency = it }
                                                )
                                        1 ->
                                                WalletStep(
                                                        walletName = walletName,
                                                        onNameChange = { walletName = it },
                                                        selectedIcon = selectedIcon,
                                                        onIconSelect = { selectedIcon = it },
                                                        selectedWalletType = selectedWalletType,
                                                        onWalletTypeSelect = { selectedWalletType = it },
                                                        focusManager = focusManager
                                                )
                                }
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // Button
                        val buttonText = if (step == 0) "Tiếp tục" else "Xong"
                        val buttonIcon =
                                if (step == 0) Icons.Filled.ArrowForward else Icons.Filled.Check
                        val isEnabled = if (step == 1) walletName.isNotBlank() else true

                        Button(
                                onClick = {
                                        if (step == 0) {
                                                step++
                                        } else {
                                                focusManager.clearFocus()
                                                onComplete(
                                                        selectedCurrency.code,
                                                        walletName.ifBlank { "Ví của tôi" },
                                                        parseMoneyInput(walletBalanceText),
                                                        WalletIconMap.toKey(selectedIcon),   // chuyển ImageVector → key string lưu DB
                                                        selectedWalletType
                                                )
                                        }
                                },
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(56.dp)
                                                .shadow(
                                                        elevation =
                                                                if (isEnabled && !isLoading) 12.dp
                                                                else 0.dp,
                                                        shape = RoundedCornerShape(16.dp),
                                                        ambientColor = Color(0x330047FF),
                                                        spotColor = Color(0x330047FF)
                                                ),
                                enabled = isEnabled && !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF0047FF),
                                                disabledContainerColor =
                                                        Color(0xFF0047FF).copy(alpha = 0.5f)
                                        )
                        ) {
                                if (isLoading && step == 1) {
                                        CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                        )
                                } else {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                        ) {
                                                Text(
                                                        text = buttonText,
                                                        color = Color.White,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                        imageVector = buttonIcon,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                )
                                        }
                                }
                        }

                        if (step == 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                        text = "BẠN CÓ THỂ THAY ĐỔI SAU TRONG CÀI ĐẶT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp,
                                        color = Color(0xFF9E9E9E)
                                )
                        } else {
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = { step = 0 }) {
                                        Text(
                                                "Quay lại",
                                                color = Color(0xFF737688),
                                                fontWeight = FontWeight.SemiBold
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun CurrencyStep(selected: Currency, onSelect: (Currency) -> Unit) {
        Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Text(
                        text = "Chọn loại tiền tệ chính",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF1C2024),
                        textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Currency.entries.forEach { currency ->
                                val isSelected = currency == selected
                                // Surface xử lý shadow + clip + border cùng 1 layer.
                                // Đưa clip lên trước background tránh răng cưa ở cạnh bo góc.
                                Surface(
                                        modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelect(currency) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        shadowElevation = 8.dp,
                                        border = if (isSelected)
                                                BorderStroke(1.5.dp, Color(0xFF0047FF))
                                        else null
                                ) {
                                Row(
                                        modifier = Modifier.padding(
                                                horizontal = 20.dp,
                                                vertical = 20.dp
                                        ),

                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                // Flag icon or emoji inside a circle
                                                Box(
                                                        modifier =
                                                                Modifier.size(44.dp)
                                                                        .clip(CircleShape)
                                                                        .background(
                                                                                Color(0xFFF3F4F8)
                                                                        ),
                                                        contentAlignment = Alignment.Center
                                                ) { Text(currency.flag, fontSize = 24.sp) }

                                                Column {
                                                        Text(
                                                                text = currency.code,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 16.sp,
                                                                color = Color(0xFF1C2024)
                                                        )
                                                        Text(
                                                                text = currency.label,
                                                                fontSize = 13.sp,
                                                                color = Color(0xFF737688)
                                                        )
                                                }
                                        }

                                        // Custom Radio button
                                        // Dùng Surface với CircleShape: border được vẽ trong cùng
                                        // render layer với clip → không có mép răng cưa.
                                        Surface(
                                                modifier = Modifier.size(24.dp),
                                                shape = CircleShape,
                                                color = Color.Transparent,
                                                border = BorderStroke(
                                                        width = 2.dp,
                                                        color = if (isSelected) Color(0xFF0047FF) else Color(0xFFE0E2E8)
                                                )
                                                ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                        if (isSelected) {
                                                                Box(
                                                                        modifier = Modifier
                                                                                .size(12.dp)
                                                                                .clip(CircleShape)
                                                                                .background(Color(0xFF0047FF))
                                                                )
                                                        }
                                                }
                                        } // đóng Surface radio
                                } // đóng Row ngoài
                                } // đóng Surface currency card
                        }
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletStep(
        walletName: String,
        onNameChange: (String) -> Unit,
        selectedIcon: ImageVector,
        onIconSelect: (ImageVector) -> Unit,
        selectedWalletType: WalletType,
        onWalletTypeSelect: (WalletType) -> Unit,
        focusManager: androidx.compose.ui.focus.FocusManager
) {
        var showIconPicker by remember { mutableStateOf(false) }
        val icons =
                listOf(
                        Icons.Outlined.AccountBalanceWallet,
                        Icons.Outlined.Savings,
                        Icons.Outlined.ShoppingCart,
                        Icons.Outlined.DirectionsCar,
                        Icons.Outlined.Home,
                        Icons.Outlined.Flight,
                        Icons.Outlined.Restaurant,
                        Icons.Outlined.Celebration,
                        Icons.Outlined.School,
                        Icons.Outlined.MoreHoriz
                )

        Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Text(
                        text = "Ví đầu tiên",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF1C2024),
                        textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                        text = "Thiết lập ví của bạn chỉ trong vài giây.",
                        fontSize = 15.sp,
                        color = Color(0xFF5A5D6B),
                        textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                                text = "TÊN VÍ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color(0xFF5A5D6B)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(60.dp)
                                                .background(
                                                        Color(0xFFEFEFFA),
                                                        RoundedCornerShape(16.dp)
                                                )
                                                .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterStart
                        ) {
                                BasicTextField(
                                        value = walletName,
                                        onValueChange = onNameChange,
                                        textStyle =
                                                TextStyle(
                                                        fontSize = 16.sp,
                                                        color = Color(0xFF1C2024),
                                                        fontWeight = FontWeight.Medium
                                                ),
                                        singleLine = true,
                                        keyboardOptions =
                                                KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions =
                                                KeyboardActions(
                                                        onDone = { focusManager.clearFocus() }
                                                ),
                                        cursorBrush = SolidColor(Color(0xFF0047FF)),
                                        modifier = Modifier.fillMaxWidth()
                                )
                                if (walletName.isEmpty()) {
                                        Text(
                                                text = "Ví dụ: Ăn uống, Tiết kiệm...",
                                                color = Color(0xFF9E9E9E),
                                                fontSize = 16.sp
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // ── Loại ví ──────────────────────────────────────────
                        Text(
                                text = "LOẠI VÍ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color(0xFF5A5D6B)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        data class WalletTypeOption(
                                val type: WalletType,
                                val label: String,
                                val icon: ImageVector,
                                val iconBg: Color,
                                val iconTint: Color
                        )
                        val walletTypes = listOf(
                                WalletTypeOption(WalletType.CASH,    "Tiền mặt",     Icons.Rounded.Payments,       Color(0xFFE8F5E9), Color(0xFF2E7D32)),
                                WalletTypeOption(WalletType.BANK,    "Ngân hàng",    Icons.Rounded.AccountBalance,  Color(0xFFE3F2FD), Color(0xFF1565C0)),
                                WalletTypeOption(WalletType.EWALLET, "Ví điện tử",   Icons.Rounded.PhoneAndroid,    Color(0xFFF3E5F5), Color(0xFF6A1B9A)),
                                WalletTypeOption(WalletType.CREDIT_CARD,  "Thẻ tín dụng", Icons.Rounded.CreditCard,      Color(0xFFFFF3E0), Color(0xFFE65100))
                        )
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                                walletTypes.forEach { option ->
                                        val isSelected = option.type == selectedWalletType
                                        // Surface xử lý shadow + clip + border cùng 1 layer.
                                        Surface(
                                                modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { onWalletTypeSelect(option.type) },
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color.White,
                                                shadowElevation = if (isSelected) 8.dp else 1.dp,
                                                border = BorderStroke(
                                                        width = if (isSelected) 2.dp else 1.dp,
                                                        color = if (isSelected) option.iconTint else Color(0xFFE8E8F0)
                                                )
                                        ) {
                                        Column(
                                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp),

                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                // Icon trong circle màu
                                                Box(
                                                        modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                        if (isSelected) option.iconTint.copy(alpha = 0.15f)
                                                                        else option.iconBg
                                                                ),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Icon(
                                                                imageVector = option.icon,
                                                                contentDescription = option.label,
                                                                tint = option.iconTint,
                                                                modifier = Modifier.size(24.dp)
                                                        )
                                                }
                                                Text(
                                                        text = option.label,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        textAlign = TextAlign.Center,
                                                        lineHeight = 13.sp,
                                                        color = if (isSelected) option.iconTint else Color(0xFF5A5D6B)
                                                )
                                        } // đóng Column
                                        } // đóng Surface wallet type
                                } // đóng forEach
                        } // đóng Row

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                                text = "BIỂU TƯỢNG",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color(0xFF5A5D6B)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected Icon Box
                        Box(
                                modifier =
                                        Modifier.size(64.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(0xFFEFEFFA))
                                                .clickable { showIconPicker = true },
                                contentAlignment = Alignment.Center
                        ) {
                                Icon(
                                        imageVector = selectedIcon,
                                        contentDescription = "Chọn biểu tượng",
                                        tint = Color(0xFF0047FF),
                                        modifier = Modifier.size(32.dp)
                                )
                        }
                }
        }

        if (showIconPicker) {
                ModalBottomSheet(
                        onDismissRequest = { showIconPicker = false },
                        containerColor = Color.White,
                        dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                                .padding(bottom = 24.dp)
                        ) {
                                Text(
                                        text = "Chọn biểu tượng",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C2024),
                                        modifier = Modifier.padding(bottom = 24.dp)
                                )

                                // Icon grid inside BottomSheet
                                val columns = 5
                                val rows = icons.chunked(columns)
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        rows.forEach { rowIcons ->
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        rowIcons.forEach { icon ->
                                                                val isSelected = icon == selectedIcon
                                                                Box(
                                                                        modifier =
                                                                                Modifier.size(56.dp)
                                                                                        .clip(
                                                                                                RoundedCornerShape(
                                                                                                        16.dp
                                                                                                )
                                                                                        )
                                                                                        .background(
                                                                                                if (isSelected
                                                                                                )
                                                                                                        Color(
                                                                                                                0xFF2C6BFF
                                                                                                        )
                                                                                                else
                                                                                                        Color(
                                                                                                                0xFFF3F4F8
                                                                                                        )
                                                                                        )
                                                                                        .clickable {
                                                                                                onIconSelect(icon)
                                                                                                showIconPicker = false
                                                                                        },
                                                                        contentAlignment = Alignment.Center
                                                                ) {
                                                                        Icon(
                                                                                imageVector = icon,
                                                                                contentDescription = null,
                                                                                tint =
                                                                                        if (isSelected)
                                                                                                Color.White
                                                                                        else
                                                                                                Color(0xFF3B3D4A),
                                                                                modifier = Modifier.size(24.dp)
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}
