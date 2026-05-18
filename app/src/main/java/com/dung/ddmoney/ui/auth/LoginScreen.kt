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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.R

@Composable
fun LoginScreen(
        onLoginClick: (email: String, password: String) -> Unit,
        onNavigateToRegister: () -> Unit,
        onGoogleSignInClick: () -> Unit = {},
        onBack: () -> Unit = {},
        isLoading: Boolean = false,
        errorMessage: String? = null
) {
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showEmptyWarning by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun validateInput(): Boolean {
        if (email.isBlank() || password.isBlank()) {
            showEmptyWarning = true
            validationError = null
            return false
        }
        showEmptyWarning = false

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        if (!email.matches(emailRegex)) {
            validationError = "Email không hợp lệ"
            return false
        }

        val asciiRegex = "^[\\x20-\\x7E]+\$".toRegex()
        if (!password.matches(asciiRegex)) {
            validationError = "Mật khẩu không được chứa tiếng Việt có dấu"
            return false
        }

        validationError = null
        return true
    }

    // Reset warning when typing
    LaunchedEffect(email, password) {
        if (email.isNotBlank() && password.isNotBlank()) {
            showEmptyWarning = false
            validationError = null
        }
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
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        Box(
                                modifier =
                                        Modifier.size(48.dp)
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
                    modifier =
                            Modifier.fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(horizontal = 32.dp)
                                    .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) {

                // Title
                Text(
                        "Chào mừng trở lại!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = Color(0xFF003CC7)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Email Field
                FigmaInputField(
                        placeholder = "Email",
                        value = email,
                        onValueChange = { email = it },
                        keyboardOptions =
                                KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                ),
                        keyboardActions =
                                KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                FigmaInputField(
                        placeholder = "Mật khẩu",
                        value = password,
                        onValueChange = { password = it },
                        isPassword = true,
                        keyboardOptions =
                                KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                ),
                        keyboardActions =
                                KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (validateInput()) {
                                                onLoginClick(email, password)
                                            }
                                        }
                                )
                )

                val displayError =
                        errorMessage
                                ?: validationError
                                        ?: if (showEmptyWarning)
                                        "Vui lòng nhập đầy đủ Email và Mật khẩu"
                                else null
                displayError?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text = msg,
                            color = Color(0xFFBA1A1A),
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Login Button
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                                Brush.linearGradient(
                                                        listOf(Color(0xFF003CC7), Color(0xFF0D51FB))
                                                )
                                        )
                                        .clickable(enabled = !isLoading) {
                                            focusManager.clearFocus()
                                            if (validateInput()) {
                                                onLoginClick(email, password)
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
                                "Đăng nhập",
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
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(52.dp)
                                        .border(1.dp, Color(0xFFE0E2E8), RoundedCornerShape(12.dp))
                                        .clickable(enabled = !isLoading) { onGoogleSignInClick() },
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

                Spacer(modifier = Modifier.height(16.dp))

                // TOS text
                Text(
                        text =
                                buildAnnotatedString {
                                    append(
                                            "Bằng cách nhấp vào \"Tiếp tục với Google\", bạn đồng ý với "
                                    )
                                    withStyle(
                                            SpanStyle(
                                                    color = Color(0xFF0D51FB),
                                                    textDecoration = TextDecoration.Underline
                                            )
                                    ) { append("Điều khoản") }
                                    append("\nvà xác nhận ")
                                    withStyle(
                                            SpanStyle(
                                                    color = Color(0xFF0D51FB),
                                                    textDecoration = TextDecoration.Underline
                                            )
                                    ) { append("Chính sách bảo mật") }
                                    append(" của DDMoney.")
                                },
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Links
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                            "Quên mật khẩu",
                            fontSize = 14.sp,
                            color = Color(0xFF003CC7),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { /* TODO */}
                    )
                    Row(modifier = Modifier.clickable { onNavigateToRegister() }) {
                        Text("Chưa có tài khoản? ", fontSize = 14.sp, color = Color(0xFF666666))
                        Text(
                                "Tạo một tài khoản",
                                fontSize = 14.sp,
                                color = Color(0xFF003CC7),
                                fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun FigmaInputField(
        placeholder: String,
        value: String,
        onValueChange: (String) -> Unit,
        isPassword: Boolean = false,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFFF1F3F9), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE0E2E8), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                visualTransformation =
                        if (isPassword) PasswordVisualTransformation()
                        else VisualTransformation.None,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                textStyle =
                        LocalTextStyle.current.copy(fontSize = 15.sp, color = Color(0xFF181C20)),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                    placeholder,
                                    fontSize = 14.sp,
                                    color = Color(0xFF737688),
                                    fontWeight = FontWeight.Medium
                            )
                        }
                        innerTextField()
                    }
                }
        )
    }
}
