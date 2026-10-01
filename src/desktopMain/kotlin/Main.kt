import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.model.SavingsGoal
import data.model.TransactionType
import data.repository.FinanceRepository
import ui.components.*
import ui.screens.*
import ui.theme.AppTheme
import ui.theme.ShadcnTheme
import util.CurrencyFormatter

enum class AppNavScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("대시보드 (Overview)", Icons.Default.Dashboard),
    TRANSACTIONS("거래 내역 (Transactions)", Icons.AutoMirrored.Filled.ReceiptLong),
    BUDGETING("예산 분배 (Budgeting)", Icons.Default.PieChart),
    ANALYTICS("차트 분석 (Analytics)", Icons.Default.BarChart),
    SAVINGS("저축 목표 (Goals)", Icons.Default.Savings),
    SETTINGS("설정 & 백업 (Settings)", Icons.Default.Settings)
}

fun main() {
    println(">>> Starting MoneyManagementApp main()...")
    try {
        application {
            println(">>> Inside application block...")
            val windowState = rememberWindowState(
                position = androidx.compose.ui.window.WindowPosition(Alignment.Center),
                size = androidx.compose.ui.unit.DpSize(1080.dp, 720.dp)
            )

            Window(
                onCloseRequest = ::exitApplication,
                state = windowState,
                title = "₩ Korean Won Money Management - 스마트 가계부 & 예산 관리"
            ) {
                println(">>> 1. Composing Window content...")
                val repository = remember { FinanceRepository() }
                println(">>> 2. Repository initialized...")
                var currentScreen by remember { mutableStateOf(AppNavScreen.DASHBOARD) }
                var isDarkTheme by remember { mutableStateOf(true) }

                // Dialog states
                var isAddTransactionOpen by remember { mutableStateOf(false) }
                var addTransactionInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }

                var isSetBudgetOpen by remember { mutableStateOf(false) }
                var budgetInitialCategoryId by remember { mutableStateOf("") }
                var budgetInitialLimit by remember { mutableStateOf(0L) }

                var isAddGoalOpen by remember { mutableStateOf(false) }
                var depositGoalTarget by remember { mutableStateOf<SavingsGoal?>(null) }

                println(">>> 3. Collecting states...")
                val categories by repository.categories.collectAsState()
                val totalBalance by repository.totalBalance.collectAsState()

                println(">>> 4. Entering AppTheme...")
                AppTheme(darkTheme = isDarkTheme) {
                    println(">>> 5. Inside AppTheme...")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ShadcnTheme.colors.background)
                    ) {
                        println(">>> 6. Inside Box...")
                        Row(modifier = Modifier.fillMaxSize()) {
                    // Left Sidebar (Shadcn style)
                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .background(ShadcnTheme.colors.card)
                            .border(width = 1.dp, color = ShadcnTheme.colors.cardBorder)
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            // App Brand Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ShadcnTheme.colors.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "₩",
                                        color = ShadcnTheme.colors.primary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Text(
                                        text = "WonManager",
                                        color = ShadcnTheme.colors.foreground,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "스마트 가계부 (KRW)",
                                        color = ShadcnTheme.colors.mutedForeground,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Quick Add Button
                            ShadcnButton(
                                onClick = {
                                    addTransactionInitialType = TransactionType.EXPENSE
                                    isAddTransactionOpen = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                variant = ButtonVariant.PRIMARY,
                                contentPadding = PaddingValues(vertical = 11.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = ShadcnTheme.colors.primaryForeground, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "빠른 거래 등록",
                                    color = ShadcnTheme.colors.primaryForeground,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Navigation Items
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                AppNavScreen.entries.forEach { screen ->
                                    val isSelected = currentScreen == screen
                                    val navBg = if (isSelected) ShadcnTheme.colors.secondary else Color.Transparent
                                    val navText = if (isSelected) ShadcnTheme.colors.foreground else ShadcnTheme.colors.mutedForeground
                                    val navIcon = if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.mutedForeground

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(navBg)
                                            .clickable { currentScreen = screen }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            tint = navIcon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = screen.title,
                                            color = navText,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Balance Widget in Sidebar
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShadcnTheme.colors.muted)
                                    .border(1.dp, ShadcnTheme.colors.cardBorder, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "현재 총 잔액 (Balance)",
                                        color = ShadcnTheme.colors.mutedForeground,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(totalBalance),
                                        color = ShadcnTheme.colors.foreground,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "SQLite 100% Offline • ₩ KRW",
                                color = ShadcnTheme.colors.mutedForeground.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    // Main Content Area
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Crossfade(targetState = currentScreen) { screen ->
                            when (screen) {
                                AppNavScreen.DASHBOARD -> DashboardScreen(
                                    repository = repository,
                                    onOpenAddTransaction = { type ->
                                        addTransactionInitialType = type
                                        isAddTransactionOpen = true
                                    },
                                    onNavigateToBudgeting = { currentScreen = AppNavScreen.BUDGETING },
                                    onNavigateToAnalytics = { currentScreen = AppNavScreen.ANALYTICS },
                                    onNavigateToTransactions = { currentScreen = AppNavScreen.TRANSACTIONS }
                                )
                                AppNavScreen.TRANSACTIONS -> TransactionsScreen(
                                    repository = repository,
                                    onOpenAddTransaction = { type ->
                                        addTransactionInitialType = type
                                        isAddTransactionOpen = true
                                    }
                                )
                                AppNavScreen.BUDGETING -> BudgetingScreen(
                                    repository = repository,
                                    onOpenSetBudgetDialog = { catId, limit ->
                                        budgetInitialCategoryId = catId
                                        budgetInitialLimit = limit
                                        isSetBudgetOpen = true
                                    }
                                )
                                AppNavScreen.ANALYTICS -> AnalyticsScreen(repository = repository)
                                AppNavScreen.SAVINGS -> SavingsGoalsScreen(
                                    repository = repository,
                                    onOpenAddGoal = { isAddGoalOpen = true },
                                    onOpenDeposit = { goal -> depositGoalTarget = goal }
                                )
                                AppNavScreen.SETTINGS -> SettingsScreen(
                                    repository = repository,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = { isDarkTheme = !isDarkTheme }
                                )
                            }
                        }
                    }
                }

                // Global Modals / Dialogs
                AddTransactionDialog(
                    isOpen = isAddTransactionOpen,
                    categories = categories,
                    initialType = addTransactionInitialType,
                    onDismiss = { isAddTransactionOpen = false },
                    onSave = { newTx ->
                        repository.addTransaction(newTx)
                    }
                )

                SetBudgetDialog(
                    isOpen = isSetBudgetOpen,
                    categories = categories,
                    initialCategoryId = budgetInitialCategoryId,
                    initialLimit = budgetInitialLimit,
                    onDismiss = { isSetBudgetOpen = false },
                    onSave = { catId, limit ->
                        repository.setBudgetLimit(catId, limit)
                    }
                )

                AddSavingsGoalDialog(
                    isOpen = isAddGoalOpen,
                    onDismiss = { isAddGoalOpen = false },
                    onSave = { goal ->
                        repository.addSavingsGoal(goal)
                    }
                )

                AddGoalFundsDialog(
                    goal = depositGoalTarget,
                    onDismiss = { depositGoalTarget = null },
                    onConfirm = { amount ->
                        depositGoalTarget?.let { goal ->
                            repository.addFundsToGoal(goal.id, amount)
                        }
                    }
                )
            } // Box
        } // AppTheme
    } // Window
} // application
} catch (e: Throwable) {
    println(">>> CRITICAL ERROR in MoneyManagementApp: ${e.message}")
    e.printStackTrace()
}
}
