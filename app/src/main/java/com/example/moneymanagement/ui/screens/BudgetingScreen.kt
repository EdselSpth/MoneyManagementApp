package com.example.moneymanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.moneymanagement.data.model.BudgetGroup
import com.example.moneymanagement.data.model.BudgetStatus
import com.example.moneymanagement.data.model.SavingsGoal
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.repository.FinanceRepository
import com.example.moneymanagement.ui.components.*
import com.example.moneymanagement.ui.theme.ShadcnTheme
import com.example.moneymanagement.util.ColorParser
import com.example.moneymanagement.util.CurrencyFormatter

enum class BudgetTab { ALLOCATION_AND_LIMITS, SAVINGS_GOALS }

@Composable
fun BudgetingScreen(
    repository: FinanceRepository,
    onOpenSetBudgetDialog: (initialCategoryId: String, initialLimit: Long) -> Unit,
    onOpenEditRatio: () -> Unit,
    onOpenAddGoal: () -> Unit,
    onOpenDeposit: (SavingsGoal) -> Unit
) {
    var activeTab by remember { mutableStateOf(BudgetTab.ALLOCATION_AND_LIMITS) }

    val monthlyIncome by repository.monthlyIncome.collectAsState()
    val allocationPlan by repository.allocationPlan.collectAsState()
    val needsRatio by repository.needsRatio.collectAsState()
    val wantsRatio by repository.wantsRatio.collectAsState()
    val savingsRatio by repository.savingsRatio.collectAsState()
    val budgetStatuses by repository.budgetStatuses.collectAsState()
    val monthlyTransactions by repository.monthlyTransactions.collectAsState()
    val categories by repository.categories.collectAsState()
    val savingsGoals by repository.savingsGoals.collectAsState()

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
                    Text(text = "Budgets & Goals", color = ShadcnTheme.colors.foreground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Allocation ratios, category limits & savings", color = ShadcnTheme.colors.mutedForeground, fontSize = 13.sp)
                }

                if (activeTab == BudgetTab.ALLOCATION_AND_LIMITS) {
                    ShadcnButtonText(text = "+ Set Limit", variant = ButtonVariant.PRIMARY, onClick = { onOpenSetBudgetDialog("", 0L) })
                } else {
                    ShadcnButtonText(text = "+ New Goal", variant = ButtonVariant.PRIMARY, onClick = onOpenAddGoal)
                }
            }
        }

        // Sub-Tab Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ShadcnTheme.colors.muted)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isBudgets = activeTab == BudgetTab.ALLOCATION_AND_LIMITS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBudgets) ShadcnTheme.colors.card else Color.Transparent)
                        .clickable { activeTab = BudgetTab.ALLOCATION_AND_LIMITS }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Budgets & Ratio",
                        color = if (isBudgets) ShadcnTheme.colors.foreground else ShadcnTheme.colors.mutedForeground,
                        fontSize = 14.sp,
                        fontWeight = if (isBudgets) FontWeight.Bold else FontWeight.Medium
                    )
                }

                val isGoals = activeTab == BudgetTab.SAVINGS_GOALS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isGoals) ShadcnTheme.colors.card else Color.Transparent)
                        .clickable { activeTab = BudgetTab.SAVINGS_GOALS }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Savings Goals (${savingsGoals.size})",
                        color = if (isGoals) ShadcnTheme.colors.foreground else ShadcnTheme.colors.mutedForeground,
                        fontSize = 14.sp,
                        fontWeight = if (isGoals) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (activeTab == BudgetTab.ALLOCATION_AND_LIMITS) {
            // Smart Allocation Ratio Section
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
                                    text = "Smart Income Allocation",
                                    color = ShadcnTheme.colors.foreground,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Current Ratio: $needsRatio% Needs / $wantsRatio% Wants / $savingsRatio% Savings",
                                    color = ShadcnTheme.colors.mutedForeground,
                                    fontSize = 12.sp
                                )
                            }

                            ShadcnButtonText(text = "Change Ratio", variant = ButtonVariant.OUTLINE, onClick = onOpenEditRatio)
                        }

                        Text(
                            text = "Calculated on current month income: ${CurrencyFormatter.format(monthlyIncome)}",
                            color = ShadcnTheme.colors.krwAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        AllocationCard(
                            title = "Needs ($needsRatio%)",
                            recommended = allocationPlan.needsAmount,
                            actual = actualNeedsSpent,
                            color = Color(0xFFF59E0B),
                            desc = "Rent, groceries, utilities, transit"
                        )
                        AllocationCard(
                            title = "Wants ($wantsRatio%)",
                            recommended = allocationPlan.wantsAmount,
                            actual = actualWantsSpent,
                            color = Color(0xFFA855F7),
                            desc = "Dining out, shopping, hobbies, cafes"
                        )
                        AllocationCard(
                            title = "Savings ($savingsRatio%)",
                            recommended = allocationPlan.savingsAmount,
                            actual = actualSavingsSpent,
                            color = Color(0xFF10B981),
                            desc = "Emergency funds, investments, deposits"
                        )
                    }
                }
            }

            // Category Budgets Header
            item {
                Text(
                    text = "Category Monthly Limits (${budgetStatuses.size})",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (budgetStatuses.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No category limits set yet.", color = ShadcnTheme.colors.mutedForeground, fontSize = 14.sp)
                    }
                }
            } else {
                items(budgetStatuses) { status ->
                    val catColor = ColorParser.parse(status.category.colorHex, ShadcnTheme.colors.primary)

                    ShadcnCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    CategoryIcon(iconName = status.category.iconName, tint = catColor, modifier = Modifier.size(18.dp))
                                    Text(text = status.category.name, color = ShadcnTheme.colors.foreground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    when {
                                        status.percentage >= 1.0f -> ShadcnBadge(text = "Exceeded", variant = BadgeVariant.DESTRUCTIVE)
                                        status.percentage >= 0.8f -> ShadcnBadge(text = "Warning", variant = BadgeVariant.WARNING)
                                        else -> ShadcnBadge(text = "On Track", variant = BadgeVariant.SUCCESS)
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = ShadcnTheme.colors.mutedForeground,
                                        modifier = Modifier.size(18.dp).clickable { onOpenSetBudgetDialog(status.category.id, status.budget.monthlyLimit) }
                                    )
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = ShadcnTheme.colors.mutedForeground,
                                        modifier = Modifier.size(18.dp).clickable { repository.deleteBudget(status.budget.id) }
                                    )
                                }
                            }

                            ShadcnProgress(progress = status.percentage, height = 9.dp)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "${CurrencyFormatter.format(status.spent)} of ${CurrencyFormatter.format(status.budget.monthlyLimit)}",
                                    color = ShadcnTheme.colors.mutedForeground,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (status.remaining >= 0) "Remaining: ${CurrencyFormatter.format(status.remaining)}" else "Over: ${CurrencyFormatter.format(-status.remaining)}",
                                    color = if (status.remaining >= 0) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Savings Goals Tab
            if (savingsGoals.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No savings goals created yet.", color = ShadcnTheme.colors.mutedForeground, fontSize = 14.sp)
                    }
                }
            } else {
                items(savingsGoals, key = { it.id }) { goal ->
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
                                        Text(text = goal.name, color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        if (isCompleted) ShadcnBadge(text = "Completed 🎉", variant = BadgeVariant.SUCCESS)
                                    }
                                    if (goal.note.isNotBlank()) {
                                        Text(text = goal.note, color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ShadcnButtonText(text = "+ Deposit", variant = ButtonVariant.OUTLINE, onClick = { onOpenDeposit(goal) })
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = ShadcnTheme.colors.mutedForeground,
                                        modifier = Modifier.size(20.dp).clickable { repository.deleteGoal(goal.id) }
                                    )
                                }
                            }

                            ShadcnProgress(progress = progress, height = 9.dp, colorOverride = if (isCompleted) ShadcnTheme.colors.income else ShadcnTheme.colors.krwAccent)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Saved: ${CurrencyFormatter.format(goal.currentAmount)} (${(progress * 100).toInt()}%)",
                                    color = if (isCompleted) ShadcnTheme.colors.income else ShadcnTheme.colors.foreground,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0L)
                                Text(
                                    text = if (isCompleted) "Goal reached!" else "Remaining: ${CurrencyFormatter.format(remaining)}",
                                    color = ShadcnTheme.colors.mutedForeground,
                                    fontSize = 12.sp
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

@Composable
private fun AllocationCard(
    title: String,
    recommended: Long,
    actual: Long,
    color: Color,
    desc: String
) {
    val progress = if (recommended > 0) (actual.toFloat() / recommended.toFloat()) else 0f

    ShadcnCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = title, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = desc, color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
                }

                Text(
                    text = "${CurrencyFormatter.format(actual)} / ${CurrencyFormatter.format(recommended)}",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            ShadcnProgress(progress = progress, height = 7.dp, colorOverride = color)
        }
    }
}
