package com.dung.ddmoney.ui.wallets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.dung.ddmoney.R
import com.dung.ddmoney.ui.dashboard.model.WalletType
import com.dung.ddmoney.ui.theme.InvestAmber50
import com.dung.ddmoney.ui.theme.InvestAmber600
import com.dung.ddmoney.ui.theme.OceanBlue50
import com.dung.ddmoney.ui.theme.OceanBlue600
import com.dung.ddmoney.ui.theme.SavingsTeal50
import com.dung.ddmoney.ui.theme.SavingsTeal600

object WalletIconMap {
    const val DEFAULT_KEY = ""

    data class Option(
        val key: String,
        val label: String,
        val preferredType: WalletType? = null
    )

    private val defaultOptions = listOf(
        Option("money_icon", "Tiền mặt", WalletType.CASH),
        Option("wallet_icon1", "Ví"),
        Option("card", "Thẻ", WalletType.CREDIT_CARD),
        Option("target", "Mục tiêu", WalletType.SAVINGS),
        Option("portfolio", "Đầu tư", WalletType.INVESTMENT),
        Option("calendar", "Lịch"),
        Option("clock", "Thời gian"),
        Option("note", "Ghi chú"),
        Option("papper", "Tài liệu"),
        Option("planet", "Hành tinh"),
        Option("referal", "Liên kết"),
        Option("chain", "Chuỗi"),
        Option("bars", "Thanh toán"),
        Option("bars_2", "Biểu đồ"),
        Option("calcu", "Máy tính"),
        Option("convert", "Chuyển đổi"),
        Option("diagram", "Sơ đồ"),
        Option("nate_2", "Danh sách")
    )

    fun optionsFor(type: WalletType): List<Option> {
        val preferred = defaultOptions.filter { it.preferredType == type }
        val rest = defaultOptions.filter { it.preferredType != type }
        return preferred + rest
    }

    fun hasSelectedIcon(key: String): Boolean = key.trim().isNotEmpty()

    @DrawableRes
    fun drawableResFor(key: String?, walletType: WalletType? = null): Int =
        when (key.normalizedIconKey()) {
            "money_icon", "cash", "money", "payments", "coin", "income" -> R.drawable.money_icon
            "card", "credit", "credit_card" -> R.drawable.card
            "target", "savings", "saving", "goal" -> R.drawable.target
            "portfolio", "investment", "invest" -> R.drawable.portfolio
            "calendar", "travel_fund", "flight", "luggage" -> R.drawable.calendar
            "clock" -> R.drawable.clock
            "note", "expense", "bill", "restaurant", "food_fun", "coffee" -> R.drawable.note
            "papper", "paper", "education_fund", "school" -> R.drawable.papper
            "planet", "travel_fun", "rocket", "sunny" -> R.drawable.planet
            "referal", "referral", "gift", "party", "celebration", "heart" -> R.drawable.referal
            "chain", "business", "store_fund", "home", "home_fund" -> R.drawable.chain
            "bars", "cart", "shopping_cart", "shopping_fun" -> R.drawable.bars
            "bars_2", "trophy" -> R.drawable.bars_2
            "calcu", "more" -> R.drawable.calcu
            "convert", "transfer" -> R.drawable.convert
            "diagram", "sparkle", "star" -> R.drawable.diagram
            "nate_2", "list", "game", "music" -> R.drawable.nate_2
            "wallet_icon1", "wallet", "wallet_main", "bank", "account_balance", "ewallet",
            "phone", "phone_android", "momo", "zalopay" -> R.drawable.wallet_icon1
            else -> fallbackDrawableFor(walletType)
        }

    @Composable
    fun WalletIcon(
        key: String?,
        walletType: WalletType?,
        contentDescription: String?,
        modifier: Modifier = Modifier
    ) {
        Image(
            painter = painterResource(drawableResFor(key, walletType)),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }

    fun toVector(key: String?, walletType: WalletType? = null): ImageVector =
        when (key?.trim()?.lowercase()) {
            "investment", "invest", "portfolio" -> Icons.AutoMirrored.Outlined.TrendingUp
            "savings", "saving", "target" -> Icons.Outlined.Savings
            "card", "credit", "credit_card" -> Icons.Outlined.CreditCard
            "bank", "account_balance" -> Icons.Outlined.AccountBalance
            "ewallet", "phone", "phone_android", "momo", "zalopay" -> Icons.Outlined.PhoneAndroid
            "cash", "money", "money_icon", "payments" -> Icons.Outlined.Payments
            "expense", "bill", "note" -> Icons.AutoMirrored.Outlined.ReceiptLong
            "education_fund", "papper" -> Icons.AutoMirrored.Outlined.MenuBook
            else -> defaultFor(walletType)
        }

    fun toKey(icon: ImageVector): String =
        when (icon) {
            Icons.Outlined.Payments -> "money_icon"
            Icons.Outlined.AccountBalance -> "wallet_icon1"
            Icons.Outlined.PhoneAndroid -> "wallet_icon1"
            Icons.Outlined.CreditCard -> "card"
            Icons.Outlined.Savings -> "target"
            Icons.AutoMirrored.Outlined.TrendingUp -> "portfolio"
            Icons.Outlined.AccountBalanceWallet -> "wallet_icon1"
            else -> "wallet_icon1"
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
        WalletType.CASH -> "money_icon"
        WalletType.BANK -> "wallet_icon1"
        WalletType.EWALLET -> "wallet_icon1"
        WalletType.CREDIT_CARD -> "card"
        WalletType.SAVINGS -> "target"
        WalletType.INVESTMENT -> "portfolio"
    }

    @DrawableRes
    private fun fallbackDrawableFor(type: WalletType?): Int = when (type) {
        WalletType.CASH -> R.drawable.money_icon
        WalletType.CREDIT_CARD -> R.drawable.card
        WalletType.SAVINGS -> R.drawable.target
        WalletType.INVESTMENT -> R.drawable.portfolio
        WalletType.BANK,
        WalletType.EWALLET,
        null -> R.drawable.wallet_icon1
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

private fun String?.normalizedIconKey(): String = this?.trim()?.lowercase().orEmpty()
