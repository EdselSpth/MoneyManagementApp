package com.example.moneymanagement.data.repository

import android.content.Context
import com.example.moneymanagement.data.database.DatabaseHelper
import com.example.moneymanagement.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.moneymanagement.ui.theme.AppThemeMode
import java.time.LocalDate

@Serializable
data class BackupPayload(
    val exportedAt: Long,
    val categories: List<Category>,
    val transactions: List<Transaction>,
    val budgets: List<Budget>,
    val goals: List<SavingsGoal>,
    val accounts: List<Account> = emptyList()
)

class FinanceRepository(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val db = DatabaseHelper.getInstance(context)

    private val _themeMode = MutableStateFlow(
        runCatching { AppThemeMode.valueOf(db.getSetting("app_theme_mode", AppThemeMode.AETHER.name)) }
            .getOrDefault(AppThemeMode.AETHER)
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        scope.launch {
            db.setSetting("app_theme_mode", mode.name)
            _themeMode.value = mode
        }
    }

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

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

    private val _needsRatio = MutableStateFlow(50)
    val needsRatio: StateFlow<Int> = _needsRatio.asStateFlow()

    private val _wantsRatio = MutableStateFlow(30)
    val wantsRatio: StateFlow<Int> = _wantsRatio.asStateFlow()

    private val _savingsRatio = MutableStateFlow(20)
    val savingsRatio: StateFlow<Int> = _savingsRatio.asStateFlow()

    private val defaultIssuers = listOf(
        "Aether Genshin",
        "Kakao Bank",
        "Toss Bank",
        "Shinhan Card",
        "KB Kookmin",
        "Hyundai Card",
        "T-Money",
        "Cash Wallet",
        "Woori Bank",
        "Hana Card"
    )

    private val _issuers = MutableStateFlow<List<String>>(defaultIssuers)
    val issuers: StateFlow<List<String>> = _issuers.asStateFlow()

    private val _allocationPlan = MutableStateFlow(AllocationPlan(0L, 0L, 50.0, 0L, 30.0, 0L, 20.0))
    val allocationPlan: StateFlow<AllocationPlan> = _allocationPlan.asStateFlow()

    init {
        loadRatios()
        loadIssuers()
        refreshAll()
    }

    private fun loadIssuers() {
        val raw = db.getSetting("custom_issuers", "")
        if (raw.isNotBlank()) {
            val list = raw.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
            if (list.isNotEmpty()) {
                _issuers.value = list
                return
            }
        }
        _issuers.value = defaultIssuers
    }

    fun addIssuer(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        scope.launch {
            val current = _issuers.value.toMutableList()
            if (!current.any { it.equals(trimmed, ignoreCase = true) }) {
                current.add(trimmed)
                _issuers.value = current
                db.setSetting("custom_issuers", current.joinToString("|||"))
            }
        }
    }

    fun removeIssuer(name: String) {
        scope.launch {
            val current = _issuers.value.toMutableList()
            current.removeAll { it.equals(name, ignoreCase = true) }
            _issuers.value = current
            db.setSetting("custom_issuers", current.joinToString("|||"))
        }
    }

    fun resetIssuers() {
        scope.launch {
            _issuers.value = defaultIssuers
            db.setSetting("custom_issuers", defaultIssuers.joinToString("|||"))
        }
    }

    private fun loadRatios() {
        val n = db.getSetting("ratio_needs", "50").toIntOrNull() ?: 50
        val w = db.getSetting("ratio_wants", "30").toIntOrNull() ?: 30
        val s = db.getSetting("ratio_savings", "20").toIntOrNull() ?: 20
        _needsRatio.value = n
        _wantsRatio.value = w
        _savingsRatio.value = s
    }

    fun updateAllocationRatios(needs: Int, wants: Int, savings: Int) {
        scope.launch {
            db.setSetting("ratio_needs", needs.toString())
            db.setSetting("ratio_wants", wants.toString())
            db.setSetting("ratio_savings", savings.toString())
            _needsRatio.value = needs
            _wantsRatio.value = wants
            _savingsRatio.value = savings
            refreshMonthlyData()
        }
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
            loadRatios()
            loadIssuers()
            val cats = db.getCategories()
            _categories.value = cats

            val accs = db.getAllAccounts()
            _accounts.value = accs

            val allTx = db.getAllTransactions()
            _transactions.value = allTx

            val goals = db.getAllGoals()
            _savingsGoals.value = goals

            _totalBalance.value = accs.sumOf { it.balance }

            refreshMonthlyData()
        }
    }

    fun topUpAccount(accountId: String, amount: Long) {
        scope.launch {
            db.updateAccountBalance(accountId, amount)
            refreshAll()
        }
    }

    fun setAccountBalance(accountId: String, balance: Long) {
        scope.launch {
            db.setAccountBalance(accountId, balance)
            refreshAll()
        }
    }

    fun addAccount(account: Account) {
        scope.launch {
            db.insertAccount(account)
            refreshAll()
        }
    }

    fun updateAccount(account: Account) {
        scope.launch {
            db.updateAccount(account)
            refreshAll()
        }
    }

    fun deleteAccount(id: String) {
        scope.launch {
            db.deleteAccount(id)
            refreshAll()
        }
    }

    fun setDefaultAccount(id: String) {
        scope.launch {
            db.setDefaultAccount(id)
            refreshAll()
        }
    }

    private fun refreshMonthlyData() {
        val currentDate = _selectedDate.value
        val year = currentDate.year
        val month = currentDate.monthValue

        val mTx = db.getTransactionsByMonth(year, month)
        _monthlyTransactions.value = mTx

        var inc = 0L
        var exp = 0L
        val categorySpendMap = mutableMapOf<String, Long>()

        mTx.forEach { tx ->
            when (tx.type) {
                TransactionType.INCOME -> {
                    inc += tx.amount
                }
                TransactionType.EXPENSE -> {
                    exp += tx.amount
                    categorySpendMap[tx.categoryId] = (categorySpendMap[tx.categoryId] ?: 0L) + tx.amount
                }
                TransactionType.TRANSFER -> {
                    // Internal transfer: does not count towards external Income or Expense!
                }
            }
        }

        _monthlyIncome.value = inc
        _monthlyExpense.value = exp
        val net = inc - exp
        _netSavings.value = net
        _savingsRate.value = if (inc > 0) (net.coerceAtLeast(0).toDouble() / inc.toDouble()) * 100.0 else 0.0

        // Custom Smart Allocation according to user settings
        val nRatio = _needsRatio.value
        val wRatio = _wantsRatio.value
        val sRatio = _savingsRatio.value

        val needs = (inc * (nRatio / 100.0)).toLong()
        val wants = (inc * (wRatio / 100.0)).toLong()
        val savings = (inc * (sRatio / 100.0)).toLong()

        _allocationPlan.value = AllocationPlan(
            totalIncome = inc,
            needsAmount = needs,
            needsPercent = nRatio.toDouble(),
            wantsAmount = wants,
            wantsPercent = wRatio.toDouble(),
            savingsAmount = savings,
            savingsPercent = sRatio.toDouble()
        )

        // Budgets status
        val budgets = db.getBudgetsForMonth(year, month)
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
            db.insertTransaction(tx)
            refreshAll()
        }
    }

    fun deleteTransaction(id: String) {
        scope.launch {
            db.deleteTransaction(id)
            refreshAll()
        }
    }

    fun setBudgetLimit(categoryId: String, limit: Long) {
        scope.launch {
            val date = _selectedDate.value
            db.setBudgetLimit(categoryId, date.monthValue, date.year, limit)
            refreshMonthlyData()
        }
    }

    fun deleteBudget(id: String) {
        scope.launch {
            db.deleteBudget(id)
            refreshMonthlyData()
        }
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        scope.launch {
            db.insertGoal(goal)
            _savingsGoals.value = db.getAllGoals()
        }
    }

    fun addFundsToGoal(id: String, amount: Long) {
        scope.launch {
            db.updateGoalFunds(id, amount)
            _savingsGoals.value = db.getAllGoals()
        }
    }

    fun deleteGoal(id: String) {
        scope.launch {
            db.deleteGoal(id)
            _savingsGoals.value = db.getAllGoals()
        }
    }

    private val jsonSerializer = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun exportBackupJson(): String {
        val payload = BackupPayload(
            exportedAt = System.currentTimeMillis(),
            categories = db.getCategories(),
            transactions = db.getAllTransactions(),
            budgets = db.getBudgetsForMonth(_selectedDate.value.year, _selectedDate.value.monthValue),
            goals = db.getAllGoals(),
            accounts = db.getAllAccounts()
        )
        return jsonSerializer.encodeToString(payload)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val payload = jsonSerializer.decodeFromString<BackupPayload>(jsonString)
            payload.categories.forEach { db.insertCategory(it) }
            payload.accounts.forEach { db.insertAccount(it) }
            payload.transactions.forEach { db.insertTransaction(it) }
            payload.budgets.forEach { db.setBudgetLimit(it.categoryId, it.month, it.year, it.monthlyLimit) }
            payload.goals.forEach { db.insertGoal(it) }
            refreshAll()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
