package com.dung.ddmoney.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.theme.*

/**
 * HomeHeroSection
 *
 * ┌─────────────────────────────────────────────┐
 * │  [Navy→Blue gradient — edge-to-edge]         │
 * │  ┌─ search pill ─────────────────── 🔔 ─┐  │
 * │  └──────────────────────────────────────┘  │
 * │                                             │
 * │  ┌─── floating balance card (3/4 overlap) ┐  │
 * │  │  Tổng số dư   2,000,000 đ  👁          │  │
 * │  └────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────┘
 * [LuminousBackground continues below]
 */
@Composable
internal fun HomeHeroSection(
    name: String,
    balance: Double,
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {

        // ── Layer 1: Gradient slab ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF0D3B8E),  // deep navy
                            0.5f to Color(0xFF1565C0),  // rich blue
                            1.0f to Color(0xFF42A5F5)   // sky blue
                        )
                    )
                )
        ) {
            // Decorative bokeh circles
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 55.dp, y = (-55).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.09f))
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
            )
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-30).dp, y = 50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            // Soft scrim fade at bottom — blends gradient into background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                LuminousBackground.copy(alpha = 0.55f),
                                LuminousBackground
                            )
                        )
                    )
            )
        }

        // ── Layer 2: Content column ───────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {

            // Search bar pill + bell icon
            HomeSearchBar()

            // Balance card — floats 3/4 over the gradient
            BalanceSummaryCard(
                balance   = balance,
                isVisible = isVisible,
                onToggle  = onToggle
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ─── Search bar pill ──────────────────────────────────────────────────────────
@Composable
private fun HomeSearchBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.22f))
            .clickable { }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector    = Icons.Outlined.Search,
                contentDescription = "Tìm kiếm",
                modifier = Modifier.size(18.dp),
                tint     = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text       = "Tìm kiếm giao dịch...",
                fontSize   = 14.sp,
                color      = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.weight(1f)
            )
            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(Color.White.copy(alpha = 0.25f))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector    = Icons.Outlined.Notifications,
                contentDescription = "Thông báo",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { },
                tint = Color.White
            )
        }
    }
}

// ─── Balance card ─────────────────────────────────────────────────────────────
@Composable
private fun BalanceSummaryCard(
    balance: Double,
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape          = RoundedCornerShape(24.dp),
        color          = LuminousSurfaceContainerLowest,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text       = "Tổng số dư",
                fontSize   = 12.sp,
                color      = NeutralGray600,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text       = if (isVisible)
                        "${String.format("%,.0f", balance)} đ"
                    else
                        "••••••••• đ",
                    fontSize   = 30.sp,
                    fontWeight = FontWeight.Black,
                    color      = OceanBlue800
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(OceanBlue50)
                        .clickable { onToggle() }
                        .padding(7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVisible)
                            Icons.Outlined.Visibility
                        else
                            Icons.Outlined.VisibilityOff,
                        contentDescription = "Ẩn/hiện số dư",
                        modifier = Modifier.size(17.dp),
                        tint     = OceanBlue600
                    )
                }
            }
        }
    }
}
