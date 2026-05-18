package com.dung.ddmoney.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dung.ddmoney.local.SyncStatus

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,         // UUID local
    val serverId: Long? = null,         // ID từ MySQL
    val userId: Long? = null,
    val name: String,
    val balance: Double,
    val type: String,                   // WalletType.name
    val bankName: String? = null,
    val cardNumber: String? = null,
    val colorHex: String = "#4659A6",
    val icon: String = "wallet",
    val currency: String = "VND",
    val isDefault: Boolean = false,
    val isActive: Boolean = true,       // backward compat
    val isArchived: Boolean = false,
    val isIncludedInTotal: Boolean = true,
    val sortOrder: Int = 0,
    // Credit card specific
    val creditLimit: Double? = null,
    val currentDebt: Double? = null,
    val billingDay: Int? = null,
    val paymentDueDay: Int? = null,
    // Savings wallet specific
    val targetAmount: Double? = null,
    val targetDate: String? = null,
    // Metadata
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
