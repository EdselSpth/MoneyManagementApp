package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import data.model.TransactionType
import data.repository.FinanceRepository
import ui.components.*
import ui.theme.ShadcnTheme
import util.ColorParser
import util.CurrencyFormatter

@Composable
fun AnalyticsScreen(repository: FinanceRepository) {
    val monthlyTransactions by repository.monthlyTransactions.collectAsState()
    val monthlyIncome by repository.monthlyIncome.collectAsState()
    val monthlyExpense by repository.monthlyExpense.collectAsState()
    val categories by repository.categories.collectAsState()
    val catMap = remember(categories) { categories.associateBy { it.id } }

    // Category breakdown
    val categorySpendMap = remember(monthlyTransactions) {
        val map = mutableMapOf<String, Long>()
        monthlyTransactions.filter { it.type == TransactionType.EXPENSE }.forEach { tx ->
            map[tx.categoryId] = (map[tx.categoryId] ?: 0L) + tx.amount
        }
        map
    }

    val totalExpense = remember(categorySpendMap) { categorySpendMap.values.sum() }

    val defaultChartColor = ShadcnTheme.colors.primary
    val donutSlices = remember(categorySpendMap, catMap, totalExpense, defaultChartColor) {
        categorySpendMap.entries.mapNotNull { (catId, amount) ->
            val cat = catMap[catId] ?: return@mapNotNull null
            val color = ColorParser.parse(cat.colorHex, defaultChartColor)
            val percentage = if (totalExpense > 0) amount.toFloat() / totalExpense.toFloat() else 0f
            ChartSlice(
                label = cat.name.split("(")[0].trim(),
                value = amount,
                color = color,
                percentage = percentage
            )
        }.sortedByDescending { it.value }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "지출 및 수입 분석 (Visual Analytics)",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "카테고리별 지출 도넛 차트 및 수입-지출 현금흐름 심층 분석",
                    color = ShadcnTheme.colors.mutedForeground,
                    fontSize = 13.sp
                )
            }
        }

        // Donut Chart Card
        item {
            ShadcnCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "카테고리별 지출 구성비 (Expense Breakdown)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    DonutChart(
                        slices = donutSlices,
                        totalAmount = totalExpense,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }

        // Monthly Cashflow Card
        item {
            ShadcnCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "월간 현금 흐름 (Monthly Cashflow)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    CashflowComparisonBar(
                        income = monthlyIncome,
                        expense = monthlyExpense
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "순 저축액 (Net Savings)", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                            val net = monthlyIncome - monthlyExpense
                            Text(
                                text = CurrencyFormatter.format(net, includeSign = true),
                                color = if (net >= 0) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "저축률 (Savings Rate)", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                            val rate = if (monthlyIncome > 0) ((monthlyIncome - monthlyExpense).coerceAtLeast(0).toDouble() / monthlyIncome.toDouble()) * 100.0 else 0.0
                            Text(
                                text = "${rate.toInt()}%",
                                color = ShadcnTheme.colors.krwAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Ranked Category List
        item {
            ShadcnCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "지출 상위 카테고리 순위 (Top Expense Ranking)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (donutSlices.isEmpty()) {
                        Text(text = "분석할 지출 데이터가 없습니다.", color = ShadcnTheme.colors.mutedForeground, fontSize = 13.sp)
                    } else {
                        donutSlices.forEachIndexed { index, slice ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ShadcnBadge(
                                        text = "#${index + 1}",
                                        variant = if (index == 0) BadgeVariant.WARNING else BadgeVariant.SECONDARY
                                    )
                                    Text(
                                        text = slice.label,
                                        color = ShadcnTheme.colors.foreground,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "${(slice.percentage * 100).toInt()}%",
                                        color = ShadcnTheme.colors.mutedForeground,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(slice.value),
                                        color = ShadcnTheme.colors.foreground,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
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
