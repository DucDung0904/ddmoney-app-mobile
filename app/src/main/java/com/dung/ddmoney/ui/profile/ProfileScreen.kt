package com.dung.ddmoney.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dung.ddmoney.ui.theme.*

@Composable
fun ProfileScreen(
    userName: String = "Người dùng",
    userEmail: String = "nguoidung@gmail.com",
    avatarUrl: String = "",
    onManageWallets: () -> Unit = {},
    onUpdateAvatar: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onUpdateAvatar(it.toString()) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuminousBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // --- Header: Profile Summary ---
            item {
                ProfileHeader(
                    name = userName,
                    email = userEmail,
                    avatarUrl = avatarUrl,
                    onAvatarClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }

            // --- App Settings Section ---
            item {
                SettingsSectionTitle("CÀI ĐẶT ỨNG DỤNG")
            }
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = LuminousSurfaceContainerLowest
                ) {
                    Column {
                        SettingItem(
                            icon = Icons.Outlined.AccountBalanceWallet,
                            iconColor = OceanBlue600,
                            title = "Quản lý ví",
                            subtitle = "Thêm, sửa và sắp xếp các ví của bạn",
                            trailing = { Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = NeutralGray400) },
                            onClick = onManageWallets
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = LuminousSurfaceContainerLow)
                        var darkMode by remember { mutableStateOf(false) }
                        SettingItem(
                            icon = Icons.Outlined.NightsStay,
                            iconColor = OceanBlue600,
                            title = "Chế độ tối",
                            subtitle = "Tiết kiệm pin và bảo vệ mắt",
                            trailing = {
                                Switch(
                                    checked = darkMode,
                                    onCheckedChange = { darkMode = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = OceanBlue600
                                    )
                                )
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = LuminousSurfaceContainerLow)
                        SettingItem(
                            icon = Icons.Outlined.Payments,
                            iconColor = OceanBlue600,
                            title = "Lựa chọn tiền tệ",
                            subtitle = "VNĐ (₫)",
                            trailing = { Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = NeutralGray400) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = LuminousSurfaceContainerLow)
                        SettingItem(
                            icon = Icons.Outlined.Language,
                            iconColor = OceanBlue600,
                            title = "Ngôn ngữ",
                            subtitle = "Tiếng Việt",
                            trailing = { Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = NeutralGray400) }
                        )
                    }
                }
            }

            // --- Security & Data Section ---
            item {
                SettingsSectionTitle("BẢO MẬT & DỮ LIỆU")
            }
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = LuminousSurfaceContainerLowest
                ) {
                    Column {
                        SettingItem(
                            icon = Icons.Outlined.Security,
                            iconColor = OceanBlue600,
                            title = "Bảo mật",
                            subtitle = "Mật khẩu & Face ID",
                            trailing = { Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = NeutralGray400) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = LuminousSurfaceContainerLow)
                        SettingItem(
                            icon = Icons.Outlined.FileDownload,
                            iconColor = OceanBlue600,
                            title = "Xuất dữ liệu",
                            subtitle = "CSV, PDF",
                            trailing = { Icon(Icons.Outlined.FileDownload, null, modifier = Modifier.size(20.dp), tint = NeutralGray400) }
                        )
                    }
                }
            }

            // --- Logout ---
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clickable { onLogout() },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = ExpenseRed600)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Đăng xuất",
                            color = ExpenseRed600,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    avatarUrl: String,
    onAvatarClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // ─── 1. Background Header ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(OceanBlue600, OceanBlue400)
                    )
                )
        )

        // ─── 2. Main Info Card ─────────────────────────────────────────
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp), // Even smaller top padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = LuminousOnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    fontSize = 15.sp,
                    color = NeutralGray600
                )
            }
        }

        // ─── 3. Overlapping Avatar ────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 75.dp)
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(4.dp)
                .clickable { onAvatarClick() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(OceanBlue50),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = name.take(1).uppercase(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = OceanBlue600
                    )
                }
            }

            // Edit Icon Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, OceanBlue600, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = OceanBlue600
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = NeutralGray400,
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconColor
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = NeutralGray600
                )
            }
        }

        if (trailing != null) {
            trailing()
        }
    }
}
