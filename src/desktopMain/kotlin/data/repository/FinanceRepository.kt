package data.repository

import data.database.DatabaseManager
import data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Serializable
data class BackupPayload(
    val exportedAt: Long,
    val categories: List<Category>,
    val transactions: List<Transaction>,
    val budgets: List<Budget>,
    val goals: List<SavingsGoal>
)

class FinanceRepository(private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _monthlyTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val monthlyTransactions: StateFlow<List<Transaction>> = _monthlyTransactions.asStateFlow()

    private val _budgetStatuses = MutableStateFlow<List<BudgetStatus>>(emptyList())
    val budgetStatuses: StateFlow<List<BudgetStatus>> = _budgetStatuses.asStateFlow()

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals.asStateFlow()

    private val _totalBalance = MutableStateFlow(0L)
    val totalBalance: StateFlow<Long> = _totalBalance.asStateFlow()

    private val _monthlyIncome = MutableStateFlow(0L)
    val monthlyIncome: StateFlow<Long> = _monthlyIncome.asStateFlow()

    private val _monthlyExpense = MutableStateFlow(0L)
    val monthlyExpense: StateFlow<Long> = _monthlyExpense.asStateFlow()

    private val _netSavings = MutableStateFlow(0L)
    val netSavings: StateFlow<Long> = _netSavings.asStateFlow()

    private val _savingsRate = MutableStateFlow(0.0)
    val savingsRate: StateFlow<Double> = _savingsRate.asStateFlow()

    private val _allocationPlan = MutableStateFlow(AllocationPlan(0L, 0L, 50.0, 0L, 30.0, 0L, 20.0))
    val allocationPlan: StateFlow<AllocationPlan> = _allocationPlan.asStateFlow()

    init {
        refreshAll()
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        refreshMonthlyData()
    }

    fun nextMonth() {
        _selectedDate.value = _selectedDate.value.plusMonths(1)
        refreshMonthlyData()
    }

    fun previousMonth() {
        _selectedDate.value = _selectedDate.value.minusMonths(1)
        refreshMonthlyData()
    }

    fun refreshAll() {
        scope.launch {
            val cats = DatabaseManager.getCategories()
            _categories.value = cats

            val allTx = DatabaseManager.getAllTransactions()
            _transactions.value = allTx

            val goals = DatabaseManager.getAllGoals()
            _savingsGoals.value = goals

            // Compute overall total balance across all time
            var balance = 0L
            allTx.forEach { tx ->
                if (tx.type == TransactionType.INCOME) balance += tx.amount
                else balance -= tx.amount
            }
            _totalBalance.value = balance

            refreshMonthlyData()
        }
    }

    private fun refreshMonthlyData() {
        val currentDate = _selectedDate.value
        val year = currentDate.year
        val month = currentDate.monthValue

        val mTx = DatabaseManager.getTransactionsByMonth(year, month)
        _monthlyTransactions.value = mTx

        var inc = 0L
        var exp = 0L
        val categorySpendMap = mutableMapOf<String, Long>()

        mTx.forEach { tx ->
            if (tx.type == TransactionType.INCOME) {
                inc += tx.amount
            } else {
                exp += tx.amount
                categorySpendMap[tx.categoryId] = (categorySpendMap[tx.categoryId] ?: 0L) + tx.amount
            }
        }

        _monthlyIncome.value = inc
        _monthlyExpense.value = exp
        val net = inc - exp
        _netSavings.value = net
        _savingsRate.value = if (inc > 0) (net.coerceAtLeast(0).toDouble() / inc.toDouble()) * 100.0 else 0.0

        // 50/30/20 Smart Allocation based on current month's income
        val needs = (inc * 0.50).toLong()
        val wants = (inc * 0.30).toLong()
        val savings = (inc * 0.20).toLong()
        _allocationPlan.value = AllocationPlan(
            totalIncome = inc,
            needsAmount = needs,
            needsPercent = 50.0,
            wantsAmount = wants,
            wantsPercent = 30.0,
            savingsAmount = savings,
            savingsPercent = 20.0
        )

        // Budgets status
        val budgets = DatabaseManager.getBudgetsForMonth(year, month)
        val catMap = _categories.value.associateBy { it.id }

        val statuses = budgets.mapNotNull { b ->
            val cat = catMap[b.categoryId] ?: return@mapNotNull null
            val spent = categorySpendMap[b.categoryId] ?: 0L
            val remaining = b.monthlyLimit - spent
            val percentage = if (b.monthlyLimit > 0) (spent.toFloat() / b.monthlyLimit.toFloat()) else 0f
            BudgetStatus(
                budget = b,
                category = cat,
                spent = spent,
                remaining = remaining,
                percentage = percentage
            )
        }
        _budgetStatuses.value = statuses
    }

    fun addTransaction(tx: Transaction) {
        scope.launch {
            DatabaseManager.insertTransaction(tx)
            refreshAll()
        }
    }

    fun deleteTransaction(id: String) {
        scope.launch {
            DatabaseManager.deleteTransaction(id)
            refreshAll()
        }
    }

    fun setBudgetLimit(categoryId: String, limit: Long) {
        scope.launch {
            val date = _selectedDate.value
            DatabaseManager.upsertBudget(categoryId, date.monthValue, date.year, limit)
            refreshMonthlyData()
        }
    }

    fun deleteBudget(id: String) {
        scope.launch {
            DatabaseManager.deleteBudget(id)
            refreshMonthlyData()
        }
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        scope.launch {
            DatabaseManager.insertGoal(goal)
            _savingsGoals.value = DatabaseManager.getAllGoals()
        }
    }

    fun addFundsToGoal(id: String, amount: Long) {
        scope.launch {
            DatabaseManager.updateGoalFunds(id, amount)
            _savingsGoals.value = DatabaseManager.getAllGoals()
        }
    }

    fun deleteGoal(id: String) {
        scope.launch {
            DatabaseManager.deleteGoal(id)
            _savingsGoals.value = DatabaseManager.getAllGoals()
        }
    }

    private val jsonSerializer = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun exportBackupJson(): String {
        val payload = BackupPayload(
            exportedAt = System.currentTimeMillis(),
            categories = DatabaseManager.getCategories(),
            transactions = DatabaseManager.getAllTransactions(),
            budgets = DatabaseManager.getBudgetsForMonth(_selectedDate.value.year, _selectedDate.value.monthValue),
            goals = DatabaseManager.getAllGoals()
        )
        return jsonSerializer.encodeToString(payload)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val payload = jsonSerializer.decodeFromString<BackupPayload>(jsonString)
            payload.categories.forEach { DatabaseManager.insertCategory(it) }
            payload.transactions.forEach { DatabaseManager.insertTransaction(it) }
            payload.budgets.forEach { DatabaseManager.upsertBudget(it.categoryId, it.month, it.year, it.monthlyLimit) }
            payload.goals.forEach { DatabaseManager.insertGoal(it) }
            refreshAll()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
