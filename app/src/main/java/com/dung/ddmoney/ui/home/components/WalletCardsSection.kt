package com.dung.ddmoney.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.dashboard.model.WalletType
import com.dung.ddmoney.ui.theme.*

// ─── Dimensions ───────────────────────────────────────────────────────────────
private val CARD_WIDTH  = 200.dp
private val CARD_HEIGHT = 120.dp

// ─── Per-card colour palette ──────────────────────────────────────────────────
private data class WalletTheme(
    val gradStart: Color,
    val gradEnd:   Color,
    val chipColor: Color
)

private val walletThemes = listOf(
    WalletTheme(Color(0xFF0C447C), Color(0xFF378ADD), Color(0xFFB5D4F4)),
    WalletTheme(Color(0xFF0F5C43), Color(0xFF3DBFA0), Color(0xFFA6E4D0)),
    WalletTheme(Color(0xFF8A5606), Color(0xFFF3BC56), Color(0xFFFCDDA0)),
    WalletTheme(Color(0xFF8E1F1F), Color(0xFFEB7070), Color(0xFFF5BABA)),
    WalletTheme(Color(0xFF3D1A78), Color(0xFF9B59D0), Color(0xFFD7B8F4)),
)

// ─── Display-ordering logic ───────────────────────────────────────────────────
/**
 * Returns at most 3 wallets to show on Home:
 *   slot 0 = default wallet (isDefault == true), or first if none marked
 *   slot 1,2 = the remaining wallets sorted by balance descending
 */
internal fun selectDisplayWallets(wallets: List<Wallet>): List<Wallet> {
    if (wallets.isEmpty()) return emptyList()
    val default = wallets.firstOrNull { it.isDefault } ?: wallets.first()
    val others  = wallets.filter { it.id != default.id }
        .sortedByDescending { it.balance }
        .take(2)
    return listOf(default) + others
}

// ─────────────────────────────────────────────────────────────────────────────
//  WalletCardsSection
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun WalletCardsSection(
    wallets:      List<Wallet>,
    isVisible:    Boolean,
    onSeeAll:     () -> Unit,
    onAddWallet:  ((Wallet) -> Unit)? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // ─── Header ──────────────────────────────────────────────────────
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            WalletSectionHeader(
                wallets   = wallets,
                onSeeAll  = onSeeAll,
                onAdd     = { showAddDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─── Cards frame ─────────────────────────────────────────────────
        Surface(
            modifier        = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape           = RoundedCornerShape(28.dp),
            color           = LuminousSurfaceContainerLowest,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                val listState = rememberLazyListState()
                val displayWallets = selectDisplayWallets(wallets)

                Box(modifier = Modifier.fillMaxWidth()) {
                    LazyRow(
                        state                 = listState,
                        modifier              = Modifier.fillMaxWidth(),
                        contentPadding        = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (displayWallets.isEmpty()) {
                            item { EmptyWalletCard(onClick = { showAddDialog = true }) }
                        } else {
                            items(displayWallets.size) { index ->
                                WalletCard(
                                    wallet    = displayWallets[index],
                                    isVisible = isVisible,
                                    index     = index,
                                    isDefault = displayWallets[index].isDefault
                                )
                            }
                            item { AddWalletGhostCard(onClick = { showAddDialog = true }) }
                        }
                    }

                    // Left fade
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxHeight()
                            .width(40.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(LuminousSurfaceContainerLowest, Color.Transparent)
                                )
                            )
                    )
                    // Right fade
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(40.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, LuminousSurfaceContainerLowest)
                                )
                            )
                    )
                }

                // Dot indicators
                if (displayWallets.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyScrollDotIndicator(count = displayWallets.size, listState = listState)
                }
            }
        }
    }

    // ─── Add Wallet Dialog ────────────────────────────────────────────────
    if (showAddDialog) {
        AddWalletDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { wallet ->
                onAddWallet?.invoke(wallet)
                showAddDialog = false
            }
        )
    }
}

