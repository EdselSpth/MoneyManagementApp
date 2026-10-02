package com.example.moneymanagement.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val krwFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    fun format(amount: Long, includePrefix: Boolean = true, includeSign: Boolean = false): String {
        val formattedNumber = krwFormat.format(kotlin.math.abs(amount))
        val prefix = if (includePrefix) "₩" else ""
        return when {
            amount < 0 -> "-$prefix$formattedNumber"
            amount > 0 && includeSign -> "+$prefix$formattedNumber"
            else -> "$prefix$formattedNumber"
        }
    }

    fun formatTransaction(amount: Long, type: com.example.moneymanagement.data.model.TransactionType): String {
        val formatted = format(amount)
        return when (type) {
            com.example.moneymanagement.data.model.TransactionType.INCOME -> "+$formatted"
            com.example.moneymanagement.data.model.TransactionType.EXPENSE -> "-$formatted"
            com.example.moneymanagement.data.model.TransactionType.TRANSFER -> "⇄ $formatted"
        }
    }

    fun parse(text: String): Long {
        val clean = text.replace(Regex("[^0-9-]"), "")
        return clean.toLongOrNull() ?: 0L
    }
}
