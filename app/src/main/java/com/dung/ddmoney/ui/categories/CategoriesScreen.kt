package com.dung.ddmoney.ui.categories

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.dung.ddmoney.AppState
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import com.dung.ddmoney.ui.transactions.EMOJI_OPTIONS

// ─── Preset color options ─────────────────────────────────────────────
val COLOR_OPTIONS = listOf(
    CategoryFood, CategoryTransport, CategoryShopping, CategoryHealth,
    CategoryEntertainment, LuminousPrimary, SavingsBlue, InvestmentYellow,
    Color(0xFFD82D8B), Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFF8BC34A),
    Color(0xFFFF5722), Color(0xFF607D8B), ExpenseRed, IncomeGreen,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    appState: AppState,
    onAddCategory: (name: String, icon: String, color: Color, type: CategoryType) -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0=Expense, 1=Income
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }

    val expenseCategories = appState.expenseCategories()
    val incomeCategories = appState.incomeCategories()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background) // Asymmetric breathable design
                .padding(top = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Quản lý Danh mục",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    IconButton(onClick = { showAddDialog = true }) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    listOf("Chi tiêu", "Thu nhập").forEachIndexed { index, label ->
                        val isSelected = selectedTab == index
                        val tabColor = if (index == 0) ExpenseRed else IncomeGreen
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = index }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    label,
                                    color = if (isSelected) tabColor else MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(Modifier.height(6.dp))
                                AnimatedVisibility(visible = isSelected) {
                                    Box(
                                        Modifier
                                            .width(48.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(1.5.dp))
                                            .background(tabColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Category List ────────────────────────────────────────────
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState > initialState) slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                else slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
            },
            label = "TabContent"
        ) { tab ->
            val categories = if (tab == 0) expenseCategories else incomeCategories
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 16.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 48.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryListItem(
                        category = category,
                        onEdit = { editingCategory = category },
                        onDelete = {
                            if (!category.isDefault) deleteConfirmId = category.id
                        }
                    )
                }
                item {
                    // Add button at bottom
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Thêm danh mục mới",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // ── Add/Edit Dialog ──────────────────────────────────────────────
    if (showAddDialog || editingCategory != null) {
        CategoryFormDialog(
            existing = editingCategory,
            initialType = if (selectedTab == 0) CategoryType.EXPENSE else CategoryType.INCOME,
            onDismiss = { showAddDialog = false; editingCategory = null },
            onSave = { name, icon, color, type ->
                if (editingCategory != null) {
                    onEditCategory(
                        editingCategory!!.copy(
                            name = name,
                            icon = icon,
                            color = color,
                            type = type
                        )
                    )
                } else {
                    onAddCategory(name, icon, color, type)
                }
                showAddDialog = false
                editingCategory = null
            }
        )
    }

    // ── Delete Confirm Dialog ────────────────────────────────────────
    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            title = {
                Text(
                    "Xóa danh mục?",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Danh mục này sẽ bị xóa vĩnh viễn.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteCategory(id)
                    deleteConfirmId = null
                }) { Text("Xóa", color = ExpenseRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) {
                    Text(
                        "Huỷ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

// ─── Category row item ────────────────────────────────────────────────
@Composable
private fun CategoryListItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(category.icon, fontSize = 24.sp)
        }

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (category.isDefault) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(category.color.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Mặc định",
                            color = category.color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = when (category.type) {
                    CategoryType.EXPENSE -> "Chi tiêu"
                    CategoryType.INCOME  -> "Thu nhập"
                    CategoryType.BOTH    -> "Thu & Chi"
                    CategoryType.DEBT    -> "Vay/Nợ"
                },
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape)
            ) {
                Icon(
                    Icons.Default.Edit,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (!category.isDefault) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Add/Edit Category Dialog ─────────────────────────────────────────
@Composable
private fun CategoryFormDialog(
    existing: Category?,
    initialType: CategoryType,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: Color, type: CategoryType) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(existing?.icon ?: "📦") }
    var selectedColor by remember { mutableStateOf(existing?.color ?: CategoryFood) }
    var selectedType by remember { mutableStateOf(existing?.type ?: initialType) }

    val isValid = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                text = if (existing != null) "Chỉnh sửa danh mục" else "Danh mục mới",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(selectedColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedIcon, fontSize = 28.sp)
                    }
                    Column {
                        Text(
                            text = name.ifBlank { "Tên danh mục" },
                            color = if (name.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when (selectedType) {
                                CategoryType.EXPENSE -> "Chi tiêu"
                                CategoryType.INCOME  -> "Thu nhập"
                                CategoryType.BOTH    -> "Thu & Chi"
                                CategoryType.DEBT    -> "Vay/Nợ"
                            },
                            color = selectedColor,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            "Tên danh mục",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Type selector
                Text(
                    "Loại danh mục",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(
                        CategoryType.EXPENSE to "Chi tiêu",
                        CategoryType.INCOME to "Thu nhập"
                    ).forEach { (type, label) ->
                        val isSelected = selectedType == type
                        val color = if (type == CategoryType.EXPENSE) ExpenseRed else IncomeGreen
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) color.copy(0.15f) else MaterialTheme.colorScheme.surfaceContainerLow)
                                .clickable { selectedType = type }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSelected) color else MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Icon picker
                Text(
                    "Chọn icon",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier.heightIn(max = 200.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = true
                ) {
                    items(EMOJI_OPTIONS) { emoji ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedIcon == emoji) selectedColor.copy(0.25f) else MaterialTheme.colorScheme.surfaceContainerHighest)
                                .clickable { selectedIcon = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 16.sp)
                        }
                    }
                }

                // Color picker
                Text(
                    "Chọn màu",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier.heightIn(max = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(COLOR_OPTIONS) { color ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    3.dp,
                                    if (selectedColor == color) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, selectedIcon, selectedColor, selectedType) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Lưu",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        dismissButton = {}
    )
}
