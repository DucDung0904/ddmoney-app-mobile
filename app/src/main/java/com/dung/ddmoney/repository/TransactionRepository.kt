package com.dung.ddmoney.repository

import android.content.Context
import com.dung.ddmoney.local.SyncStatus
import com.dung.ddmoney.local.SyncWorker
import com.dung.ddmoney.local.dao.CategoryDao
import com.dung.ddmoney.local.dao.TransactionDao
import com.dung.ddmoney.local.dao.WalletDao
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
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val api: ApiService,
    private val dao: TransactionDao,
    private val walletDao: WalletDao,
    private val categoryDao: CategoryDao,
    private val context: Context
) {

    fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { entities -> entities.map { it.toModel() } }

    fun observeByMonth(month: Int, year: Int): Flow<List<Transaction>> =
        dao.observeByMonth(month, year).map { entities -> entities.map { it.toModel() } }

    suspend fun sync(month: Int? = null, year: Int? = null): Result<Unit> = safeCall {
        val pendingTransactions =
            if (month == null && year == null) {
                dao.getPendingTransactions()
            } else {
                emptyList()
            }
        val remote = api.getTransactions(month, year)
        if (month == null && year == null) {
            dao.deleteSynced()
        }
        dao.upsertAll(remote.map { it.toEntity() })
        if (pendingTransactions.isNotEmpty()) {
            dao.upsertAll(pendingTransactions)
        }
    }

    /** Tạo mới offline-first: ghi Room trước, sau đó WorkManager tự đẩy lên server khi có mạng. */
    suspend fun create(req: TransactionRequest): Result<Unit> =
        try {
            createPendingLocal(req) // Lưu xuống Local Room DB
            SyncWorker.enqueue(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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

    private suspend fun createPendingLocal(req: TransactionRequest) {
        val wallet = walletDao.getByServerId(req.walletId)
        val category = categoryDao.getByServerId(req.categoryId)
        val title = req.title?.takeIf { it.isNotBlank() } ?: category?.name
        val transferWalletId = req.transferToWalletId
        val transferToWalletName =
            if (transferWalletId != null) walletDao.getByServerId(transferWalletId)?.name else null

        dao.upsert(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                serverId = null,
                title = title,
                amount = req.amount,
                type = req.type,
                date = req.date,
                walletId = req.walletId,
                walletName = wallet?.name,
                categoryId = req.categoryId,
                categoryName = category?.name,
                categoryIcon = category?.icon,
                categoryColor = category?.colorHex,
                transferToWalletId = req.transferToWalletId,
                transferToWalletName = transferToWalletName,
                note = req.note,
                syncStatus = SyncStatus.PENDING_INSERT
            )
        )

        applyLocalWalletDelta(req)
    }

    private suspend fun applyLocalWalletDelta(req: TransactionRequest) {
        when (req.type.uppercase(Locale.ROOT)) {
            "INCOME" -> walletDao.adjustBalanceByServerId(req.walletId, req.amount)
            "EXPENSE" -> walletDao.adjustBalanceByServerId(req.walletId, -req.amount)
            "TRANSFER" -> {
                walletDao.adjustBalanceByServerId(req.walletId, -req.amount)
                req.transferToWalletId?.let { walletDao.adjustBalanceByServerId(it, req.amount) }
            }
        }
    }

}
