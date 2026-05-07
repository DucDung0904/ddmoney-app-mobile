package com.dung.ddmoney.network.dto

import com.google.gson.annotations.SerializedName

// ─── Wallet DTOs ──────────────────────────────────────────────────────

data class WalletResponse(
    val id: Long,
    val name: String,
    val balance: Double,
    val type: String,           // "CASH" | "BANK" | "EWALLET" | "CREDIT"
    val bankName: String?,
    val cardNumber: String?,
    val colorHex: String?,
    val isActive: Boolean?
)

data class WalletRequest(
    val name: String,
    val balance: Double = 0.0,
    val type: String,           // "CASH" | "BANK" | "EWALLET" | "CREDIT"
    val bankName: String? = null,
    val cardNumber: String? = null,
    val colorHex: String = "#4659A6"
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
    val name: String,
    val icon: String,
    val colorHex: String?,
    val type: String,           // "INCOME" | "EXPENSE" | "DEBT" | "BOTH"
    val isDefault: Boolean?
)

data class CategoryRequest(
    val name: String,
    val icon: String = "📦",
    val colorHex: String = "#4659A6",
    val type: String            // "INCOME" | "EXPENSE" | "DEBT" | "BOTH"
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
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String?,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentage: Float,
    val month: Int,
    val year: Int
)

data class BudgetRequest(
    val categoryId: Long,
    val amount: Double,
    val month: Int,
    val year: Int
)
