package data.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Long, // in KRW (no cents)
    val categoryId: String,
    val dateString: String, // YYYY-MM-DD
    val note: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val createdAt: Long = System.currentTimeMillis()
)
