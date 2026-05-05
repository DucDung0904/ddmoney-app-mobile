package com.dung.ddmoney.local

import android.content.Context
import androidx.work.*
import com.dung.ddmoney.network.RetrofitClient
import com.dung.ddmoney.network.dto.TransactionRequest
import java.util.concurrent.TimeUnit

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val api = RetrofitClient.instance
        
        val pendingTx = db.transactionDao().getPendingTransactions()

        for (tx in pendingTx) {
            try {
                // Tạo request từ entity
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
                    // Xóa row cũ theo id (UUID)
                    db.transactionDao().deleteByLocalId(tx.id)
                    // Insert row mới từ server với ID đồng nhất
                    db.transactionDao().upsert(response.toEntity())
                }
                // (Bạn có thể thêm xử lý cho UPDATE và DELETE ở đây tương tự)

            } catch (e: Exception) {
                // Nếu lỗi mạng, trả về retry để WorkManager thử lại sau
                return Result.retry()
            }
        }

        return Result.success()
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
                ExistingWorkPolicy.REPLACE, // Hoặc APPEND_OR_REPLACE tùy nhu cầu
                syncRequest
            )
        }
    }
}
