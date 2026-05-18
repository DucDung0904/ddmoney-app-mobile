package com.dung.ddmoney.ui.dashboard.model

import androidx.compose.ui.graphics.Color
import com.dung.ddmoney.ui.home.components.DefaultCategorySeed
import com.dung.ddmoney.ui.home.components.DefaultCategorySpec
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// ─── Enums ───────────────────────────────────────────────────────────
enum class TransactionType { INCOME, EXPENSE, TRANSFER, DEBT }
enum class WalletType {
    CASH,
    BANK,
    EWALLET,
    CREDIT_CARD,
    SAVINGS,
    INVESTMENT;

    val supportsExpense: Boolean get() = this != INVESTMENT
    val supportsIncome: Boolean get() = this != CREDIT_CARD
    val supportsTransfer: Boolean get() = this != INVESTMENT
    val isQuickAccessDefault: Boolean get() = this in setOf(CASH, BANK, EWALLET, CREDIT_CARD)
    val displayName: String get() = when (this) {
        CASH -> "Tiền mặt"
        BANK -> "Ngân hàng"
        EWALLET -> "Ví điện tử"
        CREDIT_CARD -> "Thẻ tín dụng"
        SAVINGS -> "Tiết kiệm"
        INVESTMENT -> "Đầu tư"
    }

    companion object {
        /** Parse with backward compat: "CREDIT" → CREDIT_CARD */
        fun fromString(value: String): WalletType = when (value.uppercase()) {
            "CREDIT" -> CREDIT_CARD
            else -> runCatching { valueOf(value.uppercase()) }.getOrDefault(CASH)
        }
    }
}
enum class CategoryType { INCOME, EXPENSE, DEBT, BOTH }

// ─── Transaction Model ────────────────────────────────────────────────
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: Color,
    val amount: Double,
    val type: TransactionType,
    val walletId: String,
    val walletName: String,
    val date: LocalDate = LocalDate.now(),
    val note: String = ""
) {
    val isIncome: Boolean get() = type == TransactionType.INCOME
    val displayDate: String
        get() {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            return when (date) {
                today -> "Hôm nay"
                yesterday -> "Hôm qua"
                else -> date.format(DateTimeFormatter.ofPattern("dd/MM"))
            }
        }
    val displayTime: String get() = ""
}

// ─── Category Model ───────────────────────────────────────────────────
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String,
    val color: Color,
    val type: CategoryType,
    val isDefault: Boolean = false,
    val isEditable: Boolean = !isDefault,
    val isDeletable: Boolean = !isDefault,
    val isDeleted: Boolean = false,
    val parentId: String? = null,
    val sortOrder: Int = 0,
    val userId: Long? = null,
    // For dashboard display only
    val totalAmount: Double = 0.0,
    val percentage: Float = 0f
)

// ─── Wallet Model ─────────────────────────────────────────────────────
data class Wallet(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val balance: Double,
    val type: WalletType,
    val bank: String = "",
    val cardNumber: String = "",
    val color: Color,
    val colorHex: String = "#4659A6",
    val icon: String = "wallet",
    val currency: String = "VND",
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val isIncludedInTotal: Boolean = true,
    val sortOrder: Int = 0,
    // Credit card specific
    val creditLimit: Double? = null,
    val currentDebt: Double? = null,
    val billingDay: Int? = null,
    val paymentDueDay: Int? = null,
    // Savings wallet specific
    val targetAmount: Double? = null,
    val targetDate: LocalDate? = null
) {
    /** Available balance: for credit cards = limit - debt, else normal balance */
    val availableBalance: Double get() = when (type) {
        WalletType.CREDIT_CARD -> (creditLimit ?: 0.0) - (currentDebt ?: 0.0)
        else -> balance
    }

    val savingsProgress: Float
        get() {
            val target = targetAmount ?: return 0f
            if (target <= 0.0) return 0f
            return (balance / target).toFloat().coerceIn(0f, 1f)
        }
}

// ─── Monthly Summary ──────────────────────────────────────────────────
data class MonthlySummary(
    val month: String,
    val income: Double,
    val expense: Double
) {
    val savings: Double get() = income - expense
}

// ─── Dashboard State ──────────────────────────────────────────────────
data class DashboardState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val selectedMonth: String = "Tháng 4, 2026",
    val wallets: List<Wallet> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val monthlyData: List<MonthlySummary> = SampleData.monthlyData
)

