package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import data.model.Category
import data.model.TransactionType
import ui.theme.ShadcnTheme
import util.ColorParser
import util.CurrencyFormatter

@Composable
fun SetBudgetDialog(
    isOpen: Boolean,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (categoryId: String, monthlyLimit: Long) -> Unit,
    initialCategoryId: String = "",
    initialLimit: Long = 0L
) {
    if (!isOpen) return

    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
    var selectedCategoryId by remember {
        mutableStateOf(if (initialCategoryId.isNotEmpty()) initialCategoryId else expenseCategories.firstOrNull()?.id ?: "")
    }
    var limitText by remember { mutableStateOf(if (initialLimit > 0) initialLimit.toString() else "") }
    val currentLimit = limitText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(
            modifier = Modifier.width(480.dp),
            contentPadding = 24.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "월간 예산 설정 (Set Monthly Budget)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    ShadcnBadge(text = "₩ KRW", variant = BadgeVariant.KRW)
                }

                // Category selector
                Column {
                    Text(
                        text = "예산 대상 카테고리 (Category)",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(expenseCategories) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            val catColor = ColorParser.parse(cat.colorHex, ShadcnTheme.colors.primary)

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) catColor.copy(alpha = 0.25f) else ShadcnTheme.colors.secondary)
                                    .border(1.dp, if (isSelected) catColor else ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategoryId = cat.id }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CategoryIcon(iconName = cat.iconName, tint = catColor, modifier = Modifier.size(16.dp))
                                Text(
                                    text = cat.name.split("(")[0].trim(),
                                    color = if (isSelected) Color.White else ShadcnTheme.colors.foreground,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Limit amount
                Column {
                    ShadcnInput(
                        value = limitText,
                        onValueChange = { input -> limitText = input.filter { it.isDigit() } },
                        label = "월간 한도 금액 (Monthly Limit in Won)",
                        placeholder = "예: 300000",
                        prefix = "₩"
                    )

                    if (currentLimit > 0) {
                        Text(
                            text = "설정액: ${CurrencyFormatter.format(currentLimit)}",
                            color = ShadcnTheme.colors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                        )
                    }
                }

                // Quick add Won buttons
                KrwQuickSelector(
                    currentAmount = currentLimit,
                    onAmountSelected = { newAmount ->
                        limitText = if (newAmount > 0) newAmount.toString() else ""
                    }
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ShadcnButtonText(
                        text = "취소 (Cancel)",
                        variant = ButtonVariant.GHOST,
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    ShadcnButtonText(
                        text = "예산 저장 (Save Budget)",
                        variant = ButtonVariant.PRIMARY,
                        enabled = currentLimit > 0 && selectedCategoryId.isNotEmpty(),
                        onClick = {
                            if (currentLimit > 0 && selectedCategoryId.isNotEmpty()) {
                                onSave(selectedCategoryId, currentLimit)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}
