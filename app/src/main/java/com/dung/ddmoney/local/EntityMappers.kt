package com.dung.ddmoney.local

import com.dung.ddmoney.local.entity.CategoryEntity
import com.dung.ddmoney.local.entity.TransactionEntity
import com.dung.ddmoney.local.entity.WalletEntity
import com.dung.ddmoney.local.entity.BudgetEntity
import com.dung.ddmoney.local.entity.BudgetCategoryEntity
import com.dung.ddmoney.network.dto.BudgetResponse
import com.dung.ddmoney.network.dto.CategoryResponse
import com.dung.ddmoney.network.dto.TransactionResponse
import com.dung.ddmoney.network.dto.WalletResponse
import com.dung.ddmoney.parseColor
import com.dung.ddmoney.ui.dashboard.model.Category
import com.dung.ddmoney.ui.dashboard.model.CategoryType
import com.dung.ddmoney.ui.dashboard.model.Transaction
import com.dung.ddmoney.ui.dashboard.model.TransactionType
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.dashboard.model.WalletType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// ─── WalletResponse → WalletEntity ───────────────────────────────────────
fun WalletResponse.toEntity(): WalletEntity = WalletEntity(
    id = id.toString(),
    serverId = id,
    userId = userId,
    name = name,
    balance = balance,
    type = type,
    bankName = bankName,
    cardNumber = cardNumber,
    icon = icon.orEmpty(),
    currency = currency ?: "VND",
    isDefault = isDefault ?: false,
    isActive = isActive ?: true,
    isArchived = isArchived ?: false,
    isIncludedInTotal = isIncludedInTotal ?: true,
    sortOrder = sortOrder ?: 0,
    creditLimit = creditLimit,
    currentDebt = currentDebt,
    billingDay = billingDay,
    paymentDueDay = paymentDueDay,
    targetAmount = targetAmount,
    targetDate = targetDate,
    syncStatus = SyncStatus.SYNCED
)

// ─── WalletEntity → Domain Wallet ────────────────────────────────────────
fun WalletEntity.toModel(): Wallet = Wallet(
    id = serverId?.toString() ?: id,
    name = name,
    balance = balance,
    type = WalletType.fromString(type),
    bank = bankName ?: "",
    cardNumber = cardNumber ?: "",
    icon = icon,
    currency = currency,
    isDefault = isDefault,
    isArchived = isArchived,
    isIncludedInTotal = isIncludedInTotal,
    sortOrder = sortOrder,
    creditLimit = creditLimit,
    currentDebt = currentDebt,
    billingDay = billingDay,
    paymentDueDay = paymentDueDay,
    targetAmount = targetAmount,
    targetDate = targetDate?.let {
        runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
    }
)

// ─── CategoryResponse → CategoryEntity ───────────────────────────────────
fun CategoryResponse.toEntity(): CategoryEntity = CategoryEntity(
    id = id.toString(),
    serverId = id,
    userId = userId,
    name = name,
    icon = icon,
    colorHex = colorHex ?: "#4659A6",
    type = type,
    isDefault = isDefault ?: false,
    isEditable = isEditable ?: !(isDefault ?: false),
    isDeletable = isDeletable ?: !(isDefault ?: false),
    isDeleted = isDeleted ?: false,
    parentId = parentId,
    sortOrder = sortOrder ?: 10_000,
    syncStatus = SyncStatus.SYNCED
)

// ─── CategoryEntity → Domain Category ────────────────────────────────────
fun CategoryEntity.toModel(): Category = Category(
    id = serverId?.toString() ?: id,
    name = name,
    icon = icon,
    color = parseColor(colorHex),
    type = runCatching { CategoryType.valueOf(type) }.getOrDefault(CategoryType.EXPENSE),
    isDefault = isDefault,
    isEditable = isEditable,
    isDeletable = isDeletable,
    isDeleted = isDeleted,
    parentId = parentId?.toString(),
    sortOrder = sortOrder,
    userId = userId
)

// ─── TransactionResponse → TransactionEntity ─────────────────────────────
fun TransactionResponse.toEntity(): TransactionEntity = TransactionEntity(
    id = id.toString(),
    serverId = id,
    title = title,
    amount = amount,
    type = type,
    date = date,
    walletId = walletId,
    walletName = walletName,
    categoryId = categoryId,
    categoryName = categoryName,
    categoryIcon = categoryIcon,
    categoryColor = categoryColor,
    transferToWalletId = transferToWalletId,
    transferToWalletName = transferToWalletName,
    note = note,
    syncStatus = SyncStatus.SYNCED
)

// ─── TransactionEntity → Domain Transaction ───────────────────────────────
fun TransactionEntity.toModel(): Transaction = Transaction(
    id = serverId?.toString() ?: id,
    title = title ?: "",
    categoryId = categoryId.toString(),
    categoryName = categoryName ?: "",
    categoryIcon = categoryIcon ?: "📦",
    categoryColor = parseColor(categoryColor),
    amount = amount,
    type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE),
    walletId = walletId.toString(),
    walletName = walletName ?: "",
    date = runCatching {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrDefault(LocalDate.now()),
    note = note ?: ""
)

// ─── BudgetResponse → BudgetEntity ───────────────────────────────────────
fun BudgetResponse.toEntity(
    localId: String? = null,
    userId: Long = 0L
): BudgetEntity {
    val resolvedId = localId ?: id.toString()
    return BudgetEntity(
        id = resolvedId,
        serverId = id,
        userId = userId,
        name = name,
        amount = amount,
        month = month,
        year = year,
        periodType = periodType ?: "MONTH",
        startDate = startDate,
        endDate = endDate,
        walletId = walletId,
        syncStatus = SyncStatus.SYNCED,
        updatedAt = System.currentTimeMillis()
    )
}

/** Trích xuất danh sách BudgetCategoryEntity từ BudgetResponse */
fun BudgetResponse.extractCategories(budgetId: String): List<BudgetCategoryEntity> {
    val fromCategoryIds = categoryIds?.map { BudgetCategoryEntity(budgetId, it) }
    if (!fromCategoryIds.isNullOrEmpty()) return fromCategoryIds
    return categories.map { BudgetCategoryEntity(budgetId, it.id) }
}