// ─── Default Category IDs (stable) ───────────────────────────────────
object CategoryIds {
    const val FOOD = "1001"
    const val FOOD_BREAKFAST = "1002"
    const val FOOD_CAFE = "1003"
    const val FOOD_RESTAURANT = "1004"
    const val TRANSPORT = "1005"
    const val TRANSPORT_GAS = "1006"
    const val TRANSPORT_GRAB = "1007"
    const val TRANSPORT_MAINTENANCE = "1008"
    const val SHOPPING = "1009"
    const val SHOPPING_PERSONAL = "1010"
    const val SHOPPING_HOME_GOODS = "1011"
    const val SHOPPING_CLOTHES = "1012"
    const val BILLS = "1013"
    const val BILL_ELECTRICITY = "1014"
    const val BILL_WATER = "1015"
    const val BILL_INTERNET = "1016"
    const val BILL_PHONE = "1017"
    const val HOUSING = "1018"
    const val HOUSING_RENT = "1019"
    const val HOUSING_REPAIR = "1020"
    const val ENTERTAINMENT = "1021"
    const val ENTERTAINMENT_MOVIE = "1022"
    const val ENTERTAINMENT_TRAVEL = "1023"
    const val ENTERTAINMENT_GAME = "1024"
    const val HEALTH = "1025"
    const val HEALTH_MEDICINE = "1026"
    const val HEALTH_DOCTOR = "1027"
    const val EDUCATION = "1028"
    const val EDUCATION_TUITION = "1029"
    const val EDUCATION_BOOKS = "1030"
    const val INCOME = "1031"
    const val SALARY = "1032"
    const val BONUS = "1033"
    const val FREELANCE = "1034"
    const val GIFT = "1035"
    const val INVESTMENT = "1036"

    // Transfer
    const val TRANSFER = "cat_transfer"
}

// ─── Default Wallet IDs ───────────────────────────────────────────────
object WalletIds {
    const val CASH = "wallet_cash"
    const val VIETCOMBANK = "wallet_vcb"
    const val MOMO = "wallet_momo"
}

// ─── Sample / Default Data ────────────────────────────────────────────
object SampleData {

    val defaultCategories = DefaultCategorySeed.items.map { it.toDashboardCategory() }
    val defaultExpenseCategories = defaultCategories.filter { it.type == CategoryType.EXPENSE }
    val defaultIncomeCategories = defaultCategories.filter { it.type == CategoryType.INCOME }
    val defaultDebtCategories = defaultCategories.filter { it.type == CategoryType.DEBT }

    val defaultWallets = listOf(
        Wallet(WalletIds.CASH, "Tiền mặt", 2_500_000.0, WalletType.CASH, icon = "wallet", color = LuminousSecondary, isDefault = true),
        Wallet(
            WalletIds.VIETCOMBANK,
            "Vietcombank",
            15_300_000.0,
            WalletType.BANK,
            "Vietcombank",
            "**** 4829",
            icon = "savings",
            color = SavingsTeal600
        ),
        Wallet(WalletIds.MOMO, "Ví MoMo", 280_000.0, WalletType.EWALLET, icon = "wallet", color = Color(0xFFD82D8B)),
    )

