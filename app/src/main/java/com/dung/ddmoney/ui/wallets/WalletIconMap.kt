package com.dung.ddmoney.ui.wallets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

object WalletIconMap {
    const val DEFAULT_KEY = "wallet"

    fun toVector(key: String?): ImageVector {
        return when (key) {
            "savings" -> Icons.Outlined.Savings
            "cart" -> Icons.Outlined.ShoppingCart
            "car" -> Icons.Outlined.DirectionsCar
            "home" -> Icons.Outlined.Home
            "flight" -> Icons.Outlined.Flight
            "restaurant" -> Icons.Outlined.Restaurant
            "celebration" -> Icons.Outlined.Celebration
            "school" -> Icons.Outlined.School
            "more" -> Icons.Outlined.MoreHoriz
            "wallet" -> Icons.Outlined.AccountBalanceWallet
            else -> Icons.Outlined.AccountBalanceWallet
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
            Icons.Outlined.AccountBalanceWallet -> "wallet"
            else -> DEFAULT_KEY
        }
    }
}
