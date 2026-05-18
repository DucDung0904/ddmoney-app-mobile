package com.dung.ddmoney.ui.wallets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
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

    data class Option(
        val key: String,
        val label: String,
        val preferredType: WalletType? = null
    )

    private val defaultOptions = listOf(
        Option("cash", "Tiền mặt", WalletType.CASH),
        Option("bank", "Ngân hàng", WalletType.BANK),
        Option("ewallet", "Ví số", WalletType.EWALLET),
        Option("credit_card", "Tín dụng", WalletType.CREDIT_CARD),
        Option("savings", "Tiết kiệm", WalletType.SAVINGS),
        Option("investment", "Đầu tư", WalletType.INVESTMENT),
        Option("sparkle", "Lấp lánh"),
        Option("star", "Ngôi sao"),
        Option("gift", "Quà tặng"),
        Option("party", "Ăn mừng"),
        Option("game", "Giải trí"),
        Option("coffee", "Cà phê"),
        Option("shopping_fun", "Mua sắm"),
        Option("rocket", "Bứt phá"),
        Option("sunny", "Ngày mới"),
        Option("heart", "Yêu thích"),
        Option("trophy", "Thành tựu"),
        Option("music", "Âm nhạc"),
        Option("travel_fun", "Du lịch"),
        Option("food_fun", "Ăn uống")
    )

    fun optionsFor(type: WalletType): List<Option> {
        val preferred = defaultOptions.filter { it.preferredType == type }
        val rest = defaultOptions.filter { it.preferredType != type }
        return preferred + rest
    }

    fun toVector(key: String?, walletType: WalletType? = null): ImageVector {
        return when (key?.trim()?.lowercase()) {
            "wallet_main" -> Icons.Outlined.AccountBalanceWallet
            "sparkle" -> Icons.Outlined.AutoAwesome
            "star" -> Icons.Outlined.StarBorder
            "gift" -> Icons.Outlined.CardGiftcard
            "party" -> Icons.Outlined.Celebration
            "game" -> Icons.Outlined.SportsEsports
            "coffee" -> Icons.Outlined.LocalCafe
            "shopping_fun" -> Icons.Outlined.ShoppingBag
            "rocket" -> Icons.Outlined.RocketLaunch
            "sunny" -> Icons.Outlined.WbSunny
            "heart" -> Icons.Outlined.FavoriteBorder
            "trophy" -> Icons.Outlined.EmojiEvents
            "music" -> Icons.Outlined.MusicNote
            "travel_fun" -> Icons.Outlined.Luggage
            "food_fun" -> Icons.Outlined.Restaurant
            "safe" -> Icons.Outlined.Lock
            "savings", "saving" -> Icons.Outlined.Savings
            "goal" -> Icons.Outlined.Flag
            "coin" -> Icons.Outlined.MonetizationOn
            "income" -> Icons.Outlined.Paid
            "expense" -> Icons.AutoMirrored.Outlined.ReceiptLong
            "bill" -> Icons.Outlined.RequestQuote
            "portfolio" -> Icons.Outlined.DonutSmall
            "investment", "invest" -> Icons.AutoMirrored.Outlined.TrendingUp
            "business" -> Icons.Outlined.BusinessCenter
            "travel_fund" -> Icons.Outlined.Luggage
            "education_fund" -> Icons.AutoMirrored.Outlined.MenuBook
            "home_fund" -> Icons.Outlined.Home
            "store_fund" -> Icons.Outlined.Storefront
            "mobile_pay" -> Icons.Outlined.Contactless
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
            "card" -> Icons.Outlined.Style
            "credit", "credit_card" -> Icons.Outlined.CreditCard
            null, "", DEFAULT_KEY -> defaultFor(walletType)
            else -> defaultFor(walletType)
        }
    }

    fun toKey(icon: ImageVector): String {
        return when (icon) {
            Icons.Outlined.Savings -> "savings"
            Icons.Outlined.AutoAwesome -> "sparkle"
            Icons.Outlined.StarBorder -> "star"
            Icons.Outlined.CardGiftcard -> "gift"
            Icons.Outlined.SportsEsports -> "game"
            Icons.Outlined.LocalCafe -> "coffee"
            Icons.Outlined.ShoppingBag -> "shopping_fun"
            Icons.Outlined.RocketLaunch -> "rocket"
            Icons.Outlined.WbSunny -> "sunny"
            Icons.Outlined.FavoriteBorder -> "heart"
            Icons.Outlined.EmojiEvents -> "trophy"
            Icons.Outlined.MusicNote -> "music"
            Icons.Outlined.Lock -> "safe"
            Icons.Outlined.Flag -> "goal"
            Icons.Outlined.ShoppingCart -> "cart"
            Icons.Outlined.DirectionsCar -> "car"
            Icons.Outlined.Home -> "home"
            Icons.Outlined.Flight -> "flight"
            Icons.Outlined.Restaurant -> "restaurant"
            Icons.Outlined.Celebration -> "celebration"
            Icons.Outlined.School -> "school"
            Icons.Outlined.MoreHoriz -> "more"
            Icons.Outlined.MonetizationOn -> "coin"
            Icons.Outlined.Paid -> "income"
            Icons.AutoMirrored.Outlined.ReceiptLong -> "expense"
            Icons.Outlined.RequestQuote -> "bill"
            Icons.Outlined.DonutSmall -> "portfolio"
            Icons.Outlined.BusinessCenter -> "business"
            Icons.Outlined.Luggage -> "travel_fund"
            Icons.AutoMirrored.Outlined.MenuBook -> "education_fund"
            Icons.Outlined.Storefront -> "store_fund"
            Icons.Outlined.Contactless -> "mobile_pay"
            Icons.Outlined.Payments -> "cash"
            Icons.Outlined.AccountBalance -> "bank"
            Icons.Outlined.PhoneAndroid -> "ewallet"
            Icons.Outlined.Style -> "card"
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
