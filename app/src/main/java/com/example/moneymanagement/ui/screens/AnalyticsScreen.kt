package com.example.moneymanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.repository.FinanceRepository
import com.example.moneymanagement.ui.components.*
import com.example.moneymanagement.ui.theme.ShadcnTheme
import com.example.moneymanagement.util.ColorParser
import com.example.moneymanagement.util.CurrencyFormatter

@Composable
fun AnalyticsScreen(
    repository: FinanceRepository,
    onOpenSettings: () -> Unit = {}
) {
    val monthlyTransactions by repository.monthlyTransactions.collectAsState()
    val monthlyIncome by repository.monthlyIncome.collectAsState()
    val monthlyExpense by repository.monthlyExpense.collectAsState()
    val categories by repository.categories.collectAsState()
    val catMap = remember(categories) { categories.associateBy { it.id } }

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
            ChartSlice(label = cat.name, value = amount, color = color, percentage = percentage)
        }.sortedByDescending { it.value }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Financial Analytics", color = ShadcnTheme.colors.foreground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Spending breakdown and cashflow metrics", color = ShadcnTheme.colors.mutedForeground, fontSize = 13.sp)
                }

                ShadcnButton(
                    onClick = onOpenSettings,
                    variant = ButtonVariant.OUTLINE,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ShadcnTheme.colors.foreground, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Settings", color = ShadcnTheme.colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Donut Chart Card
        item {
            ShadcnCard(contentPadding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(text = "Expenses by Category", color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    DonutChart(slices = donutSlices, totalAmount = totalExpense, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        // Monthly Cashflow Card
        item {
            ShadcnCard(contentPadding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Monthly Cash Flow", color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    CashflowComparisonBar(income = monthlyIncome, expense = monthlyExpense)

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        val net = monthlyIncome - monthlyExpense
                        Text(
                            text = "Net Savings: ${CurrencyFormatter.format(net, includeSign = true)}",
                            color = if (net >= 0) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val rate = if (monthlyIncome > 0) ((monthlyIncome - monthlyExpense).coerceAtLeast(0).toDouble() / monthlyIncome.toDouble()) * 100.0 else 0.0
                        Text(
                            text = "Savings Rate: ${rate.toInt()}%",
                            color = ShadcnTheme.colors.krwAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Top Ranking Card
        item {
            ShadcnCard(contentPadding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Top Spending Categories", color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    if (donutSlices.isEmpty()) {
                        Text(text = "No expense data to analyze yet.", color = ShadcnTheme.colors.mutedForeground, fontSize = 13.sp)
                    } else {
                        donutSlices.forEachIndexed { index, slice ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ShadcnBadge(text = "#${index + 1}", variant = if (index == 0) BadgeVariant.WARNING else BadgeVariant.SECONDARY)
                                    Text(text = slice.label, color = ShadcnTheme.colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    text = "${CurrencyFormatter.format(slice.value)} (${(slice.percentage * 100).toInt()}%)",
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

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
