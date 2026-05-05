package com.dung.ddmoney.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dung.ddmoney.local.SyncStatus

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,         // UUID local
    val serverId: Long? = null,         // ID từ MySQL
    val name: String,
    val icon: String,
    val colorHex: String,
    val type: String,
    val isDefault: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis()
)
