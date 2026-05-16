package com.dung.ddmoney.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dung.ddmoney.local.SyncStatus

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,         // UUID local
    val serverId: Long? = null,         // ID từ MySQL
    val userId: Long? = null,
    val name: String,
    val icon: String,
    val colorHex: String,
    val type: String,
    val isDefault: Boolean = false,
    val isEditable: Boolean = !isDefault,
    val isDeletable: Boolean = !isDefault,
    val isDeleted: Boolean = false,
    val parentId: Long? = null,
    val sortOrder: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis()
)