// ─── Dot indicator ────────────────────────────────────────────────────────────
@Composable
private fun LazyScrollDotIndicator(count: Int, listState: LazyListState) {
    val activeIndex = listState.firstVisibleItemIndex
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val isActive = i == activeIndex
            val width by animateDpAsState(
                targetValue = if (isActive) 18.dp else 6.dp,
                animationSpec = tween(200), label = "dot_$i"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(6.dp)
                    .width(width)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isActive) OceanBlue600 else NeutralGray100)
            )
        }
    }
}

// ─── Section header ───────────────────────────────────────────────────────────
@Composable
private fun WalletSectionHeader(
    wallets:  List<Wallet>,
    onSeeAll: () -> Unit,
    onAdd:    () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                modifier           = Modifier.size(18.dp),
                tint               = OceanBlue600
            )
            Text(
                "Ví của tôi",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Black,
                color      = LuminousOnSurface
            )
            if (wallets.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(OceanBlue50)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text       = "${wallets.size}",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Black,
                        color      = OceanBlue600
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Add button
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(OceanBlue50)
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Add,
                    contentDescription = "Thêm ví",
                    tint               = OceanBlue600,
                    modifier           = Modifier.size(18.dp)
                )
            }
            Text(
                "Tất cả",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = OceanBlue600,
                modifier   = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onSeeAll() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

// ─── Individual wallet card ───────────────────────────────────────────────────
@Composable
private fun WalletCard(
    wallet:    Wallet,
    isVisible: Boolean,
    index:     Int,
    isDefault: Boolean = false
) {
    val theme = walletThemes[index % walletThemes.size]

    Box(
        modifier = Modifier
            .width(CARD_WIDTH)
            .height(CARD_HEIGHT)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(theme.gradStart, theme.gradEnd),
                    start  = Offset(0f, 0f),
                    end    = Offset(700f, 700f)
                )
            )
            .clickable { }
    ) {
        // Depth circles
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 16.dp, y = (-16).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: chip bars + default badge
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                // EMV chip (3 bars)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(theme.chipColor)
                        )
                    }
                }

                // Default badge
                if (isDefault) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = "Mặc định",
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            // Wallet name + balance
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text          = wallet.name.uppercase(),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text       = if (isVisible)
                        "${String.format("%,.0f", wallet.balance)} đ"
                    else
                        "•••••• đ",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Black,
                    color      = Color.White
                )
            }
        }
    }
}

