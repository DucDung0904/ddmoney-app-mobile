package com.dung.ddmoney.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*

// ─── Shared Input Field ───────────────────────────────────────────────────────
@Composable
fun DDMoneyInputField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String = "",
        leadingIcon: ImageVector? = null,
        isPassword: Boolean = false,
        passwordVisible: Boolean = false,
        onPasswordToggle: (() -> Unit)? = null,
        trailingLabel: String? = null,
        onTrailingLabelClick: (() -> Unit)? = null,
        isError: Boolean = false,
        errorText: String? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by
            animateColorAsState(
                    targetValue =
                            when {
                                isError -> Color(0xFFBA1A1A)
                                isFocused -> Color(0xFF003CC7).copy(alpha = 0.5f)
                                else -> Color.Transparent
                            },
                    animationSpec = tween(200),
                    label = "border"
            )

    val bgColor by
            animateColorAsState(
                    targetValue = if (isFocused) Color(0xFFF1F3F9) else Color(0xFFF1F3F9),
                    animationSpec = tween(200),
                    label = "bg"
            )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Label row
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isError) Color(0xFFBA1A1A) else Color(0xFF434656)
            )
            trailingLabel?.let {
                Text(
                        it,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF003CC7),
                        modifier = Modifier.clickable { onTrailingLabelClick?.invoke() }
                )
            }
        }

        // Input box
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(bgColor, RoundedCornerShape(14.dp))
                                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
        ) {
            BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    visualTransformation =
                            if (isPassword && !passwordVisible) PasswordVisualTransformation()
                            else VisualTransformation.None,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    textStyle =
                            LocalTextStyle.current.copy(
                                    fontSize = 15.sp,
                                    color = Color(0xFF181C20)
                            ),
                    modifier =
                            Modifier.fillMaxWidth()
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .padding(
                                            start = if (leadingIcon != null) 48.dp else 16.dp,
                                            end =
                                                    if (isPassword || trailingLabel != null) 48.dp
                                                    else 16.dp,
                                            top = 16.dp,
                                            bottom = 16.dp
                                    ),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(placeholder, fontSize = 15.sp, color = Color(0xFFB0B3C6))
                        }
                        innerTextField()
                    }
            )

            // Leading icon
            leadingIcon?.let {
                Icon(
                        it,
                        contentDescription = null,
                        tint = if (isFocused) Color(0xFF003CC7) else Color(0xFF737688),
                        modifier =
                                Modifier.align(Alignment.CenterStart)
                                        .padding(start = 14.dp)
                                        .size(20.dp)
                )
            }

            // Trailing icon (password toggle)
            if (isPassword && onPasswordToggle != null) {
                IconButton(
                        onClick = onPasswordToggle,
                        modifier = Modifier.align(Alignment.CenterEnd).size(48.dp)
                ) {
                    Icon(
                            if (passwordVisible) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF737688),
                            modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Error text
        if (isError && errorText != null) {
            Text(
                    errorText,
                    fontSize = 12.sp,
                    color = Color(0xFFBA1A1A),
                    modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ─── Gradient CTA Button ──────────────────────────────────────────────────────
@Composable
fun GradientButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        isLoading: Boolean = false
) {
    Box(
            modifier =
                    modifier.fillMaxWidth()
                            .height(54.dp)
                            .background(
                                    brush =
                                            if (enabled)
                                                    Brush.linearGradient(
                                                            colors =
                                                                    listOf(
                                                                            Color(0xFF003CC7),
                                                                            Color(0xFF0D51FB)
                                                                    ),
                                                            start = Offset(0f, 0f),
                                                            end =
                                                                    Offset(
                                                                            Float.POSITIVE_INFINITY,
                                                                            Float.POSITIVE_INFINITY
                                                                    )
                                                    )
                                            else
                                                    Brush.linearGradient(
                                                            colors =
                                                                    listOf(
                                                                            Color(0xFFB0B3C6),
                                                                            Color(0xFFB0B3C6)
                                                                    )
                                                    ),
                                    shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = enabled && !isLoading) { onClick() },
            contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
            )
        } else {
            Text(
                    text,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 0.3.sp
            )
        }
    }
}

// ─── Social Button ────────────────────────────────────────────────────────────
@Composable
fun SocialButton(text: String, emoji: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
            modifier =
                    modifier.height(50.dp)
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE6E8EE), RoundedCornerShape(14.dp))
                            .clickable { onClick() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF181C20))
    }
}
