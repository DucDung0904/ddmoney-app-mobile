package com.dung.ddmoney.network.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetRequestTest {

    @Test
    fun `wallet scope follows selected wallet`() {
        val oneWallet =
            BudgetRequest(
                name = "Ăn uống",
                categoryIds = listOf(1L, 2L),
                amount = 1_000_000.0,
                month = 6,
                year = 2026,
                periodType = "MONTH",
                startDate = "2026-06-01",
                endDate = "2026-06-30",
                walletId = 9L,
                walletScope = "ONE_WALLET"
            )
        val allWallets =
            oneWallet.copy(
                walletId = null,
                walletScope = "ALL_WALLETS"
            )

        assertEquals("ONE_WALLET", oneWallet.walletScope)
        assertEquals("ALL_WALLETS", allWallets.walletScope)
        assertEquals(listOf(1L, 2L), oneWallet.categoryIds)
    }
}
