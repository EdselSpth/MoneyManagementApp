package com.example.moneymanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.data.model.SavingsGoal
import com.example.moneymanagement.data.repository.FinanceRepository
import com.example.moneymanagement.ui.components.*
import com.example.moneymanagement.ui.theme.ShadcnTheme
import com.example.moneymanagement.util.CurrencyFormatter

@Composable
fun SavingsGoalsScreen(
    repository: FinanceRepository,
    onOpenAddGoal: () -> Unit,
    onOpenDeposit: (SavingsGoal) -> Unit
) {
    val goals by repository.savingsGoals.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "저축 목표", color = ShadcnTheme.colors.foreground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "디지털 저축 통장 & 목표 관리", color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
                }

                ShadcnButtonText(text = "+ 새 목표", variant = ButtonVariant.PRIMARY, onClick = onOpenAddGoal)
            }
        }

        if (goals.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(text = "등록된 저축 목표가 없습니다.", color = ShadcnTheme.colors.mutedForeground, fontSize = 13.sp)
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                val progress = if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()) else 0f
                val isCompleted = goal.currentAmount >= goal.targetAmount

                ShadcnCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = goal.name, color = ShadcnTheme.colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    if (isCompleted) ShadcnBadge(text = "완료 🎉", variant = BadgeVariant.SUCCESS)
                                }
                                if (goal.note.isNotBlank()) {
                                    Text(text = goal.note, color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShadcnButtonText(text = "+ 입금", variant = ButtonVariant.OUTLINE, onClick = { onOpenDeposit(goal) })
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "삭제", tint = ShadcnTheme.colors.mutedForeground, modifier = Modifier.size(18.dp).clickable { repository.deleteGoal(goal.id) })
                            }
                        }

                        ShadcnProgress(progress = progress, height = 8.dp, colorOverride = if (isCompleted) ShadcnTheme.colors.income else ShadcnTheme.colors.krwAccent)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "모은 금액: ${CurrencyFormatter.format(goal.currentAmount)} (${(progress * 100).toInt()}%)", color = if (isCompleted) ShadcnTheme.colors.income else ShadcnTheme.colors.foreground, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0L)
                            Text(text = if (isCompleted) "목표 달성!" else "남은액: ${CurrencyFormatter.format(remaining)}", color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
