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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

import androidx.compose.ui.text.style.TextAlign
import com.dung.ddmoney.R

@Composable
fun RegisterScreen(
    onRegisterClick: (fullName: String, email: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val focusManager = LocalFocusManager.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var showEmptyWarning by remember { mutableStateOf(false) }

    val passwordsMatch = password.isEmpty() || confirmPassword.isEmpty() || password == confirmPassword
    val isFormValid = fullName.isNotBlank() && email.isNotBlank() &&
            password.length >= 6 && password == confirmPassword && agreedToTerms

    // Reset warning when form becomes valid
    LaunchedEffect(isFormValid) {
        if (isFormValid) showEmptyWarning = false
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFF0F0F0), CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF181C20),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 150 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    "Tạo tài khoản",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = Color(0xFF003CC7)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Bắt đầu hành trình tài chính của bạn.",
                    fontSize = 15.sp,
                    color = Color(0xFF737688)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Full Name Field
                FigmaInputField(
                    placeholder = "Họ và tên",
                    value = fullName,
                    onValueChange = { fullName = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Field
                FigmaInputField(
                    placeholder = "Email",
                    value = email,
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                FigmaInputField(
                    placeholder = "Mật khẩu (Tối thiểu 6 ký tự)",
                    value = password,
                    onValueChange = { password = it },
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Field
                FigmaInputField(
                    placeholder = "Xác nhận mật khẩu",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (!isFormValid) {
                                showEmptyWarning = true
                            } else {
                                showEmptyWarning = false
                                onRegisterClick(fullName, email, password)
                            }
                        }
                    )
                )

                if (!passwordsMatch) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mật khẩu không khớp",
                        color = Color(0xFFBA1A1A),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Terms Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { agreedToTerms = !agreedToTerms }
                ) {
                    Checkbox(
                        checked = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF003CC7),
                            uncheckedColor = Color(0xFFB0B3C6)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Tôi đồng ý với Điều khoản & Chính sách",
                        fontSize = 13.sp,
                        color = Color(0xFF737688)
                    )
                }

                val displayError = errorMessage ?: if (showEmptyWarning) "Vui lòng nhập đầy đủ thông tin hợp lệ" else null
                displayError?.let { msg ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = msg,
                        color = Color(0xFFBA1A1A),
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Register Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF003CC7), Color(0xFF0D51FB))))
                        .clickable(enabled = !isLoading) {
                            if (!isFormValid) {
                                showEmptyWarning = true
                            } else {
                                showEmptyWarning = false
                                onRegisterClick(fullName, email, password)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Đăng ký",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "hoặc",
                    fontSize = 13.sp,
                    color = Color(0xFF737688),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Google Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.dp, Color(0xFFE0E2E8), RoundedCornerShape(12.dp))
                        .clickable { onGoogleSignIn() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Tiếp tục với Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF181C20)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Links
                Row(modifier = Modifier.clickable { onNavigateToLogin() }) {
                    Text(
                        "Đã có tài khoản? ",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        "Đăng nhập",
                        fontSize = 14.sp,
                        color = Color(0xFF003CC7),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
