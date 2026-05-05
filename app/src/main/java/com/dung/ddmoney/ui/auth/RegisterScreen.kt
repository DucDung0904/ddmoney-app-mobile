package com.dung.ddmoney.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    onRegisterClick: (fullName: String, email: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val focusManager = LocalFocusManager.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }

    val passwordsMatch = password.isEmpty() || confirmPassword.isEmpty() || password == confirmPassword
    val isFormValid = fullName.isNotBlank() && email.isNotBlank()
            && password.length >= 6 && password == confirmPassword && agreedToTerms

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FF))
    ) {
        // ── Decorative blobs ───────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x0D4659A6), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.3f),
                    radius = 280f
                ),
                radius = 280f,
                center = Offset(size.width * 0.9f, size.height * 0.3f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1A003CC7), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.7f),
                    radius = 220f
                ),
                radius = 220f,
                center = Offset(size.width * 0.1f, size.height * 0.7f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 60.dp, bottom = 40.dp),
        ) {
            // ── Back button ────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400))
            ) {
                IconButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .shadow(0.dp, RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF181C20),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Headline ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(600, 100)) { -30 }
            ) {
                Column {
                    Text(
                        "Create Account",
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        color = Color(0xFF181C20),
                        letterSpacing = (-0.5).sp,
                        lineHeight = 38.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start designing your financial architecture today.",
                        fontSize = 15.sp,
                        color = Color(0xFF434656),
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Form Card ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(700, 200)) { 40 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Full Name
                    DDMoneyInputField(
                        label = "Full Name",
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "John Doe",
                        leadingIcon = Icons.Outlined.Person,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Email
                    DDMoneyInputField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "you@example.com",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Password
                    DDMoneyInputField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Min. 6 characters",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Confirm Password
                    DDMoneyInputField(
                        label = "Confirm Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Re-enter password",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                        isError = !passwordsMatch,
                        errorText = if (!passwordsMatch) "Passwords do not match" else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    // Terms & Conditions
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.clickable { agreedToTerms = !agreedToTerms }
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF003CC7),
                                uncheckedColor = Color(0xFFC3C5D9)
                            ),
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Text(
                            buildString {
                                append("I agree to the ")
                                append("Terms of Service")
                                append(" and ")
                                append("Privacy Policy")
                            },
                            fontSize = 13.sp,
                            color = Color(0xFF434656),
                            lineHeight = 20.sp
                        )
                    }

                    // Error Message
                    errorMessage?.let {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF0F0), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Outlined.Info, null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(16.dp))
                            Text(it, color = Color(0xFFBA1A1A), fontSize = 13.sp)
                        }
                    }

                    // Register button
                    GradientButton(
                        text = if (isLoading) "Creating account..." else "Create Account",
                        onClick = { onRegisterClick(fullName, email, password) },
                        enabled = isFormValid && !isLoading,
                        isLoading = isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Divider ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 350))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFE6E8EE))
                    Text("or register with", fontSize = 12.sp, color = Color(0xFF737688))
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFE6E8EE))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Social Buttons ─────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 400))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SocialButton(
                        text = "Google",
                        emoji = "🔍",
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                    SocialButton(
                        text = "Apple",
                        emoji = "🍎",
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Footer ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 450))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ", fontSize = 14.sp, color = Color(0xFF434656))
                    Text(
                        "Log In",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF003CC7),
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}
