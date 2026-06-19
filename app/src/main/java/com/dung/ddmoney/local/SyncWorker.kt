package com.dung.ddmoney.local

import android.content.Context
import androidx.work.*
import com.dung.ddmoney.local.entity.BudgetCategoryEntity
import com.dung.ddmoney.local.entity.BudgetEntity
import com.dung.ddmoney.local.extractCategories
import com.dung.ddmoney.local.toEntity
import com.dung.ddmoney.network.RetrofitClient
import com.dung.ddmoney.network.dto.BudgetRequest
import com.dung.ddmoney.network.dto.TransactionRequest
import com.dung.ddmoney.network.dto.WalletRequest
import java.util.concurrent.TimeUnit

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val api = RetrofitClient.instance

        // ── Block 1: Wallet sync ──────────────────────────────────────────
        val pendingWallets = db.walletDao().getPendingWallets()
        for (wallet in pendingWallets) {
            try {
                when (wallet.syncStatus) {
                    SyncStatus.PENDING_INSERT -> {
                        val req = WalletRequest(
                            name = wallet.name,
                            balance = wallet.balance,
                            type = wallet.type,
                            bankName = wallet.bankName,
                            cardNumber = wallet.cardNumber,
                            icon = wallet.icon,
                            currency = wallet.currency,
                            isDefault = wallet.isDefault,
                            isIncludedInTotal = wallet.isIncludedInTotal,
                            sortOrder = wallet.sortOrder,
                            creditLimit = wallet.creditLimit,
                            currentDebt = wallet.currentDebt,
                            billingDay = wallet.billingDay,
                            paymentDueDay = wallet.paymentDueDay,
                            targetAmount = wallet.targetAmount,
                            targetDate = wallet.targetDate
                        )
                        val response = api.createWallet(req)
                        // Xóa row UUID local, upsert row với serverId thật từ server
                        db.walletDao().deleteByLocalId(wallet.id)
                        db.walletDao().upsert(response.toEntity())
                        // Nếu ví này là default, đảm bảo default được đặt đúng
                        if (wallet.isDefault) {
                            db.walletDao().clearAllDefaults()
                            db.walletDao().setDefault(response.id.toString())
                        }
                    }
                    SyncStatus.PENDING_UPDATE -> {
                        val serverId = wallet.serverId ?: continue
                        val req = WalletRequest(
                            name = wallet.name,
                            balance = wallet.balance,
                            type = wallet.type,
                            bankName = wallet.bankName,
                            cardNumber = wallet.cardNumber,
                            icon = wallet.icon,
                            currency = wallet.currency,
                            isDefault = wallet.isDefault,
                            isIncludedInTotal = wallet.isIncludedInTotal,
                            sortOrder = wallet.sortOrder,
                            creditLimit = wallet.creditLimit,
                            currentDebt = wallet.currentDebt,
                            billingDay = wallet.billingDay,
                            paymentDueDay = wallet.paymentDueDay,
                            targetAmount = wallet.targetAmount,
                            targetDate = wallet.targetDate
                        )
                        val response = api.updateWallet(serverId, req)
                        db.walletDao().upsert(response.toEntity())
                    }
                    SyncStatus.PENDING_DELETE -> {
                        val serverId = wallet.serverId ?: continue
                        api.deleteWallet(serverId)
                        db.walletDao().deleteByServerId(serverId)
                    }
                    SyncStatus.SYNCED -> { /* bỏ qua */ }
                }
            } catch (e: Exception) {
                return Result.retry()
                return Result.retry()
            }
        }

        // ── Block 2: Transaction sync (giữ nguyên) ────────────────────────
        val pendingTx = db.transactionDao().getPendingTransactions()
        var hasSyncedTransactions = false

        for (tx in pendingTx) {
            try {
                val req = TransactionRequest(
                    title = tx.title,
                    amount = tx.amount,
                    type = tx.type,
                    date = tx.date,
                    walletId = tx.walletId,
                    categoryId = tx.categoryId,
                    transferToWalletId = tx.transferToWalletId,
                    note = tx.note
                )

                if (tx.syncStatus == SyncStatus.PENDING_INSERT) {
                    val response = api.createTransaction(req)
                    db.transactionDao().deleteByLocalId(tx.id)
                    db.transactionDao().upsert(response.toEntity())
                    hasSyncedTransactions = true
                }
               
            } catch (e: Exception) {
                return Result.retry()
            }
        }

        if (hasSyncedTransactions) {
            val remoteWallets = api.getWallets()
            db.walletDao().upsertAll(remoteWallets.map { it.toEntity() })
        }

        // ── Block 3: Budget sync ──────────────────────────────────────────
        val pendingBudgets = db.budgetDao().getPendingBudgets()
        for (budget in pendingBudgets) {
            try {
                when (budget.syncStatus) {
                    SyncStatus.PENDING_INSERT -> {
                        val catIds = db.budgetDao().getAllBudgets()
                            .find { it.id == budget.id }
                            ?.let { /* categories already stored */ Unit }
                        // Lấy categoryIds từ budget_categories
                        val categoryIds = getBudgetCategoryIds(db, budget.id)
                        val req = budget.toBudgetRequest(categoryIds)
                        val response = api.createBudget(req)
                        // Ghi đè local với ID từ server
                        val serverEntity = BudgetEntity(
                            id = response.id.toString(),
                            serverId = response.id,
                            userId = budget.userId,
                            name = response.name,
                            amount = response.amount,
                            month = response.month,
                            year = response.year,
                            periodType = response.periodType ?: budget.periodType,
                            startDate = response.startDate ?: budget.startDate,
                            endDate = response.endDate ?: budget.endDate,
                            walletId = response.walletId ?: budget.walletId,
                            syncStatus = SyncStatus.SYNCED
                        )
                        // Xóa local UUID row và insert server-ID row
                        db.budgetDao().deleteBudgetCategories(budget.id)
                        db.budgetDao().deleteBudget(budget.id)
                        db.budgetDao().upsertBudget(serverEntity)
                        val newCategories = response.extractCategories(serverEntity.id)
                        if (newCategories.isEmpty()) {
                            // Fallback: dùng categories đã lưu local
                            val localCatIds = categoryIds.map {
                                BudgetCategoryEntity(serverEntity.id, it)
                            }
                            db.budgetDao().insertBudgetCategories(localCatIds)
                        } else {
                            db.budgetDao().insertBudgetCategories(newCategories)
                        }
                    }
                    SyncStatus.PENDING_UPDATE -> {
                        val serverId = budget.serverId ?: continue
                        val categoryIds = getBudgetCategoryIds(db, budget.id)
                        val req = budget.toBudgetRequest(categoryIds)
                        val response = api.updateBudget(serverId, req)
                        val updatedEntity = budget.copy(
                            name = response.name,
                            amount = response.amount,
                            syncStatus = SyncStatus.SYNCED
                        )
                        db.budgetDao().upsertBudget(updatedEntity)
                        db.budgetDao().deleteBudgetCategories(budget.id)
                        val newCategories = response.extractCategories(budget.id)
                        if (newCategories.isEmpty()) {
                            db.budgetDao().insertBudgetCategories(
                                categoryIds.map { BudgetCategoryEntity(budget.id, it) }
                            )
                        } else {
                            db.budgetDao().insertBudgetCategories(newCategories)
                        }
                    }
                    SyncStatus.PENDING_DELETE -> {
                        val serverId = budget.serverId
                        if (serverId == null) {
                            // Chưa lên server, xóa thẳng local
                            db.budgetDao().deleteBudgetCategories(budget.id)
                            db.budgetDao().deleteBudget(budget.id)
                        } else {
                            api.deleteBudget(serverId)
                            db.budgetDao().deleteBudgetCategories(budget.id)
                            db.budgetDao().deleteBudget(budget.id)
                        }
                    }
                    SyncStatus.SYNCED -> { /* bỏ qua */ }
                }
            } catch (e: Exception) {
                return Result.retry()
            }
        }

        return Result.success()
    }

    /** Lấy danh sách categoryId của một budget từ bảng budget_categories */
    private suspend fun getBudgetCategoryIds(db: AppDatabase, budgetId: String): List<Long> {
        // Lấy từ BudgetWithCategories flow → one-shot qua getAllBudgets + query thủ công
        // Đây là cách đơn giản nhất không cần thêm query mới
        return db.budgetDao().getBudgetCategoryIds(budgetId)
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "DDMoneySync",
                ExistingWorkPolicy.REPLACE, 
                syncRequest
            )
        }
    }
}

private fun BudgetEntity.toBudgetRequest(categoryIds: List<Long>): BudgetRequest = BudgetRequest(
    name = name,
    categoryIds = categoryIds,
    amount = amount,
    month = month,
    year = year,
    periodType = periodType,
    startDate = startDate ?: "",
    endDate = endDate ?: "",
    walletId = walletId,
    walletScope = if (walletId == null) "ALL_WALLETS" else "ONE_WALLET"
)
