package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import data.model.*
import data.repository.FinanceRepository
import ui.components.*
import ui.theme.ShadcnTheme
import util.ColorParser
import util.CurrencyFormatter
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    repository: FinanceRepository,
    onOpenAddTransaction: (TransactionType) -> Unit,
    onNavigateToBudgeting: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    println(">>> 7. Rendering DashboardScreen...")
    val totalBalance by repository.totalBalance.collectAsState()
    val monthlyIncome by repository.monthlyIncome.collectAsState()
    val monthlyExpense by repository.monthlyExpense.collectAsState()
    val netSavings by repository.netSavings.collectAsState()
    val savingsRate by repository.savingsRate.collectAsState()
    val allocationPlan by repository.allocationPlan.collectAsState()
    val budgetStatuses by repository.budgetStatuses.collectAsState()
    val monthlyTransactions by repository.monthlyTransactions.collectAsState()
    val categories by repository.categories.collectAsState()
    val selectedDate by repository.selectedDate.collectAsState()

    val catMap = remember(categories) { categories.associateBy { it.id } }

    val monthHeaderStr = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 (MMMM yyyy)"))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Month Selector Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "대시보드 (Overview)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "스마트 가계부 & 예산 관리 시스템",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 13.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShadcnButton(
                        onClick = { repository.previousMonth() },
                        variant = ButtonVariant.OUTLINE,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "이전달", tint = ShadcnTheme.colors.foreground, modifier = Modifier.size(18.dp))
                    }

                    ShadcnCard(
                        contentPadding = 8.dp,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = monthHeaderStr,
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Center).padding(horizontal = 8.dp)
                        )
                    }

                    ShadcnButton(
                        onClick = { repository.nextMonth() },
                        variant = ButtonVariant.OUTLINE,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "다음달", tint = ShadcnTheme.colors.foreground, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    ShadcnButtonText(
                        text = "+ 지출 기록 (Add Expense)",
                        variant = ButtonVariant.DESTRUCTIVE,
                        onClick = { onOpenAddTransaction(TransactionType.EXPENSE) }
                    )

                    ShadcnButtonText(
                        text = "+ 수입 기록 (Add Income)",
                        variant = ButtonVariant.PRIMARY,
                        onClick = { onOpenAddTransaction(TransactionType.INCOME) }
                    )
                }
            }
        }

        // Top Metrics 4-Grid Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Balance
                MetricCard(
                    title = "총 자산 잔액 (Total Balance)",
                    value = CurrencyFormatter.format(totalBalance),
                    subtext = "누적 전체 잔액",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = ShadcnTheme.colors.primary,
                    modifier = Modifier.weight(1f)
                )

                // Monthly Income
                MetricCard(
                    title = "이번 달 총 수입 (Income)",
                    value = CurrencyFormatter.format(monthlyIncome),
                    subtext = "월간 누적 수입",
                    icon = Icons.Default.ArrowDownward,
                    iconTint = ShadcnTheme.colors.income,
                    valueColor = ShadcnTheme.colors.income,
                    modifier = Modifier.weight(1f)
                )

                // Monthly Expense
                MetricCard(
                    title = "이번 달 총 지출 (Expense)",
                    value = CurrencyFormatter.format(monthlyExpense),
                    subtext = "월간 누적 지출",
                    icon = Icons.Default.ArrowUpward,
                    iconTint = ShadcnTheme.colors.expense,
                    valueColor = ShadcnTheme.colors.expense,
                    modifier = Modifier.weight(1f)
                )

                // Net Savings & Rate
                MetricCard(
                    title = "저축률 (Savings Rate)",
                    value = "${savingsRate.toInt()}%",
                    subtext = "순저축: ${CurrencyFormatter.format(netSavings)}",
                    icon = Icons.Default.Savings,
                    iconTint = ShadcnTheme.colors.krwAccent,
                    valueColor = if (netSavings >= 0) ShadcnTheme.colors.krwAccent else ShadcnTheme.colors.expense,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 50/30/20 Smart Allocation Widget & Quick Cashflow
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 50/30/20 Rule Smart Allocation Card
                ShadcnCard(modifier = Modifier.weight(1.4f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "50/30/20 스마트 수입 배분 (Smart Budgeting)",
                                    color = ShadcnTheme.colors.foreground,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "이번 달 수입(${CurrencyFormatter.format(allocationPlan.totalIncome)}) 기준 최적화 가이드",
                                    color = ShadcnTheme.colors.mutedForeground,
                                    fontSize = 12.sp
                                )
                            }
                            ShadcnButtonText(
                                text = "예산 관리 상세",
                                variant = ButtonVariant.OUTLINE,
                                onClick = onNavigateToBudgeting
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AllocationItem(
                                title = "필수 지출 (Needs 50%)",
                                amount = allocationPlan.needsAmount,
                                color = Color(0xFFF59E0B),
                                description = "월세, 식비, 교통, 공과금",
                                modifier = Modifier.weight(1f)
                            )
                            AllocationItem(
                                title = "선택 지출 (Wants 30%)",
                                amount = allocationPlan.wantsAmount,
                                color = Color(0xFFA855F7),
                                description = "카페, 쇼핑, 문화생활",
                                modifier = Modifier.weight(1f)
                            )
                            AllocationItem(
                                title = "저축 & 투자 (Savings 20%)",
                                amount = allocationPlan.savingsAmount,
                                color = Color(0xFF10B981),
                                description = "비상금, 적금, 주식",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Cashflow Balance Bar
                ShadcnCard(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "수입 대 지출 비율",
                                color = ShadcnTheme.colors.foreground,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            ShadcnButtonText(
                                text = "차트 분석",
                                variant = ButtonVariant.GHOST,
                                onClick = onNavigateToAnalytics
                            )
                        }

                        CashflowComparisonBar(
                            income = monthlyIncome,
                            expense = monthlyExpense,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = if (monthlyIncome >= monthlyExpense)
                                " 이번 달 수입이 지출보다 넉넉합니다! 잉여 자금을 저축 목표에 넣어보세요."
                            else
                                "⚠️ 이번 달 지출이 수입을 초과했습니다. 예산 한도를 점검하세요!",
                            color = if (monthlyIncome >= monthlyExpense) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Budget Status Alert List
        if (budgetStatuses.isNotEmpty()) {
            item {
                ShadcnCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "월간 카테고리 예산 현황 (Budget Limits)",
                                    color = ShadcnTheme.colors.foreground,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                ShadcnBadge(text = "${budgetStatuses.size}개 설정됨", variant = BadgeVariant.SECONDARY)
                            }
                            ShadcnButtonText(
                                text = "예산 수정 / 추가",
                                variant = ButtonVariant.OUTLINE,
                                onClick = onNavigateToBudgeting
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            budgetStatuses.take(4).forEach { status ->
                                val catColor = ColorParser.parse(status.category.colorHex, ShadcnTheme.colors.primary)

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            CategoryIcon(iconName = status.category.iconName, tint = catColor, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = status.category.name.split("(")[0].trim(),
                                                color = ShadcnTheme.colors.foreground,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${CurrencyFormatter.format(status.spent)} / ${CurrencyFormatter.format(status.budget.monthlyLimit)}",
                                                color = ShadcnTheme.colors.mutedForeground,
                                                fontSize = 12.sp
                                            )
                                            when {
                                                status.percentage >= 1.0f -> ShadcnBadge(text = "초과", variant = BadgeVariant.DESTRUCTIVE)
                                                status.percentage >= 0.75f -> ShadcnBadge(text = "주의", variant = BadgeVariant.WARNING)
                                                else -> ShadcnBadge(text = "안전", variant = BadgeVariant.SUCCESS)
                                            }
                                        }
                                    }

                                    ShadcnProgress(progress = status.percentage, height = 7.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions
        item {
            ShadcnCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "최근 거래 내역 (Recent Transactions)",
                                color = ShadcnTheme.colors.foreground,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            ShadcnBadge(text = "${monthlyTransactions.size}건", variant = BadgeVariant.SECONDARY)
                        }
                        ShadcnButtonText(
                            text = "전체 거래 보기",
                            variant = ButtonVariant.GHOST,
                            onClick = onNavigateToTransactions
                        )
                    }

                    if (monthlyTransactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "이번 달 거래 내역이 아직 없습니다. 새 거래를 등록해보세요!",
                                color = ShadcnTheme.colors.mutedForeground,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            monthlyTransactions.take(7).forEach { tx ->
                                val cat = catMap[tx.categoryId]
                                val catColor = ColorParser.parse(cat?.colorHex ?: "#6B7280", ShadcnTheme.colors.primary)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ShadcnTheme.colors.secondary.copy(alpha = 0.5f))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(catColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CategoryIcon(iconName = cat?.iconName ?: "Category", tint = catColor, modifier = Modifier.size(18.dp))
                                        }

                                        Column {
                                            Text(
                                                text = if (tx.note.isNotBlank()) tx.note else cat?.name?.split("(")?.get(0)?.trim() ?: "기타",
                                                color = ShadcnTheme.colors.foreground,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${tx.dateString} • ${cat?.name?.split("(")?.get(0)?.trim()} • ${tx.paymentMethod.label.split("(")[0].trim()}",
                                                color = ShadcnTheme.colors.mutedForeground,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = CurrencyFormatter.format(tx.amount, includeSign = true),
                                            color = if (tx.type == TransactionType.INCOME) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "삭제",
                                            tint = ShadcnTheme.colors.mutedForeground,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable { repository.deleteTransaction(tx.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    valueColor: Color = ShadcnTheme.colors.foreground,
    modifier: Modifier = Modifier
) {
    ShadcnCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = ShadcnTheme.colors.mutedForeground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = value,
                color = valueColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtext,
                color = ShadcnTheme.colors.mutedForeground,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun AllocationItem(
    title: String,
    amount: Long,
    color: Color,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = CurrencyFormatter.format(amount),
                color = ShadcnTheme.colors.foreground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = ShadcnTheme.colors.mutedForeground,
                fontSize = 10.sp
            )
        }
    }
}
