package ui.screens

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
import data.model.SavingsGoal
import data.repository.FinanceRepository
import ui.components.*
import ui.theme.ShadcnTheme
import util.CurrencyFormatter

@Composable
fun SavingsGoalsScreen(
    repository: FinanceRepository,
    onOpenAddGoal: () -> Unit,
    onOpenDeposit: (SavingsGoal) -> Unit
) {
    val goals by repository.savingsGoals.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "저축 목표 관리 (Savings Goals)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "목표 금액을 설정하고 차곡차곡 모아가는 디지털 저축 통장",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 13.sp
                    )
                }

                ShadcnButtonText(
                    text = "+ 새 목표 만들기",
                    variant = ButtonVariant.PRIMARY,
                    onClick = onOpenAddGoal
                )
            }
        }

        // Goals List
        if (goals.isEmpty()) {
            item {
                ShadcnCard(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "현재 진행 중인 저축 목표가 없습니다.\n'+ 새 목표 만들기'를 눌러 시작해보세요!",
                            color = ShadcnTheme.colors.mutedForeground,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                val progress = if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()) else 0f
                val isCompleted = goal.currentAmount >= goal.targetAmount

                ShadcnCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 18.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = goal.name,
                                        color = ShadcnTheme.colors.foreground,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (isCompleted) {
                                        ShadcnBadge(text = "달성 완료! 🎉", variant = BadgeVariant.SUCCESS)
                                    } else {
                                        ShadcnBadge(text = "목표일: ${goal.targetDateString}", variant = BadgeVariant.SECONDARY)
                                    }
                                }

                                if (goal.note.isNotBlank()) {
                                    Text(
                                        text = goal.note,
                                        color = ShadcnTheme.colors.mutedForeground,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ShadcnButtonText(
                                    text = "+ 입금하기 (Deposit)",
                                    variant = ButtonVariant.OUTLINE,
                                    onClick = { onOpenDeposit(goal) }
                                )

                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "삭제",
                                    tint = ShadcnTheme.colors.mutedForeground,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { repository.deleteGoal(goal.id) }
                                )
                            }
                        }

                        // Progress Bar
                        ShadcnProgress(
                            progress = progress,
                            height = 10.dp,
                            colorOverride = if (isCompleted) ShadcnTheme.colors.income else ShadcnTheme.colors.krwAccent
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "현재 모은 금액: ${CurrencyFormatter.format(goal.currentAmount)} (${(progress * 100).toInt()}%)",
                                color = if (isCompleted) ShadcnTheme.colors.income else ShadcnTheme.colors.foreground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0L)
                            Text(
                                text = if (isCompleted) "목표 달성!" else "남은 금액: ${CurrencyFormatter.format(remaining)}",
                                color = ShadcnTheme.colors.mutedForeground,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
