package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import data.model.SavingsGoal
import ui.theme.ShadcnTheme
import util.CurrencyFormatter
import java.time.LocalDate
import java.util.UUID

@Composable
fun AddSavingsGoalDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (SavingsGoal) -> Unit
) {
    if (!isOpen) return

    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initialSavedText by remember { mutableStateOf("0") }
    var targetDateText by remember { mutableStateOf(LocalDate.now().plusMonths(6).toString()) }
    var note by remember { mutableStateOf("") }

    val targetAmount = targetText.toLongOrNull() ?: 0L
    val initialSaved = initialSavedText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(
            modifier = Modifier.width(480.dp),
            contentPadding = 24.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "새 저축 목표 추가 (New Savings Goal)",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                ShadcnInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "목표 이름 (Goal Name)",
                    placeholder = "예: 제주도 여행, 비상금, 맥북 구매"
                )

                ShadcnInput(
                    value = targetText,
                    onValueChange = { input -> targetText = input.filter { it.isDigit() } },
                    label = "목표 금액 (Target in KRW)",
                    placeholder = "예: 1000000",
                    prefix = "₩"
                )

                if (targetAmount > 0) {
                    Text(
                        text = "목표: ${CurrencyFormatter.format(targetAmount)}",
                        color = ShadcnTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                KrwQuickSelector(
                    currentAmount = targetAmount,
                    onAmountSelected = { newAmount ->
                        targetText = if (newAmount > 0) newAmount.toString() else ""
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShadcnInput(
                        value = initialSavedText,
                        onValueChange = { input -> initialSavedText = input.filter { it.isDigit() } },
                        label = "현재 모은 금액 (Current Saved)",
                        prefix = "₩",
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        ShadcnInput(
                            value = targetDateText,
                            onValueChange = { targetDateText = it },
                            label = "목표 날짜 (Target Date)",
                            placeholder = "YYYY-MM-DD",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            val plus3 = LocalDate.now().plusMonths(3).toString()
                            val plus6 = LocalDate.now().plusMonths(6).toString()
                            val plus1Yr = LocalDate.now().plusYears(1).toString()
                            listOf("+3개월" to plus3, "+6개월" to plus6, "+1년" to plus1Yr).forEach { (lbl, dVal) ->
                                val isSelected = targetDateText == dVal
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) ShadcnTheme.colors.primary.copy(alpha = 0.2f) else ShadcnTheme.colors.muted)
                                        .clickable { targetDateText = dVal }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = lbl,
                                        color = if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.mutedForeground,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                ShadcnInput(
                    value = note,
                    onValueChange = { note = it },
                    label = "메모 (Note)",
                    placeholder = "예: 매달 10만원씩 모으기"
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ShadcnButtonText(
                        text = "취소 (Cancel)",
                        variant = ButtonVariant.GHOST,
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    ShadcnButtonText(
                        text = "목표 생성 (Create Goal)",
                        variant = ButtonVariant.PRIMARY,
                        enabled = name.isNotBlank() && targetAmount > 0,
                        onClick = {
                            if (name.isNotBlank() && targetAmount > 0) {
                                onSave(
                                    SavingsGoal(
                                        id = UUID.randomUUID().toString(),
                                        name = name.trim(),
                                        targetAmount = targetAmount,
                                        currentAmount = initialSaved,
                                        targetDateString = targetDateText.trim().ifEmpty { LocalDate.now().toString() },
                                        note = note.trim()
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

@Composable
fun AddGoalFundsDialog(
    goal: SavingsGoal?,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long) -> Unit
) {
    if (goal == null) return

    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(
            modifier = Modifier.width(420.dp),
            contentPadding = 24.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "저축액 입금: ${goal.name}",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "현재 모인 금액: ${CurrencyFormatter.format(goal.currentAmount)} / ${CurrencyFormatter.format(goal.targetAmount)}",
                    color = ShadcnTheme.colors.mutedForeground,
                    fontSize = 13.sp
                )

                ShadcnInput(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() } },
                    label = "추가 입금액 (Amount to deposit in Won)",
                    placeholder = "예: 50000",
                    prefix = "₩"
                )

                KrwQuickSelector(
                    currentAmount = amount,
                    onAmountSelected = { newAmount ->
                        amountText = if (newAmount > 0) newAmount.toString() else ""
                    }
                )

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
                        text = "입금 완료 (Deposit)",
                        variant = ButtonVariant.PRIMARY,
                        enabled = amount > 0,
                        onClick = {
                            if (amount > 0) {
                                onConfirm(amount)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}
