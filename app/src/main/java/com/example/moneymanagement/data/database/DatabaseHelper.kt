package com.example.moneymanagement.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.moneymanagement.data.model.*
import java.time.LocalDate
import java.util.UUID

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "moneymanagement.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE categories (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                icon_name TEXT NOT NULL,
                color_hex TEXT NOT NULL,
                type TEXT NOT NULL,
                budget_group TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS accounts (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                balance INTEGER NOT NULL,
                color_start_hex TEXT NOT NULL,
                color_end_hex TEXT NOT NULL,
                last_four TEXT NOT NULL,
                issuer TEXT NOT NULL,
                is_default INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE transactions (
                id TEXT PRIMARY KEY,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                category_id TEXT NOT NULL,
                date_string TEXT NOT NULL,
                note TEXT,
                payment_method TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                account_id TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE budgets (
                id TEXT PRIMARY KEY,
                category_id TEXT NOT NULL,
                month INTEGER NOT NULL,
                year INTEGER NOT NULL,
                monthly_limit INTEGER NOT NULL,
                UNIQUE(category_id, month, year)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE savings_goals (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                target_amount INTEGER NOT NULL,
                current_amount INTEGER NOT NULL,
                target_date_string TEXT NOT NULL,
                note TEXT,
                icon_name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS app_settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """.trimIndent())

        seedDefaultData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS app_settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """.trimIndent())
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Ensure app_settings and accounts tables exist
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS app_settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS accounts (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                balance INTEGER NOT NULL,
                color_start_hex TEXT NOT NULL,
                color_end_hex TEXT NOT NULL,
                last_four TEXT NOT NULL,
                issuer TEXT NOT NULL,
                is_default INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Ensure transactions table has account_id column
        try {
            db.execSQL("ALTER TABLE transactions ADD COLUMN account_id TEXT")
        } catch (_: Exception) {
            // Already exists
        }

        ensureDefaultAccounts(db)
        migrateCategoriesToEnglish(db)

        // Ensure sample_data_initialized flag is set so deleted transactions never resurrect
        setSettingInternal(db, "sample_data_initialized", "1")

        // Backfill account_id for legacy transactions if null
        try {
            db.execSQL("UPDATE transactions SET account_id = 'acc_tmoney' WHERE category_id = 'exp_transport' AND account_id IS NULL")
            db.execSQL("UPDATE transactions SET account_id = 'acc_shinhan' WHERE (category_id = 'exp_housing' OR category_id = 'inc_salary') AND account_id IS NULL")
            db.execSQL("UPDATE transactions SET account_id = 'acc_toss' WHERE account_id IS NULL")
        } catch (_: Exception) {}
    }

    private fun ensureDefaultAccounts(db: SQLiteDatabase) {
        val isAccountsInit = getSettingInternal(db, "accounts_initialized", "0") == "1"
        if (isAccountsInit) return

        val cursor = db.rawQuery("SELECT COUNT(*) FROM accounts", null)
        var count = 0
        cursor.use {
            if (it.moveToFirst()) count = it.getInt(0)
        }
        if (count == 0) {
            insertAccount(db, Account("acc_toss", "Toss Bank Card", AccountType.FINTECH, 1420000L, "#0064FF", "#0038A8", "1084", "Toss Bank", true))
            insertAccount(db, Account("acc_tmoney", "T-Money Transit", AccountType.TRANSIT, 35000L, "#06B6D4", "#0891B2", "9021", "T-Money", false))
            insertAccount(db, Account("acc_shinhan", "Shinhan Debit", AccountType.DEBIT_CARD, 1650000L, "#1E3A8A", "#0F172A", "5532", "Shinhan Card", false))
            insertAccount(db, Account("acc_cash", "Pocket Cash", AccountType.CASH, 71000L, "#10B981", "#047857", "CASH", "Cash Wallet", false))
            setSettingInternal(db, "accounts_initialized", "1")
        }
    }

    private fun migrateCategoriesToEnglish(db: SQLiteDatabase) {
        val englishCategoryMap = mapOf(
            "inc_salary" to "Salary",
            "inc_freelance" to "Freelance & Gig",
            "inc_investment" to "Investments",
            "inc_allowance" to "Allowance & Bonus",
            "exp_food" to "Food & Groceries",
            "exp_housing" to "Housing & Rent",
            "exp_transport" to "Transportation",
            "exp_bills" to "Bills & Utilities",
            "exp_health" to "Healthcare",
            "exp_cafe" to "Coffee & Desserts",
            "exp_shopping" to "Shopping & Retail",
            "exp_entertainment" to "Entertainment",
            "exp_hobby" to "Hobbies & Leisure",
            "exp_savings" to "Savings Deposit"
        )
        englishCategoryMap.forEach { (id, name) ->
            db.execSQL("UPDATE categories SET name = ? WHERE id = ?", arrayOf(name, id))
        }

        // Migrate sample goals if legacy
        db.execSQL("UPDATE savings_goals SET name = 'Jeju Island Trip', note = 'Flight & accommodation' WHERE name LIKE '%제주%'")
        db.execSQL("UPDATE savings_goals SET name = '3-Month Emergency Fund', note = 'Living expense reserve' WHERE name LIKE '%비상금%'")

        // Migrate sample transactions if legacy
        db.execSQL("UPDATE transactions SET note = 'Monthly Salary' WHERE note LIKE '%급여%'")
        db.execSQL("UPDATE transactions SET note = 'UI Design Freelance' WHERE note LIKE '%디자인%'")
        db.execSQL("UPDATE transactions SET note = 'Monthly Rent & Maintenance' WHERE note LIKE '%월세%'")
        db.execSQL("UPDATE transactions SET note = 'E-Mart Grocery' WHERE note LIKE '%이마트%'")
        db.execSQL("UPDATE transactions SET note = 'Lunch Dining' WHERE note LIKE '%김치찌개%'")
        db.execSQL("UPDATE transactions SET note = 'Starbucks Coffee' WHERE note LIKE '%스타벅스%'")
        db.execSQL("UPDATE transactions SET note = 'Subway Transit Pass' WHERE note LIKE '%기후동행카드%'")
    }

    private fun seedDefaultData(db: SQLiteDatabase) {
        val defaultCategories = listOf(
            Category("inc_salary", "Salary", "Work", "#10B981", TransactionType.INCOME, BudgetGroup.INCOME),
            Category("inc_freelance", "Freelance & Gig", "Laptop", "#06B6D4", TransactionType.INCOME, BudgetGroup.INCOME),
            Category("inc_investment", "Investments", "TrendingUp", "#3B82F6", TransactionType.INCOME, BudgetGroup.INCOME),
            Category("inc_allowance", "Allowance & Bonus", "Gift", "#8B5CF6", TransactionType.INCOME, BudgetGroup.INCOME),

            Category("exp_food", "Food & Groceries", "Restaurant", "#EF4444", TransactionType.EXPENSE, BudgetGroup.NEEDS),
            Category("exp_housing", "Housing & Rent", "Home", "#F97316", TransactionType.EXPENSE, BudgetGroup.NEEDS),
            Category("exp_transport", "Transportation", "DirectionsTransit", "#F59E0B", TransactionType.EXPENSE, BudgetGroup.NEEDS),
            Category("exp_bills", "Bills & Utilities", "Receipt", "#64748B", TransactionType.EXPENSE, BudgetGroup.NEEDS),
            Category("exp_health", "Healthcare", "LocalHospital", "#EC4899", TransactionType.EXPENSE, BudgetGroup.NEEDS),

            Category("exp_cafe", "Coffee & Desserts", "Coffee", "#854D0E", TransactionType.EXPENSE, BudgetGroup.WANTS),
            Category("exp_shopping", "Shopping & Retail", "ShoppingBag", "#A855F7", TransactionType.EXPENSE, BudgetGroup.WANTS),
            Category("exp_entertainment", "Entertainment", "Movie", "#6366F1", TransactionType.EXPENSE, BudgetGroup.WANTS),
            Category("exp_hobby", "Hobbies & Leisure", "Palette", "#14B8A6", TransactionType.EXPENSE, BudgetGroup.WANTS),

            Category("exp_savings", "Savings Deposit", "Savings", "#22C55E", TransactionType.EXPENSE, BudgetGroup.SAVINGS)
        )

        defaultCategories.forEach { cat ->
            val values = ContentValues().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("icon_name", cat.iconName)
                put("color_hex", cat.colorHex)
                put("type", cat.type.name)
                put("budget_group", cat.budgetGroup.name)
            }
            db.insertWithOnConflict("categories", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }

        // Default allocation ratios: 50% Needs, 30% Wants, 20% Savings
        setSettingInternal(db, "ratio_needs", "50")
        setSettingInternal(db, "ratio_wants", "30")
        setSettingInternal(db, "ratio_savings", "20")

        val now = LocalDate.now()
        val currentMonthStr = String.format("%04d-%02d", now.year, now.monthValue)
        val todayStr = now.toString()
        val yesterdayStr = now.minusDays(1).toString()
        val fiveDaysAgoStr = now.minusDays(5).toString()

        ensureDefaultAccounts(db)

        insertTx(db, Transaction(UUID.randomUUID().toString(), TransactionType.INCOME, 3500000L, "inc_salary", "$currentMonthStr-01", "Monthly Salary", PaymentMethod.BANK_TRANSFER, accountId = "acc_shinhan"))
        insertTx(db, Transaction(UUID.randomUUID().toString(), TransactionType.INCOME, 450000L, "inc_freelance", fiveDaysAgoStr, "UI Design Freelance", PaymentMethod.BANK_TRANSFER, accountId = "acc_toss"))
        insertTx(db, Transaction(UUID.randomUUID().toString(), TransactionType.EXPENSE, 650000L, "exp_housing", "$currentMonthStr-02", "Monthly Rent & Maintenance", PaymentMethod.BANK_TRANSFER, accountId = "acc_shinhan"))
        insertTx(db, Transaction(UUID.randomUUID().toString(), TransactionType.EXPENSE, 48500L, "exp_food", yesterdayStr, "E-Mart Grocery", PaymentMethod.CARD, accountId = "acc_toss"))
        insertTx(db, Transaction(UUID.randomUUID().toString(), TransactionType.EXPENSE, 14500L, "exp_food", todayStr, "Lunch Dining", PaymentMethod.CARD, accountId = "acc_toss"))
        insertTx(db, Transaction(UUID.randomUUID().toString(), TransactionType.EXPENSE, 6000L, "exp_cafe", todayStr, "Starbucks Coffee", PaymentMethod.MOBILE_PAY, accountId = "acc_toss"))
        insertTx(db, Transaction(UUID.randomUUID().toString(), TransactionType.EXPENSE, 55000L, "exp_transport", "$currentMonthStr-03", "Subway Transit Pass", PaymentMethod.CARD, accountId = "acc_tmoney"))

        upsertBudget(db, "exp_food", now.monthValue, now.year, 500000L)
        upsertBudget(db, "exp_housing", now.monthValue, now.year, 700000L)
        upsertBudget(db, "exp_transport", now.monthValue, now.year, 100000L)
        upsertBudget(db, "exp_cafe", now.monthValue, now.year, 80000L)
        upsertBudget(db, "exp_shopping", now.monthValue, now.year, 250000L)

        insertGoal(db, SavingsGoal(UUID.randomUUID().toString(), "Jeju Island Trip", 600000L, 420000L, now.plusMonths(2).toString(), "Flight & accommodation"))
        insertGoal(db, SavingsGoal(UUID.randomUUID().toString(), "3-Month Emergency Fund", 6000000L, 2400000L, now.plusYears(1).toString(), "Living expense reserve"))

        setSettingInternal(db, "sample_data_initialized", "1")
        setSettingInternal(db, "accounts_initialized", "1")
    }

    private fun getSettingInternal(db: SQLiteDatabase, key: String, defaultValue: String): String {
        try {
            val cursor = db.rawQuery("SELECT value FROM app_settings WHERE key = ?", arrayOf(key))
            cursor.use {
                if (it.moveToFirst()) {
                    return it.getString(0)
                }
            }
        } catch (_: Exception) {}
        return defaultValue
    }

    private fun setSettingInternal(db: SQLiteDatabase, key: String, value: String) {
        val cv = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        db.insertWithOnConflict("app_settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getSetting(key: String, defaultValue: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT value FROM app_settings WHERE key = ?", arrayOf(key))
        cursor.use {
            if (it.moveToFirst()) {
                return it.getString(0)
            }
        }
        return defaultValue
    }

    fun setSetting(key: String, value: String) {
        setSettingInternal(writableDatabase, key, value)
    }

    private fun insertAccount(db: SQLiteDatabase, acc: Account) {
        val cv = ContentValues().apply {
            put("id", acc.id)
            put("name", acc.name)
            put("type", acc.type.name)
            put("balance", acc.balance)
            put("color_start_hex", acc.colorStartHex)
            put("color_end_hex", acc.colorEndHex)
            put("last_four", acc.lastFour)
            put("issuer", acc.issuer)
            put("is_default", if (acc.isDefault) 1 else 0)
        }
        db.insertWithOnConflict("accounts", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllAccounts(): List<Account> {
        val list = mutableListOf<Account>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM accounts ORDER BY is_default DESC, name ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapAccount(it))
            }
        }
        return list
    }

    private fun mapAccount(c: Cursor): Account {
        return Account(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            name = c.getString(c.getColumnIndexOrThrow("name")),
            type = runCatching { AccountType.valueOf(c.getString(c.getColumnIndexOrThrow("type"))) }.getOrDefault(AccountType.DEBIT_CARD),
            balance = c.getLong(c.getColumnIndexOrThrow("balance")),
            colorStartHex = c.getString(c.getColumnIndexOrThrow("color_start_hex")),
            colorEndHex = c.getString(c.getColumnIndexOrThrow("color_end_hex")),
            lastFour = c.getString(c.getColumnIndexOrThrow("last_four")),
            issuer = c.getString(c.getColumnIndexOrThrow("issuer")),
            isDefault = c.getInt(c.getColumnIndexOrThrow("is_default")) == 1
        )
    }

    fun updateAccountBalance(id: String, delta: Long) {
        writableDatabase.execSQL("UPDATE accounts SET balance = balance + ? WHERE id = ?", arrayOf(delta, id))
    }

    fun setAccountBalance(id: String, newBalance: Long) {
        writableDatabase.execSQL("UPDATE accounts SET balance = ? WHERE id = ?", arrayOf(newBalance, id))
    }

    fun insertAccount(acc: Account) {
        insertAccount(writableDatabase, acc)
    }

    fun updateAccount(acc: Account) {
        val cv = ContentValues().apply {
            put("name", acc.name)
            put("type", acc.type.name)
            put("balance", acc.balance)
            put("color_start_hex", acc.colorStartHex)
            put("color_end_hex", acc.colorEndHex)
            put("last_four", acc.lastFour)
            put("issuer", acc.issuer)
            put("is_default", if (acc.isDefault) 1 else 0)
        }
        writableDatabase.update("accounts", cv, "id = ?", arrayOf(acc.id))
    }

    fun deleteAccount(id: String): Boolean {
        val countCursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM accounts", null)
        var count = 0
        countCursor.use {
            if (it.moveToFirst()) count = it.getInt(0)
        }
        if (count <= 1) return false // Protect last account from deletion

        writableDatabase.delete("accounts", "id = ?", arrayOf(id))
        writableDatabase.execSQL("UPDATE transactions SET account_id = NULL WHERE account_id = ?", arrayOf(id))
        return true
    }

    fun setDefaultAccount(id: String) {
        writableDatabase.execSQL("UPDATE accounts SET is_default = 0")
        writableDatabase.execSQL("UPDATE accounts SET is_default = 1 WHERE id = ?", arrayOf(id))
    }

    private fun insertTx(db: SQLiteDatabase, tx: Transaction) {
        val values = ContentValues().apply {
            put("id", tx.id)
            put("type", tx.type.name)
            put("amount", tx.amount)
            put("category_id", tx.categoryId)
            put("date_string", tx.dateString)
            put("note", tx.note)
            put("payment_method", tx.paymentMethod.name)
            put("created_at", tx.createdAt)
            put("account_id", tx.accountId)
        }
        db.insertWithOnConflict("transactions", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun upsertBudget(db: SQLiteDatabase, categoryId: String, month: Int, year: Int, limit: Long) {
        val id = "b_${categoryId}_${year}_$month"
        val values = ContentValues().apply {
            put("id", id)
            put("category_id", categoryId)
            put("month", month)
            put("year", year)
            put("monthly_limit", limit)
        }
        db.insertWithOnConflict("budgets", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun insertGoal(db: SQLiteDatabase, goal: SavingsGoal) {
        val values = ContentValues().apply {
            put("id", goal.id)
            put("name", goal.name)
            put("target_amount", goal.targetAmount)
            put("current_amount", goal.currentAmount)
            put("target_date_string", goal.targetDateString)
            put("note", goal.note)
            put("icon_name", goal.iconName)
        }
        db.insertWithOnConflict("savings_goals", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // --- PUBLIC REPOSITORY METHODS ---
    fun getCategories(): List<Category> {
        val list = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM categories ORDER BY name ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Category(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        iconName = it.getString(it.getColumnIndexOrThrow("icon_name")),
                        colorHex = it.getString(it.getColumnIndexOrThrow("color_hex")),
                        type = TransactionType.valueOf(it.getString(it.getColumnIndexOrThrow("type"))),
                        budgetGroup = runCatching { BudgetGroup.valueOf(it.getString(it.getColumnIndexOrThrow("budget_group"))) }.getOrDefault(BudgetGroup.NEEDS)
                    )
                )
            }
        }
        return list
    }

    fun insertCategory(cat: Category) {
        val values = ContentValues().apply {
            put("id", cat.id)
            put("name", cat.name)
            put("icon_name", cat.iconName)
            put("color_hex", cat.colorHex)
            put("type", cat.type.name)
            put("budget_group", cat.budgetGroup.name)
        }
        writableDatabase.insertWithOnConflict("categories", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllTransactions(): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM transactions ORDER BY date_string DESC, created_at DESC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapTransaction(it))
            }
        }
        return list
    }

    fun getTransactionsByMonth(year: Int, month: Int): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val monthPattern = String.format("%04d-%02d%%", year, month)
        val cursor = readableDatabase.rawQuery("SELECT * FROM transactions WHERE date_string LIKE ? ORDER BY date_string DESC, created_at DESC", arrayOf(monthPattern))
        cursor.use {
            while (it.moveToNext()) {
                list.add(mapTransaction(it))
            }
        }
        return list
    }

    fun insertTransaction(t: Transaction) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            insertTx(db, t)
            t.accountId?.let { accId ->
                val delta = if (t.type == TransactionType.INCOME) t.amount else -t.amount
                db.execSQL("UPDATE accounts SET balance = balance + ? WHERE id = ?", arrayOf(delta, accId))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteTransaction(id: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.rawQuery("SELECT type, amount, account_id FROM transactions WHERE id = ?", arrayOf(id))
            var txType: String? = null
            var amount: Long = 0
            var accountId: String? = null
            cursor.use {
                if (it.moveToFirst()) {
                    txType = it.getString(0)
                    amount = it.getLong(1)
                    accountId = if (!it.isNull(2)) it.getString(2) else null
                }
            }
            if (accountId != null && txType != null) {
                val delta = if (txType == TransactionType.INCOME.name) -amount else amount
                db.execSQL("UPDATE accounts SET balance = balance + ? WHERE id = ?", arrayOf(delta, accountId))
            }
            db.delete("transactions", "id = ?", arrayOf(id))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun mapTransaction(c: Cursor): Transaction {
        val accIdIdx = c.getColumnIndex("account_id")
        val accountId = if (accIdIdx != -1 && !c.isNull(accIdIdx)) c.getString(accIdIdx) else null
        return Transaction(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            type = TransactionType.valueOf(c.getString(c.getColumnIndexOrThrow("type"))),
            amount = c.getLong(c.getColumnIndexOrThrow("amount")),
            categoryId = c.getString(c.getColumnIndexOrThrow("category_id")),
            dateString = c.getString(c.getColumnIndexOrThrow("date_string")),
            note = c.getString(c.getColumnIndexOrThrow("note")) ?: "",
            paymentMethod = runCatching { PaymentMethod.valueOf(c.getString(c.getColumnIndexOrThrow("payment_method"))) }.getOrDefault(PaymentMethod.CARD),
            createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
            accountId = accountId
        )
    }

    fun getBudgetsForMonth(year: Int, month: Int): List<Budget> {
        val list = mutableListOf<Budget>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM budgets WHERE month = ? AND year = ?", arrayOf(month.toString(), year.toString()))
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Budget(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        categoryId = it.getString(it.getColumnIndexOrThrow("category_id")),
                        month = it.getInt(it.getColumnIndexOrThrow("month")),
                        year = it.getInt(it.getColumnIndexOrThrow("year")),
                        monthlyLimit = it.getLong(it.getColumnIndexOrThrow("monthly_limit"))
                    )
                )
            }
        }
        return list
    }

    fun setBudgetLimit(categoryId: String, month: Int, year: Int, limit: Long) {
        upsertBudget(writableDatabase, categoryId, month, year, limit)
    }

    fun deleteBudget(id: String) {
        writableDatabase.delete("budgets", "id = ?", arrayOf(id))
    }

    fun getAllGoals(): List<SavingsGoal> {
        val list = mutableListOf<SavingsGoal>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM savings_goals ORDER BY target_date_string ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    SavingsGoal(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        targetAmount = it.getLong(it.getColumnIndexOrThrow("target_amount")),
                        currentAmount = it.getLong(it.getColumnIndexOrThrow("current_amount")),
                        targetDateString = it.getString(it.getColumnIndexOrThrow("target_date_string")),
                        note = it.getString(it.getColumnIndexOrThrow("note")) ?: "",
                        iconName = it.getString(it.getColumnIndexOrThrow("icon_name"))
                    )
                )
            }
        }
        return list
    }

    fun insertGoal(goal: SavingsGoal) {
        insertGoal(writableDatabase, goal)
    }

    fun updateGoalFunds(id: String, addedAmount: Long) {
        writableDatabase.execSQL("UPDATE savings_goals SET current_amount = current_amount + ? WHERE id = ?", arrayOf(addedAmount, id))
    }

    fun deleteGoal(id: String) {
        writableDatabase.delete("savings_goals", "id = ?", arrayOf(id))
    }
}
