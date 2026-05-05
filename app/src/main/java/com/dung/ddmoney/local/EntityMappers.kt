package com.dung.ddmoney.local

import com.dung.ddmoney.local.entity.CategoryEntity
import com.dung.ddmoney.local.entity.TransactionEntity
import com.dung.ddmoney.local.entity.WalletEntity
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
    id = id.toString(), // Sử dụng serverId làm local id để tránh bị nhân bản khi update
    serverId = id,
    name = name,
    balance = balance,
    type = type,
    bankName = bankName,
    cardNumber = cardNumber,
    colorHex = colorHex ?: "#4659A6",
    isActive = isActive ?: true,
    syncStatus = SyncStatus.SYNCED
)

// ─── WalletEntity → Domain Wallet ────────────────────────────────────────
fun WalletEntity.toModel(): Wallet = Wallet(
    id = serverId?.toString() ?: id, // Ưu tiên serverId để hiển thị
    name = name,
    balance = balance,
    type = runCatching { WalletType.valueOf(type) }.getOrDefault(WalletType.CASH),
    bank = bankName ?: "",
    cardNumber = cardNumber ?: "",
    color = parseColor(colorHex)
)

// ─── CategoryResponse → CategoryEntity ───────────────────────────────────
fun CategoryResponse.toEntity(): CategoryEntity = CategoryEntity(
    id = id.toString(),
    serverId = id,
    name = name,
    icon = icon,
    colorHex = colorHex ?: "#4659A6",
    type = type,
    isDefault = isDefault ?: false,
    syncStatus = SyncStatus.SYNCED
)

// ─── CategoryEntity → Domain Category ────────────────────────────────────
fun CategoryEntity.toModel(): Category = Category(
    id = serverId?.toString() ?: id,
    name = name,
    icon = icon,
    color = parseColor(colorHex),
    type = runCatching { CategoryType.valueOf(type) }.getOrDefault(CategoryType.EXPENSE),
    isDefault = isDefault
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
