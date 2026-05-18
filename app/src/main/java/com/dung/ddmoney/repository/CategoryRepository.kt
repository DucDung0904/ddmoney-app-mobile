package com.dung.ddmoney.repository

import com.dung.ddmoney.ui.home.components.DefaultCategorySeed
import com.dung.ddmoney.ui.home.components.DefaultCategorySpec
import com.dung.ddmoney.local.dao.CategoryDao
import com.dung.ddmoney.local.SyncStatus
import com.dung.ddmoney.local.entity.CategoryEntity
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

    fun observeAll(currentUserId: Long): Flow<List<Category>> =
        dao.observeVisibleForUser(currentUserId)
            .map { entities -> entities.map { it.toModel() } }

    suspend fun sync(): Result<Unit> = safeCall {
        runCatching { api.getCategories() }
            .onSuccess { remote ->
                dao.upsertAll(remote.map { it.toEntity() })
                hideDefaultSeedsMissingOnServer(remote)
            }
            .onFailure {
                seedDefaultCategoriesIfNeeded()
            }

        dao.deleteUnusedLegacyDefaults(DefaultCategorySeed.ids)
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
        val localCategory = dao.getByServerId(id)
        if (localCategory?.isDeletable == false || localCategory?.isDefault == true) {
            throw IllegalStateException("Danh mục mặc định không thể xóa")
        }
        if (dao.countTransactionsByServerCategoryId(id) > 0) {
            dao.softDeleteByServerId(id)
            return@safeCall
        }
        api.deleteCategory(id)
        dao.softDeleteByServerId(id)
    }

    private suspend fun seedDefaultCategoriesIfNeeded() {
        val existingDefaultCount = dao.countByServerIds(DefaultCategorySeed.ids)
        if (existingDefaultCount < DefaultCategorySeed.items.size) {
            dao.upsertAll(DefaultCategorySeed.items.map { it.toEntity() })
        }
    }

    private suspend fun hideDefaultSeedsMissingOnServer(remote: List<CategoryResponse>) {
        val remoteIds = remote.map { it.id }.toSet()
        val missingServerIds = DefaultCategorySeed.ids.filterNot { it in remoteIds }
        if (missingServerIds.isNotEmpty()) {
            dao.hideDefaultSeedsByServerIds(missingServerIds)
        }
    }
}

private fun DefaultCategorySpec.toEntity(): CategoryEntity =
    CategoryEntity(
        id = id.toString(),
        serverId = id,
        userId = null,
        name = name,
        icon = icon,
        colorHex = colorHex,
        type = type,
        isDefault = true,
        isEditable = false,
        isDeletable = false,
        isDeleted = false,
        parentId = parentId,
        sortOrder = sortOrder,
        syncStatus = SyncStatus.SYNCED
    )
