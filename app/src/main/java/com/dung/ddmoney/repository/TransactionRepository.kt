package com.dung.ddmoney.repository

import android.content.Context
import com.dung.ddmoney.local.SyncStatus
import com.dung.ddmoney.local.SyncWorker
import com.dung.ddmoney.local.dao.TransactionDao
import com.dung.ddmoney.local.entity.TransactionEntity
import com.dung.ddmoney.local.toEntity
import com.dung.ddmoney.local.toModel
import com.dung.ddmoney.network.ApiService
import com.dung.ddmoney.network.dto.CategorySpending
import com.dung.ddmoney.network.dto.MonthlyChart
import com.dung.ddmoney.network.dto.TransactionRequest
import com.dung.ddmoney.network.dto.TransactionResponse
import com.dung.ddmoney.network.dto.TransactionSummary
import com.dung.ddmoney.ui.dashboard.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TransactionRepository(
    private val api: ApiService,
    private val dao: TransactionDao,
    private val context: Context
) {

    fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { entities -> entities.map { it.toModel() } }

    fun observeByMonth(month: Int, year: Int): Flow<List<Transaction>> =
        dao.observeByMonth(month, year).map { entities -> entities.map { it.toModel() } }

    /** Sync từ Server về Local (Download) */
    suspend fun sync(month: Int? = null, year: Int? = null): Result<Unit> = safeCall {
        val remote = api.getTransactions(month, year)
        if (month == null && year == null) {
            dao.deleteAll()
        }
        dao.upsertAll(remote.map { it.toEntity() })
    }

    /** Tạo mới: Gửi lên Server trước, nhận về lưu Local sau (Đồng bộ ngay để tránh bị xóa khi syncAll) */
    suspend fun create(req: TransactionRequest): Result<Unit> = safeCall {
        // 1. Gửi lên server ngay lập tức
        val response = api.createTransaction(req)
        
        // 2. Lưu data chuẩn từ server về local DB
        dao.upsert(response.toEntity())
    }

    suspend fun getSummary(month: Int, year: Int): Result<TransactionSummary> = safeCall {
        api.getTransactionSummary(month, year)
    }

    suspend fun getMonthlyChart(months: Int = 4): Result<List<MonthlyChart>> = safeCall {
        api.getMonthlyChart(months)
    }

    suspend fun getCategorySpending(month: Int, year: Int): Result<List<CategorySpending>> = safeCall {
        api.getCategorySpending(month, year)
    }

    suspend fun update(id: Long, req: TransactionRequest): Result<TransactionResponse> = safeCall {
        val response = api.updateTransaction(id, req)
        dao.upsert(response.toEntity())
        response
    }

    suspend fun delete(localId: String): Result<Unit> = try {
        // Tạm thời xóa local, thực tế doanh nghiệp nên dùng PENDING_DELETE
        dao.deleteByLocalId(localId)
        SyncWorker.enqueue(context)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
