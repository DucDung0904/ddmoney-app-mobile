package com.dung.ddmoney.repository

import com.dung.ddmoney.local.dao.WalletDao
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

    // ─── CRUD ────────────────────────────────────────────────────────────

    suspend fun create(req: WalletRequest): Result<WalletResponse> = safeCall {
        val response = api.createWallet(req)
        if (req.isDefault || response.isDefault == true) {
            dao.clearAllDefaults()
        }
        dao.upsert(response.toEntity())
        // If this is the user's first wallet, auto-set as default
        if (req.isDefault || dao.getAll().size == 1) {
            dao.setDefault(response.id.toString())
        }
        response
    }

    suspend fun update(id: Long, req: WalletRequest): Result<WalletResponse> = safeCall {
        val response = api.updateWallet(id, req)
        if (req.isDefault || response.isDefault == true) {
            dao.clearAllDefaults()
        }
        dao.upsert(response.toEntity())
        ensureDefaultWallet()
        response
    }

    /**
     * Safe delete: API deletes on server, locally we archive
     * to preserve transaction history references.
     */
    suspend fun delete(id: Long): Result<Unit> = safeCall {
        api.deleteWallet(id)
        val entity = dao.getByServerId(id)
        if (entity != null) {
            dao.archive(entity.id)
        }
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
        val serverId = walletId.toLongOrNull()
        if (serverId != null) {
            api.deleteWallet(serverId)
        }
        dao.archive(walletId)
        // If archived wallet was default, pick a new default
        val archived = dao.getById(walletId)
        if (archived?.isDefault == true) {
            dao.clearAllDefaults()
            val remaining = dao.getAll().filter { !it.isArchived }
            if (remaining.isNotEmpty()) {
                dao.setDefault(remaining.first().id)
            }
        }
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
}
