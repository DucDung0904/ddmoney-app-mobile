package com.dung.ddmoney.repository

import com.dung.ddmoney.local.dao.CategoryDao
import com.dung.ddmoney.local.toEntity
import com.dung.ddmoney.local.toModel
import com.dung.ddmoney.network.ApiService
import com.dung.ddmoney.network.dto.CategoryRequest
import com.dung.ddmoney.network.dto.CategoryResponse
import com.dung.ddmoney.ui.dashboard.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(
    private val api: ApiService,
    private val dao: CategoryDao
) {

    fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { entities -> entities.map { it.toModel() } }

    suspend fun sync(): Result<Unit> = safeCall {
        val remote = api.getCategories()
        dao.deleteAll()
        dao.upsertAll(remote.map { it.toEntity() })
    }

    suspend fun create(req: CategoryRequest): Result<CategoryResponse> = safeCall {
        val response = api.createCategory(req)
        dao.upsert(response.toEntity())
        response
    }

    suspend fun update(id: Long, req: CategoryRequest): Result<CategoryResponse> = safeCall {
        val response = api.updateCategory(id, req)
        dao.upsert(response.toEntity())
        response
    }

    suspend fun delete(id: Long): Result<Unit> = safeCall {
        api.deleteCategory(id)
        dao.deleteByServerId(id)
    }
}
