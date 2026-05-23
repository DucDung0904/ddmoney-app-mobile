package com.dung.ddmoney.ui.budget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.repository.BudgetDisplayModel
import com.dung.ddmoney.ui.components.AppMoneyNumberPadSheet
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.components.formatMoneyInput
import com.dung.ddmoney.ui.components.parseMoneyInput
import com.dung.ddmoney.ui.components.withResolvedCategoryHierarchy
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.CategoryType
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.theme.LuminousBackground
import com.dung.ddmoney.ui.theme.LuminousOnBackground
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLow
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLowest
import com.dung.ddmoney.ui.theme.NeutralGray100
import com.dung.ddmoney.ui.theme.NeutralGray400
import com.dung.ddmoney.ui.theme.NeutralGray600
import com.dung.ddmoney.ui.theme.OceanBlue50
import com.dung.ddmoney.ui.theme.OceanBlue600
import com.dung.ddmoney.ui.theme.OceanBlue800
import com.dung.ddmoney.ui.wallets.WalletIconMap
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val AddBudgetBackground = LuminousBackground
private val DisabledSave = Color(0xFFC9C9C6)
private val SoftDivider = Color(0xFFE2E4EA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    categories: List<Category>,
    wallets: List<Wallet> = emptyList(),
    initialBudget: BudgetDisplayModel? = null,
    onSave: (name: String, amount: Double, categoryIds: List<Long>, month: Int, year: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember(initialBudget) {
        mutableStateOf(
            initialBudget
                ?.amount
                ?.toLong()
                ?.toString()
                ?.let { formatMoneyInput(it, emptyAsZero = true) }
                ?: "0"
        )
    }
    var repeatEnabled by remember { mutableStateOf(false) }
    var showAmountPad by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var selectedPeriod by remember(initialBudget) {
        mutableStateOf(initialBudget?.let { YearMonth.of(it.year, it.month) } ?: YearMonth.now())
    }
    var selectedWallet by remember { mutableStateOf<Wallet?>(null) }
    val selectedCategoryIds = remember(initialBudget) {
        mutableStateListOf<Long>().apply {
            initialBudget?.categories?.forEach { add(it.id) }
        }
    }

    LaunchedEffect(wallets) {
        val currentId = selectedWallet?.id
        if (currentId == null || wallets.none { it.id == currentId }) {
            selectedWallet = wallets.firstOrNull { it.isDefault } ?: wallets.firstOrNull()
        }
    }

    val expenseCategories =
        remember(categories) {
            categories
                .filter { it.type == CategoryType.EXPENSE }
                .withResolvedCategoryHierarchy()
                .sortedWith(compareBy({ it.sortOrder }, { it.name }))
        }
    val selectedIds = selectedCategoryIds.toSet()
    val selectedCategories =
        remember(selectedIds, expenseCategories) {
            expenseCategories.filter { category ->
                val categoryId = category.id.toLongOrNull()
                categoryId != null && categoryId in selectedIds
            }
        }
    val categoryLabel =
        when {
            selectedCategories.isEmpty() -> "Chọn nhóm"
            selectedCategories.size == 1 -> selectedCategories.first().name
            else -> "${selectedCategories.size} nhóm đã chọn"
        }
    val amount = parseMoneyInput(amountText)
    val canSave = selectedCategoryIds.isNotEmpty() && amount > 0.0
    val inferredName =
        when {
            selectedCategories.isEmpty() -> initialBudget?.name ?: "Ngân sách tháng ${selectedPeriod.monthValue}"
            selectedCategories.size == 1 -> selectedCategories.first().name
            else -> selectedCategories.joinToString(", ") { it.name }
        }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AddBudgetBackground,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        dragHandle = null
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f)
                    .statusBarsPadding()
        ) {
            AddBudgetHeader(
                isEditing = initialBudget != null,
                onDismiss = onDismiss
            )

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BudgetFormCard(
                    categoryLabel = categoryLabel,
                    selectedCategories = selectedCategories,
                    amountText = amountText,
                    period = selectedPeriod,
                    wallet = selectedWallet,
                    onCategoryClick = { showCategoryPicker = true },
                    onAmountClick = { showAmountPad = true },
                    onPeriodClick = { showMonthPicker = true }
                )

                RepeatBudgetCard(
                    checked = repeatEnabled,
                    onCheckedChange = { repeatEnabled = it }
                )
            }

            Button(
                onClick = {
                    if (canSave) {
                        onSave(
                            inferredName,
                            amount,
                            selectedCategoryIds.toList(),
                            selectedPeriod.monthValue,
                            selectedPeriod.year
                        )
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                        .navigationBarsPadding()
                        .height(52.dp),
                enabled = canSave,
                shape = RoundedCornerShape(29.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = OceanBlue600,
                        disabledContainerColor = DisabledSave,
                        contentColor = LuminousSurfaceContainerLowest,
                        disabledContentColor = LuminousSurfaceContainerLowest.copy(alpha = 0.82f)
                    )
            ) {
                Text(
                text = "Lưu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }

    AppMoneyNumberPadSheet(
        visible = showAmountPad,
        amountText = amountText,
        onAmountChange = { amountText = formatMoneyInput(it, emptyAsZero = true) },
        onDismiss = { showAmountPad = false },
        title = "Số tiền",
        showSuggestions = true
    )

    BudgetCategoryPickerSheet(
        visible = showCategoryPicker,
        categories = expenseCategories,
        selectedCategoryIds = selectedCategoryIds,
        onDismiss = { showCategoryPicker = false }
    )

    BudgetMonthPickerSheet(
        visible = showMonthPicker,
        selectedPeriod = selectedPeriod,
        onSelect = {
            selectedPeriod = it
            showMonthPicker = false
        },
        onDismiss = { showMonthPicker = false }
    )
}

@Composable
private fun AddBudgetHeader(isEditing: Boolean, onDismiss: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 6.dp, bottom = 22.dp)
    ) {
        Surface(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onDismiss,
            shape = RoundedCornerShape(26.dp),
            color = LuminousSurfaceContainerLowest,
            shadowElevation = 8.dp,
            tonalElevation = 2.dp
        ) {
            Text(
                text = "Huỷ",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                color = LuminousOnBackground,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = if (isEditing) "Sửa ngân sách" else "Thêm ngân sách",
            modifier = Modifier.align(Alignment.Center),
            color = LuminousOnBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun BudgetFormCard(
    categoryLabel: String,
    selectedCategories: List<Category>,
    amountText: String,
    period: YearMonth,
    wallet: Wallet?,
    onCategoryClick: () -> Unit,
    onAmountClick: () -> Unit,
    onPeriodClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(start = 16.dp)) {
            BudgetFormRow(
                onClick = onCategoryClick,
                icon = {
                    CategoryAvatar(selectedCategories = selectedCategories)
                },
                content = {
                    Text(
                        text = categoryLabel,
                        color = if (selectedCategories.isEmpty()) NeutralGray400 else LuminousOnBackground,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )

            FormDivider()

            AmountFormRow(
                amountText = amountText,
                onClick = onAmountClick
            )

            FormDivider()

            BudgetFormRow(
                onClick = onPeriodClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = LuminousOnBackground,
                        modifier = Modifier.size(22.dp)
                    )
                },
                content = {
                    Text(
                        text = formatPeriodLabel(period),
                        color = LuminousOnBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )

            FormDivider()

            BudgetFormRow(
                onClick = {},
                showChevron = false,
                icon = {
                    WalletAvatar(wallet = wallet)
                },
                content = {
                    Text(
                        text = wallet?.name ?: "Tất cả ví",
                        color = LuminousOnBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun CategoryAvatar(selectedCategories: List<Category>) {
    val first = selectedCategories.firstOrNull()
    Box(
        modifier =
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(first?.color?.copy(alpha = 0.14f) ?: NeutralGray100),
        contentAlignment = Alignment.Center
    ) {
        if (first == null) {
            Box(
                modifier =
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(NeutralGray400.copy(alpha = 0.34f))
            )
        } else {
            CategoryIcon(
                icon = first.icon,
                modifier = Modifier.size(23.dp),
                tint = first.color
            )
        }
    }
}

@Composable
private fun WalletAvatar(wallet: Wallet?) {
    Box(
        modifier =
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (wallet == null) OceanBlue50 else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (wallet == null) {
            Icon(
                imageVector = Icons.Outlined.CreditCard,
                contentDescription = null,
                tint = OceanBlue600,
                modifier = Modifier.size(20.dp)
            )
        } else {
            WalletIconMap.WalletIcon(
                key = wallet.icon,
                walletType = wallet.type,
                contentDescription = null,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun AmountFormRow(
    amountText: String,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(52.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = LuminousSurfaceContainerLowest,
                border = BorderStroke(1.dp, SoftDivider)
            ) {
                Text(
                    text = "VND",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    color = NeutralGray600,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Số tiền",
                color = NeutralGray600,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amountText.ifBlank { "0" },
                color = LuminousOnBackground,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BudgetFormRow(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
    showChevron: Boolean = true
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(end = 14.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(52.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            icon()
        }
        Box(modifier = Modifier.weight(1f)) { content() }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = NeutralGray400,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun FormDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = SoftDivider,
        thickness = 1.dp
    )
}

@Composable
private fun RepeatBudgetCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = LuminousSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 14.dp, top = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lặp lại ngân sách này",
                    color = LuminousOnBackground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ngân sách được tự động lặp lại ở kỳ hạn tiếp theo.",
                    color = NeutralGray600,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = LuminousSurfaceContainerLowest,
                        checkedTrackColor = OceanBlue600,
                        uncheckedThumbColor = LuminousSurfaceContainerLowest,
                        uncheckedTrackColor = DisabledSave,
                        uncheckedBorderColor = Color.Transparent,
                        checkedBorderColor = Color.Transparent
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetCategoryPickerSheet(
    visible: Boolean,
    categories: List<Category>,
    selectedCategoryIds: MutableList<Long>,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val parentCategories = remember(categories) {
        categories
            .filter { it.parentId == null }
            .sortedWith(compareBy({ it.sortOrder }, { it.name }))
    }
    val expandedParentIds = remember(categories) {
        mutableStateMapOf<String, Boolean>().apply {
            parentCategories.forEach { put(it.id, true) }
        }
    }
    val defaultCategories = remember(categories) { categories.filter { it.isDefault } }
    val customCategories = remember(categories) { categories.filter { !it.isDefault } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LuminousSurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = 0.2f)
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "Chọn nhóm ngân sách",
                color = LuminousOnBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier =
                    Modifier
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryPickerSection(
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
                    CategoryPickerSection(
                        title = "Danh mục của tôi",
                        categories = customCategories,
                        expandedParentIds = expandedParentIds,
                        selectedCategoryIds = selectedCategoryIds,
                        onToggleExpand = { parentId, expanded ->
                            expandedParentIds[parentId] = !expanded
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue600)
            ) {
                Text(
                    text = "Xong",
                    color = LuminousSurfaceContainerLowest,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun CategoryPickerSection(
    title: String,
    categories: List<Category>,
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
        color = NeutralGray600,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )

    parentCategories.forEach { parent ->
        val children =
            childrenByParent[parent.id]
                ?.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                .orEmpty()
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
                val childId = child.id.toLongOrNull()
                BudgetCategoryRow(
                    category = child,
                    isParent = false,
                    isExpanded = false,
                    isSelected = childId != null && selectedCategoryIds.contains(childId),
                    indent = 22.dp,
                    onClick = { toggleBudgetCategory(child.id, selectedCategoryIds) },
                    onToggleExpand = null
                )
            }
        }
    }
}

@Composable
private fun BudgetCategoryRow(
    category: Category,
    isParent: Boolean,
    isExpanded: Boolean,
    isSelected: Boolean,
    indent: Dp,
    onClick: () -> Unit,
    onToggleExpand: (() -> Unit)?
) {
    Row(
        modifier =
            Modifier
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(if (isParent) 42.dp else 38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(category.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            CategoryIcon(
                icon = category.icon,
                modifier = Modifier.size(if (isParent) 20.dp else 18.dp),
                tint = category.color
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = category.name,
            modifier = Modifier.weight(1f),
            color = if (isSelected || isParent) LuminousOnBackground else NeutralGray600,
            fontSize = if (isParent) 14.sp else 13.sp,
            fontWeight = if (isParent) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (isParent) {
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleExpand?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector =
                        if (isExpanded) Icons.Outlined.KeyboardArrowDown
                        else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = NeutralGray600,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Icon(
                imageVector =
                    if (isSelected) Icons.Outlined.CheckCircle
                    else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) OceanBlue600 else NeutralGray400,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetMonthPickerSheet(
    visible: Boolean,
    selectedPeriod: YearMonth,
    onSelect: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val current = remember { YearMonth.now() }
    val months = remember(current) { (-1L..5L).map { current.plusMonths(it) } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LuminousSurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = 0.2f)
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "Chọn kỳ ngân sách",
                color = LuminousOnBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            months.forEach { month ->
                val selected = month == selectedPeriod
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) OceanBlue50 else Color.Transparent)
                            .clickable { onSelect(month) }
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = if (selected) OceanBlue600 else NeutralGray600,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formatPeriodLabel(month),
                        modifier = Modifier.weight(1f),
                        color = if (selected) OceanBlue800 else LuminousOnBackground,
                        fontSize = 15.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = OceanBlue600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
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
    parent: Category,
    children: List<Category>,
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

private fun formatPeriodLabel(period: YearMonth): String {
    val current = YearMonth.now()
    val start = period.atDay(1)
    val end = period.atEndOfMonth()
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
    val prefix = if (period == current) "Tháng này" else "Tháng ${period.monthValue}"
    return "$prefix (${start.format(dateFormatter)} - ${end.format(dateFormatter)})"
}
