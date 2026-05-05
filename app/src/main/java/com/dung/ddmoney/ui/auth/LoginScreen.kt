package com.dung.ddmoney.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun LoginScreen(
        onLoginClick: (email: String, password: String) -> Unit,
        onNavigateToRegister: () -> Unit,
        isLoading: Boolean = false,
        errorMessage: String? = null
) {
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FF))) {
        // ── Decorative background blobs ────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                    brush =
                            Brush.radialGradient(
                                    colors = listOf(Color(0x1A003CC7), Color.Transparent),
                                    center = Offset(size.width * 0.85f, size.height * 0.1f),
                                    radius = 300f
                            ),
                    radius = 300f,
                    center = Offset(size.width * 0.85f, size.height * 0.1f)
            )
            drawCircle(
                    brush =
                            Brush.radialGradient(
                                    colors = listOf(Color(0x0D4659A6), Color.Transparent),
                                    center = Offset(size.width * 0.15f, size.height * 0.85f),
                                    radius = 250f
                            ),
                    radius = 250f,
                    center = Offset(size.width * 0.15f, size.height * 0.85f)
            )
        }

        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 28.dp)
                                .padding(top = 72.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Logo / Brand ───────────────────────────────────────────
            AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(600)) { -40 }
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                            modifier =
                                    Modifier.size(40.dp)
                                            .background(
                                                    brush =
                                                            Brush.linearGradient(
                                                                    colors =
                                                                            listOf(
                                                                                    Color(
                                                                                            0xFF003CC7
                                                                                    ),
                                                                                    Color(
                                                                                            0xFF0D51FB
                                                                                    )
                                                                            ),
                                                                    start = Offset(0f, 0f),
                                                                    end = Offset(40f, 40f)
                                                            ),
                                                    shape = RoundedCornerShape(12.dp)
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                "D",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                        )
                    }
                    Text(
                            "DDMoney",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF181C20)
                    )
                }
            }

            Spacer(modifier = Modifier.height(52.dp))

            // ── Headline ───────────────────────────────────────────────
            AnimatedVisibility(
                    visible = visible,
                    enter =
                            fadeIn(tween(500, delayMillis = 100)) +
                                    slideInVertically(tween(600, 100)) { -30 }
            ) {
                Column {
                    Text(
                            "Welcome back.",
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp,
                            color = Color(0xFF181C20),
                            letterSpacing = (-0.5).sp,
                            lineHeight = 40.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            "Sign in to continue managing your finances.",
                            fontSize = 15.sp,
                            color = Color(0xFF434656),
                            lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Form Card ──────────────────────────────────────────────
            AnimatedVisibility(
                    visible = visible,
                    enter =
                            fadeIn(tween(500, delayMillis = 200)) +
                                    slideInVertically(tween(700, 200)) { 40 }
            ) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .shadow(elevation = 0.dp, shape = RoundedCornerShape(24.dp))
                                        .background(Color.White, RoundedCornerShape(24.dp))
                                        .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Email field
                    DDMoneyInputField(
                            label = "Email Address",
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "you@example.com",
                            leadingIcon = Icons.Outlined.Email,
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

                    // Password field
                    DDMoneyInputField(
                            label = "Password",
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "••••••••",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordToggle = { passwordVisible = !passwordVisible },
                            trailingLabel = "Forgot?",
                            onTrailingLabelClick = { /* TODO: Forgot password */},
                            keyboardOptions =
                                    KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                    ),
                            keyboardActions =
                                    KeyboardActions(
                                            onDone = {
                                                focusManager.clearFocus()
                                                if (email.isNotBlank() && password.isNotBlank()) {
                                                    onLoginClick(email, password)
                                                }
                                            }
                                    )
                    )

                    // Error message
                    errorMessage?.let {
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .background(
                                                        Color(0xFFFFF0F0),
                                                        RoundedCornerShape(10.dp)
                                                )
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                    Icons.Outlined.Info,
                                    null,
                                    tint = Color(0xFFBA1A1A),
                                    modifier = Modifier.size(16.dp)
                            )
                            Text(it, color = Color(0xFFBA1A1A), fontSize = 13.sp)
                        }
                    }

                    // Login Button
                    GradientButton(
                            text = if (isLoading) "Signing in..." else "Log In",
                            onClick = { onLoginClick(email, password) },
                            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                            isLoading = isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Divider ────────────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500, delayMillis = 350))) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFE6E8EE))
                    Text("Or continue with", fontSize = 12.sp, color = Color(0xFF737688))
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFE6E8EE))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Social Buttons ─────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500, delayMillis = 400))) {
                Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                ) {
                    SocialButton(
                            text = "Google",
                            emoji = "🔍",
                            modifier = Modifier.weight(1f),
                            onClick = { /* TODO: Google OAuth */}
                    )
                    SocialButton(
                            text = "Apple",
                            emoji = "🍎",
                            modifier = Modifier.weight(1f),
                            onClick = { /* TODO: Apple OAuth */}
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // ── Footer ─────────────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500, delayMillis = 500))) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have an account? ", fontSize = 14.sp, color = Color(0xFF434656))
                    Text(
                            "Sign Up",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF003CC7),
                            modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}
