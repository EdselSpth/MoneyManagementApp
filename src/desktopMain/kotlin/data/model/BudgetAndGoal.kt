package data.model

import kotlinx.serialization.Serializable

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
    val percentage: Float // 0.0 to 1.0+
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
