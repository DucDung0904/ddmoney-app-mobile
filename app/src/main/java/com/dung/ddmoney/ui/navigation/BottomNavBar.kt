package com.dung.ddmoney.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.theme.NeutralGray600
import com.dung.ddmoney.ui.theme.OceanBlue600

sealed class NavItem(
        val route: String,
        val label: String,
        val icon: ImageVector,
        val selectedIcon: ImageVector
) {
    object Home : NavItem("home", "Trang chủ", Icons.Outlined.GridView, Icons.Filled.GridView)
    object Budget :
            NavItem(
                    "budget",
                    "Ngân sách",
                    Icons.Outlined.AccountBalanceWallet,
                    Icons.Outlined.AccountBalanceWallet
            )
    object Ledger :
            NavItem("ledger", "Sổ chi tiêu", Icons.Outlined.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Profile : NavItem("profile", "Cá nhân", Icons.Outlined.Person, Icons.Outlined.Person)
}

@Composable
fun BottomNavBar(currentRoute: String, onNavigate: (String) -> Unit, onAddClick: () -> Unit) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp) // Margin dưới lớn để tạo độ nổi
    ) {
        // Thanh Dock chính (Hiệu ứng Frosted Glass đậm hơn)
        Surface(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(36.dp),
                color = Color.White.copy(alpha = 0.85f), // Đậm hơn một chút để tạo độ dày cho kính
                border =
                        BorderStroke(
                                1.2.dp,
                                Color.White.copy(alpha = 0.6f)
                        ), // Viền sáng hơn để nổi khối
                shadowElevation = 20.dp, // Đổ bóng mạnh hơn
                tonalElevation = 12.dp // Tăng độ mờ ảo qua Tonal Elevation
        ) {
            Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
            ) {
                NavBarItem(NavItem.Home, currentRoute == NavItem.Home.route) {
                    onNavigate(NavItem.Home.route)
                }
                NavBarItem(NavItem.Budget, currentRoute == NavItem.Budget.route) {
                    onNavigate(NavItem.Budget.route)
                }

                Spacer(modifier = Modifier.width(48.dp)) // Chừa chỗ cho nút Add

                NavBarItem(NavItem.Ledger, currentRoute == NavItem.Ledger.route) {
                    onNavigate(NavItem.Ledger.route)
                }
                NavBarItem(NavItem.Profile, currentRoute == NavItem.Profile.route) {
                    onNavigate(NavItem.Profile.route)
                }
            }
        }

        // Nút FAB nổi bật ở giữa
        Surface(
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-24).dp).size(56.dp),
                shape = CircleShape,
                color = OceanBlue600,
                shadowElevation = 8.dp
        ) {
            Box(
                    modifier = Modifier.fillMaxSize().clickable { onAddClick() },
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(item: NavItem, isSelected: Boolean, onClick: () -> Unit) {
    val contentColor by
            animateColorAsState(
                    targetValue = if (isSelected) OceanBlue600 else NeutralGray600,
                    animationSpec = tween(300),
                    label = "color"
            )

    val scale by
            animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = tween(300),
                    label = "scale"
            )

    Column(
            modifier =
                    Modifier.width(60.dp).clip(RoundedCornerShape(12.dp)).clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                            ) { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                imageVector = if (isSelected) item.selectedIcon else item.icon,
                contentDescription = item.label,
                modifier =
                        Modifier.size(24.dp).graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                tint = contentColor
        )

        AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                    text = item.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
