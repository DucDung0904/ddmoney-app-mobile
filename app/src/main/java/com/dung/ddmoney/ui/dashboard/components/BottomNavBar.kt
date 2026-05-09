package com.dung.ddmoney.ui.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.theme.*

// ─── Nav Item Data ─────────────────────────────────────────────────────
data class NavItem(
        val label: String,
        val iconFilled: ImageVector,
        val iconOutlined: ImageVector,
        val route: String
)

val bottomNavItems =
        listOf(
                NavItem("Home", Icons.Filled.GridView, Icons.Outlined.GridView, "home"),
                NavItem("Stats", Icons.Filled.Insights, Icons.Outlined.Insights, "stats"),
                NavItem(
                        "Budget",
                        Icons.Filled.AccountBalanceWallet,
                        Icons.Outlined.AccountBalanceWallet,
                        "budget"
                ),
                NavItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "account"),
        )

// ─── Bottom Navigation Bar ─────────────────────────────────────────────
@Composable
fun BottomNavBar(
        selectedRoute: String,
        onItemSelected: (String) -> Unit,
        onAddClick: () -> Unit = {},
        modifier: Modifier = Modifier
) {
    Surface(
            modifier =
                    modifier.fillMaxWidth()
                            .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(0.dp),
                                    ambientColor = Color(0x18000000),
                                    spotColor = Color(0x12000000)
                            ),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
    ) {
        BoxWithConstraints(
                modifier =
                        Modifier.fillMaxWidth()
                                .navigationBarsPadding()
                                .height(72.dp)
                                .padding(horizontal = 8.dp)
        ) {
            val selectedIndex =
                    bottomNavItems.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)
            val tabWidth = maxWidth / bottomNavItems.size

            // Animate offset
            val indicatorOffset by
                    animateDpAsState(
                            targetValue = tabWidth * selectedIndex,
                            animationSpec =
                                    spring(
                                            stiffness = Spring.StiffnessMediumLow,
                                            dampingRatio = Spring.DampingRatioMediumBouncy
                                    ),
                            label = "indicatorOffset"
                    )

            // The animated background pill wrapper
            Box(
                    modifier = Modifier.offset(x = indicatorOffset).width(tabWidth).fillMaxHeight(),
                    contentAlignment = Alignment.Center
            ) {
                Box(
                        modifier =
                                Modifier.size(width = 72.dp, height = 64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.35f
                                                )
                                        )
                )
            }

            Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    NavBarItem(
                            item = item,
                            isSelected = selectedRoute == item.route,
                            onSelected = { onItemSelected(item.route) },
                            modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─── Single Nav Item ──────────────────────────────────────────────────
@Composable
fun NavBarItem(
        item: NavItem,
        isSelected: Boolean,
        onSelected: () -> Unit,
        modifier: Modifier = Modifier
) {
    val iconTint by
            animateColorAsState(
                    targetValue =
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "iconTint"
            )
    val labelColor by
            animateColorAsState(
                    targetValue =
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "labelColor"
            )

    Box(
            modifier =
                    modifier.fillMaxHeight()
                            .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onSelected
                            ),
            contentAlignment = Alignment.Center
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                    imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                    contentDescription = item.label,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                    text = item.label,
                    color = labelColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1
            )
        }
    }
}

// ─── Add Transaction FAB (standalone, used outside nav bar) ────────────
@Composable
fun AddTransactionFab(onClick: () -> Unit) {
    Box(
            modifier =
                    Modifier.size(56.dp)
                            .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0x40000000),
                                    spotColor = Color(0x30000000)
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onClick
                            ),
            contentAlignment = Alignment.Center
    ) {
        Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
        )
    }
}

// ─── Dashboard Top AppBar ──────────────────────────────────────────────
@Composable
fun DashboardTopBar(userName: String = "Dũng", modifier: Modifier = Modifier) {
    Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                    text = "DDMoney",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
            )
            Text(
                    text = "Hello, $userName 👋",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
            )
        }

        Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                    modifier =
                            Modifier.size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .clickable {},
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                )
                Box(
                        modifier =
                                Modifier.size(7.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-2).dp, y = 2.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                )
            }

            Box(
                    modifier =
                            Modifier.size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                            Brush.linearGradient(
                                                    listOf(
                                                            MaterialTheme.colorScheme.secondary,
                                                            MaterialTheme.colorScheme.primary
                                                    )
                                            )
                                    ),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = userName.first().toString().uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF7F9FF)
@Composable
private fun BottomNavPreview() {
    DDMoneyTheme(darkTheme = false) {
        var selected by remember { mutableStateOf("budget") }
        Box(modifier = Modifier.fillMaxSize()) {
            Column { DashboardTopBar() }
            BottomNavBar(
                    selectedRoute = selected,
                    onItemSelected = { selected = it },
                    onAddClick = {},
                    modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
