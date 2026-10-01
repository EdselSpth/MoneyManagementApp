package test

import data.database.DatabaseManager
import data.model.*
import data.repository.BackupPayload
import data.repository.FinanceRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import util.CurrencyFormatter
import java.time.LocalDate
import java.util.UUID
import kotlin.test.*

class FinanceAppTest {

    @Test
    fun testCurrencyFormatting() {
        assertEquals("₩1,250,000", CurrencyFormatter.format(1250000L))
        assertEquals("₩0", CurrencyFormatter.format(0L))
        assertEquals("-₩50,000", CurrencyFormatter.format(-50000L))
        assertEquals("+₩3,500,000", CurrencyFormatter.format(3500000L, includeSign = true))
        assertEquals("1,250,000", CurrencyFormatter.format(1250000L, includePrefix = false))
        assertEquals(50000L, CurrencyFormatter.parse("₩50,000"))
        assertEquals(1000000L, CurrencyFormatter.parse("1000000"))
    }

    @Test
    fun testDatabaseInitializationAndSeeding() {
        val categories = DatabaseManager.getCategories()
        assertTrue(categories.isNotEmpty(), "Categories should be pre-seeded")

        val hasIncomeCategory = categories.any { it.type == TransactionType.INCOME }
        val hasExpenseCategory = categories.any { it.type == TransactionType.EXPENSE }
        assertTrue(hasIncomeCategory, "Should contain income category")
        assertTrue(hasExpenseCategory, "Should contain expense category")

        val allTx = DatabaseManager.getAllTransactions()
        assertTrue(allTx.isNotEmpty(), "Sample transactions should exist")
    }

    @Test
    fun testTransactionCrud() {
        val testTx = Transaction(
            id = UUID.randomUUID().toString(),
            type = TransactionType.EXPENSE,
            amount = 12000L, // ₩12,000
            categoryId = "exp_cafe",
            dateString = LocalDate.now().toString(),
            note = "테스트 아메리카노",
            paymentMethod = PaymentMethod.CARD
        )

        DatabaseManager.insertTransaction(testTx)

        val retrieved = DatabaseManager.getAllTransactions().find { it.id == testTx.id }
        assertNotNull(retrieved)
        assertEquals(12000L, retrieved.amount)
        assertEquals("테스트 아메리카노", retrieved.note)

        DatabaseManager.deleteTransaction(testTx.id)
        val afterDelete = DatabaseManager.getAllTransactions().find { it.id == testTx.id }
        assertNull(afterDelete)
    }

    @Test
    fun testBudgetUpsert() {
        val now = LocalDate.now()
        DatabaseManager.upsertBudget("exp_cafe", now.monthValue, now.year, 150000L)

        val budgets = DatabaseManager.getBudgetsForMonth(now.year, now.monthValue)
        val cafeBudget = budgets.find { it.categoryId == "exp_cafe" }
        assertNotNull(cafeBudget)
        assertEquals(150000L, cafeBudget.monthlyLimit)
    }

    @Test
    fun testSavingsGoals() {
        val goal = SavingsGoal(
            id = UUID.randomUUID().toString(),
            name = "테스트 여행",
            targetAmount = 500000L,
            currentAmount = 100000L,
            targetDateString = LocalDate.now().plusMonths(3).toString()
        )

        DatabaseManager.insertGoal(goal)
        val goals = DatabaseManager.getAllGoals()
        val retrieved = goals.find { it.id == goal.id }
        assertNotNull(retrieved)
        assertEquals(500000L, retrieved.targetAmount)

        DatabaseManager.updateGoalFunds(goal.id, 50000L)
        val afterDeposit = DatabaseManager.getAllGoals().find { it.id == goal.id }
        assertNotNull(afterDeposit)
        assertEquals(150000L, afterDeposit.currentAmount)

        DatabaseManager.deleteGoal(goal.id)
    }

    @Test
    fun testBackupPayloadSerialization() {
        val payload = BackupPayload(
            exportedAt = System.currentTimeMillis(),
            categories = DatabaseManager.getCategories().take(2),
            transactions = emptyList(),
            budgets = emptyList(),
            goals = emptyList()
        )

        val json = Json { prettyPrint = true }
        val serialized = json.encodeToString(payload)
        assertTrue(serialized.contains("exportedAt"))

        val deserialized = json.decodeFromString<BackupPayload>(serialized)
        assertEquals(payload.exportedAt, deserialized.exportedAt)
        assertEquals(2, deserialized.categories.size)
    }
}
