package com.dung.ddmoney.ui.dashboard.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalletModelTest {

    @Test
    fun walletType_supportsLegacyCreditValue() {
        assertEquals(WalletType.CREDIT_CARD, WalletType.fromString("credit"))
        assertEquals(WalletType.EWALLET, WalletType.fromString("ewallet"))
        assertEquals(WalletType.CASH, WalletType.fromString("unknown"))
    }

    @Test
    fun creditCardAvailableBalance_usesLimitMinusDebt() {
        val wallet =
            Wallet(
                name = "Thẻ",
                balance = 0.0,
                type = WalletType.CREDIT_CARD,
                creditLimit = 20_000_000.0,
                currentDebt = 3_500_000.0
            )

        assertEquals(16_500_000.0, wallet.availableBalance, 0.0)
    }

    @Test
    fun savingsProgress_isClampedToValidRange() {
        val overTarget =
            Wallet(
                name = "Tiết kiệm",
                balance = 12_000_000.0,
                type = WalletType.SAVINGS,
                targetAmount = 10_000_000.0
            )
        val noTarget =
            Wallet(
                name = "Tiết kiệm",
                balance = 1_000_000.0,
                type = WalletType.SAVINGS
            )

        assertEquals(1f, overTarget.savingsProgress, 0f)
        assertEquals(0f, noTarget.savingsProgress, 0f)
    }

    @Test
    fun walletCapabilities_matchBusinessRules() {
        assertFalse(WalletType.INVESTMENT.supportsExpense)
        assertFalse(WalletType.CREDIT_CARD.supportsIncome)
        assertTrue(WalletType.BANK.supportsTransfer)
    }
}
