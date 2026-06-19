package com.dung.ddmoney.repository

import android.content.Context
import com.dung.ddmoney.local.SyncStatus
import com.dung.ddmoney.local.SyncWorker
import com.dung.ddmoney.local.dao.WalletDao
import com.dung.ddmoney.local.entity.WalletEntity
import com.dung.ddmoney.local.toEntity
import com.dung.ddmoney.local.toModel
import com.dung.ddmoney.network.ApiService
import com.dung.ddmoney.network.dto.TransferRequest
import com.dung.ddmoney.network.dto.WalletRequest
import com.dung.ddmoney.network.dto.WalletResponse
import com.dung.ddmoney.ui.dashboard.model.Wallet
import com.dung.ddmoney.ui.dashboard.model.WalletType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WalletRepository(
    private val api: ApiService,
    private val dao: WalletDao
) {

    // ─── Observe streams ─────────────────────────────────────────────────

    /** Stream all wallets — for management / "see all" screen */
    fun observeAll(): Flow<List<Wallet>> =
        dao.observeAll().map { entities -> entities.map { it.toModel() } }

    /** Stream active (non-archived) wallets — for home UI */
    fun observeActive(): Flow<List<Wallet>> =
        dao.observeActive().map { entities -> entities.map { it.toModel() } }

    // ─── Sync ────────────────────────────────────────────────────────────

    /** Pull toàn bộ dữ liệu từ API → ghi đè Room */
    suspend fun sync(): Result<Unit> = safeCall {
        val remote = api.getWallets()
        dao.deleteAll()
        dao.upsertAll(remote.map { it.toEntity() })
        // Auto-set first wallet as default if none is marked
        ensureDefaultWallet()
    }

    // ─── Offline-first CRUD ──────────────────────────────────────────────

    /**
     * Tạo ví theo offline-first:
     * 1. Lưu local với UUID + PENDING_INSERT ngay lập tức
     * 2. Trigger SyncWorker để đẩy lên server khi có mạng
     */
    suspend fun createOfflineFirst(context: Context, req: WalletRequest): Result<Unit> = safeCall {
        val isFirstWallet = dao.getAll().isEmpty()
        val guardedReq = req.normalizedForWalletType(isDefaultWallet = req.isDefault || isFirstWallet)
        val localId = UUID.randomUUID().toString()
        val entity = WalletEntity(
            id = localId,
            serverId = null,
            name = guardedReq.name,
            balance = guardedReq.balance,
            type = guardedReq.type,
            bankName = guardedReq.bankName,
            cardNumber = guardedReq.cardNumber,
            icon = guardedReq.icon,
            currency = guardedReq.currency,
            isDefault = guardedReq.isDefault,
            isActive = true,
            isArchived = false,
            isIncludedInTotal = guardedReq.isIncludedInTotal,
            sortOrder = guardedReq.sortOrder,
            creditLimit = guardedReq.creditLimit,
            currentDebt = guardedReq.currentDebt,
            billingDay = guardedReq.billingDay,
            paymentDueDay = guardedReq.paymentDueDay,
            targetAmount = guardedReq.targetAmount,
            targetDate = guardedReq.targetDate,
            syncStatus = SyncStatus.PENDING_INSERT
        )
        if (guardedReq.isDefault || isFirstWallet) {
            dao.clearAllDefaults()
        }
        dao.upsert(entity)
        if (guardedReq.isDefault || isFirstWallet) {
            dao.setDefault(localId)
        }
        SyncWorker.enqueue(context)
    }

    /**
     * Cập nhật ví theo offline-first:
     * 1. Cập nhật local với PENDING_UPDATE ngay (nếu đã có serverId) hoặc giữ PENDING_INSERT
     * 2. Trigger SyncWorker
     * walletId có thể là UUID (local) hoặc numeric string (serverId)
     */
    suspend fun updateOfflineFirst(context: Context, walletId: String, req: WalletRequest): Result<Unit> = safeCall {
        val current = dao.getById(walletId)
            ?: walletId.toLongOrNull()?.let { dao.getByServerId(it) }
            ?: throw IllegalArgumentException("Không tìm thấy ví: $walletId")
        val guardedReq = req.normalizedForWalletType(isDefaultWallet = current.isDefault || req.isDefault)
        val newStatus = when (current.syncStatus) {
            SyncStatus.PENDING_INSERT -> SyncStatus.PENDING_INSERT  // Chưa lên server → giữ INSERT
            else -> SyncStatus.PENDING_UPDATE
        }
        val updated = current.copy(
            name = guardedReq.name,
            balance = guardedReq.balance,
            type = guardedReq.type,
            bankName = guardedReq.bankName,
            cardNumber = guardedReq.cardNumber,
            icon = guardedReq.icon,
            currency = guardedReq.currency,
            isDefault = guardedReq.isDefault,
            isIncludedInTotal = guardedReq.isIncludedInTotal,
            sortOrder = guardedReq.sortOrder,
            creditLimit = guardedReq.creditLimit,
            currentDebt = guardedReq.currentDebt,
            billingDay = guardedReq.billingDay,
            paymentDueDay = guardedReq.paymentDueDay,
            targetAmount = guardedReq.targetAmount,
            targetDate = guardedReq.targetDate,
            syncStatus = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        if (guardedReq.isDefault) {
            dao.clearAllDefaults()
        }
        dao.upsert(updated)
        if (guardedReq.isDefault) {
            dao.setDefault(current.id)
        }
        ensureDefaultWallet()
        SyncWorker.enqueue(context)
    }

    // ─── CRUD ────────────────────────────────────────────────────────────

    suspend fun create(req: WalletRequest): Result<WalletResponse> = safeCall {
        val isFirstWallet = dao.getAll().isEmpty()
        val guardedReq = req.normalizedForWalletType(isDefaultWallet = req.isDefault || isFirstWallet)
        val response = api.createWallet(guardedReq)
        if (guardedReq.isDefault || response.isDefault == true) {
            dao.clearAllDefaults()
        }
        dao.upsert(response.toEntity())
        // If this is the user's first wallet, auto-set as default
        if (guardedReq.isDefault || isFirstWallet) {
            dao.setDefault(response.id.toString())
        }
        response
    }

    suspend fun update(id: Long, req: WalletRequest): Result<WalletResponse> = safeCall {
        val current = dao.getByServerId(id)
        val guardedReq = req.normalizedForWalletType(isDefaultWallet = current?.isDefault == true || req.isDefault)
        val response = api.updateWallet(id, guardedReq)
        if (guardedReq.isDefault || response.isDefault == true) {
            dao.clearAllDefaults()
        }
        dao.upsert(response.toEntity())
        ensureDefaultWallet()
        response
    }


    suspend fun transfer(fromId: Long, toId: Long, amount: Double, note: String? = null): Result<Unit> =
        safeCall {
            api.transfer(TransferRequest(fromId, toId, amount, note))
            // Sync lại cả 2 ví sau transfer
            val remote = api.getWallets()
            dao.upsertAll(remote.map { it.toEntity() })
        }

    suspend fun transfer(fromWalletId: String, toWalletId: String, amount: Double, note: String? = null): Result<Unit> {
        val fromServerId = fromWalletId.toLongOrNull()
            ?: return Result.failure(IllegalArgumentException("Ví nguồn chưa đồng bộ"))
        val toServerId = toWalletId.toLongOrNull()
            ?: return Result.failure(IllegalArgumentException("Ví đích chưa đồng bộ"))
        return transfer(fromServerId, toServerId, amount, note)
    }

    // ─── Default Wallet Logic ────────────────────────────────────────────

    /**
     * Set wallet as default. Enforces single-default rule:
     * clears all other defaults, then sets this one.
     */
    suspend fun setDefaultWallet(walletId: String): Result<Unit> = safeCall {
        dao.setDefault(walletId)
    }

    /**
     * If no wallet is currently marked as default, set the first active one.
     * Called after sync to ensure invariant.
     */
    private suspend fun ensureDefaultWallet() {
        val current = dao.getDefault()
        if (current == null) {
            val all = dao.getAll().filter { !it.isArchived }
            if (all.isNotEmpty()) {
                dao.setDefault(all.first().id)
            }
        }
    }

    // ─── Archive ─────────────────────────────────────────────────────────

    /** Archive wallet (soft-delete). Wallet stays in DB for history. */
    suspend fun archiveWallet(walletId: String): Result<Unit> = safeCall {
        val wallet = dao.getById(walletId)
        if (wallet?.isDefault == true) {
            throw IllegalStateException("Ví mặc định không thể lưu trữ")
        }

        val serverId = walletId.toLongOrNull()
        if (serverId != null) {
            api.deleteWallet(serverId)
        }
        dao.archive(walletId)
    }

    /** Restore an archived wallet */
    suspend fun unarchiveWallet(walletId: String): Result<Unit> = safeCall {
        val serverId = walletId.toLongOrNull()
        if (serverId != null) {
            val response = api.restoreWallet(serverId)
            dao.upsert(response.toEntity())
        } else {
            dao.unarchive(walletId)
        }
        ensureDefaultWallet()
    }

    // ─── Smart Wallet Selection (for Home quick section) ─────────────────

    /**
     * Select up to 3 wallets for the Home quick section:
     *   1. Default wallet (always first)
     *   2. Recently used wallets (if provided)
     *   3. Frequently used / highest balance active wallets
     *
     * Filters out:
     *   - Archived wallets
     *   - SAVINGS/INVESTMENT without recent activity (deprioritized)
     */
    fun selectQuickWallets(
        wallets: List<Wallet>,
        recentWalletIds: List<String> = emptyList()
    ): List<Wallet> {
        val active = wallets.filter { !it.isArchived }
        if (active.isEmpty()) return emptyList()

        val default = active.firstOrNull { it.isDefault } ?: active.first()
        val candidates = active.filter { it.id != default.id }

        val prioritized = candidates
            .sortedWith(
                compareByDescending<Wallet> { it.id in recentWalletIds }
                    .thenByDescending { it.type.isQuickAccessDefault }
                    .thenByDescending { it.balance }
            )
            .take(2)

        return listOf(default) + prioritized
    }

    private fun WalletRequest.normalizedForWalletType(isDefaultWallet: Boolean): WalletRequest {
        val walletType = WalletType.fromString(type)
        val base = copy(
            name = name.trim(),
            icon = icon.trim(),
            currency = currency.ifBlank { "VND" },
            isDefault = isDefault || isDefaultWallet,
            isIncludedInTotal = if (isDefault || isDefaultWallet) true else isIncludedInTotal
        )

        val normalized = when (walletType) {
            WalletType.CREDIT_CARD -> base.copy(
                balance = 0.0,
                type = WalletType.CREDIT_CARD.name,
                bankName = null,
                cardNumber = null,
                creditLimit = creditLimit ?: 0.0,
                currentDebt = currentDebt ?: 0.0,
                targetAmount = null,
                targetDate = null
            )

            WalletType.SAVINGS -> base.copy(
                type = WalletType.SAVINGS.name,
                bankName = null,
                cardNumber = null,
                creditLimit = null,
                currentDebt = null,
                billingDay = null,
                paymentDueDay = null
            )

            WalletType.CASH,
            WalletType.BANK,
            WalletType.EWALLET -> base.copy(
                type = walletType.name,
                bankName = null,
                cardNumber = null,
                creditLimit = null,
                currentDebt = null,
                billingDay = null,
                paymentDueDay = null,
                targetAmount = null,
                targetDate = null
            )

            WalletType.INVESTMENT -> base.copy(
                type = walletType.name,
                bankName = null,
                cardNumber = null,
                creditLimit = null,
                currentDebt = null,
                billingDay = null,
                paymentDueDay = null
            )
        }

        normalized.validateWalletBusinessRules()
        return normalized
    }

    private fun WalletRequest.validateWalletBusinessRules() {
        require(name.isNotBlank()) { "Tên ví không được rỗng" }
        require(icon.isNotBlank()) { "Vui lòng chọn biểu tượng ví" }

        val walletType = WalletType.fromString(type)
        when (walletType) {
            WalletType.CREDIT_CARD -> {
                val limit = creditLimit ?: 0.0
                val debt = currentDebt ?: 0.0
                require(limit >= 0.0) { "Hạn mức không được âm" }
                require(debt >= 0.0) { "Dư nợ không được âm" }
                require(limit >= debt) { "Hạn mức phải lớn hơn hoặc bằng dư nợ hiện tại" }
                require(billingDay?.let { it in 1..31 } == true) { "Ngày sao kê phải nằm trong 1..31" }
                require(paymentDueDay?.let { it in 1..31 } == true) { "Ngày thanh toán phải nằm trong 1..31" }
            }

            WalletType.SAVINGS -> {
                require(balance >= 0.0) { "Số dư không được âm" }
                targetAmount?.let {
                    require(it > balance) { "Mục tiêu tiết kiệm phải lớn hơn số dư hiện tại" }
                }
            }

            else -> {
                require(balance >= 0.0) { "Số dư không được âm" }
            }
        }
    }
}
