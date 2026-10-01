package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
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
import data.model.Category
import data.model.Transaction
import data.model.TransactionType
import data.repository.FinanceRepository
import ui.components.*
import ui.theme.ShadcnTheme
import util.ColorParser
import util.CurrencyFormatter

@Composable
fun TransactionsScreen(
    repository: FinanceRepository,
    onOpenAddTransaction: (TransactionType) -> Unit
) {
    val allTransactions by repository.transactions.collectAsState()
    val categories by repository.categories.collectAsState()
    val catMap = remember(categories) { categories.associateBy { it.id } }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Filter logic
    val filteredTransactions = remember(allTransactions, searchQuery, selectedTypeFilter, selectedCategoryFilter) {
        allTransactions.filter { tx ->
            val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter
            val matchesCat = selectedCategoryFilter == null || tx.categoryId == selectedCategoryFilter
            val catName = catMap[tx.categoryId]?.name ?: ""
            val matchesSearch = searchQuery.isBlank() ||
                    tx.note.contains(searchQuery, ignoreCase = true) ||
                    catName.contains(searchQuery, ignoreCase = true) ||
                    tx.dateString.contains(searchQuery, ignoreCase = true)
            matchesType && matchesCat && matchesSearch
        }
    }

    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
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
                    Text(
                        text = "거래 내역 (Transactions)",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "모든 수입과 지출 내역 검색 및 필터링",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 13.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShadcnButtonText(
                        text = "+ 지출 추가",
                        variant = ButtonVariant.DESTRUCTIVE,
                        onClick = { onOpenAddTransaction(TransactionType.EXPENSE) }
                    )
                    ShadcnButtonText(
                        text = "+ 수입 추가",
                        variant = ButtonVariant.PRIMARY,
                        onClick = { onOpenAddTransaction(TransactionType.INCOME) }
                    )
                }
            }
        }

        // Summary Stats of Filtered Results
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryPill(
                    label = "필터된 거래 건수",
                    value = "${filteredTransactions.size}건",
                    color = ShadcnTheme.colors.foreground,
                    modifier = Modifier.weight(1f)
                )
                SummaryPill(
                    label = "총 수입 (Inflow)",
                    value = CurrencyFormatter.format(totalIncome),
                    color = ShadcnTheme.colors.income,
                    modifier = Modifier.weight(1f)
                )
                SummaryPill(
                    label = "총 지출 (Outflow)",
                    value = CurrencyFormatter.format(totalExpense),
                    color = ShadcnTheme.colors.expense,
                    modifier = Modifier.weight(1f)
                )
                SummaryPill(
                    label = "순 잔액 (Net)",
                    value = CurrencyFormatter.format(totalIncome - totalExpense, includeSign = true),
                    color = if (totalIncome >= totalExpense) ShadcnTheme.colors.krwAccent else ShadcnTheme.colors.expense,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search Bar & Filter Controls
        item {
            ShadcnCard(contentPadding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Search Input
                    ShadcnInput(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "메모, 카테고리, 날짜 검색...",
                        prefix = "🔍"
                    )

                    // Type filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "구분:",
                            color = ShadcnTheme.colors.mutedForeground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        FilterChip(
                            text = "전체 (All)",
                            isSelected = selectedTypeFilter == null,
                            onClick = { selectedTypeFilter = null }
                        )

                        FilterChip(
                            text = "수입 (Income)",
                            isSelected = selectedTypeFilter == TransactionType.INCOME,
                            activeColor = ShadcnTheme.colors.income,
                            onClick = { selectedTypeFilter = TransactionType.INCOME }
                        )

                        FilterChip(
                            text = "지출 (Expense)",
                            isSelected = selectedTypeFilter == TransactionType.EXPENSE,
                            activeColor = ShadcnTheme.colors.expense,
                            onClick = { selectedTypeFilter = TransactionType.EXPENSE }
                        )
                    }

                    // Category filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                text = "모든 카테고리",
                                isSelected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null }
                            )
                        }

                        items(categories) { cat ->
                            val isSelected = cat.id == selectedCategoryFilter
                            val catColor = ColorParser.parse(cat.colorHex, ShadcnTheme.colors.primary)

                            FilterChip(
                                text = cat.name.split("(")[0].trim(),
                                isSelected = isSelected,
                                activeColor = catColor,
                                onClick = {
                                    selectedCategoryFilter = if (isSelected) null else cat.id
                                }
                            )
                        }
                    }
                }
            }
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            item {
                ShadcnCard(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "조건에 일치하는 거래 내역이 없습니다.",
                            color = ShadcnTheme.colors.mutedForeground,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                val cat = catMap[tx.categoryId]
                val catColor = ColorParser.parse(cat?.colorHex ?: "#6B7280", ShadcnTheme.colors.primary)

                ShadcnCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(catColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CategoryIcon(
                                    iconName = cat?.iconName ?: "Category",
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (tx.note.isNotBlank()) tx.note else cat?.name?.split("(")?.get(0)?.trim() ?: "거래",
                                        color = ShadcnTheme.colors.foreground,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    ShadcnBadge(
                                        text = tx.paymentMethod.label.split("(")[0].trim(),
                                        variant = BadgeVariant.SECONDARY
                                    )
                                }

                                Text(
                                    text = "${tx.dateString} • ${cat?.name?.split("(")?.get(0)?.trim() ?: "미지정"}",
                                    color = ShadcnTheme.colors.mutedForeground,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = CurrencyFormatter.format(tx.amount, includeSign = true),
                                color = if (tx.type == TransactionType.INCOME) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "삭제",
                                tint = ShadcnTheme.colors.mutedForeground,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { repository.deleteTransaction(tx.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    ShadcnCard(modifier = modifier, contentPadding = 10.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                color = ShadcnTheme.colors.mutedForeground,
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = ShadcnTheme.colors.primary
) {
    val shape = RoundedCornerShape(6.dp)
    val bgColor = if (isSelected) activeColor.copy(alpha = 0.2f) else ShadcnTheme.colors.secondary
    val borderColor = if (isSelected) activeColor else ShadcnTheme.colors.cardBorder
    val textColor = if (isSelected) activeColor else ShadcnTheme.colors.foreground

    Box(
        modifier = Modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
