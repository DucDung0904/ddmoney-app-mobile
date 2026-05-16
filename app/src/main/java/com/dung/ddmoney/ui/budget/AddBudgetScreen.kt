package com.dung.ddmoney.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.components.withResolvedCategoryHierarchy
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    categories: List<com.dung.ddmoney.ui.dashboard.model.Category>,
    onSave: (name: String, amount: Double, categoryIds: List<Long>, month: Int, year: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val selectedCategoryIds = remember { mutableStateListOf<Long>() }
    
    val currentDate = LocalDate.now()
    var selectedMonth by remember { mutableStateOf(currentDate.monthValue) }
    var selectedYear by remember { mutableStateOf(currentDate.year) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LuminousBackground,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(NeutralGray400.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Tạo ngân sách mới",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                "Thiết lập hạn mức chi tiêu cho các danh mục",
                fontSize = 14.sp,
                color = NeutralGray600,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tên ngân sách
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên ngân sách (VD: Ăn uống & giải trí)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OceanBlue600,
                    unfocusedBorderColor = NeutralGray100
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Số tiền
            OutlinedTextField(
                value = amountText,
                onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                label = { Text("Hạn mức (VND)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OceanBlue600,
                    unfocusedBorderColor = NeutralGray100
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Chọn danh mục áp dụng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val expenseCategories =
                categories
                    .filter { it.type == com.dung.ddmoney.ui.dashboard.model.CategoryType.EXPENSE }
                    .withResolvedCategoryHierarchy()
                    .sortedWith(compareBy({ it.sortOrder }, { it.name }))
            val parentCategories = expenseCategories.filter { it.parentId == null }
            val expandedParentIds = remember(expenseCategories) {
                mutableStateMapOf<String, Boolean>().apply {
                    parentCategories.forEach { put(it.id, true) }
                }
            }
            val defaultCategories = expenseCategories.filter { it.isDefault }
            val customCategories = expenseCategories.filter { !it.isDefault }

            Column(
                modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BudgetCategorySection(
                    title = "Danh mục mặc định",
                    categories = defaultCategories,
                    expandedParentIds = expandedParentIds,
                    selectedCategoryIds = selectedCategoryIds,
                    onToggleExpand = { parentId, expanded ->
                        expandedParentIds[parentId] = !expanded
                    }
                )

                if (customCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BudgetCategorySection(
                        title = "Danh mục của tôi",
                        categories = customCategories,
                        expandedParentIds = expandedParentIds,
                        selectedCategoryIds = selectedCategoryIds,
                        onToggleExpand = { parentId, expanded ->
                            expandedParentIds[parentId] = !expanded
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nút Lưu
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0 && selectedCategoryIds.isNotEmpty()) {
                        onSave(name, amount, selectedCategoryIds.toList(), selectedMonth, selectedYear)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600),
                enabled = name.isNotBlank() && amountText.isNotBlank() && selectedCategoryIds.isNotEmpty()
            ) {
                Text("Lưu ngân sách", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BudgetCategorySection(
    title: String,
    categories: List<com.dung.ddmoney.ui.dashboard.model.Category>,
    expandedParentIds: Map<String, Boolean>,
    selectedCategoryIds: MutableList<Long>,
    onToggleExpand: (parentId: String, isExpanded: Boolean) -> Unit
) {
    if (categories.isEmpty()) return

    val categoryIds = categories.map { it.id }.toSet()
    val childrenByParent =
        categories
            .filter { it.parentId != null && it.parentId in categoryIds }
            .groupBy { it.parentId }
    val parentCategories =
        categories
            .filter {
                val parentId = it.parentId
                parentId == null || parentId !in categoryIds
            }
            .sortedWith(compareBy({ it.sortOrder }, { it.name }))

    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = NeutralGray600,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )

    parentCategories.forEach { parent ->
        val children = childrenByParent[parent.id].orEmpty()
        val isExpanded = expandedParentIds[parent.id] ?: true
        val groupIds = (listOf(parent) + children).mapNotNull { it.id.toLongOrNull() }
        val isParentSelected =
            groupIds.isNotEmpty() && groupIds.all { selectedCategoryIds.contains(it) }
        BudgetCategoryRow(
            category = parent,
            isParent = children.isNotEmpty(),
            isExpanded = isExpanded,
            isSelected = isParentSelected,
            indent = 0.dp,
            onClick = {
                if (children.isEmpty()) {
                    toggleBudgetCategory(parent.id, selectedCategoryIds)
                } else {
                    toggleBudgetCategoryGroup(parent, children, selectedCategoryIds)
                }
            },
            onToggleExpand =
                if (children.isNotEmpty()) {
                    { onToggleExpand(parent.id, isExpanded) }
                } else {
                    null
                }
        )
        if (isExpanded) {
            children.forEach { child ->
                val childId = child.id.toLongOrNull() ?: 0L
                BudgetCategoryRow(
                    category = child,
                    isParent = false,
                    isExpanded = false,
                    isSelected = selectedCategoryIds.contains(childId),
                    indent = 22.dp,
                    onClick = { toggleBudgetCategory(child.id, selectedCategoryIds) },
                    onToggleExpand = null
                )
            }
        }
    }
}

private fun toggleBudgetCategory(
    categoryId: String,
    selectedCategoryIds: MutableList<Long>
) {
    val catIdLong = categoryId.toLongOrNull() ?: return
    if (selectedCategoryIds.contains(catIdLong)) {
        selectedCategoryIds.remove(catIdLong)
    } else {
        selectedCategoryIds.add(catIdLong)
    }
}

private fun toggleBudgetCategoryGroup(
    parent: com.dung.ddmoney.ui.dashboard.model.Category,
    children: List<com.dung.ddmoney.ui.dashboard.model.Category>,
    selectedCategoryIds: MutableList<Long>
) {
    val ids = (listOf(parent) + children).mapNotNull { it.id.toLongOrNull() }
    if (ids.isEmpty()) return

    if (ids.all { selectedCategoryIds.contains(it) }) {
        selectedCategoryIds.removeAll(ids.toSet())
    } else {
        ids.forEach { id ->
            if (!selectedCategoryIds.contains(id)) {
                selectedCategoryIds.add(id)
            }
        }
    }
}

@Composable
private fun BudgetCategoryRow(
    category: com.dung.ddmoney.ui.dashboard.model.Category,
    isParent: Boolean,
    isExpanded: Boolean,
    isSelected: Boolean,
    indent: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onToggleExpand: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .clip(RoundedCornerShape(18.dp))
            .background(
                when {
                    isSelected -> OceanBlue50
                    isParent -> LuminousSurfaceContainerLow
                    else -> Color.Transparent
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f).clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isParent) 42.dp else 38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) OceanBlue100 else NeutralGray50),
                contentAlignment = Alignment.Center
            ) {
                CategoryIcon(
                    icon = category.icon,
                    modifier = Modifier.size(if (isParent) 22.dp else 20.dp),
                    tint = category.color
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = category.name,
                modifier = Modifier.weight(1f),
                fontSize = if (isParent) 15.sp else 14.sp,
                fontWeight = if (isParent) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected || isParent) OceanBlue600 else NeutralGray600
            )
        }

        if (isParent) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onToggleExpand?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector =
                        if (isExpanded) Icons.Outlined.KeyboardArrowDown
                        else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = NeutralGray600,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
