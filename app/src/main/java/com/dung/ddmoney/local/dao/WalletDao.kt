package com.dung.ddmoney.local.dao

import androidx.room.*
import com.dung.ddmoney.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    /** Phát stream liên tục — UI tự cập nhật khi DB thay đổi */
    @Query("SELECT * FROM wallets ORDER BY name ASC")
    fun observeAll(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets ORDER BY name ASC")
    suspend fun getAll(): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getById(id: String): WalletEntity?

    @Query("SELECT * FROM wallets WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): WalletEntity?

    /** Upsert: insert hoặc replace nếu id đã tồn tại */
    @Upsert
    suspend fun upsertAll(wallets: List<WalletEntity>)

    @Upsert
    suspend fun upsert(wallet: WalletEntity)

    @Query("DELETE FROM wallets WHERE serverId = :serverId")
    suspend fun deleteByServerId(serverId: Long)

    @Query("DELETE FROM wallets")
    suspend fun deleteAll()
}
