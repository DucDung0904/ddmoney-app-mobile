package com.dung.ddmoney.network.dto

import com.google.gson.annotations.SerializedName

// ─── Wallet DTOs ──────────────────────────────────────────────────────

data class WalletResponse(
    val id: Long,
    val name: String,
    val balance: Double,
    val type: String,                   // "CASH" | "BANK" | "EWALLET" | "CREDIT_CARD" | "SAVINGS" | "INVESTMENT"
    val bankName: String? = null,
    val cardNumber: String? = null,
    val colorHex: String? = null,
    val icon: String? = null,
    val currency: String? = null,
    val isActive: Boolean? = null,
    val isDefault: Boolean? = null,
    val isArchived: Boolean? = null,
    val sortOrder: Int? = null,
    // Credit card fields
    val creditLimit: Double? = null,
    val currentDebt: Double? = null,
    val billingDay: Int? = null,
    val paymentDueDay: Int? = null
)

data class WalletRequest(
    val name: String,
    val balance: Double = 0.0,
    val type: String,                   // "CASH" | "BANK" | "EWALLET" | "CREDIT_CARD" | "SAVINGS" | "INVESTMENT"
    val bankName: String? = null,
    val cardNumber: String? = null,
    val colorHex: String = "#4659A6",
    val icon: String = "wallet",
    val currency: String = "VND",
    val isDefault: Boolean = false,
    // Credit card fields
    val creditLimit: Double? = null,
    val currentDebt: Double? = null,
    val billingDay: Int? = null,
    val paymentDueDay: Int? = null
)

data class TransferRequest(
    val fromWalletId: Long,
    val toWalletId: Long,
    val amount: Double,
    val note: String? = null
)

data class TotalBalanceResponse(
    val totalBalance: Double
)

// ─── Category DTOs ────────────────────────────────────────────────────

data class CategoryResponse(
    val id: Long,
    @SerializedName(value = "userId", alternate = ["user_id"])
    val userId: Long? = null,
    val name: String,
    val icon: String,
    @SerializedName(value = "colorHex", alternate = ["color_hex"])
    val colorHex: String?,
    val type: String,           // "INCOME" | "EXPENSE" | "DEBT" | "BOTH"
    @SerializedName(value = "isDefault", alternate = ["is_default"])
    val isDefault: Boolean?,
    @SerializedName(value = "isEditable", alternate = ["is_editable"])
    val isEditable: Boolean? = null,
    @SerializedName(value = "isDeletable", alternate = ["is_deletable"])
    val isDeletable: Boolean? = null,
    @SerializedName(value = "isDeleted", alternate = ["is_deleted"])
    val isDeleted: Boolean? = null,
    @SerializedName(value = "parentId", alternate = ["parent_id"])
    val parentId: Long? = null,
    @SerializedName(value = "sortOrder", alternate = ["sort_order"])
    val sortOrder: Int? = null
)

data class CategoryRequest(
    val name: String,
    val icon: String = "category",
    val colorHex: String = "#4659A6",
    val type: String,           // "INCOME" | "EXPENSE" | "DEBT" | "BOTH"
    val parentId: Long? = null,
    val sortOrder: Int = 10_000,
    val isDefault: Boolean = false,
    val isEditable: Boolean = true,
    val isDeletable: Boolean = true
)

// ─── Transaction DTOs ─────────────────────────────────────────────────

data class TransactionResponse(
    val id: Long,
    val title: String,
    val amount: Double,
    val type: String,           // "INCOME" | "EXPENSE" | "TRANSFER" | "DEBT"
    val date: String,           // "yyyy-MM-dd"
    val note: String?,
    val walletId: Long,
    val walletName: String?,
    val categoryId: Long,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val transferToWalletId: Long?,
    val transferToWalletName: String?
)

data class TransactionRequest(
    val title: String?,
    val amount: Double,
    val type: String,           // "INCOME" | "EXPENSE" | "TRANSFER" | "DEBT"
    val date: String,           // "yyyy-MM-dd"
    val walletId: Long,
    val categoryId: Long,
    val transferToWalletId: Long? = null,
    val note: String? = null
)

// ─── Summary DTOs ─────────────────────────────────────────────────────

data class TransactionSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val month: Int,
    val year: Int
)

data class MonthlyChart(
    val month: Int,
    val year: Int,
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String?,
    val amount: Double,
    val percentage: Float
)

// ─── Generic API Message ─────────────────────────────────────────────

data class ApiMessage(val message: String)

// ─── Budget DTOs ──────────────────────────────────────────────────────

data class BudgetResponse(
    val id: Long,
    val name: String,
    val categories: List<CategoryResponse>,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentage: Float,
    val month: Int,
    val year: Int
)

data class BudgetRequest(
    val name: String,
    val categoryIds: List<Long>,
    val amount: Double,
    val month: Int,
    val year: Int
)
