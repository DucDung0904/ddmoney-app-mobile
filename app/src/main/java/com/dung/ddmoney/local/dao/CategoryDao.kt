package com.dung.ddmoney.local.dao

import androidx.room.*
import com.dung.ddmoney.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE serverId = :serverId")
    suspend fun deleteByServerId(serverId: Long)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
