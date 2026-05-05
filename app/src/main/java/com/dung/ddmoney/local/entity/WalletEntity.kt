package com.dung.ddmoney.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dung.ddmoney.local.SyncStatus

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,         // UUID local
    val serverId: Long? = null,         // ID từ MySQL
    val name: String,
    val balance: Double,
    val type: String,
    val bankName: String?,
    val cardNumber: String?,
    val colorHex: String,
    val isActive: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.SYNCED, // Thường wallet sẽ được tạo online hoặc sync từ đầu
    val updatedAt: Long = System.currentTimeMillis()
)
