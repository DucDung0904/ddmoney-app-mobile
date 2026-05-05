package com.dung.ddmoney.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.AppState
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@Composable
fun SettingsScreen(
    appState: AppState,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onManageWallets: () -> Unit,
    onLogout: () -> Unit,
    onUpdateAvatar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAvatarDialog by remember { mutableStateOf(false) }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            currentAvatarUrl = appState.userInfo.avatarUrl,
            onDismiss = { showAvatarDialog = false },
            onSelectAvatar = { url ->
                onUpdateAvatar(url)
                showAvatarDialog = false
            }
        )
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Top Bar ──────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Small avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (appState.userInfo.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = appState.userInfo.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = appState.userInfo.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Cài đặt",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    Icons.Default.NotificationsNone,
                    contentDescription = "Thông báo",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // ── Profile Card ──────────────────────────────────────────────
        item {
            val name = appState.userInfo.name.ifBlank { "Người dùng" }
            val email = appState.userInfo.email.ifBlank { "Chưa cập nhật email" }
            ProfileCard(
                userName = name, 
                userEmail = email, 
                avatarUrl = appState.userInfo.avatarUrl,
                onEditClick = { showAvatarDialog = true }
            )
        }

        item { Spacer(Modifier.height(24.dp)) }

        // ── CÀI ĐẶT TÀI KHOẢN ────────────────────────────────────────
        item { SectionHeader(title = "CÀI ĐẶT TÀI KHOẢN") }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            SettingsGroup(
                items = listOf(
                    SettingsItemData(Icons.Default.AccountBalanceWallet, "Quản lý ví", onClick = onManageWallets),
                    SettingsItemData(Icons.Default.Payments, "Tiền tệ", trailingText = "VND (đ)"),
                    SettingsItemData(Icons.Default.Language, "Ngôn ngữ", trailingText = "Tiếng Việt"),
                    SettingsItemData(Icons.Default.Link, "Tài khoản liên kết", trailingBadge = "02 NGÂN HÀNG"),
                )
            )
        }

        item { Spacer(Modifier.height(24.dp)) }

        // ── TÙY CHỈNH ỨNG DỤNG ───────────────────────────────────────
        item { SectionHeader(title = "TÙY CHỈNH ỨNG DỤNG") }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            SettingsGroup(
                items = listOf(
                    SettingsItemData(
                        icon = Icons.Default.DarkMode, 
                        title = "Chế độ tối", 
                        isSwitch = true, 
                        switchState = isDarkMode,
                        onClick = { onDarkModeToggle(!isDarkMode) }
                    ),
                    SettingsItemData(Icons.Default.NotificationsActive, "Thông báo ứng dụng", isSwitch = true, switchState = true),
                    SettingsItemData(Icons.Default.Fingerprint, "Mở khóa sinh trắc học", isSwitch = true, switchState = true),
                )
            )
        }

        item { Spacer(Modifier.height(24.dp)) }

        // ── DỮ LIỆU & HỖ TRỢ ──────────────────────────────────────────
        item { SectionHeader(title = "DỮ LIỆU & HỖ TRỢ") }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            SettingsGroup(
                items = listOf(
                    SettingsItemData(Icons.Default.FileDownload, "Xuất dữ liệu (.CSV)"),
                    SettingsItemData(Icons.Default.HelpOutline, "Trung tâm trợ giúp"),
                    SettingsItemData(Icons.Default.Info, "Về chúng tôi"),
                )
            )
        }

        item { Spacer(Modifier.height(32.dp)) }

        // ── Đăng xuất ────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable { onLogout() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Đăng xuất",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        
        // ── Footer ───────────────────────────────────────────────────
        item {
            Text(
                "Phiên bản 2.4.0 (Build 892)",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Profile Card ─────────────────────────────────────────────────────
@Composable
private fun ProfileCard(userName: String, userEmail: String, avatarUrl: String, onEditClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.clickable { onEditClick() }) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(10.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = userEmail,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Edit button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onEditClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Chỉnh\nsửa", 
                    color = MaterialTheme.colorScheme.onPrimaryContainer, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.SemiBold, 
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 28.dp)
    )
}

// ─── Settings Group ───────────────────────────────────────────────────
data class SettingsItemData(
    val icon: ImageVector,
    val title: String,
    val trailingText: String? = null,
    val trailingBadge: String? = null,
    val isSwitch: Boolean = false,
    val switchState: Boolean = false,
    val iconColor: Color = Color(0xFF1E3A8A),
    val onClick: (() -> Unit)? = null
)

@Composable
private fun SettingsGroup(items: List<SettingsItemData>) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        items.forEachIndexed { index, item ->
            SettingsRow(item = item)
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick?.invoke() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        // Title
        Text(
            item.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Trailing elements
        if (item.trailingText != null) {
            Text(
                text = item.trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        } else if (item.trailingBadge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.trailingBadge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        } else if (item.isSwitch) {
            Switch(
                checked = item.switchState,
                onCheckedChange = { item.onClick?.invoke() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.scale(0.8f).height(24.dp)
            )
        } else {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun AvatarSelectionDialog(
    currentAvatarUrl: String,
    onDismiss: () -> Unit,
    onSelectAvatar: (String) -> Unit
) {
    val predefinedAvatars = listOf(
        "https://api.dicebear.com/7.x/avataaars/png?seed=Felix",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Max",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Bella",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Lucy",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Lola",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Oliver",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Jack",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Luna",
        "https://api.dicebear.com/7.x/avataaars/png?seed=Charlie"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Chọn ảnh đại diện",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                items(predefinedAvatars) { avatarUrl ->
                    val isSelected = currentAvatarUrl == avatarUrl
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSelectAvatar(avatarUrl) },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
