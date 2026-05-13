package com.dung.ddmoney.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
        showLabel: Boolean = true,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        modifier: Modifier = Modifier
) {
        var isFocused by remember { mutableStateOf(false) }

        val borderColor by animateColorAsState(
                targetValue = when {
                        isError -> Color(0xFFBA1A1A)
                        isFocused -> Color(0xFF003CC7).copy(alpha = 0.5f)
                        else -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "border"
        )

        val bgColor by animateColorAsState(
                targetValue = Color(0xFFF1F3F9),
                animationSpec = tween(200),
                label = "bg"
        )

        Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(if (showLabel) 6.dp else 0.dp)
        ) {
                // Label row
                if (showLabel) {
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
                }

                // Input box — Dùng Surface để clip + background + border được xử lý trên
                // cùng một render layer, tự động khử răng cưa tại các góc bo tròn.
                // Thứ tự đúng: Surface (shape) → border → content (không cần clip thủ công).
                Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = bgColor,
                        border = if (borderColor != Color.Transparent)
                                BorderStroke(1.5.dp, borderColor)
                        else null
                ) {
                        Box {
                                BasicTextField(
                                        value = value,
                                        onValueChange = onValueChange,
                                        singleLine = true,
                                        visualTransformation = if (isPassword && !passwordVisible)
                                                PasswordVisualTransformation()
                                        else VisualTransformation.None,
                                        keyboardOptions = keyboardOptions,
                                        keyboardActions = keyboardActions,
                                        textStyle = LocalTextStyle.current.copy(
                                                fontSize = 15.sp,
                                                color = Color(0xFF181C20)
                                        ),
                                        modifier = Modifier
                                                .fillMaxWidth()
                                                .onFocusChanged { isFocused = it.isFocused }
                                                .padding(
                                                        start = if (leadingIcon != null) 48.dp else 16.dp,
                                                        end = if (isPassword || trailingLabel != null) 48.dp else 16.dp,
                                                        top = 16.dp,
                                                        bottom = 16.dp
                                                ),
                                        decorationBox = { innerTextField ->
                                                if (value.isEmpty()) {
                                                        Text(
                                                                placeholder,
                                                                fontSize = 15.sp,
                                                                color = Color(0xFFB0B3C6)
                                                        )
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
                                                modifier = Modifier
                                                        .align(Alignment.CenterStart)
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
        // graphicsLayer { clip = true } đẩy việc render lên GPU layer riêng biệt.
        // GPU layer này tự khử răng cưa (hardware anti-aliasing) trước khi
        // composite lên màn hình — hiệu quả hơn nhiều so với clip() Modifier thuần.
        // Thứ tự bắt buộc: graphicsLayer(clip=true) TRƯỚC .background() để nền không tràn góc.
        val buttonShape = RoundedCornerShape(16.dp)
        Box(
                modifier = modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .graphicsLayer {
                                shape = buttonShape
                                clip = true
                        }
                        .background(
                                brush = if (enabled)
                                        Brush.linearGradient(
                                                colors = listOf(Color(0xFF003CC7), Color(0xFF0D51FB)),
                                                start = Offset(0f, 0f),
                                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                        )
                                else Brush.linearGradient(
                                        colors = listOf(Color(0xFFB0B3C6), Color(0xFFB0B3C6))
                                )
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
        // Surface đảm bảo border + background + clip được xử lý trên cùng một render layer.
        // Nếu dùng .background() + .border() riêng lẻ, Compose vẽ chúng độc lập —
        // border có thể vẽ đè lên pixel đã bị clip ở mép ngoài, tạo ra răng cưa.
        Surface(
                modifier = modifier.height(50.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE6E8EE)),
                onClick = onClick
        ) {
                Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF181C20)
                        )
                }
        }
}
