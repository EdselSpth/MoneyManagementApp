package com.example.moneymanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.data.model.*
import com.example.moneymanagement.data.repository.FinanceRepository
import com.example.moneymanagement.ui.components.*
import com.example.moneymanagement.ui.screens.*
import com.example.moneymanagement.ui.theme.AppTheme
import com.example.moneymanagement.ui.theme.ShadcnTheme

enum class MobileNavScreen(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    TRANSACTIONS("Transactions", Icons.AutoMirrored.Filled.ReceiptLong),
    BUDGETS("Budgets", Icons.Default.PieChart),
    ANALYTICS("Analytics", Icons.Default.BarChart)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val repository = remember { FinanceRepository(context) }
            var currentScreen by remember { mutableStateOf(MobileNavScreen.HOME) }
            var isSettingsOpen by remember { mutableStateOf(false) }

            // Dialog states
            var isAddTransactionOpen by remember { mutableStateOf(false) }
            var addTransactionInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }

            var isSetBudgetOpen by remember { mutableStateOf(false) }
            var budgetInitialCategoryId by remember { mutableStateOf("") }
            var budgetInitialLimit by remember { mutableStateOf(0L) }

            var isAddGoalOpen by remember { mutableStateOf(false) }
            var depositGoalTarget by remember { mutableStateOf<SavingsGoal?>(null) }

            var isEditRatioOpen by remember { mutableStateOf(false) }
            var topUpTargetAccount by remember { mutableStateOf<Account?>(null) }

            var isAddAccountOpen by remember { mutableStateOf(false) }
            var isManageAccountsOpen by remember { mutableStateOf(false) }
            var editingAccount by remember { mutableStateOf<Account?>(null) }

            val categories by repository.categories.collectAsState()
            val accounts by repository.accounts.collectAsState()
            val needsRatio by repository.needsRatio.collectAsState()
            val wantsRatio by repository.wantsRatio.collectAsState()
            val savingsRatio by repository.savingsRatio.collectAsState()
            val themeMode by repository.themeMode.collectAsState()
            val issuers by repository.issuers.collectAsState()

            AppTheme(themeMode = themeMode) {
                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(ShadcnTheme.colors.background)
                    ) {
                        if (isSettingsOpen) {
                            SettingsScreen(
                                context = context,
                                repository = repository,
                                themeMode = themeMode,
                                onSelectThemeMode = { repository.setThemeMode(it) },
                                onOpenEditRatio = { isEditRatioOpen = true },
                                onAddNewCard = { isAddAccountOpen = true },
                                onManageCards = { isManageAccountsOpen = true },
                                onEditCard = { editingAccount = it },
                                onBack = { isSettingsOpen = false }
                            )
                        } else {
                            Crossfade(targetState = currentScreen) { screen ->
                                when (screen) {
                                    MobileNavScreen.HOME -> DashboardScreen(
                                        repository = repository,
                                        onOpenAddTransaction = { type ->
                                            addTransactionInitialType = type
                                            isAddTransactionOpen = true
                                        },
                                        onNavigateToBudgeting = { currentScreen = MobileNavScreen.BUDGETS },
                                        onNavigateToAnalytics = { currentScreen = MobileNavScreen.ANALYTICS },
                                        onNavigateToTransactions = { currentScreen = MobileNavScreen.TRANSACTIONS },
                                        onOpenSettings = { isSettingsOpen = true },
                                        onOpenEditRatio = { isEditRatioOpen = true },
                                        onTopUpAccount = { topUpTargetAccount = it },
                                        onAddNewCard = { isAddAccountOpen = true },
                                        onManageCards = { isManageAccountsOpen = true }
                                    )
                                    MobileNavScreen.TRANSACTIONS -> TransactionsScreen(
                                        repository = repository,
                                        onOpenAddTransaction = { type ->
                                            addTransactionInitialType = type
                                            isAddTransactionOpen = true
                                        }
                                    )
                                    MobileNavScreen.BUDGETS -> BudgetingScreen(
                                        repository = repository,
                                        onOpenSetBudgetDialog = { catId, limit ->
                                            budgetInitialCategoryId = catId
                                            budgetInitialLimit = limit
                                            isSetBudgetOpen = true
                                        },
                                        onOpenEditRatio = { isEditRatioOpen = true },
                                        onOpenAddGoal = { isAddGoalOpen = true },
                                        onOpenDeposit = { goal -> depositGoalTarget = goal }
                                    )
                                    MobileNavScreen.ANALYTICS -> AnalyticsScreen(
                                        repository = repository,
                                        onOpenSettings = { isSettingsOpen = true }
                                    )
                                }
                            }
                        }

                        // Apple-Style Floating Dock Island
                        if (!isSettingsOpen) {
                            AppleFloatingNavBar(
                                currentScreen = currentScreen,
                                onScreenSelected = { screen ->
                                    isSettingsOpen = false
                                    currentScreen = screen
                                },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }

                        // Dialogs
                        AddTransactionDialog(
                            isOpen = isAddTransactionOpen,
                            categories = categories,
                            accounts = accounts,
                            initialType = addTransactionInitialType,
                            onDismiss = { isAddTransactionOpen = false },
                            onSave = { newTx -> repository.addTransaction(newTx) }
                        )

                        TopUpAccountDialog(
                            account = topUpTargetAccount,
                            onDismiss = { topUpTargetAccount = null },
                            onConfirm = { amount ->
                                topUpTargetAccount?.let { acc ->
                                    repository.topUpAccount(acc.id, amount)
                                }
                            }
                        )

                        SetBudgetDialog(
                            isOpen = isSetBudgetOpen,
                            categories = categories,
                            initialCategoryId = budgetInitialCategoryId,
                            initialLimit = budgetInitialLimit,
                            onDismiss = { isSetBudgetOpen = false },
                            onSave = { catId, limit -> repository.setBudgetLimit(catId, limit) }
                        )

                        AddSavingsGoalDialog(
                            isOpen = isAddGoalOpen,
                            onDismiss = { isAddGoalOpen = false },
                            onSave = { goal -> repository.addSavingsGoal(goal) }
                        )

                        AddGoalFundsDialog(
                            goal = depositGoalTarget,
                            onDismiss = { depositGoalTarget = null },
                            onConfirm = { amount ->
                                depositGoalTarget?.let { goal -> repository.addFundsToGoal(goal.id, amount) }
                            }
                        )

                        EditAllocationRatioDialog(
                            isOpen = isEditRatioOpen,
                            currentNeeds = needsRatio,
                            currentWants = wantsRatio,
                            currentSavings = savingsRatio,
                            onDismiss = { isEditRatioOpen = false },
                            onSave = { needs, wants, savings ->
                                repository.updateAllocationRatios(needs, wants, savings)
                            }
                        )

                        AddAccountDialog(
                            isOpen = isAddAccountOpen,
                            issuers = issuers,
                            onDismiss = { isAddAccountOpen = false },
                            onSave = { newAcc -> repository.addAccount(newAcc) }
                        )

                        EditAccountDialog(
                            account = editingAccount,
                            issuers = issuers,
                            onDismiss = { editingAccount = null },
                            onSave = { updatedAcc -> repository.updateAccount(updatedAcc) },
                            onDelete = { id -> repository.deleteAccount(id) },
                            canDelete = accounts.size > 1
                        )

                        ManageAccountsDialog(
                            isOpen = isManageAccountsOpen,
                            accounts = accounts,
                            onDismiss = { isManageAccountsOpen = false },
                            onAddNew = { isAddAccountOpen = true },
                            onEdit = { acc -> editingAccount = acc },
                            onSetDefault = { id -> repository.setDefaultAccount(id) },
                            onDelete = { id -> repository.deleteAccount(id) }
                        )
                    }
                }
            }
        }
    }
}
