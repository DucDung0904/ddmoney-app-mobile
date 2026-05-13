package com.dung.ddmoney.ui.dashboard.model

import androidx.compose.ui.graphics.Color
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// ─── Enums ───────────────────────────────────────────────────────────
enum class TransactionType { INCOME, EXPENSE, TRANSFER, DEBT }
enum class WalletType { CASH, BANK, EWALLET, CREDIT }
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
    val icon: String = "wallet"
)

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
    // Expense
    const val FOOD = "cat_food"
    const val TRANSPORT = "cat_transport"
    const val SHOPPING = "cat_shopping"
    const val HEALTH = "cat_health"
    const val ENTERTAINMENT = "cat_entertainment"
    const val EDUCATION = "cat_education"
    const val BILLS = "cat_bills"
    const val OTHER_EXPENSE = "cat_other_expense"

    // Income
    const val SALARY = "cat_salary"
    const val FREELANCE = "cat_freelance"
    const val INVESTMENT = "cat_investment"
    const val GIFT = "cat_gift"
    const val OTHER_INCOME = "cat_other_income"

    // Debt
    const val LEND  = "cat_lend"   // Cho vay
    const val BORROW = "cat_borrow" // Đi vay
    const val REPAY = "cat_repay"  // Trả nợ
    const val COLLECT = "cat_collect" // Thu nợ

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

    val defaultExpenseCategories = listOf(
        Category(CategoryIds.FOOD, "Ăn uống", "🍜", CategoryFood, CategoryType.EXPENSE, true),
        Category(
            CategoryIds.TRANSPORT,
            "Di chuyển",
            "🚗",
            CategoryTransport,
            CategoryType.EXPENSE,
            true
        ),
        Category(
            CategoryIds.SHOPPING,
            "Mua sắm",
            "🛍️",
            CategoryShopping,
            CategoryType.EXPENSE,
            true
        ),
        Category(CategoryIds.HEALTH, "Sức khỏe", "💊", CategoryHealth, CategoryType.EXPENSE, true),
        Category(
            CategoryIds.ENTERTAINMENT,
            "Giải trí",
            "🎮",
            CategoryEntertainment,
            CategoryType.EXPENSE,
            true
        ),
        Category(CategoryIds.EDUCATION, "Giáo dục", "📚", SavingsTeal600, CategoryType.EXPENSE, true),
        Category(CategoryIds.BILLS, "Hóa đơn", "💡", InvestAmber600, CategoryType.EXPENSE, true),
        Category(CategoryIds.OTHER_EXPENSE, "Khác", "📦", CategoryOther, CategoryType.EXPENSE, true),
    )

    val defaultIncomeCategories = listOf(
        Category(CategoryIds.SALARY, "Lương", "💰", IncomeGreen600, CategoryType.INCOME, true),
        Category(
            CategoryIds.FREELANCE,
            "Freelance",
            "🎨",
            LuminousSecondary,
            CategoryType.INCOME,
            true
        ),
        Category(
            CategoryIds.INVESTMENT,
            "Đầu tư",
            "📈",
            InvestAmber600,
            CategoryType.INCOME,
            true
        ),
        Category(CategoryIds.GIFT, "Quà tặng", "🎁", CategoryShopping, CategoryType.INCOME, true),
        Category(
            CategoryIds.OTHER_INCOME,
            "Thu khác",
            "✨",
            CategoryOther,
            CategoryType.INCOME,
            true
        ),
    )

    val defaultDebtCategories = listOf(
        Category(CategoryIds.LEND,    "Cho vay",  "🤝", Color(0xFF7C4DFF), CategoryType.DEBT, true),
        Category(CategoryIds.BORROW,  "Đi vay",   "🏦", Color(0xFFFF6D00), CategoryType.DEBT, true),
        Category(CategoryIds.REPAY,   "Trả nợ",   "💸", Color(0xFFD50000), CategoryType.DEBT, true),
        Category(CategoryIds.COLLECT, "Thu nợ",   "💹", Color(0xFF00897B), CategoryType.DEBT, true),
    )

    val defaultCategories = defaultExpenseCategories + defaultIncomeCategories + defaultDebtCategories

    val defaultWallets = listOf(
        Wallet(WalletIds.CASH, "Tiền mặt", 2_500_000.0, WalletType.CASH, icon = "wallet", color = LuminousSecondary),
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
            id = "t1", title = "Bữa Sáng", categoryId = CategoryIds.FOOD,
            categoryName = "Ăn uống", categoryIcon = "🍜", categoryColor = CategoryFood,
            amount = 55_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.CASH, walletName = "Tiền mặt",
            date = LocalDate.now()
        ),
        Transaction(
            id = "t2", title = "Lương Tháng 4", categoryId = CategoryIds.SALARY,
            categoryName = "Lương", categoryIcon = "💰", categoryColor = IncomeGreen600,
            amount = 28_000_000.0, type = TransactionType.INCOME,
            walletId = WalletIds.VIETCOMBANK, walletName = "Vietcombank",
            date = LocalDate.now()
        ),
        Transaction(
            id = "t3", title = "Grab Bike", categoryId = CategoryIds.TRANSPORT,
            categoryName = "Di chuyển", categoryIcon = "🚗", categoryColor = CategoryTransport,
            amount = 35_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.CASH, walletName = "Tiền mặt",
            date = LocalDate.now().minusDays(1)
        ),
        Transaction(
            id = "t4", title = "Shopee Order", categoryId = CategoryIds.SHOPPING,
            categoryName = "Mua sắm", categoryIcon = "🛍️", categoryColor = CategoryShopping,
            amount = 320_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.VIETCOMBANK, walletName = "Vietcombank",
            date = LocalDate.now().minusDays(1)
        ),
        Transaction(
            id = "t5", title = "Freelance Design", categoryId = CategoryIds.FREELANCE,
            categoryName = "Freelance", categoryIcon = "🎨", categoryColor = LuminousSecondary,
            amount = 5_000_000.0, type = TransactionType.INCOME,
            walletId = WalletIds.VIETCOMBANK, walletName = "Vietcombank",
            date = LocalDate.now().minusDays(4)
        ),
        Transaction(
            id = "t6", title = "Phòng Gym", categoryId = CategoryIds.HEALTH,
            categoryName = "Sức khỏe", categoryIcon = "💊", categoryColor = CategoryHealth,
            amount = 300_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.VIETCOMBANK, walletName = "Vietcombank",
            date = LocalDate.now().minusDays(5)
        ),
        Transaction(
            id = "t7", title = "Netflix", categoryId = CategoryIds.ENTERTAINMENT,
            categoryName = "Giải trí", categoryIcon = "🎮", categoryColor = CategoryEntertainment,
            amount = 199_000.0, type = TransactionType.EXPENSE,
            walletId = WalletIds.MOMO, walletName = "Ví MoMo",
            date = LocalDate.now().minusDays(6)
        ),
    )

    // Sample categories with amounts for dashboard display
    val categories = listOf(
        Category(
            CategoryIds.FOOD,
            "Ăn uống",
            "🍜",
            CategoryFood,
            CategoryType.EXPENSE,
            true,
            2_150_000.0,
            0.31f
        ),
        Category(
            CategoryIds.SHOPPING,
            "Mua sắm",
            "🛍️",
            CategoryShopping,
            CategoryType.EXPENSE,
            true,
            1_850_000.0,
            0.27f
        ),
        Category(
            CategoryIds.TRANSPORT,
            "Di chuyển",
            "🚗",
            CategoryTransport,
            CategoryType.EXPENSE,
            true,
            980_000.0,
            0.14f
        ),
        Category(
            CategoryIds.ENTERTAINMENT,
            "Giải trí",
            "🎮",
            CategoryEntertainment,
            CategoryType.EXPENSE,
            true,
            750_000.0,
            0.11f
        ),
        Category(
            CategoryIds.HEALTH,
            "Sức khỏe",
            "💊",
            CategoryHealth,
            CategoryType.EXPENSE,
            true,
            620_000.0,
            0.09f
        ),
        Category(
            CategoryIds.OTHER_EXPENSE,
            "Khác",
            "📦",
            CategoryOther,
            CategoryType.EXPENSE,
            true,
            570_000.0,
            0.08f
        ),
    )

    val monthlyData = listOf(
        MonthlySummary("T1", 32_000_000.0, 14_000_000.0),
        MonthlySummary("T2", 35_000_000.0, 16_500_000.0),
        MonthlySummary("T3", 38_000_000.0, 15_000_000.0),
        MonthlySummary("T4", 42_000_000.0, 17_420_000.0),
    )
}
