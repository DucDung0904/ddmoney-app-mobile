package com.dung.ddmoney.local.dao

import androidx.room.*
import com.dung.ddmoney.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("""
        SELECT * FROM transactions 
        WHERE strftime('%m', date) = printf('%02d', :month)
          AND strftime('%Y', date) = CAST(:year AS TEXT)
        ORDER BY date DESC, id DESC
    """)
    fun observeByMonth(month: Int, year: Int): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :fromDate AND :toDate
        ORDER BY date DESC, updatedAt DESC
        """
    )
    suspend fun getBetweenDates(fromDate: String, toDate: String): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :fromDate AND :toDate
          AND (:type IS NULL OR type = :type)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:walletId IS NULL OR walletId = :walletId)
        ORDER BY date DESC, updatedAt DESC
        """
    )
    suspend fun getForExpenseBook(
        fromDate: String,
        toDate: String,
        type: String?,
        categoryId: Long?,
        walletId: Long?
    ): List<TransactionEntity>

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingTransactions(): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE id = :localId")
    suspend fun deleteByLocalId(localId: String)

    @Query("DELETE FROM transactions WHERE serverId = :serverId")
    suspend fun deleteByServerId(serverId: Long)

    @Query("DELETE FROM transactions WHERE syncStatus = 'SYNCED'")
    suspend fun deleteSynced()

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
