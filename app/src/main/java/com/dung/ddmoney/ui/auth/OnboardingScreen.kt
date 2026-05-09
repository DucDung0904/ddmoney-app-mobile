package com.dung.ddmoney.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

// ─── Dữ liệu tiền tệ ─────────────────────────────────────────────────────────
private enum class Currency(
        val code: String,
        val flag: String,
        val label: String,
        val symbol: String
) {
    VND("VND", "🇻🇳", "Đồng Việt Nam", "₫"),
    USD("USD", "🇺🇸", "Đô la Mỹ", "$")
}

// ─── Màn hình Onboarding ─────────────────────────────────────────────────────
@Composable
fun OnboardingScreen(
        userName: String = "",
        isLoading: Boolean = false,
        onComplete: (currency: String, walletName: String, walletBalance: Double) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedCurrency by remember { mutableStateOf(Currency.VND) }
    var walletName by remember { mutableStateOf("") }
    var walletBalanceText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val floatY by
            infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec =
                            infiniteRepeatable(
                                    tween(3500, easing = EaseInOutSine),
                                    RepeatMode.Reverse
                            ),
                    label = "floatY"
            )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FF))) {

        // ── Hero gradient strip ────────────────────────────────────────
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(230.dp)
                                .background(
                                        Brush.linearGradient(
                                                colors =
                                                        listOf(
                                                                Color(0xFF002FA3),
                                                                Color(0xFF1A4FBE),
                                                                Color(0xFF4659A6)
                                                        ),
                                                start = Offset(0f, 0f),
                                                end =
                                                        Offset(
                                                                Float.POSITIVE_INFINITY,
                                                                Float.POSITIVE_INFINITY
                                                        )
                                        )
                                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * 0.9f, -20f)
                )
                drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        radius = size.width * 0.35f,
                        center = Offset(0f, size.height * 1.1f)
                )
                drawCircle(
                        color = Color(0xFF7B9FFF).copy(alpha = 0.3f),
                        radius = 35f + floatY * 10f,
                        center = Offset(size.width * 0.2f, size.height * 0.4f + floatY * 15f)
                )
            }

            // Step indicator dots
            Row(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 52.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    val isActive = i == step
                    Box(
                            modifier =
                                    Modifier.height(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                    if (i < step) Color.White.copy(alpha = 0.6f)
                                                    else if (isActive) Color.White
                                                    else Color.White.copy(alpha = 0.28f)
                                            )
                                            .animateContentSize(
                                                    spring(stiffness = Spring.StiffnessMedium)
                                            )
                                            .then(
                                                    if (isActive) Modifier.width(28.dp)
                                                    else Modifier.width(8.dp)
                                            )
                    )
                }
            }

            // Emoji per step (floating)
            val heroContent =
                    when (step) {
                        0 -> Triple("👋", "Xin chào!", "Hãy cùng thiết lập tài khoản")
                        1 -> Triple("💱", "Đơn vị tiền tệ", "Chọn loại tiền bạn sử dụng")
                        else -> Triple("👜", "Ví đầu tiên", "Thêm ví để bắt đầu theo dõi")
                    }

            Column(
                    modifier =
                            Modifier.align(Alignment.Center)
                                    .padding(top = 16.dp)
                                    .offset(y = (-floatY * 5f).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(heroContent.first, fontSize = 52.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        heroContent.second,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(heroContent.third, color = Color.White.copy(alpha = 0.68f), fontSize = 13.sp)
            }

            // Bottom curve
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(32.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                            Color(0xFFF7F9FF),
                                            RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                                    )
            )
        }

        // ── Step content ───────────────────────────────────────────────
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(top = 212.dp)
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 40.dp)
        ) {
            AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        (fadeIn(tween(280)) + slideInHorizontally(tween(300)) { it / 3 * dir })
                                .togetherWith(
                                        fadeOut(tween(200)) +
                                                slideOutHorizontally(tween(220)) { -it / 3 * dir }
                                )
                    },
                    label = "stepContent"
            ) { currentStep ->
                when (currentStep) {
                    0 -> WelcomeStep(userName = userName)
                    1 ->
                            CurrencyStep(
                                    selected = selectedCurrency,
                                    onSelect = { selectedCurrency = it }
                            )
                    else ->
                            WalletStep(
                                    walletName = walletName,
                                    walletBalanceText = walletBalanceText,
                                    currency = selectedCurrency,
                                    onNameChange = { walletName = it },
                                    onBalanceChange = { v ->
                                        if (v.isEmpty() || v.matches(Regex("^\\d*\\.?\\d*$")))
                                                walletBalanceText = v
                                    },
                                    focusManager = focusManager
                            )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CTA Button
            val ctaEnabled =
                    when (step) {
                        2 -> walletName.isNotBlank()
                        else -> true
                    }

            GradientButton(
                    text =
                            when {
                                step < 2 -> "Tiếp tục →"
                                isLoading -> "Đang thiết lập…"
                                else -> "Bắt đầu ngay 🚀"
                            },
                    onClick = {
                        if (step < 2) {
                            step++
                        } else {
                            focusManager.clearFocus()
                            onComplete(
                                    selectedCurrency.code,
                                    walletName.ifBlank { "Ví của tôi" },
                                    walletBalanceText.toDoubleOrNull() ?: 0.0
                            )
                        }
                    },
                    enabled = ctaEnabled && !isLoading,
                    isLoading = isLoading && step == 2
            )

            if (step > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = { step-- }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                            "← Quay lại",
                            color = Color(0xFF737688),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Bước 0: Chào mừng ───────────────────────────────────────────────────────
@Composable
private fun WelcomeStep(userName: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60)
        visible = true
    }

    AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(500)) { 20 }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                    buildString {
                        append("Xin chào")
                        if (userName.isNotBlank()) append(", ${userName.split(" ").last()}")
                        append("!")
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color(0xFF181C20),
                    letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    "Chỉ cần vài bước ngắn để bắt đầu theo dõi tài chính thông minh.",
                    fontSize = 14.sp,
                    color = Color(0xFF737688),
                    lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Checklist preview
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .shadow(
                                            12.dp,
                                            RoundedCornerShape(20.dp),
                                            ambientColor = Color(0x0A003CC7)
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(
                                Triple("", "Chọn đơn vị tiền tệ", "VND hoặc USD"),
                                Triple("", "Tạo ví đầu tiên", "Tiền mặt, ngân hàng…"),
                                Triple("", "Sẵn sàng rồi!", "Bắt đầu ghi chép ngay")
                        )
                        .forEachIndexed { index, (emoji, title, subtitle) ->
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                        modifier =
                                                Modifier.size(42.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(Color(0xFFF1F3F9)),
                                        contentAlignment = Alignment.Center
                                ) { Text(emoji, fontSize = 20.sp) }
                                Column {
                                    Text(
                                            title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF181C20)
                                    )
                                    Text(subtitle, fontSize = 12.sp, color = Color(0xFF737688))
                                }
                            }
                            if (index < 2) Divider(color = Color(0xFFF1F3F9))
                        }
            }
        }
    }
}

