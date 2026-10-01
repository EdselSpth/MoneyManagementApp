package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import data.model.*
import ui.theme.ShadcnTheme
import util.CurrencyFormatter
import java.time.LocalDate
import java.util.UUID

@Composable
fun AddTransactionDialog(
    isOpen: Boolean,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    initialType: TransactionType = TransactionType.EXPENSE
) {
    if (!isOpen) return

    var type by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf(LocalDate.now().toString()) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CARD) }

    val filteredCategories = categories.filter { it.type == type }
    var selectedCategoryId by remember(type) {
        mutableStateOf(filteredCategories.firstOrNull()?.id ?: "")
    }

    val currentAmount = amountText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(
            modifier = Modifier
                .width(520.dp)
                .wrapContentHeight(),
            contentPadding = 24.dp
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (type == TransactionType.EXPENSE) "지출 기록 (New Expense)" else "수입 기록 (New Income)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    ShadcnBadge(
                        text = "₩ KRW",
                        variant = BadgeVariant.KRW
                    )
                }

                // Type Toggle (Expense / Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ShadcnTheme.colors.muted)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val expenseSelected = type == TransactionType.EXPENSE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (expenseSelected) ShadcnTheme.colors.expense else Color.Transparent)
                            .clickable { type = TransactionType.EXPENSE }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "지출 (Expense)",
                            color = if (expenseSelected) Color.White else ShadcnTheme.colors.mutedForeground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val incomeSelected = type == TransactionType.INCOME
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (incomeSelected) ShadcnTheme.colors.income else Color.Transparent)
                            .clickable { type = TransactionType.INCOME }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "수입 (Income)",
                            color = if (incomeSelected) Color.White else ShadcnTheme.colors.mutedForeground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Amount input with KRW formatted preview
                Column {
                    ShadcnInput(
                        value = amountText,
                        onValueChange = { input ->
                            // Allow digits only
                            val clean = input.filter { it.isDigit() }
                            amountText = clean
                        },
                        label = "금액 (Amount in Korean Won)",
                        placeholder = "예: 15000",
                        prefix = "₩"
                    )

                    if (currentAmount > 0) {
                        Text(
                            text = "환산: ${CurrencyFormatter.format(currentAmount)}",
                            color = if (type == TransactionType.INCOME) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                        )
                    }
                }

                // Quick Add Buttons for Won
                KrwQuickSelector(
                    currentAmount = currentAmount,
                    onAmountSelected = { newAmount ->
                        amountText = if (newAmount > 0) newAmount.toString() else ""
                    }
                )

                // Category Selector
                Column {
                    Text(
                        text = "카테고리 (Category)",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredCategories) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            val catColor = util.ColorParser.parse(cat.colorHex, ShadcnTheme.colors.primary)

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) catColor.copy(alpha = 0.25f) else ShadcnTheme.colors.secondary)
                                    .border(
                                        1.dp,
                                        if (isSelected) catColor else ShadcnTheme.colors.cardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCategoryId = cat.id }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CategoryIcon(iconName = cat.iconName, tint = catColor, modifier = Modifier.size(16.dp))
                                Text(
                                    text = cat.name.split("(")[0].trim(),
                                    color = if (isSelected) Color.White else ShadcnTheme.colors.foreground,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Payment Method
                Column {
                    Text(
                        text = "결제 수단 (Payment Method)",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMethod.entries.forEach { method ->
                            val isSelected = method == paymentMethod
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ShadcnTheme.colors.primary.copy(alpha = 0.15f) else ShadcnTheme.colors.secondary)
                                    .border(
                                        1.dp,
                                        if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.cardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { paymentMethod = method }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method.label.split("(")[0].trim(),
                                    color = if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.foreground,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Date & Note inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShadcnInput(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = "날짜 (Date YYYY-MM-DD)",
                        placeholder = "2026-10-01",
                        modifier = Modifier.weight(1f)
                    )

                    ShadcnInput(
                        value = note,
                        onValueChange = { note = it },
                        label = "메모 (Note / Description)",
                        placeholder = "예: 저녁 식사, 카페",
                        modifier = Modifier.weight(1.5f)
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadcnButtonText(
                        text = "취소 (Cancel)",
                        variant = ButtonVariant.GHOST,
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    ShadcnButtonText(
                        text = "저장하기 (Save Transaction)",
                        variant = if (type == TransactionType.INCOME) ButtonVariant.PRIMARY else ButtonVariant.PRIMARY,
                        enabled = currentAmount > 0 && selectedCategoryId.isNotEmpty(),
                        onClick = {
                            if (currentAmount > 0 && selectedCategoryId.isNotEmpty()) {
                                onSave(
                                    Transaction(
                                        id = UUID.randomUUID().toString(),
                                        type = type,
                                        amount = currentAmount,
                                        categoryId = selectedCategoryId,
                                        dateString = dateString.trim().ifEmpty { LocalDate.now().toString() },
                                        note = note.trim(),
                                        paymentMethod = paymentMethod
                                    )
                                )
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}
