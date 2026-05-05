package com.dung.ddmoney.repository

import com.dung.ddmoney.local.dao.WalletDao
import com.dung.ddmoney.local.toEntity
import com.dung.ddmoney.local.toModel
import com.dung.ddmoney.network.ApiService
import com.dung.ddmoney.network.dto.TransferRequest
import com.dung.ddmoney.network.dto.WalletRequest
import com.dung.ddmoney.network.dto.WalletResponse
import com.dung.ddmoney.ui.dashboard.model.Wallet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WalletRepository(
    private val api: ApiService,
    private val dao: WalletDao
) {

    /** Stream liên tục từ Room — UI tự update khi data thay đổi */
    fun observeAll(): Flow<List<Wallet>> =
        dao.observeAll().map { entities -> entities.map { it.toModel() } }

    /** Pull toàn bộ dữ liệu từ API → ghi đè Room */
    suspend fun sync(): Result<Unit> = safeCall {
        val remote = api.getWallets()
        dao.deleteAll()
        dao.upsertAll(remote.map { it.toEntity() })
    }

    suspend fun create(req: WalletRequest): Result<WalletResponse> = safeCall {
        val response = api.createWallet(req)
        dao.upsert(response.toEntity())
        response
    }

    suspend fun update(id: Long, req: WalletRequest): Result<WalletResponse> = safeCall {
        val response = api.updateWallet(id, req)
        dao.upsert(response.toEntity())
        response
    }

    suspend fun delete(id: Long): Result<Unit> = safeCall {
        api.deleteWallet(id)
        dao.deleteByServerId(id)
    }

    suspend fun transfer(fromId: Long, toId: Long, amount: Double, note: String? = null): Result<Unit> =
        safeCall {
            api.transfer(TransferRequest(fromId, toId, amount, note))
            // Sync lại cả 2 ví sau transfer
            val remote = api.getWallets()
            dao.upsertAll(remote.map { it.toEntity() })
        }
}
