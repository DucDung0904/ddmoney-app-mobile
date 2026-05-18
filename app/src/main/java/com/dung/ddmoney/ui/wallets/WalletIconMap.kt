package com.dung.ddmoney.ui.wallets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dung.ddmoney.ui.dashboard.model.WalletType
import com.dung.ddmoney.ui.theme.InvestAmber50
import com.dung.ddmoney.ui.theme.InvestAmber600
import com.dung.ddmoney.ui.theme.OceanBlue50
import com.dung.ddmoney.ui.theme.OceanBlue600
import com.dung.ddmoney.ui.theme.SavingsTeal50
import com.dung.ddmoney.ui.theme.SavingsTeal600

object WalletIconMap {
    const val DEFAULT_KEY = "wallet"

    fun toVector(key: String?, walletType: WalletType? = null): ImageVector {
        return when (key?.trim()?.lowercase()) {
            "savings" -> Icons.Outlined.Savings
            "saving" -> Icons.Outlined.Savings
            "cart" -> Icons.Outlined.ShoppingCart
            "car" -> Icons.Outlined.DirectionsCar
            "home" -> Icons.Outlined.Home
            "flight" -> Icons.Outlined.Flight
            "restaurant" -> Icons.Outlined.Restaurant
            "celebration" -> Icons.Outlined.Celebration
            "school" -> Icons.Outlined.School
            "more" -> Icons.Outlined.MoreHoriz
            "cash", "money", "payments" -> Icons.Outlined.Payments
            "bank", "account_balance" -> Icons.Outlined.AccountBalance
            "ewallet", "phone", "phone_android", "momo", "zalopay" -> Icons.Outlined.PhoneAndroid
            "credit", "credit_card", "card" -> Icons.Outlined.CreditCard
            "investment", "invest" -> Icons.AutoMirrored.Outlined.TrendingUp
            null, "", DEFAULT_KEY -> defaultFor(walletType)
            else -> defaultFor(walletType)
        }
    }

    fun toKey(icon: ImageVector): String {
        return when (icon) {
            Icons.Outlined.Savings -> "savings"
            Icons.Outlined.ShoppingCart -> "cart"
            Icons.Outlined.DirectionsCar -> "car"
            Icons.Outlined.Home -> "home"
            Icons.Outlined.Flight -> "flight"
            Icons.Outlined.Restaurant -> "restaurant"
            Icons.Outlined.Celebration -> "celebration"
            Icons.Outlined.School -> "school"
            Icons.Outlined.MoreHoriz -> "more"
            Icons.Outlined.Payments -> "cash"
            Icons.Outlined.AccountBalance -> "bank"
            Icons.Outlined.PhoneAndroid -> "ewallet"
            Icons.Outlined.CreditCard -> "credit_card"
            Icons.AutoMirrored.Outlined.TrendingUp -> "investment"
            Icons.Outlined.AccountBalanceWallet -> "wallet"
            else -> DEFAULT_KEY
        }
    }

    fun tintFor(type: WalletType): Color = when (type) {
        WalletType.CASH -> SavingsTeal600
        WalletType.BANK -> OceanBlue600
        WalletType.EWALLET -> Color(0xFFD82D8B)
        WalletType.CREDIT_CARD -> InvestAmber600
        WalletType.SAVINGS -> SavingsTeal600
        WalletType.INVESTMENT -> InvestAmber600
    }

    fun backgroundFor(type: WalletType): Color = when (type) {
        WalletType.CASH -> SavingsTeal50
        WalletType.BANK -> OceanBlue50
        WalletType.EWALLET -> Color(0xFFFCEAF4)
        WalletType.CREDIT_CARD -> InvestAmber50
        WalletType.SAVINGS -> SavingsTeal50
        WalletType.INVESTMENT -> InvestAmber50
    }

    fun defaultKeyFor(type: WalletType): String = when (type) {
        WalletType.CASH -> "cash"
        WalletType.BANK -> "bank"
        WalletType.EWALLET -> "ewallet"
        WalletType.CREDIT_CARD -> "credit_card"
        WalletType.SAVINGS -> "savings"
        WalletType.INVESTMENT -> "investment"
    }

    fun colorHexFor(type: WalletType): String = when (type) {
        WalletType.CASH -> "#1D9E75"
        WalletType.BANK -> "#185FA5"
        WalletType.EWALLET -> "#D82D8B"
        WalletType.CREDIT_CARD -> "#EF9F27"
        WalletType.SAVINGS -> "#1D9E75"
        WalletType.INVESTMENT -> "#EF9F27"
    }

    private fun defaultFor(type: WalletType?): ImageVector = when (type) {
        WalletType.CASH -> Icons.Outlined.Payments
        WalletType.BANK -> Icons.Outlined.AccountBalance
        WalletType.EWALLET -> Icons.Outlined.PhoneAndroid
        WalletType.CREDIT_CARD -> Icons.Outlined.CreditCard
        WalletType.SAVINGS -> Icons.Outlined.Savings
        WalletType.INVESTMENT -> Icons.AutoMirrored.Outlined.TrendingUp
        null -> Icons.Outlined.AccountBalanceWallet
    }
}
