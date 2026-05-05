package com.dung.ddmoney.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dung.ddmoney.local.SyncStatus

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,         // UUID local
    val serverId: Long? = null,         // ID từ MySQL (null nếu chưa sync)
    val title: String?,
    val amount: Double,
    val type: String,
    val date: String,
    val walletId: Long,
    val walletName: String?,
    val categoryId: Long,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val transferToWalletId: Long?,
    val transferToWalletName: String?,
    val note: String?,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    val updatedAt: Long = System.currentTimeMillis()
)
