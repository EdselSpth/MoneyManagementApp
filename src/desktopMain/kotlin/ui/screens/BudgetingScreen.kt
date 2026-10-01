package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.model.BudgetGroup
import data.model.BudgetStatus
import data.model.Category
import data.model.TransactionType
import data.repository.FinanceRepository
import ui.components.*
import ui.theme.ShadcnTheme
import util.ColorParser
import util.CurrencyFormatter

@Composable
fun BudgetingScreen(
    repository: FinanceRepository,
    onOpenSetBudgetDialog: (initialCategoryId: String, initialLimit: Long) -> Unit
) {
    val monthlyIncome by repository.monthlyIncome.collectAsState()
    val monthlyExpense by repository.monthlyExpense.collectAsState()
    val allocationPlan by repository.allocationPlan.collectAsState()
    val budgetStatuses by repository.budgetStatuses.collectAsState()
    val monthlyTransactions by repository.monthlyTransactions.collectAsState()
    val categories by repository.categories.collectAsState()

    // Calculate actual spending per BudgetGroup (Needs, Wants, Savings)
    val catGroupMap = remember(categories) { categories.associate { it.id to it.budgetGroup } }
    var actualNeedsSpent = 0L
    var actualWantsSpent = 0L
    var actualSavingsSpent = 0L

    monthlyTransactions.filter { it.type == TransactionType.EXPENSE }.forEach { tx ->
        when (catGroupMap[tx.categoryId]) {
            BudgetGroup.NEEDS -> actualNeedsSpent += tx.amount
            BudgetGroup.WANTS -> actualWantsSpent += tx.amount
            BudgetGroup.SAVINGS -> actualSavingsSpent += tx.amount
            else -> {}
        }
    }

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
                        text = "예산 및 수입 분배 (Budgeting & Income Allocation)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "50/30/20 법칙 기반 스마트 수입 배분 및 카테고리별 지출 한도 관리",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 13.sp
                    )
                }

                ShadcnButtonText(
                    text = "+ 새 예산 한도 설정",
                    variant = ButtonVariant.PRIMARY,
                    onClick = { onOpenSetBudgetDialog("", 0L) }
                )
            }
        }

        // 50/30/20 Smart Allocation Breakdown Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "📊 이번 달 수입 기반 50/30/20 배분 분석 (기준 수입: ${CurrencyFormatter.format(monthlyIncome)})",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GroupAllocationCard(
                        title = "1. 필수 지출 (Needs 50%)",
                        recommended = allocationPlan.needsAmount,
                        actualSpent = actualNeedsSpent,
                        color = Color(0xFFF59E0B),
                        categoriesDesc = "식비, 주거비/월세, 교통, 공과금, 병원비",
                        modifier = Modifier.weight(1f)
                    )

                    GroupAllocationCard(
                        title = "2. 선택 지출 (Wants 30%)",
                        recommended = allocationPlan.wantsAmount,
                        actualSpent = actualWantsSpent,
                        color = Color(0xFFA855F7),
                        categoriesDesc = "카페/디저트, 쇼핑, 문화생활, 취미",
                        modifier = Modifier.weight(1f)
                    )

                    GroupAllocationCard(
                        title = "3. 저축 & 투자 (Savings 20%)",
                        recommended = allocationPlan.savingsAmount,
                        actualSpent = actualSavingsSpent,
                        color = Color(0xFF10B981),
                        categoriesDesc = "적금, 비상금, 주식 투자, 목표 저축",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Category Budgets List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "카테고리별 월간 예산 한도 (${budgetStatuses.size}개)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (budgetStatuses.isEmpty()) {
                    ShadcnCard(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "설정된 카테고리 예산이 없습니다. 우측 상단의 '+ 새 예산 한도 설정'을 클릭하세요.",
                                color = ShadcnTheme.colors.mutedForeground,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        budgetStatuses.forEach { status ->
                            CategoryBudgetCard(
                                status = status,
                                onEdit = { onOpenSetBudgetDialog(status.category.id, status.budget.monthlyLimit) },
                                onDelete = { repository.deleteBudget(status.budget.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupAllocationCard(
    title: String,
    recommended: Long,
    actualSpent: Long,
    color: Color,
    categoriesDesc: String,
    modifier: Modifier = Modifier
) {
    val progress = if (recommended > 0) (actualSpent.toFloat() / recommended.toFloat()) else 0f

    ShadcnCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "권장 배분액", color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
                    Text(text = CurrencyFormatter.format(recommended), color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "실제 사용액", color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
                    Text(text = CurrencyFormatter.format(actualSpent), color = if (progress > 1f) ShadcnTheme.colors.expense else color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            ShadcnProgress(
                progress = progress,
                height = 8.dp,
                colorOverride = if (progress > 1f) ShadcnTheme.colors.expense else color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "사용률: ${(progress * 100).toInt()}%",
                    color = ShadcnTheme.colors.mutedForeground,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val diff = recommended - actualSpent
                Text(
                    text = if (diff >= 0) "남은 여유: ${CurrencyFormatter.format(diff)}" else "초과: ${CurrencyFormatter.format(-diff)}",
                    color = if (diff >= 0) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "포함: $categoriesDesc",
                color = ShadcnTheme.colors.mutedForeground,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    status: BudgetStatus,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val catColor = ColorParser.parse(status.category.colorHex, ShadcnTheme.colors.primary)

    ShadcnCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(catColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryIcon(iconName = status.category.iconName, tint = catColor, modifier = Modifier.size(18.dp))
                    }

                    Column {
                        Text(
                            text = status.category.name,
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "소속: ${status.category.budgetGroup.label}",
                            color = ShadcnTheme.colors.mutedForeground,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when {
                        status.percentage >= 1.0f -> ShadcnBadge(text = "한도 초과 (${(status.percentage * 100).toInt()}%)", variant = BadgeVariant.DESTRUCTIVE)
                        status.percentage >= 0.8f -> ShadcnBadge(text = "주의 (${(status.percentage * 100).toInt()}%)", variant = BadgeVariant.WARNING)
                        else -> ShadcnBadge(text = "안전 (${(status.percentage * 100).toInt()}%)", variant = BadgeVariant.SUCCESS)
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "수정",
                        tint = ShadcnTheme.colors.mutedForeground,
                        modifier = Modifier.size(18.dp).clickable { onEdit() }
                    )

                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "삭제",
                        tint = ShadcnTheme.colors.mutedForeground,
                        modifier = Modifier.size(18.dp).clickable { onDelete() }
                    )
                }
            }

            // Progress bar
            ShadcnProgress(progress = status.percentage, height = 9.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "지출: ${CurrencyFormatter.format(status.spent)} / 한도: ${CurrencyFormatter.format(status.budget.monthlyLimit)}",
                    color = ShadcnTheme.colors.mutedForeground,
                    fontSize = 12.sp
                )

                Text(
                    text = if (status.remaining >= 0) "잔여: ${CurrencyFormatter.format(status.remaining)}" else "초과액: ${CurrencyFormatter.format(-status.remaining)}",
                    color = if (status.remaining >= 0) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