// ─── Bước 1: Chọn tiền tệ ────────────────────────────────────────────────────
@Composable
private fun CurrencyStep(selected: Currency, onSelect: (Currency) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60)
        visible = true
    }

    AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(500)) { 20 }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                    "Bạn dùng tiền tệ nào?",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    color = Color(0xFF181C20),
                    letterSpacing = (-0.4).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                    "Số tiền sẽ hiển thị theo đơn vị bạn chọn.",
                    fontSize = 14.sp,
                    color = Color(0xFF737688)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Currency.entries.forEach { currency ->
                    val isSelected = currency == selected
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .shadow(
                                                    elevation = if (isSelected) 12.dp else 0.dp,
                                                    shape = RoundedCornerShape(18.dp),
                                                    ambientColor = Color(0x18003CC7)
                                            )
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                    if (isSelected)
                                                            Color(0xFF003CC7).copy(alpha = 0.07f)
                                                    else Color.White
                                            )
                                            .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color =
                                                            if (isSelected) Color(0xFF003CC7)
                                                            else Color(0xFFEEEFF5),
                                                    shape = RoundedCornerShape(18.dp)
                                            )
                                            .clickable { onSelect(currency) }
                                            .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(currency.flag, fontSize = 30.sp)
                            Column {
                                Text(
                                        "${currency.code} (${currency.symbol})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color =
                                                if (isSelected) Color(0xFF003CC7)
                                                else Color(0xFF181C20)
                                )
                                Text(currency.label, fontSize = 13.sp, color = Color(0xFF737688))
                            }
                        }
                        // Selection indicator
                        Box(
                                modifier =
                                        Modifier.size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                        if (isSelected) Color(0xFF003CC7)
                                                        else Color.Transparent
                                                )
                                                .border(
                                                        1.5.dp,
                                                        if (isSelected) Color.Transparent
                                                        else Color(0xFFD0D1DF),
                                                        CircleShape
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                            if (isSelected)
                                    Text(
                                            "✓",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                    )
                        }
                    }
                }
            }
        }
    }
}

// ─── Bước 2: Tạo ví đầu tiên ─────────────────────────────────────────────────
@Composable
private fun WalletStep(
        walletName: String,
        walletBalanceText: String,
        currency: Currency,
        onNameChange: (String) -> Unit,
        onBalanceChange: (String) -> Unit,
        focusManager: androidx.compose.ui.focus.FocusManager
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60)
        visible = true
    }

    AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(500)) { 20 }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                    "Tạo ví đầu tiên",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    color = Color(0xFF181C20),
                    letterSpacing = (-0.4).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                    "Đặt tên và nhập số dư ban đầu cho ví của bạn.",
                    fontSize = 14.sp,
                    color = Color(0xFF737688),
                    lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .shadow(
                                            12.dp,
                                            RoundedCornerShape(24.dp),
                                            ambientColor = Color(0x0A003CC7)
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White)
                                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    DDMoneyInputField(
                            label = "Tên ví",
                            value = walletName,
                            onValueChange = onNameChange,
                            placeholder = "Ví dụ: Tiền mặt, Agribank…",
                            leadingIcon = Icons.Outlined.AccountBalanceWallet,
                            keyboardOptions =
                                    KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Next
                                    ),
                            keyboardActions =
                                    KeyboardActions(
                                            onNext = {
                                                focusManager.moveFocus(
                                                        androidx.compose.ui.focus.FocusDirection
                                                                .Down
                                                )
                                            }
                                    )
                    )

                    DDMoneyInputField(
                            label = "Số dư ban đầu (${currency.symbol})",
                            value = walletBalanceText,
                            onValueChange = onBalanceChange,
                            placeholder = "0",
                            leadingIcon = Icons.Outlined.Payments,
                            keyboardOptions =
                                    KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal,
                                            imeAction = ImeAction.Done
                                    ),
                            keyboardActions =
                                    KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                    "Bạn có thể thêm nhiều ví hơn trong phần Cài đặt.",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B3C6),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
