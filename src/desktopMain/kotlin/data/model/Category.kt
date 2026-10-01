package data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val type: TransactionType,
    val budgetGroup: BudgetGroup = BudgetGroup.NEEDS
)
