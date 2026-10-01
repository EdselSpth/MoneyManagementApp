package data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE
}

@Serializable
enum class PaymentMethod(val label: String) {
    CARD("신용/체크카드 (Card)"),
    CASH("현금 (Cash)"),
    BANK_TRANSFER("계좌이체 (Bank Transfer)"),
    MOBILE_PAY("간편결제 (KakaoPay/Toss)")
}

@Serializable
enum class BudgetGroup(val label: String, val defaultPercent: Double) {
    NEEDS("필수지출 (Needs - 50%)", 50.0),
    WANTS("선택지출 (Wants - 30%)", 30.0),
    SAVINGS("저축/투자 (Savings - 20%)", 20.0),
    INCOME("수입 (Income)", 0.0)
}
