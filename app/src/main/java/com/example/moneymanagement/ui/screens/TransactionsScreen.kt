package com.example.moneymanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.repository.FinanceRepository
import com.example.moneymanagement.ui.components.*
import com.example.moneymanagement.ui.theme.ShadcnTheme
import com.example.moneymanagement.util.ColorParser
import com.example.moneymanagement.util.CurrencyFormatter

@Composable
fun TransactionsScreen(
    repository: FinanceRepository,
    onOpenAddTransaction: (TransactionType) -> Unit
) {
    val allTransactions by repository.transactions.collectAsState()
    val categories by repository.categories.collectAsState()
    val accounts by repository.accounts.collectAsState()
    val catMap = remember(categories) { categories.associateBy { it.id } }
    val accMap = remember(accounts) { accounts.associateBy { it.id } }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedDateFilter by remember { mutableStateOf<String?>(null) }
    var showDateFilterPicker by remember { mutableStateOf(false) }

    val filteredTransactions = remember(allTransactions, searchQuery, selectedTypeFilter, selectedCategoryFilter, selectedDateFilter) {
        allTransactions.filter { tx ->
            val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter
            val matchesCat = selectedCategoryFilter == null || tx.categoryId == selectedCategoryFilter
            val matchesDate = selectedDateFilter == null || tx.dateString == selectedDateFilter
            val catName = catMap[tx.categoryId]?.name ?: ""
            val matchesSearch = searchQuery.isBlank() ||
                    tx.note.contains(searchQuery, ignoreCase = true) ||
                    catName.contains(searchQuery, ignoreCase = true) ||
                    tx.dateString.contains(searchQuery, ignoreCase = true)
            matchesType && matchesCat && matchesDate && matchesSearch
        }
    }

    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "Transactions",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Search & filter all income, expenses, and transfers",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 13.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShadcnButton(
                        onClick = { onOpenAddTransaction(TransactionType.EXPENSE) },
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.DESTRUCTIVE,
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(
                            text = "+ Expense",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ShadcnButton(
                        onClick = { onOpenAddTransaction(TransactionType.INCOME) },
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.PRIMARY,
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(
                            text = "+ Income",
                            color = ShadcnTheme.colors.primaryForeground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ShadcnButton(
                        onClick = { onOpenAddTransaction(TransactionType.TRANSFER) },
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.SECONDARY,
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(
                            text = "⇄ Transfer",
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Summary pills
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(label = "Count (${filteredTransactions.size})", value = CurrencyFormatter.format(totalIncome - totalExpense, includeSign = true), color = if (totalIncome >= totalExpense) ShadcnTheme.colors.income else ShadcnTheme.colors.expense, modifier = Modifier.weight(1f))
                Pill(label = "Income", value = CurrencyFormatter.format(totalIncome), color = ShadcnTheme.colors.income, modifier = Modifier.weight(1f))
                Pill(label = "Expense", value = CurrencyFormatter.format(totalExpense), color = ShadcnTheme.colors.expense, modifier = Modifier.weight(1f))
            }
        }

        // Search & Filters Card
        item {
            ShadcnCard(contentPadding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShadcnInput(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = "Search by note, category, date...", prefix = "🔍")

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item { Chip(text = "All Types", isSelected = selectedTypeFilter == null, onClick = { selectedTypeFilter = null }) }
                        item { Chip(text = "Income", isSelected = selectedTypeFilter == TransactionType.INCOME, activeColor = ShadcnTheme.colors.income, onClick = { selectedTypeFilter = TransactionType.INCOME }) }
                        item { Chip(text = "Expense", isSelected = selectedTypeFilter == TransactionType.EXPENSE, activeColor = ShadcnTheme.colors.expense, onClick = { selectedTypeFilter = TransactionType.EXPENSE }) }
                        item { Chip(text = "Transfer", isSelected = selectedTypeFilter == TransactionType.TRANSFER, activeColor = ShadcnTheme.colors.primary, onClick = { selectedTypeFilter = TransactionType.TRANSFER }) }
                        item {
                            Chip(
                                text = if (selectedDateFilter != null) "📅 $selectedDateFilter ✕" else "📅 Date",
                                isSelected = selectedDateFilter != null,
                                activeColor = ShadcnTheme.colors.primary,
                                onClick = {
                                    if (selectedDateFilter != null) {
                                        selectedDateFilter = null
                                    } else {
                                        showDateFilterPicker = true
                                    }
                                }
                            )
                        }
                    }

                    if (showDateFilterPicker) {
                        val initialMillis = remember(selectedDateFilter) {
                            try {
                                LocalDate.parse(selectedDateFilter ?: "").atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                            } catch (_: Exception) {
                                LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                            }
                        }
                        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

                        DatePickerDialog(
                            onDismissRequest = { showDateFilterPicker = false },
                            confirmButton = {
                                ShadcnButtonText(
                                    text = "Filter Date",
                                    variant = ButtonVariant.PRIMARY,
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                                            selectedDateFilter = picked.toString()
                                        }
                                        showDateFilterPicker = false
                                    }
                                )
                            },
                            dismissButton = {
                                ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = { showDateFilterPicker = false })
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

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { Chip(text = "All Categories", isSelected = selectedCategoryFilter == null, onClick = { selectedCategoryFilter = null }) }
                        items(categories) { cat ->
                            val isSelected = cat.id == selectedCategoryFilter
                            val catColor = ColorParser.parse(cat.colorHex, ShadcnTheme.colors.primary)
                            Chip(text = cat.name, isSelected = isSelected, activeColor = catColor, onClick = { selectedCategoryFilter = if (isSelected) null else cat.id })
                        }
                    }
                }
            }
        }

        // List
        if (filteredTransactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No matching transactions found.", color = ShadcnTheme.colors.mutedForeground, fontSize = 14.sp)
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                val cat = catMap[tx.categoryId]
                val catColor = ColorParser.parse(cat?.colorHex ?: "#6B7280", ShadcnTheme.colors.primary)

                ShadcnCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(catColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CategoryIcon(iconName = cat?.iconName ?: "Category", tint = catColor, modifier = Modifier.size(20.dp))
                            }

                            val acc = accMap[tx.accountId]
                            val toAcc = accMap[tx.toAccountId]
                            val isTransfer = tx.type == TransactionType.TRANSFER

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (tx.note.isNotBlank()) {
                                        tx.note
                                    } else if (isTransfer && acc != null && toAcc != null) {
                                        "${acc.name} ➔ ${toAcc.name}"
                                    } else {
                                        cat?.name ?: "Transaction"
                                    },
                                    color = ShadcnTheme.colors.foreground,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(text = tx.dateString, color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp, maxLines = 1)
                                    Text(text = "•", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                                    if (isTransfer && acc != null && toAcc != null) {
                                        Text(text = "Transfer", color = ShadcnTheme.colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "•", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                                        Text(text = "${acc.name} ➔ ${toAcc.name}", color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    } else {
                                        Text(text = cat?.name ?: "", color = catColor, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (acc != null || tx.paymentMethod.label.isNotBlank()) {
                                            Text(text = "•", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                                            Text(text = acc?.name ?: tx.paymentMethod.label, color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = CurrencyFormatter.formatTransaction(tx.amount, tx.type),
                                color = when (tx.type) {
                                    TransactionType.INCOME -> ShadcnTheme.colors.income
                                    TransactionType.EXPENSE -> ShadcnTheme.colors.expense
                                    TransactionType.TRANSFER -> ShadcnTheme.colors.primary
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = ShadcnTheme.colors.mutedForeground,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { repository.deleteTransaction(tx.id) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun Pill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    ShadcnCard(modifier = modifier, contentPadding = 10.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = label, color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
            Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun Chip(
    text: String,
    isSelected: Boolean,
    activeColor: Color = ShadcnTheme.colors.primary,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else ShadcnTheme.colors.secondary)
            .border(1.dp, if (isSelected) activeColor else ShadcnTheme.colors.cardBorder, shape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) activeColor else ShadcnTheme.colors.foreground,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
