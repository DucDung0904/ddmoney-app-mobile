package com.dung.ddmoney.local.dao

import androidx.room.*
import com.dung.ddmoney.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT * FROM categories
        WHERE isDeleted = 0
          AND (userId IS NULL OR userId = :currentUserId)
        ORDER BY sortOrder ASC, name ASC
        """
    )
    fun observeVisibleForUser(currentUserId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE isDeleted = 0 AND serverId IN (:serverIds)")
    suspend fun countByServerIds(serverIds: List<Long>): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :serverId")
    suspend fun countTransactionsByServerCategoryId(serverId: Long): Int

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE serverId = :serverId")
    suspend fun deleteByServerId(serverId: Long)

    @Query("UPDATE categories SET isDeleted = 1, updatedAt = :updatedAt WHERE serverId = :serverId")
    suspend fun softDeleteByServerId(serverId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query(
        """
        UPDATE categories
        SET isDeleted = 1, updatedAt = :updatedAt
        WHERE isDefault = 1
          AND serverId IN (:serverIds)
        """
    )
    suspend fun hideDefaultSeedsByServerIds(
        serverIds: List<Long>,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE categories
        SET isDeleted = 1, updatedAt = :updatedAt
        WHERE serverId IS NOT NULL
          AND serverId NOT IN (:activeServerIds)
        """
    )
    suspend fun hideCategoriesMissingOnServer(
        activeServerIds: List<Long>,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
        DELETE FROM categories
        WHERE isDefault = 1
          AND (serverId IS NULL OR serverId NOT IN (:activeDefaultServerIds))
          AND (serverId IS NULL OR serverId NOT IN (SELECT DISTINCT categoryId FROM transactions))
        """
    )
    suspend fun deleteUnusedLegacyDefaults(activeDefaultServerIds: List<Long>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