// ─── "Add wallet" ghost card ──────────────────────────────────────────────────
@Composable
private fun AddWalletGhostCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(CARD_HEIGHT)
            .clip(RoundedCornerShape(24.dp))
            .background(LuminousSurfaceContainerLow)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(OceanBlue50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Add,
                    contentDescription = "Thêm ví",
                    tint               = OceanBlue600,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Text("Thêm", fontSize = 12.sp, color = OceanBlue600, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Empty state card ─────────────────────────────────────────────────────────
@Composable
private fun EmptyWalletCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(CARD_WIDTH)
            .height(CARD_HEIGHT)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFE6EEF8), Color(0xFFCDD9EC))))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Add,
                    contentDescription = null,
                    tint               = OceanBlue600,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Text("Thêm ví mới", fontSize = 13.sp, color = OceanBlue800, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Add Wallet Dialog ────────────────────────────────────────────────────────
@Composable
private fun AddWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (Wallet) -> Unit
) {
    var name        by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var walletType  by remember { mutableStateOf(WalletType.CASH) }
    var isDefault   by remember { mutableStateOf(false) }
    var nameError   by remember { mutableStateOf(false) }

    val walletTypeOptions = listOf(
        WalletType.CASH        to ("Tiền mặt"      to Icons.Outlined.Money),
        WalletType.BANK        to ("Ngân hàng"      to Icons.Outlined.AccountBalance),
        WalletType.EWALLET     to ("Ví điện tử"     to Icons.Outlined.PhoneAndroid),
        WalletType.CREDIT_CARD to ("Thẻ tín dụng"   to Icons.Outlined.CreditCard),
        WalletType.SAVINGS     to ("Tiết kiệm"      to Icons.Outlined.Savings),
        WalletType.INVESTMENT  to ("Đầu tư"         to Icons.AutoMirrored.Outlined.TrendingUp),
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape         = RoundedCornerShape(28.dp),
            color         = LuminousSurfaceContainerLowest,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Dialog header ──────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OceanBlue50),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint               = OceanBlue600,
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                "Thêm ví mới",
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Black,
                                color      = LuminousOnSurface
                            )
                            Text(
                                "Quản lý tài chính hiệu quả hơn",
                                fontSize = 12.sp,
                                color    = LuminousOnSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector        = Icons.Outlined.Close,
                            contentDescription = "Đóng",
                            tint               = LuminousOnSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = LuminousOutlineVariant)

                // ── Wallet name ────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Tên ví",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = LuminousOnSurfaceVariant
                    )
                    OutlinedTextField(
                        value         = name,
                        onValueChange = { name = it; nameError = false },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("Ví cá nhân, Tiết kiệm...") },
                        singleLine    = true,
                        isError       = nameError,
                        supportingText = if (nameError) {{ Text("Tên ví không được để trống") }} else null,
                        leadingIcon   = {
                            Icon(Icons.Outlined.DriveFileRenameOutline, null, tint = OceanBlue600)
                        },
                        shape  = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = OceanBlue600,
                            unfocusedBorderColor = LuminousOutlineVariant
                        )
                    )
                }

                // ── Số dư ban đầu ──────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Số dư ban đầu",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = LuminousOnSurfaceVariant
                    )
                    OutlinedTextField(
                        value         = balanceText,
                        onValueChange = { balanceText = it.filter { c -> c.isDigit() || c == '.' } },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("0") },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon   = {
                            Icon(Icons.Outlined.Payments, null, tint = OceanBlue600)
                        },
                        trailingIcon  = {
                            Text("đ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuminousOnSurfaceVariant)
                        },
                        shape  = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = OceanBlue600,
                            unfocusedBorderColor = LuminousOutlineVariant
                        )
                    )
                }

                // ── Loại ví ────────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Loại ví",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = LuminousOnSurfaceVariant
                    )
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        walletTypeOptions.forEach { (type, meta) ->
                            val (label, icon) = meta
                            val selected = walletType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) OceanBlue600 else LuminousSurfaceContainerLow
                                    )
                                    .clickable { walletType = type }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector        = icon,
                                        contentDescription = label,
                                        tint               = if (selected) Color.White else LuminousOnSurfaceVariant,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text       = label,
                                        fontSize   = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = if (selected) Color.White else LuminousOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Đặt làm mặc định ──────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    color    = if (isDefault) OceanBlue50 else LuminousSurfaceContainerLow
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clickable { isDefault = !isDefault }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Star,
                                contentDescription = null,
                                tint               = if (isDefault) OceanBlue600 else LuminousOnSurfaceVariant,
                                modifier           = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    "Ví mặc định",
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = LuminousOnSurface
                                )
                                Text(
                                    "Hiển thị đầu tiên trên trang chủ",
                                    fontSize = 11.sp,
                                    color    = LuminousOnSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked         = isDefault,
                            onCheckedChange = { isDefault = it },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor  = Color.White,
                                checkedTrackColor  = OceanBlue600
                            )
                        )
                    }
                }

                // ── Action buttons ─────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = LuminousOnSurfaceVariant),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, LuminousOutlineVariant)
                    ) {
                        Text("Hủy", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val balance = balanceText.toDoubleOrNull() ?: 0.0
                            onConfirm(
                                Wallet(
                                    name      = name.trim(),
                                    balance   = balance,
                                    type      = walletType,
                                    color     = OceanBlue600,
                                    isDefault = isDefault
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = OceanBlue600)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Thêm ví", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
