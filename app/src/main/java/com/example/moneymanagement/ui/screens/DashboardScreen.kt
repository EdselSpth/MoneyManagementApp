package com.example.moneymanagement.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.time.Instant
import java.time.ZoneOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.data.model.*
import com.example.moneymanagement.data.repository.FinanceRepository
import com.example.moneymanagement.ui.components.*
import com.example.moneymanagement.ui.theme.ShadcnTheme
import com.example.moneymanagement.util.ColorParser
import com.example.moneymanagement.util.CurrencyFormatter
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    repository: FinanceRepository,
    onOpenAddTransaction: (TransactionType) -> Unit,
    onNavigateToBudgeting: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEditRatio: () -> Unit,
    onTopUpAccount: (Account) -> Unit,
    onAddNewCard: () -> Unit,
    onManageCards: () -> Unit
) {
    val totalBalance by repository.totalBalance.collectAsState()
    val accounts by repository.accounts.collectAsState()
    val monthlyIncome by repository.monthlyIncome.collectAsState()
    val monthlyExpense by repository.monthlyExpense.collectAsState()
    val netSavings by repository.netSavings.collectAsState()
    val savingsRate by repository.savingsRate.collectAsState()
    val allocationPlan by repository.allocationPlan.collectAsState()
    val needsRatio by repository.needsRatio.collectAsState()
    val wantsRatio by repository.wantsRatio.collectAsState()
    val savingsRatio by repository.savingsRatio.collectAsState()
    val monthlyTransactions by repository.monthlyTransactions.collectAsState()
    val categories by repository.categories.collectAsState()
    val selectedDate by repository.selectedDate.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    val catMap = remember(categories) { categories.associateBy { it.id } }
    val accMap = remember(accounts) { accounts.associateBy { it.id } }
    val monthHeaderStr = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ShadcnTheme.colors.card)
                            .border(1.dp, ShadcnTheme.colors.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Shiota Star",
                            tint = ShadcnTheme.colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Shiota Wallet",
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Minimalist Financial & Wallet Tracker",
                            color = ShadcnTheme.colors.mutedForeground,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShadcnTheme.colors.secondary)
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = ShadcnTheme.colors.foreground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Multi-Account / Multi-Card Carousel
        item {
            AccountCardsCarousel(
                accounts = accounts,
                onCardClick = { onTopUpAccount(it) },
                onTopUpClick = { onTopUpAccount(it) },
                onAddNewCard = onAddNewCard,
                onManageCards = onManageCards
            )
        }

        // Month Selector Header
        item {
            ShadcnCard(contentPadding = 10.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadcnButton(
                        onClick = { repository.previousMonth() },
                        variant = ButtonVariant.OUTLINE,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = ShadcnTheme.colors.foreground, modifier = Modifier.size(18.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showMonthPicker = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = ShadcnTheme.colors.krwAccent, modifier = Modifier.size(16.dp))
                        Text(
                            text = monthHeaderStr,
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ShadcnButton(
                        onClick = { repository.nextMonth() },
                        variant = ButtonVariant.OUTLINE,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = ShadcnTheme.colors.foreground, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Top Metrics 2x2 Grid Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Total Balance",
                        value = CurrencyFormatter.format(totalBalance),
                        subtext = "Cumulative balance",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = ShadcnTheme.colors.primary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Savings Rate",
                        value = "${savingsRate.toInt()}%",
                        subtext = "Net: ${CurrencyFormatter.format(netSavings)}",
                        icon = Icons.Default.Savings,
                        iconTint = ShadcnTheme.colors.krwAccent,
                        valueColor = if (netSavings >= 0) ShadcnTheme.colors.krwAccent else ShadcnTheme.colors.expense,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Monthly Income",
                        value = CurrencyFormatter.format(monthlyIncome),
                        subtext = "Total earnings",
                        icon = Icons.Default.ArrowDownward,
                        iconTint = ShadcnTheme.colors.income,
                        valueColor = ShadcnTheme.colors.income,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Monthly Expense",
                        value = CurrencyFormatter.format(monthlyExpense),
                        subtext = "Total spent",
                        icon = Icons.Default.ArrowUpward,
                        iconTint = ShadcnTheme.colors.expense,
                        valueColor = ShadcnTheme.colors.expense,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Action Buttons
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadcnButton(
                    onClick = { onOpenAddTransaction(TransactionType.EXPENSE) },
                    modifier = Modifier.weight(1f),
                    variant = ButtonVariant.DESTRUCTIVE,
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "-", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Expense", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                ShadcnButton(
                    onClick = { onOpenAddTransaction(TransactionType.INCOME) },
                    modifier = Modifier.weight(1f),
                    variant = ButtonVariant.PRIMARY,
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "+", tint = ShadcnTheme.colors.primaryForeground, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Income", color = ShadcnTheme.colors.primaryForeground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Smart Allocation Ratio Widget
        item {
            ShadcnCard(contentPadding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Budget Allocation",
                                color = ShadcnTheme.colors.foreground,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target: $needsRatio% Needs • $wantsRatio% Wants • $savingsRatio% Savings",
                                color = ShadcnTheme.colors.mutedForeground,
                                fontSize = 12.sp
                            )
                        }
                        ShadcnButtonText(text = "Edit Ratio", variant = ButtonVariant.OUTLINE, onClick = onOpenEditRatio)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AllocationItem(title = "Needs ($needsRatio%)", amount = allocationPlan.needsAmount, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                        AllocationItem(title = "Wants ($wantsRatio%)", amount = allocationPlan.wantsAmount, color = Color(0xFFA855F7), modifier = Modifier.weight(1f))
                        AllocationItem(title = "Savings ($savingsRatio%)", amount = allocationPlan.savingsAmount, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Cashflow Bar
        item {
            ShadcnCard(contentPadding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Monthly Cash Flow", color = ShadcnTheme.colors.foreground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        ShadcnButtonText(text = "Analytics", variant = ButtonVariant.GHOST, onClick = onNavigateToAnalytics)
                    }

                    CashflowComparisonBar(income = monthlyIncome, expense = monthlyExpense)
                }
            }
        }

        // Recent Transactions
        item {
            ShadcnCard(contentPadding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Recent Transactions", color = ShadcnTheme.colors.foreground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        ShadcnButtonText(text = "View All", variant = ButtonVariant.GHOST, onClick = onNavigateToTransactions)
                    }

                    if (monthlyTransactions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            Text(text = "No transactions recorded this month.", color = ShadcnTheme.colors.mutedForeground, fontSize = 13.sp)
                        }
                    } else {
                        monthlyTransactions.take(5).forEach { tx ->
                            val cat = catMap[tx.categoryId]
                            val catColor = ColorParser.parse(cat?.colorHex ?: "#6B7280", ShadcnTheme.colors.primary)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShadcnTheme.colors.secondary.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(catColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CategoryIcon(iconName = cat?.iconName ?: "Category", tint = catColor, modifier = Modifier.size(18.dp))
                                    }

                                    val acc = accMap[tx.accountId]
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (tx.note.isNotBlank()) tx.note else cat?.name ?: "Transaction",
                                            color = ShadcnTheme.colors.foreground,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${tx.dateString} • ${cat?.name ?: ""}${if (acc != null) " • ${acc.name}" else ""}",
                                            color = ShadcnTheme.colors.mutedForeground,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    text = CurrencyFormatter.formatTransaction(tx.amount, tx.type),
                                    color = if (tx.type == TransactionType.INCOME) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom clearance for Apple floating dock
        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    if (showMonthPicker) {
        val initialMillis = remember(selectedDate) {
            selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showMonthPicker = false },
            confirmButton = {
                ShadcnButtonText(
                    text = "Select Month",
                    variant = ButtonVariant.PRIMARY,
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            repository.setSelectedDate(picked)
                        }
                        showMonthPicker = false
                    }
                )
            },
            dismissButton = {
                ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = { showMonthPicker = false })
            },
            colors = DatePickerDefaults.colors(containerColor = ShadcnTheme.colors.card),
            shape = RoundedCornerShape(16.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = ShadcnTheme.colors.card,
                    titleContentColor = ShadcnTheme.colors.foreground,
                    headlineContentColor = ShadcnTheme.colors.foreground,
                    weekdayContentColor = ShadcnTheme.colors.mutedForeground,
                    subheadContentColor = ShadcnTheme.colors.mutedForeground,
                    yearContentColor = ShadcnTheme.colors.foreground,
                    currentYearContentColor = ShadcnTheme.colors.primary,
                    selectedYearContentColor = ShadcnTheme.colors.primaryForeground,
                    selectedYearContainerColor = ShadcnTheme.colors.primary,
                    dayContentColor = ShadcnTheme.colors.foreground,
                    selectedDayContentColor = ShadcnTheme.colors.primaryForeground,
                    selectedDayContainerColor = ShadcnTheme.colors.primary,
                    todayContentColor = ShadcnTheme.colors.primary,
                    todayDateBorderColor = ShadcnTheme.colors.primary
                )
            )
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
    ShadcnCard(modifier = modifier, contentPadding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(iconTint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(16.dp))
                }
            }
            Text(text = value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = subtext, color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AllocationItem(title: String, amount: Long, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = CurrencyFormatter.format(amount), color = ShadcnTheme.colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
