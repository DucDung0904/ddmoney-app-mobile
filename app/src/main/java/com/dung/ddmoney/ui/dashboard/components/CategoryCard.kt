package com.dung.ddmoney.ui.dashboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.SampleData
import com.dung.ddmoney.ui.theme.*

// ─── Financial Ambitions (Goals) Section ─────────────────────────────
@Composable
fun CategorySection(
    categories: List<Category>,
    onManageCategories: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Financial Ambitions",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onManageCategories) {
                Text(
                    text = "Xem tất cả",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Goal cards
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoalCard(
                icon = "✈️",
                title = "Chuyến du lịch Đà Nẵng",
                targetLabel = "Mục tiêu: 15.000.000đ",
                progress = 0.65f,
                progressLabel = "65% Hoàn thành",
                color = MaterialTheme.colorScheme.primary
            )
            GoalCard(
                icon = "🏠",
                title = "Quỹ thuê phòng mới",
                targetLabel = "Mục tiêu: 20.000.000đ",
                progress = 0.32f,
                progressLabel = "32% Hoàn thành",
                color = Color(0xFF7C4DFF)
            )
            // Category spending breakdown
            categories.take(3).forEach { category ->
                CategoryProgressRow(category = category)
            }
        }
    }
}

// ─── Goal Card ────────────────────────────────────────────────────────
@Composable
fun GoalCard(
    icon: String,
    title: String,
    targetLabel: String,
    progress: Float,
    progressLabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "goal_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 20.sp)
                }
                Column {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = targetLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            // Progress percentage badge
            Box(
                modifier = Modifier
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = progressLabel,
                    color = color,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.7f), color)
                        )
                    )
            )
        }
    }
}

// ─── Category Progress Row ────────────────────────────────────────────
@Composable
fun CategoryProgressRow(
    category: Category,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = category.percentage,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress_${category.name}"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = category.icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = category.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatCurrency(category.totalAmount),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(category.percentage * 100).toInt()}%",
                    color = category.color,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                category.color.copy(alpha = 0.7f),
                                category.color
                            )
                        )
                    )
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF7F9FF)
@Composable
private fun CategorySectionPreview() {
    DDMoneyTheme(darkTheme = false) {
        CategorySection(
            categories = SampleData.categories,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
