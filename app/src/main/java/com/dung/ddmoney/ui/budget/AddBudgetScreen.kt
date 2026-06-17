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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.dung.ddmoney.repository.BudgetPeriod
import com.dung.ddmoney.repository.BudgetPeriodType
import com.dung.ddmoney.ui.components.AppMoneyNumberPadSheet
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.components.formatMoneyInput
import com.dung.ddmoney.ui.components.formatMoneyDisplay
import com.dung.ddmoney.ui.components.parseMoneyInput
import com.dung.ddmoney.ui.components.withResolvedCategoryHierarchy
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.CategoryType
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.theme.ExpenseRed50
import com.dung.ddmoney.ui.theme.LuminousBackground
import com.dung.ddmoney.ui.theme.LuminousOnBackground
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLow
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLowest
import com.dung.ddmoney.ui.theme.ExpenseRed600
import com.dung.ddmoney.ui.theme.NeutralGray100
import com.dung.ddmoney.ui.theme.NeutralGray400
import com.dung.ddmoney.ui.theme.NeutralGray600
import com.dung.ddmoney.ui.theme.OceanBlue50
import com.dung.ddmoney.ui.theme.OceanBlue600
import com.dung.ddmoney.ui.theme.OceanBlue800

private val AddBudgetBackground = LuminousBackground
private val DisabledSave = Color(0xFFC9C9C6)
private val SoftDivider = Color(0xFFE2E4EA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    categories: List<Category>,
    wallets: List<Wallet> = emptyList(),
    transactions: List<Transaction> = emptyList(),
    existingBudgets: List<BudgetDisplayModel> = emptyList(),
    initialBudget: BudgetDisplayModel? = null,
    initialPeriodType: BudgetPeriodType = BudgetPeriodType.MONTH,
    onSave: (
        name: String,
        amount: Double,
        categoryIds: List<Long>,
        period: BudgetPeriod,
        walletId: Long?
    ) -> Unit,
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
    var showAmountPad by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showWalletPicker by remember { mutableStateOf(false) }
    var selectedPeriod by remember(initialBudget, initialPeriodType) {
        mutableStateOf(
            initialBudget?.let {
                BudgetPeriod(it.periodType, it.startDate, it.endDate)
            } ?: BudgetPeriod.current(initialPeriodType)
        )
    }
    var selectedWalletId by remember(initialBudget) { mutableStateOf(initialBudget?.walletId) }
    val selectedCategoryIds = remember(initialBudget) {
        mutableStateListOf<Long>().apply {
            initialBudget?.categories?.forEach { category -> add(category.id) }
        }
    }

    LaunchedEffect(wallets, selectedWalletId) {
        if (
            selectedWalletId != null &&
                wallets.isNotEmpty() &&
                wallets.none { it.id.toLongOrNull() == selectedWalletId }
        ) {
            selectedWalletId = null
        }
    }
    val selectableWallets = remember(wallets) { wallets.filter { it.id.toLongOrNull() != null } }
    val selectedWallet =
        remember(selectableWallets, selectedWalletId) {
            selectableWallets.firstOrNull { it.id.toLongOrNull() == selectedWalletId }
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
                category.id.toLongOrNull() in selectedIds
            }
        }
    val categoryLabel =
        when {
            selectedCategories.isEmpty() -> "Chọn nhóm"
            selectedCategories.size == 1 -> selectedCategories.first().name
            else -> "${selectedCategories.size} nhóm đã chọn"
        }
    val amount = parseMoneyInput(amountText)
    val conflictingCategoryIds =
        remember(existingBudgets, selectedIds, selectedPeriod, initialBudget?.id) {
            findConflictingCategoryIdsForPeriod(
                budgets = existingBudgets,
                categoryIds = selectedIds,
                period = selectedPeriod,
                excludedBudgetId = initialBudget?.id
            )
        }
    val conflictingCategoryNames =
        remember(conflictingCategoryIds, selectedCategories) {
            selectedCategories
                .filter { it.id.toLongOrNull() in conflictingCategoryIds }
                .joinToString(", ") { it.name }
        }
    val existingSpent =
        remember(transactions, expenseCategories, selectedIds, selectedPeriod, selectedWalletId) {
            calculateSpentForBudgetDraft(
                transactions = transactions,
                categories = expenseCategories,
                selectedCategoryIds = selectedIds,
                period = selectedPeriod,
                walletId = selectedWalletId
            )
        }
    val exceededAmount = (existingSpent - amount).coerceAtLeast(0.0)
    val isAlreadyOverLimit =
        amount > 0.0 &&
            selectedCategoryIds.isNotEmpty() &&
            existingSpent > amount
    val canSave =
        selectedCategoryIds.isNotEmpty() &&
            amount > 0.0 &&
            conflictingCategoryIds.isEmpty()
    val inferredName =
        when {
            selectedCategories.isEmpty() ->
                initialBudget?.name
                    ?: "Ngân sách ${budgetPeriodTypeLabel(selectedPeriod.type).lowercase()}"
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
                    onPeriodClick = { showPeriodPicker = true },
                    onWalletClick = { showWalletPicker = true }
                )

                if (conflictingCategoryIds.isNotEmpty()) {
                    Text(
                        text =
                            "Các danh mục đã có ngân sách cho " +
                                formatBudgetPeriodLabel(selectedPeriod) +
                                ": $conflictingCategoryNames.",
                        color = ExpenseRed600,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                if (isAlreadyOverLimit) {
                    BudgetExceededWarning(
                        spentAmount = existingSpent,
                        exceededAmount = exceededAmount
                    )
                }
            }

            Button(
                onClick = {
                    if (canSave) {
                        onSave(
                            inferredName,
                            amount,
                            selectedCategoryIds.toList(),
                            selectedPeriod,
                            selectedWalletId
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

    BudgetPeriodPickerSheet(
        visible = showPeriodPicker,
        selectedPeriod = selectedPeriod,
        onSelect = {
            selectedPeriod = it
            showPeriodPicker = false
        },
        onDismiss = { showPeriodPicker = false }
    )

    BudgetWalletPickerSheet(
        visible = showWalletPicker,
        wallets = selectableWallets,
        selectedWalletId = selectedWalletId,
        onSelect = {
            selectedWalletId = it
            showWalletPicker = false
        },
        onDismiss = { showWalletPicker = false }
    )
}

@Composable
private fun BudgetExceededWarning(
    spentAmount: Double,
    exceededAmount: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ExpenseRed50
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = ExpenseRed600,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Ngân sách này đã vượt hạn mức",
                    color = ExpenseRed600,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text =
                        "Đã chi ${formatMoneyDisplay(spentAmount)} trong kỳ, " +
                            "vượt ${formatMoneyDisplay(exceededAmount)} so với hạn mức đang nhập.",
                    color = LuminousOnBackground,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
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
    period: BudgetPeriod,
    wallet: Wallet?,
    onCategoryClick: () -> Unit,
    onAmountClick: () -> Unit,
    onPeriodClick: () -> Unit,
    onWalletClick: () -> Unit
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
                        text = formatBudgetPeriodLabel(period),
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
                onClick = onWalletClick,
                icon = {
                    BudgetWalletIcon(wallet = wallet, size = 34.dp)
                },
                content = {
                    Text(
                        text = wallet?.name ?: "Tổng cộng",
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

private fun toggleBudgetCategory(
    categoryId: String,
    selectedCategoryIds: MutableList<Long>
) {
    val id = categoryId.toLongOrNull() ?: return
    if (id in selectedCategoryIds) {
        selectedCategoryIds.remove(id)
    } else {
        selectedCategoryIds.add(id)
    }
}

private fun toggleBudgetCategoryGroup(
    parent: Category,
    children: List<Category>,
    selectedCategoryIds: MutableList<Long>
) {
    val ids = (listOf(parent) + children).mapNotNull { it.id.toLongOrNull() }
    if (ids.isEmpty()) return

    if (ids.all { it in selectedCategoryIds }) {
        selectedCategoryIds.removeAll(ids.toSet())
    } else {
        ids.forEach { id ->
            if (id !in selectedCategoryIds) {
                selectedCategoryIds.add(id)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetPeriodPickerSheet(
    visible: Boolean,
    selectedPeriod: BudgetPeriod,
    onSelect: (BudgetPeriod) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var displayedType by remember(selectedPeriod) { mutableStateOf(selectedPeriod.type) }
    var displayedYear by remember(selectedPeriod) { mutableIntStateOf(selectedPeriod.startDate.year) }
    val periods =
        remember(displayedType, displayedYear) {
            BudgetPeriod.periodsInYear(displayedType, displayedYear)
        }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = { displayedYear-- },
                    shape = CircleShape,
                    color = LuminousSurfaceContainerLow
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Năm trước",
                        modifier = Modifier.padding(12.dp).size(18.dp),
                        tint = LuminousOnBackground
                    )
                }
                Text(
                    text = displayedYear.toString(),
                    color = LuminousOnBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    onClick = { displayedYear++ },
                    shape = CircleShape,
                    color = LuminousSurfaceContainerLow
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = "Năm sau",
                        modifier = Modifier.padding(12.dp).size(18.dp),
                        tint = LuminousOnBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            BudgetPeriodTabRow(
                selectedPosition = budgetPeriodTypes.indexOf(displayedType).toFloat(),
                useCurrentLabels = false,
                onSelect = { displayedType = it }
            )

            LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                items(
                    items = periods,
                    key = { "${it.type}-${it.startDate}" }
                ) { period ->
                    val selected =
                        period.type == selectedPeriod.type &&
                            period.startDate == selectedPeriod.startDate &&
                            period.endDate == selectedPeriod.endDate
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selected) OceanBlue50 else Color.Transparent)
                                .clickable { onSelect(period) }
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
                            text = formatBudgetPeriodLabel(period),
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
}
