package data.database

import data.model.*
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.LocalDate
import java.util.UUID

object DatabaseManager {
    private val dbFile = File(System.getProperty("user.home"), ".moneymanagement/moneymanagement.db")
    private val dbUrl = "jdbc:sqlite:${dbFile.absolutePath}"

    init {
        dbFile.parentFile?.mkdirs()
        initializeDatabase()
    }

    @Synchronized
    private fun getConnection(): Connection {
        val conn = DriverManager.getConnection(dbUrl)
        conn.createStatement().use { stmt ->
            stmt.execute("PRAGMA journal_mode=WAL;")
            stmt.execute("PRAGMA busy_timeout=5000;")
        }
        return conn
    }

    private fun initializeDatabase() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                // Categories
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        icon_name TEXT NOT NULL,
                        color_hex TEXT NOT NULL,
                        type TEXT NOT NULL,
                        budget_group TEXT NOT NULL
                    )
                """.trimIndent())

                // Transactions
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id TEXT PRIMARY KEY,
                        type TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        category_id TEXT NOT NULL,
                        date_string TEXT NOT NULL,
                        note TEXT,
                        payment_method TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY (category_id) REFERENCES categories (id)
                    )
                """.trimIndent())

                // Budgets
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS budgets (
                        id TEXT PRIMARY KEY,
                        category_id TEXT NOT NULL,
                        month INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        monthly_limit INTEGER NOT NULL,
                        UNIQUE(category_id, month, year),
                        FOREIGN KEY (category_id) REFERENCES categories (id)
                    )
                """.trimIndent())

                // Savings Goals
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS savings_goals (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        target_amount INTEGER NOT NULL,
                        current_amount INTEGER NOT NULL,
                        target_date_string TEXT NOT NULL,
                        note TEXT,
                        icon_name TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        // Seed default categories if empty
        if (getCategories().isEmpty()) {
            seedDefaultData()
        }
    }

    private fun seedDefaultData() {
        val defaultCategories = listOf(
            // Income
            Category("inc_salary", "급여 / 월급 (Salary)", "Work", "#10B981", TransactionType.INCOME, BudgetGroup.INCOME),
            Category("inc_freelance", "부업 / 프리랜서 (Freelance)", "Laptop", "#06B6D4", TransactionType.INCOME, BudgetGroup.INCOME),
            Category("inc_investment", "투자 / 배당금 (Investments)", "TrendingUp", "#3B82F6", TransactionType.INCOME, BudgetGroup.INCOME),
            Category("inc_allowance", "용돈 / 기타수입 (Allowance)", "Gift", "#8B5CF6", TransactionType.INCOME, BudgetGroup.INCOME),

            // Expense - Needs (50%)
            Category("exp_food", "식비 / 외식 (Food & Groceries)", "Restaurant", "#EF4444", TransactionType.EXPENSE, BudgetGroup.NEEDS),
            Category("exp_housing", "주거 / 월세 / 관리비 (Housing & Rent)", "Home", "#F97316", TransactionType.EXPENSE, BudgetGroup.NEEDS),
            Category("exp_transport", "교통 / 대중교통 (Transport)", "DirectionsTransit", "#F59E0B", TransactionType.EXPENSE, BudgetGroup.NEEDS),
            Category("exp_bills", "공과금 / 통신비 (Bills & Utilities)", "Receipt", "#64748B", TransactionType.EXPENSE, BudgetGroup.NEEDS),
            Category("exp_health", "의료 / 건강 (Health & Pharmacy)", "LocalHospital", "#EC4899", TransactionType.EXPENSE, BudgetGroup.NEEDS),

            // Expense - Wants (30%)
            Category("exp_cafe", "카페 / 디저트 (Cafe & Coffee)", "Coffee", "#854D0E", TransactionType.EXPENSE, BudgetGroup.WANTS),
            Category("exp_shopping", "쇼핑 / 패션 (Shopping)", "ShoppingBag", "#A855F7", TransactionType.EXPENSE, BudgetGroup.WANTS),
            Category("exp_entertainment", "문화 / 여가 / 영화 (Entertainment)", "Movie", "#6366F1", TransactionType.EXPENSE, BudgetGroup.WANTS),
            Category("exp_hobby", "취미 / 자기계발 (Hobbies & Books)", "Palette", "#14B8A6", TransactionType.EXPENSE, BudgetGroup.WANTS),

            // Expense - Savings (20%)
            Category("exp_savings", "적금 / 비상금 저축 (Savings Deposit)", "Savings", "#22C55E", TransactionType.EXPENSE, BudgetGroup.SAVINGS)
        )

        defaultCategories.forEach { insertCategory(it) }

        // Seed some sample transactions for current month so user sees a vibrant dashboard immediately
        val now = LocalDate.now()
        val currentMonthStr = String.format("%04d-%02d", now.year, now.monthValue)
        val todayStr = now.toString()
        val yesterdayStr = now.minusDays(1).toString()
        val fiveDaysAgoStr = now.minusDays(5).toString()

        insertTransaction(Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.INCOME,
            amount = 3500000L, // ₩3,500,000 monthly salary
            categoryId = "inc_salary",
            dateString = "$currentMonthStr-01",
            note = "10월 정기 급여 (Monthly Salary)",
            paymentMethod = PaymentMethod.BANK_TRANSFER
        ))

        insertTransaction(Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.INCOME,
            amount = 450000L, // ₩450,000 freelance project
            categoryId = "inc_freelance",
            dateString = fiveDaysAgoStr,
            note = "UI 디자인 외주 프로젝트 (Design Gig)",
            paymentMethod = PaymentMethod.BANK_TRANSFER
        ))

        insertTransaction(Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = 650000L, // ₩650,000 Rent
            categoryId = "exp_housing",
            dateString = "$currentMonthStr-02",
            note = "원룸 월세 및 관리비 (Monthly Rent)",
            paymentMethod = PaymentMethod.BANK_TRANSFER
        ))

        insertTransaction(Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = 48500L, // ₩48,500 Groceries
            categoryId = "exp_food",
            dateString = yesterdayStr,
            note = "이마트 장보기 (E-Mart Groceries)",
            paymentMethod = PaymentMethod.CARD
        ))

        insertTransaction(Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = 14500L, // ₩14,500 Lunch
            categoryId = "exp_food",
            dateString = todayStr,
            note = "김치찌개 점심 식사 (Kimchi Stew Lunch)",
            paymentMethod = PaymentMethod.CARD
        ))

        insertTransaction(Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = 6000L, // ₩6,000 Starbucks
            categoryId = "exp_cafe",
            dateString = todayStr,
            note = "스타벅스 아메리카노 (Starbucks Iced Americano)",
            paymentMethod = PaymentMethod.MOBILE_PAY
        ))

        insertTransaction(Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = 55000L, // ₩55,000 Metro pass / transportation
            categoryId = "exp_transport",
            dateString = "$currentMonthStr-03",
            note = "기후동행카드 / 지하철 충전 (Subway Pass)",
            paymentMethod = PaymentMethod.CARD
        ))

        // Seed default budget limits for the current month
        upsertBudget("exp_food", now.monthValue, now.year, 500000L) // ₩500,000 Food budget
        upsertBudget("exp_housing", now.monthValue, now.year, 700000L) // ₩700,000 Housing budget
        upsertBudget("exp_transport", now.monthValue, now.year, 100000L) // ₩100,000 Transport budget
        upsertBudget("exp_cafe", now.monthValue, now.year, 80000L) // ₩80,000 Cafe budget
        upsertBudget("exp_shopping", now.monthValue, now.year, 250000L) // ₩250,000 Shopping budget

        // Seed sample savings goals
        insertGoal(SavingsGoal(
            id = UUID.randomUUID().toString(),
            name = "제주도 가을 여행 (Jeju Holiday)",
            targetAmount = 600000L,
            currentAmount = 420000L,
            targetDateString = now.plusMonths(2).toString(),
            note = "항공권 & 렌터카 경비"
        ))

        insertGoal(SavingsGoal(
            id = UUID.randomUUID().toString(),
            name = "비상금 펀드 3개월 (Emergency Fund)",
            targetAmount = 6000000L,
            currentAmount = 2400000L,
            targetDateString = now.plusYears(1).toString(),
            note = "3개월 생활비 확보 목표"
        ))
    }

    // --- CATEGORIES ---
    fun getCategories(): List<Category> {
        val list = mutableListOf<Category>()
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM categories ORDER BY name ASC")
                while (rs.next()) {
                    list.add(
                        Category(
                            id = rs.getString("id"),
                            name = rs.getString("name"),
                            iconName = rs.getString("icon_name"),
                            colorHex = rs.getString("color_hex"),
                            type = TransactionType.valueOf(rs.getString("type")),
                            budgetGroup = runCatching { BudgetGroup.valueOf(rs.getString("budget_group")) }.getOrDefault(BudgetGroup.NEEDS)
                        )
                    )
                }
            }
        }
        return list
    }

    fun insertCategory(cat: Category) {
        getConnection().use { conn ->
            val sql = "INSERT OR REPLACE INTO categories (id, name, icon_name, color_hex, type, budget_group) VALUES (?, ?, ?, ?, ?, ?)"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, cat.id)
                pstmt.setString(2, cat.name)
                pstmt.setString(3, cat.iconName)
                pstmt.setString(4, cat.colorHex)
                pstmt.setString(5, cat.type.name)
                pstmt.setString(6, cat.budgetGroup.name)
                pstmt.executeUpdate()
            }
        }
    }

    // --- TRANSACTIONS ---
    fun getAllTransactions(): List<Transaction> {
        val list = mutableListOf<Transaction>()
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM transactions ORDER BY date_string DESC, created_at DESC")
                while (rs.next()) {
                    list.add(mapTransaction(rs))
                }
            }
        }
        return list
    }

    fun getTransactionsByMonth(year: Int, month: Int): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val monthPattern = String.format("%04d-%02d%%", year, month)
        getConnection().use { conn ->
            val sql = "SELECT * FROM transactions WHERE date_string LIKE ? ORDER BY date_string DESC, created_at DESC"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, monthPattern)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    list.add(mapTransaction(rs))
                }
            }
        }
        return list
    }

    fun insertTransaction(t: Transaction) {
        getConnection().use { conn ->
            val sql = """
                INSERT OR REPLACE INTO transactions (id, type, amount, category_id, date_string, note, payment_method, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, t.id)
                pstmt.setString(2, t.type.name)
                pstmt.setLong(3, t.amount)
                pstmt.setString(4, t.categoryId)
                pstmt.setString(5, t.dateString)
                pstmt.setString(6, t.note)
                pstmt.setString(7, t.paymentMethod.name)
                pstmt.setLong(8, t.createdAt)
                pstmt.executeUpdate()
            }
        }
    }

    fun deleteTransaction(id: String) {
        getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM transactions WHERE id = ?").use { pstmt ->
                pstmt.setString(1, id)
                pstmt.executeUpdate()
            }
        }
    }

    private fun mapTransaction(rs: ResultSet): Transaction {
        return Transaction(
            id = rs.getString("id"),
            type = TransactionType.valueOf(rs.getString("type")),
            amount = rs.getLong("amount"),
            categoryId = rs.getString("category_id"),
            dateString = rs.getString("date_string"),
            note = rs.getString("note") ?: "",
            paymentMethod = runCatching { PaymentMethod.valueOf(rs.getString("payment_method")) }.getOrDefault(PaymentMethod.CARD),
            createdAt = rs.getLong("created_at")
        )
    }

    // --- BUDGETS ---
    fun getBudgetsForMonth(year: Int, month: Int): List<Budget> {
        val list = mutableListOf<Budget>()
        getConnection().use { conn ->
            val sql = "SELECT * FROM budgets WHERE month = ? AND year = ?"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setInt(1, month)
                pstmt.setInt(2, year)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    list.add(
                        Budget(
                            id = rs.getString("id"),
                            categoryId = rs.getString("category_id"),
                            month = rs.getInt("month"),
                            year = rs.getInt("year"),
                            monthlyLimit = rs.getLong("monthly_limit")
                        )
                    )
                }
            }
        }
        return list
    }

    fun upsertBudget(categoryId: String, month: Int, year: Int, limit: Long) {
        val id = "b_${categoryId}_${year}_$month"
        getConnection().use { conn ->
            val sql = """
                INSERT INTO budgets (id, category_id, month, year, monthly_limit)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(category_id, month, year) DO UPDATE SET monthly_limit = excluded.monthly_limit
            """.trimIndent()
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, id)
                pstmt.setString(2, categoryId)
                pstmt.setInt(3, month)
                pstmt.setInt(4, year)
                pstmt.setLong(5, limit)
                pstmt.executeUpdate()
            }
        }
    }

    fun deleteBudget(id: String) {
        getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM budgets WHERE id = ?").use { pstmt ->
                pstmt.setString(1, id)
                pstmt.executeUpdate()
            }
        }
    }

    // --- SAVINGS GOALS ---
    fun getAllGoals(): List<SavingsGoal> {
        val list = mutableListOf<SavingsGoal>()
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM savings_goals ORDER BY target_date_string ASC")
                while (rs.next()) {
                    list.add(
                        SavingsGoal(
                            id = rs.getString("id"),
                            name = rs.getString("name"),
                            targetAmount = rs.getLong("target_amount"),
                            currentAmount = rs.getLong("current_amount"),
                            targetDateString = rs.getString("target_date_string"),
                            note = rs.getString("note") ?: "",
                            iconName = rs.getString("icon_name")
                        )
                    )
                }
            }
        }
        return list
    }

    fun insertGoal(goal: SavingsGoal) {
        getConnection().use { conn ->
            val sql = """
                INSERT OR REPLACE INTO savings_goals (id, name, target_amount, current_amount, target_date_string, note, icon_name)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, goal.id)
                pstmt.setString(2, goal.name)
                pstmt.setLong(3, goal.targetAmount)
                pstmt.setLong(4, goal.currentAmount)
                pstmt.setString(5, goal.targetDateString)
                pstmt.setString(6, goal.note)
                pstmt.setString(7, goal.iconName)
                pstmt.executeUpdate()
            }
        }
    }

    fun updateGoalFunds(id: String, addedAmount: Long) {
        getConnection().use { conn ->
            val sql = "UPDATE savings_goals SET current_amount = current_amount + ? WHERE id = ?"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setLong(1, addedAmount)
                pstmt.setString(2, id)
                pstmt.executeUpdate()
            }
        }
    }

    fun deleteGoal(id: String) {
        getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM savings_goals WHERE id = ?").use { pstmt ->
                pstmt.setString(1, id)
                pstmt.executeUpdate()
            }
        }
    }
}