    val sampleTransactions = listOf(
        Transaction(
            id = "t1", title = "Bữa Sáng", categoryId = CategoryIds.FOOD_BREAKFAST,
            categoryName = "Ăn sáng", categoryIcon = "breakfast", categoryColor = ExpenseRed400,
            amount = 55_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.CASH, walletName = "Tiền mặt",
            date = LocalDate.now()
        ),
        Transaction(
            id = "t2", title = "Lương Tháng 4", categoryId = CategoryIds.SALARY,
            categoryName = "Lương", categoryIcon = "payments", categoryColor = IncomeGreen600,
            amount = 28_000_000.0, type = TransactionType.INCOME,
            walletId = WalletIds.VIETCOMBANK, walletName = "Vietcombank",
            date = LocalDate.now()
        ),
        Transaction(
            id = "t3", title = "Grab Bike", categoryId = CategoryIds.TRANSPORT_GRAB,
            categoryName = "Taxi / Grab", categoryIcon = "local_taxi", categoryColor = CategoryTransport,
            amount = 35_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.CASH, walletName = "Tiền mặt",
            date = LocalDate.now().minusDays(1)
        ),
        Transaction(
            id = "t4", title = "Shopee Order", categoryId = CategoryIds.SHOPPING_PERSONAL,
            categoryName = "Đồ dùng cá nhân", categoryIcon = "shopping_bag", categoryColor = CategoryShopping,
            amount = 320_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.VIETCOMBANK, walletName = "Vietcombank",
            date = LocalDate.now().minusDays(1)
        ),
        Transaction(
            id = "t5", title = "Freelance Design", categoryId = CategoryIds.FREELANCE,
            categoryName = "Freelance", categoryIcon = "work", categoryColor = LuminousSecondary,
            amount = 5_000_000.0, type = TransactionType.INCOME,
            walletId = WalletIds.VIETCOMBANK, walletName = "Vietcombank",
            date = LocalDate.now().minusDays(4)
        ),
        Transaction(
            id = "t6", title = "Phòng Gym", categoryId = CategoryIds.HEALTH_DOCTOR,
            categoryName = "Khám bệnh", categoryIcon = "local_hospital", categoryColor = CategoryHealth,
            amount = 300_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.VIETCOMBANK, walletName = "Vietcombank",
            date = LocalDate.now().minusDays(5)
        ),
        Transaction(
            id = "t7", title = "Netflix", categoryId = CategoryIds.ENTERTAINMENT_GAME,
            categoryName = "Game / App", categoryIcon = "sports_esports", categoryColor = CategoryEntertainment,
            amount = 199_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.MOMO, walletName = "Ví MoMo",
            date = LocalDate.now().minusDays(6)
        ),
    )

    // Sample categories with amounts for dashboard display
    val categories = listOf(
        Category(
            id = CategoryIds.FOOD,
            name = "Ăn uống",
            icon = "restaurant",
            color = CategoryFood,
            type = CategoryType.EXPENSE,
            isDefault = true,
            totalAmount = 2_150_000.0,
            percentage = 0.31f
        ),
        Category(
            id = CategoryIds.SHOPPING,
            name = "Mua sắm",
            icon = "shopping_bag",
            color = CategoryShopping,
            type = CategoryType.EXPENSE,
            isDefault = true,
            totalAmount = 1_850_000.0,
            percentage = 0.27f
        ),
        Category(
            id = CategoryIds.TRANSPORT,
            name = "Di chuyển",
            icon = "directions_car",
            color = CategoryTransport,
            type = CategoryType.EXPENSE,
            isDefault = true,
            totalAmount = 980_000.0,
            percentage = 0.14f
        ),
        Category(
            id = CategoryIds.ENTERTAINMENT,
            name = "Giải trí",
            icon = "sports_esports",
            color = CategoryEntertainment,
            type = CategoryType.EXPENSE,
            isDefault = true,
            totalAmount = 750_000.0,
            percentage = 0.11f
        ),
        Category(
            id = CategoryIds.HEALTH,
            name = "Sức khỏe",
            icon = "local_hospital",
            color = CategoryHealth,
            type = CategoryType.EXPENSE,
            isDefault = true,
            totalAmount = 620_000.0,
            percentage = 0.09f
        ),
        Category(
            id = CategoryIds.BILLS,
            name = "Hóa đơn",
            icon = "receipt_long",
            color = SavingsTeal600,
            type = CategoryType.EXPENSE,
            isDefault = true,
            totalAmount = 570_000.0,
            percentage = 0.08f
        ),
    )

    val monthlyData = listOf(
        MonthlySummary("T1", 32_000_000.0, 14_000_000.0),
        MonthlySummary("T2", 35_000_000.0, 16_500_000.0),
        MonthlySummary("T3", 38_000_000.0, 15_000_000.0),
        MonthlySummary("T4", 42_000_000.0, 17_420_000.0),
        MonthlySummary("T5", 40_000_000.0, 18_200_000.0),
        MonthlySummary("T6", 45_000_000.0, 19_000_000.0),
    )
}

private fun DefaultCategorySpec.toDashboardCategory(): Category =
    Category(
        id = id.toString(),
        name = name,
        icon = icon,
        color = parseCategoryColor(colorHex),
        type = runCatching { CategoryType.valueOf(type) }.getOrDefault(CategoryType.EXPENSE),
        isDefault = true,
        parentId = parentId?.toString(),
        sortOrder = sortOrder
    )

private fun parseCategoryColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(CategoryOther)
