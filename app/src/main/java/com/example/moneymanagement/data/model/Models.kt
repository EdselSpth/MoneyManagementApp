package com.example.moneymanagement.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

@Serializable
enum class PaymentMethod(val label: String) {
    CARD("Card"),
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_PAY("Mobile Pay")
}

@Serializable
enum class AccountType(val label: String, val iconName: String) {
    TRANSIT("Transit Card (T-Money)", "DirectionsTransit"),
    FINTECH("FinTech (Toss)", "Smartphone"),
    DEBIT_CARD("Debit Card", "CreditCard"),
    CREDIT_CARD("Credit Card", "CreditCard"),
    CASH("Cash Wallet", "AccountBalanceWallet")
}

@Serializable
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val balance: Long, // in KRW
    val colorStartHex: String = "#0064FF",
    val colorEndHex: String = "#0038A8",
    val lastFour: String = "",
    val issuer: String = "Toss Bank",
    val isDefault: Boolean = false
)

@Serializable
enum class BudgetGroup(val label: String, val defaultPercent: Double) {
    NEEDS("Needs", 50.0),
    WANTS("Wants", 30.0),
    SAVINGS("Savings", 20.0),
    INCOME("Income", 0.0)
}

@Serializable
data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val type: TransactionType,
    val budgetGroup: BudgetGroup = BudgetGroup.NEEDS
)

@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Long, // in KRW (no cents)
    val categoryId: String,
    val dateString: String, // YYYY-MM-DD
    val note: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val createdAt: Long = System.currentTimeMillis(),
    val accountId: String? = null,
    val toAccountId: String? = null
)

@Serializable
data class Budget(
    val id: String,
    val categoryId: String,
    val month: Int, // 1-12
    val year: Int,
    val monthlyLimit: Long // in KRW
)

data class BudgetStatus(
    val budget: Budget,
    val category: Category,
    val spent: Long,
    val remaining: Long,
    val percentage: Float
)

@Serializable
data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmount: Long, // in KRW
    val currentAmount: Long, // in KRW
    val targetDateString: String, // YYYY-MM-DD
    val note: String = "",
    val iconName: String = "Star"
)

@Serializable
data class AllocationPlan(
    val totalIncome: Long,
    val needsAmount: Long,
    val needsPercent: Double = 50.0,
    val wantsAmount: Long,
    val wantsPercent: Double = 30.0,
    val savingsAmount: Long,
    val savingsPercent: Double = 20.0
)
