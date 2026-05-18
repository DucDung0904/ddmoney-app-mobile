package com.dung.ddmoney.local.dao

import androidx.room.*
import com.dung.ddmoney.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    // ─── Observe streams (UI reactive) ───────────────────────────────────

    /** All wallets — for sync and management screens */
    @Query("SELECT * FROM wallets ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<WalletEntity>>

    /** Active (non-archived) wallets only — for quick wallet UI */
    @Query("SELECT * FROM wallets WHERE isArchived = 0 AND isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun observeActive(): Flow<List<WalletEntity>>

    // ─── Single reads ────────────────────────────────────────────────────

    @Query("SELECT * FROM wallets ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getById(id: String): WalletEntity?

    @Query("SELECT * FROM wallets WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): WalletEntity?

    // ─── Default wallet logic ────────────────────────────────────────────

    /** Get the current default wallet */
    @Query("SELECT * FROM wallets WHERE isDefault = 1 AND isArchived = 0 AND isActive = 1 LIMIT 1")
    suspend fun getDefault(): WalletEntity?

    /** Unset ALL defaults — called before setting a new one (single-default rule) */
    @Query("UPDATE wallets SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearAllDefaults()

    /** Mark a specific wallet as default by local id */
    @Query("UPDATE wallets SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultById(id: String)

    /** Atomic: clear all defaults then set one. Enforces single-default invariant. */
    @androidx.room.Transaction
    suspend fun setDefault(id: String) {
        clearAllDefaults()
        setDefaultById(id)
    }

    // ─── Archive / soft-delete ───────────────────────────────────────────

    /** Archive a wallet (soft-delete) */
    @Query("UPDATE wallets SET isArchived = 1, isActive = 0, isDefault = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun archive(id: String, timestamp: Long = System.currentTimeMillis())

    /** Unarchive (restore) a wallet */
    @Query("UPDATE wallets SET isArchived = 0, isActive = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun unarchive(id: String, timestamp: Long = System.currentTimeMillis())

    // ─── Upsert / write ──────────────────────────────────────────────────

    /** Upsert: insert hoặc replace nếu id đã tồn tại */
    @Upsert
    suspend fun upsertAll(wallets: List<WalletEntity>)

    @Upsert
    suspend fun upsert(wallet: WalletEntity)

    // ─── Delete ──────────────────────────────────────────────────────────

    @Query("DELETE FROM wallets WHERE serverId = :serverId")
    suspend fun deleteByServerId(serverId: Long)

    @Query("DELETE FROM wallets")
    suspend fun deleteAll()
}
