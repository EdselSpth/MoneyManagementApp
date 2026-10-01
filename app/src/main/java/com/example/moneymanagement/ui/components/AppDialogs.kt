package com.example.moneymanagement.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.moneymanagement.data.model.*
import com.example.moneymanagement.ui.theme.ShadcnTheme
import com.example.moneymanagement.util.ColorParser
import com.example.moneymanagement.util.CurrencyFormatter
import java.time.LocalDate
import java.util.UUID

@Composable
fun AddTransactionDialog(
    isOpen: Boolean,
    categories: List<Category>,
    accounts: List<Account> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    initialType: TransactionType = TransactionType.EXPENSE
) {
    if (!isOpen) return

    var type by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf(LocalDate.now().toString()) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CARD) }

    val filteredCategories = categories.filter { it.type == type }
    var selectedCategoryId by remember(type) {
        mutableStateOf(filteredCategories.firstOrNull()?.id ?: "")
    }

    var selectedAccountId by remember(accounts) {
        mutableStateOf(accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id ?: "")
    }

    val currentAmount = amountText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentPadding = 20.dp
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (type == TransactionType.EXPENSE) "Add Expense" else "Add Income",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    ShadcnBadge(text = "₩ KRW", variant = BadgeVariant.KRW)
                }

                // Type Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ShadcnTheme.colors.muted)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val expSelected = type == TransactionType.EXPENSE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (expSelected) ShadcnTheme.colors.expense else Color.Transparent)
                            .clickable { type = TransactionType.EXPENSE }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            color = if (expSelected) Color.White else ShadcnTheme.colors.mutedForeground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val incSelected = type == TransactionType.INCOME
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (incSelected) ShadcnTheme.colors.income else Color.Transparent)
                            .clickable { type = TransactionType.INCOME }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            color = if (incSelected) Color.White else ShadcnTheme.colors.mutedForeground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Card / Account Selector
                if (accounts.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Payment Card / Account",
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            items(accounts) { acc ->
                                val isSelected = acc.id == selectedAccountId
                                val accColor = ColorParser.parse(acc.colorStartHex, ShadcnTheme.colors.primary)

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) accColor.copy(alpha = 0.25f) else ShadcnTheme.colors.secondary)
                                        .border(1.5.dp, if (isSelected) accColor else ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedAccountId = acc.id
                                            if (acc.type == AccountType.CASH) {
                                                paymentMethod = PaymentMethod.CASH
                                            } else if (acc.type == AccountType.TRANSIT) {
                                                paymentMethod = PaymentMethod.CARD
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(accColor)
                                    )
                                    Text(
                                        text = acc.name,
                                        color = if (isSelected) Color.White else ShadcnTheme.colors.foreground,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(acc.balance),
                                        color = if (isSelected) accColor else ShadcnTheme.colors.mutedForeground,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Amount
                Column {
                    ShadcnInput(
                        value = amountText,
                        onValueChange = { input -> amountText = input.filter { it.isDigit() } },
                        label = "Amount in Won",
                        placeholder = "e.g. 25000",
                        prefix = "₩"
                    )

                    if (currentAmount > 0) {
                        Text(
                            text = "Formatted: ${CurrencyFormatter.format(currentAmount)}",
                            color = if (type == TransactionType.INCOME) ShadcnTheme.colors.income else ShadcnTheme.colors.expense,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Quick selector
                KrwQuickSelector(
                    currentAmount = currentAmount,
                    onAmountSelected = { newAmount ->
                        amountText = if (newAmount > 0) newAmount.toString() else ""
                    }
                )

                // Category
                Column {
                    Text(
                        text = "Category",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        items(filteredCategories) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            val catColor = ColorParser.parse(cat.colorHex, ShadcnTheme.colors.primary)

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) catColor.copy(alpha = 0.25f) else ShadcnTheme.colors.secondary)
                                    .border(1.5.dp, if (isSelected) catColor else ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategoryId = cat.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CategoryIcon(iconName = cat.iconName, tint = catColor, modifier = Modifier.size(16.dp))
                                Text(
                                    text = cat.name,
                                    color = if (isSelected) Color.White else ShadcnTheme.colors.foreground,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Payment Method
                Column {
                    Text(
                        text = "Payment Method",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PaymentMethod.entries.forEach { method ->
                            val isSelected = method == paymentMethod
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ShadcnTheme.colors.primary.copy(alpha = 0.2f) else ShadcnTheme.colors.secondary)
                                    .border(1.5.dp, if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                    .clickable { paymentMethod = method }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method.label,
                                    color = if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.foreground,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Date & Note
                ShadcnInput(value = dateString, onValueChange = { dateString = it }, label = "Date (YYYY-MM-DD)")
                ShadcnInput(value = note, onValueChange = { note = it }, label = "Note / Description", placeholder = "e.g. Dinner with team, Taxi fare")

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = onDismiss, modifier = Modifier.padding(end = 8.dp))
                    ShadcnButtonText(
                        text = "Save Transaction",
                        variant = ButtonVariant.PRIMARY,
                        enabled = currentAmount > 0 && selectedCategoryId.isNotEmpty(),
                        onClick = {
                            if (currentAmount > 0 && selectedCategoryId.isNotEmpty()) {
                                onSave(
                                    Transaction(
                                        id = UUID.randomUUID().toString(),
                                        type = type,
                                        amount = currentAmount,
                                        categoryId = selectedCategoryId,
                                        dateString = dateString.trim().ifEmpty { LocalDate.now().toString() },
                                        note = note.trim(),
                                        paymentMethod = paymentMethod,
                                        accountId = selectedAccountId.ifEmpty { null }
                                    )
                                )
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TopUpAccountDialog(
    account: Account?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    if (account == null) return

    var amountText by remember { mutableStateOf("") }
    val currentAmount = amountText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(modifier = Modifier.fillMaxWidth().wrapContentHeight(), contentPadding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Top-Up ${account.name}",
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Current: ${CurrencyFormatter.format(account.balance)}",
                            color = ShadcnTheme.colors.mutedForeground,
                            fontSize = 12.sp
                        )
                    }
                    ShadcnBadge(text = account.issuer, variant = BadgeVariant.DEFAULT)
                }

                // Quick Top-up amounts
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10000L, 30000L, 50000L, 100000L).forEach { quickAmt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ShadcnTheme.colors.secondary)
                                .border(1.dp, ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                .clickable { amountText = quickAmt.toString() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${CurrencyFormatter.format(quickAmt).replace("₩", "")}",
                                color = ShadcnTheme.colors.foreground,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                ShadcnInput(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() } },
                    label = "Top-Up Amount",
                    placeholder = "e.g. 50000",
                    prefix = "₩"
                )

                if (currentAmount > 0) {
                    Text(
                        text = "New Balance: ${CurrencyFormatter.format(account.balance + currentAmount)}",
                        color = ShadcnTheme.colors.krwAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = onDismiss, modifier = Modifier.padding(end = 8.dp))
                    ShadcnButtonText(
                        text = "Confirm Top-Up",
                        variant = ButtonVariant.PRIMARY,
                        enabled = currentAmount > 0,
                        onClick = {
                            if (currentAmount > 0) {
                                onConfirm(currentAmount)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

data class CardThemePreset(
    val name: String,
    val startHex: String,
    val endHex: String,
    val previewColor: Color
)

val cardThemePresets = listOf(
    CardThemePreset("Aether Gold", "#DCB37B", "#A08066", Color(0xFFDCB37B)),
    CardThemePreset("Aether Cocoa", "#5E4A4B", "#332829", Color(0xFF5E4A4B)),
    CardThemePreset("Aether Alabaster", "#FAF0D9", "#DCB37B", Color(0xFFFAF0D9)),
    CardThemePreset("Toss Blue", "#0064FF", "#0038A8", Color(0xFF0064FF)),
    CardThemePreset("Kakao Yellow", "#FEE500", "#EAB308", Color(0xFFFEE500)),
    CardThemePreset("T-Money Cyan", "#06B6D4", "#0891B2", Color(0xFF06B6D4)),
    CardThemePreset("Shinhan Navy", "#1E3A8A", "#0F172A", Color(0xFF1E3A8A)),
    CardThemePreset("Cash Emerald", "#10B981", "#047857", Color(0xFF10B981)),
    CardThemePreset("Obsidian Black", "#27272A", "#09090B", Color(0xFF27272A)),
    CardThemePreset("Royal Purple", "#8B5CF6", "#5B21B6", Color(0xFF8B5CF6)),
    CardThemePreset("KB Crimson", "#EF4444", "#991B1B", Color(0xFFEF4444)),
    CardThemePreset("Coral Sunset", "#F97316", "#C2410C", Color(0xFFF97316))
)

@Composable
fun AddAccountDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    issuers: List<String> = emptyList(),
    onSave: (Account) -> Unit
) {
    if (!isOpen) return

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.DEBIT_CARD) }
    var issuer by remember { mutableStateOf("Kakao Bank") }
    var balanceText by remember { mutableStateOf("") }
    var lastFour by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf(cardThemePresets[1]) }
    var isDefault by remember { mutableStateOf(false) }

    val currentBalance = balanceText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentPadding = 20.dp
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+ Add New Card / Wallet",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    ShadcnBadge(text = selectedType.label, variant = BadgeVariant.SECONDARY)
                }

                // Live Card Preview
                val startCol = ColorParser.parse(selectedPreset.startHex, Color(0xFF0064FF))
                val endCol = ColorParser.parse(selectedPreset.endHex, Color(0xFF0038A8))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(startCol, endCol)))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (if (issuer.isNotBlank()) issuer else "CARD ISSUER").uppercase(),
                                color = if (selectedPreset.previewColor == Color(0xFFFEE500)) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isDefault) {
                                ShadcnBadge(text = "PRIMARY", variant = BadgeVariant.DEFAULT)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedType == AccountType.CASH) "PHYSICAL CASH" else "•••• •••• •••• ${lastFour.ifEmpty { "0000" }}",
                                color = if (selectedPreset.previewColor == Color(0xFFFEE500)) Color.Black.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = name.ifEmpty { "New Card Name" },
                                color = if (selectedPreset.previewColor == Color(0xFFFEE500)) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = CurrencyFormatter.format(currentBalance),
                                color = if (selectedPreset.previewColor == Color(0xFFFEE500)) Color.Black else Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Card / Wallet Name
                ShadcnInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "Card or Account Name",
                    placeholder = "e.g. Kakao Bank Card, Hyundai Card Zero"
                )

                // Quick Issuer chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Issuer / Bank", color = ShadcnTheme.colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    val activeIssuers = if (issuers.isNotEmpty()) issuers else listOf("Aether Genshin", "Kakao Bank", "Toss Bank", "Shinhan Card", "KB Kookmin", "Hyundai Card", "T-Money", "Cash Wallet", "Woori Bank", "Hana Card")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(activeIssuers) { iss ->
                            val isSel = issuer.equals(iss, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ShadcnTheme.colors.primary.copy(alpha = 0.2f) else ShadcnTheme.colors.secondary)
                                    .border(1.dp, if (isSel) ShadcnTheme.colors.primary else ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        issuer = iss
                                        when (iss) {
                                            "Aether Genshin" -> {
                                                selectedPreset = cardThemePresets[0] // Aether Gold
                                                selectedType = AccountType.DEBIT_CARD
                                            }
                                            "Kakao Bank" -> {
                                                selectedPreset = cardThemePresets[4]
                                                selectedType = AccountType.DEBIT_CARD
                                            }
                                            "Toss Bank" -> {
                                                selectedPreset = cardThemePresets[3]
                                                selectedType = AccountType.FINTECH
                                            }
                                            "T-Money" -> {
                                                selectedPreset = cardThemePresets[5]
                                                selectedType = AccountType.TRANSIT
                                            }
                                            "Shinhan Card" -> {
                                                selectedPreset = cardThemePresets[6]
                                                selectedType = AccountType.DEBIT_CARD
                                            }
                                            "Cash Wallet" -> {
                                                selectedPreset = cardThemePresets[7]
                                                selectedType = AccountType.CASH
                                            }
                                            "Hyundai Card" -> {
                                                selectedPreset = cardThemePresets[8]
                                                selectedType = AccountType.CREDIT_CARD
                                            }
                                            "KB Kookmin" -> {
                                                selectedPreset = cardThemePresets[10]
                                                selectedType = AccountType.DEBIT_CARD
                                            }
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = iss,
                                    color = if (isSel) ShadcnTheme.colors.primary else ShadcnTheme.colors.foreground,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Account Type Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Account Type", color = ShadcnTheme.colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(AccountType.DEBIT_CARD, AccountType.FINTECH, AccountType.TRANSIT, AccountType.CREDIT_CARD, AccountType.CASH).forEach { t ->
                            val isSel = selectedType == t
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ShadcnTheme.colors.primary.copy(alpha = 0.2f) else ShadcnTheme.colors.secondary)
                                    .border(1.dp, if (isSel) ShadcnTheme.colors.primary else ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedType = t }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(t) {
                                        AccountType.DEBIT_CARD -> "Debit"
                                        AccountType.FINTECH -> "FinTech"
                                        AccountType.TRANSIT -> "Transit"
                                        AccountType.CREDIT_CARD -> "Credit"
                                        AccountType.CASH -> "Cash"
                                    },
                                    color = if (isSel) ShadcnTheme.colors.primary else ShadcnTheme.colors.foreground,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Starting Balance
                ShadcnInput(
                    value = balanceText,
                    onValueChange = { balanceText = it.filter { ch -> ch.isDigit() } },
                    label = "Initial Balance (KRW)",
                    placeholder = "0",
                    prefix = "₩"
                )

                // Quick Balance Adders
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(50000L, 100000L, 500000L, 1000000L).forEach { amt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ShadcnTheme.colors.muted)
                                .clickable {
                                    val curr = balanceText.toLongOrNull() ?: 0L
                                    balanceText = (curr + amt).toString()
                                }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${CurrencyFormatter.format(amt).replace("₩", "")}",
                                color = ShadcnTheme.colors.mutedForeground,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Last 4 Digits
                if (selectedType != AccountType.CASH) {
                    ShadcnInput(
                        value = lastFour,
                        onValueChange = { if (it.length <= 4) lastFour = it.filter { ch -> ch.isDigit() } },
                        label = "Card Last 4 Digits",
                        placeholder = "e.g. 4892"
                    )
                }

                // Color Theme Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Card Theme", color = ShadcnTheme.colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cardThemePresets) { preset ->
                            val isSel = selectedPreset == preset
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(preset.previewColor)
                                    .border(
                                        width = if (isSel) 3.dp else 1.dp,
                                        color = if (isSel) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedPreset = preset },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Text(text = "✓", color = if (preset.previewColor == Color(0xFFFEE500)) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                // Default switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Primary Card", color = ShadcnTheme.colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Use as default payment method", color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ShadcnTheme.colors.primary,
                            uncheckedThumbColor = ShadcnTheme.colors.mutedForeground,
                            uncheckedTrackColor = ShadcnTheme.colors.muted
                        )
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = onDismiss, modifier = Modifier.padding(end = 8.dp))
                    ShadcnButtonText(
                        text = "Save Card",
                        variant = ButtonVariant.PRIMARY,
                        enabled = name.isNotBlank(),
                        onClick = {
                            if (name.isNotBlank()) {
                                val newAcc = Account(
                                    id = "acc_${UUID.randomUUID().toString().take(8)}",
                                    name = name.trim(),
                                    type = selectedType,
                                    balance = currentBalance,
                                    colorStartHex = selectedPreset.startHex,
                                    colorEndHex = selectedPreset.endHex,
                                    lastFour = if (selectedType == AccountType.CASH) "CASH" else lastFour.ifEmpty { "0000" },
                                    issuer = issuer.ifBlank { "Card" },
                                    isDefault = isDefault
                                )
                                onSave(newAcc)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditAccountDialog(
    account: Account?,
    onDismiss: () -> Unit,
    issuers: List<String> = emptyList(),
    onSave: (Account) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    canDelete: Boolean = true
) {
    if (account == null) return

    var name by remember(account) { mutableStateOf(account.name) }
    var selectedType by remember(account) { mutableStateOf(account.type) }
    var issuer by remember(account) { mutableStateOf(account.issuer) }
    var balanceText by remember(account) { mutableStateOf(account.balance.toString()) }
    var lastFour by remember(account) { mutableStateOf(account.lastFour) }
    var selectedPreset by remember(account) {
        mutableStateOf(cardThemePresets.firstOrNull { it.startHex.equals(account.colorStartHex, ignoreCase = true) } ?: cardThemePresets[0])
    }
    var isDefault by remember(account) { mutableStateOf(account.isDefault) }

    val currentBalance = balanceText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentPadding = 20.dp
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Card / Account",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (onDelete != null && canDelete) {
                        ShadcnButton(
                            onClick = {
                                onDelete(account.id)
                                onDismiss()
                            },
                            variant = ButtonVariant.DESTRUCTIVE,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Card Preview
                val startCol = ColorParser.parse(selectedPreset.startHex, Color(0xFF0064FF))
                val endCol = ColorParser.parse(selectedPreset.endHex, Color(0xFF0038A8))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(startCol, endCol)))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = issuer.uppercase(),
                                color = if (selectedPreset.previewColor == Color(0xFFFEE500)) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isDefault) {
                                ShadcnBadge(text = "PRIMARY", variant = BadgeVariant.DEFAULT)
                            }
                        }

                        Text(
                            text = if (selectedType == AccountType.CASH) "PHYSICAL CASH" else "•••• •••• •••• ${lastFour.ifEmpty { "0000" }}",
                            color = if (selectedPreset.previewColor == Color(0xFFFEE500)) Color.Black.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = name,
                                color = if (selectedPreset.previewColor == Color(0xFFFEE500)) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = CurrencyFormatter.format(currentBalance),
                                color = if (selectedPreset.previewColor == Color(0xFFFEE500)) Color.Black else Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Name
                ShadcnInput(value = name, onValueChange = { name = it }, label = "Card Name", placeholder = "Name")

                // Issuer
                ShadcnInput(value = issuer, onValueChange = { issuer = it }, label = "Card Issuer / Bank", placeholder = "Issuer")
                val activeIssuers = if (issuers.isNotEmpty()) issuers else listOf("Aether Genshin", "Kakao Bank", "Toss Bank", "Shinhan Card", "KB Kookmin", "Hyundai Card", "T-Money", "Cash Wallet", "Woori Bank", "Hana Card")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(activeIssuers) { iss ->
                        val isSel = issuer.equals(iss, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) ShadcnTheme.colors.primary.copy(alpha = 0.2f) else ShadcnTheme.colors.secondary)
                                .border(1.dp, if (isSel) ShadcnTheme.colors.primary else ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                .clickable { issuer = iss }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = iss,
                                color = if (isSel) ShadcnTheme.colors.primary else ShadcnTheme.colors.foreground,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Balance
                ShadcnInput(
                    value = balanceText,
                    onValueChange = { balanceText = it.filter { ch -> ch.isDigit() } },
                    label = "Balance (KRW)",
                    placeholder = "0",
                    prefix = "₩"
                )

                // Last 4 Digits
                if (selectedType != AccountType.CASH) {
                    ShadcnInput(
                        value = lastFour,
                        onValueChange = { if (it.length <= 4) lastFour = it.filter { ch -> ch.isDigit() } },
                        label = "Card Last 4 Digits",
                        placeholder = "e.g. 4892"
                    )
                }

                // Color Theme Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Card Theme", color = ShadcnTheme.colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cardThemePresets) { preset ->
                            val isSel = selectedPreset == preset
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(preset.previewColor)
                                    .border(
                                        width = if (isSel) 3.dp else 1.dp,
                                        color = if (isSel) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedPreset = preset },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Text(text = "✓", color = if (preset.previewColor == Color(0xFFFEE500)) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                // Default switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Primary Card", color = ShadcnTheme.colors.foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Use as default payment method", color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ShadcnTheme.colors.primary,
                            uncheckedThumbColor = ShadcnTheme.colors.mutedForeground,
                            uncheckedTrackColor = ShadcnTheme.colors.muted
                        )
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = onDismiss, modifier = Modifier.padding(end = 8.dp))
                    ShadcnButtonText(
                        text = "Save Changes",
                        variant = ButtonVariant.PRIMARY,
                        enabled = name.isNotBlank(),
                        onClick = {
                            if (name.isNotBlank()) {
                                val updatedAcc = account.copy(
                                    name = name.trim(),
                                    type = selectedType,
                                    balance = currentBalance,
                                    colorStartHex = selectedPreset.startHex,
                                    colorEndHex = selectedPreset.endHex,
                                    lastFour = if (selectedType == AccountType.CASH) "CASH" else lastFour.ifEmpty { "0000" },
                                    issuer = issuer.ifBlank { "Card" },
                                    isDefault = isDefault
                                )
                                onSave(updatedAcc)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ManageAccountsDialog(
    isOpen: Boolean,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onAddNew: () -> Unit,
    onEdit: (Account) -> Unit,
    onSetDefault: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentPadding = 20.dp
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Manage Cards & Wallets",
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${accounts.size} active payment methods",
                            color = ShadcnTheme.colors.mutedForeground,
                            fontSize = 12.sp
                        )
                    }
                    ShadcnButtonText(
                        text = "+ Add Card",
                        variant = ButtonVariant.PRIMARY,
                        onClick = {
                            onDismiss()
                            onAddNew()
                        }
                    )
                }

                // Account items list
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    accounts.forEach { acc ->
                        val startCol = ColorParser.parse(acc.colorStartHex, Color(0xFF0064FF))
                        val endCol = ColorParser.parse(acc.colorEndHex, Color(0xFF0038A8))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ShadcnTheme.colors.secondary)
                                .border(1.dp, ShadcnTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Color swatch indicator
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.horizontalGradient(listOf(startCol, endCol))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (acc.type == AccountType.TRANSIT) Icons.Default.DirectionsTransit else Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acc.name,
                                        color = ShadcnTheme.colors.foreground,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${acc.issuer} • ${if (acc.type == AccountType.CASH) "Cash" else "•••• ${acc.lastFour}"}",
                                        color = ShadcnTheme.colors.mutedForeground,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = CurrencyFormatter.format(acc.balance),
                                            color = ShadcnTheme.colors.krwAccent,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (acc.isDefault) {
                                            ShadcnBadge(text = "PRIMARY", variant = BadgeVariant.DEFAULT)
                                        }
                                    }
                                }
                            }

                            // Actions
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (!acc.isDefault) {
                                    ShadcnButton(
                                        onClick = { onSetDefault(acc.id) },
                                        variant = ButtonVariant.OUTLINE,
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = "Set Primary", tint = ShadcnTheme.colors.foreground, modifier = Modifier.size(16.dp))
                                    }
                                }

                                ShadcnButton(
                                    onClick = {
                                        onDismiss()
                                        onEdit(acc)
                                    },
                                    variant = ButtonVariant.SECONDARY,
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ShadcnTheme.colors.foreground, modifier = Modifier.size(16.dp))
                                }

                                if (accounts.size > 1) {
                                    ShadcnButton(
                                        onClick = { onDelete(acc.id) },
                                        variant = ButtonVariant.DESTRUCTIVE,
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    ShadcnButtonText(text = "Done", variant = ButtonVariant.OUTLINE, onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
fun SetBudgetDialog(
    isOpen: Boolean,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (categoryId: String, monthlyLimit: Long) -> Unit,
    initialCategoryId: String = "",
    initialLimit: Long = 0L
) {
    if (!isOpen) return

    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
    var selectedCategoryId by remember {
        mutableStateOf(if (initialCategoryId.isNotEmpty()) initialCategoryId else expenseCategories.firstOrNull()?.id ?: "")
    }
    var limitText by remember { mutableStateOf(if (initialLimit > 0) initialLimit.toString() else "") }
    val currentLimit = limitText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Set Monthly Budget Limit",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Column {
                    Text(
                        text = "Category",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(expenseCategories) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            val catColor = ColorParser.parse(cat.colorHex, ShadcnTheme.colors.primary)

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) catColor.copy(alpha = 0.25f) else ShadcnTheme.colors.secondary)
                                    .border(1.5.dp, if (isSelected) catColor else ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategoryId = cat.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CategoryIcon(iconName = cat.iconName, tint = catColor, modifier = Modifier.size(16.dp))
                                Text(
                                    text = cat.name,
                                    color = if (isSelected) Color.White else ShadcnTheme.colors.foreground,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                ShadcnInput(
                    value = limitText,
                    onValueChange = { input -> limitText = input.filter { it.isDigit() } },
                    label = "Monthly Limit in Won",
                    placeholder = "e.g. 500000",
                    prefix = "₩"
                )

                KrwQuickSelector(currentAmount = currentLimit, onAmountSelected = { limitText = if (it > 0) it.toString() else "" })

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = onDismiss, modifier = Modifier.padding(end = 8.dp))
                    ShadcnButtonText(
                        text = "Save Limit",
                        variant = ButtonVariant.PRIMARY,
                        enabled = currentLimit > 0 && selectedCategoryId.isNotEmpty(),
                        onClick = {
                            if (currentLimit > 0 && selectedCategoryId.isNotEmpty()) {
                                onSave(selectedCategoryId, currentLimit)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddSavingsGoalDialog(isOpen: Boolean, onDismiss: () -> Unit, onSave: (SavingsGoal) -> Unit) {
    if (!isOpen) return
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initialSavedText by remember { mutableStateOf("0") }
    var targetDateText by remember { mutableStateOf(LocalDate.now().plusMonths(6).toString()) }
    var note by remember { mutableStateOf("") }

    val targetAmount = targetText.toLongOrNull() ?: 0L
    val initialSaved = initialSavedText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "New Savings Goal",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                ShadcnInput(value = name, onValueChange = { name = it }, label = "Goal Name", placeholder = "e.g. Jeju Trip, New Laptop")
                ShadcnInput(value = targetText, onValueChange = { input -> targetText = input.filter { it.isDigit() } }, label = "Target Amount (Won)", prefix = "₩")
                KrwQuickSelector(currentAmount = targetAmount, onAmountSelected = { targetText = if (it > 0) it.toString() else "" })
                ShadcnInput(value = initialSavedText, onValueChange = { input -> initialSavedText = input.filter { it.isDigit() } }, label = "Current Saved (Won)", prefix = "₩")
                ShadcnInput(value = targetDateText, onValueChange = { targetDateText = it }, label = "Target Date (YYYY-MM-DD)")
                ShadcnInput(value = note, onValueChange = { note = it }, label = "Notes (Optional)", placeholder = "e.g. Flight tickets & hotel reservation")

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = onDismiss, modifier = Modifier.padding(end = 8.dp))
                    ShadcnButtonText(
                        text = "Create Goal",
                        variant = ButtonVariant.PRIMARY,
                        enabled = name.isNotBlank() && targetAmount > 0,
                        onClick = {
                            if (name.isNotBlank() && targetAmount > 0) {
                                onSave(SavingsGoal(UUID.randomUUID().toString(), name.trim(), targetAmount, initialSaved, targetDateText.trim().ifEmpty { LocalDate.now().toString() }, note.trim()))
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddGoalFundsDialog(goal: SavingsGoal?, onDismiss: () -> Unit, onConfirm: (amount: Long) -> Unit) {
    if (goal == null) return
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toLongOrNull() ?: 0L

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(text = "Deposit to: ${goal.name}", color = ShadcnTheme.colors.foreground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Current: ${CurrencyFormatter.format(goal.currentAmount)} / ${CurrencyFormatter.format(goal.targetAmount)}",
                    color = ShadcnTheme.colors.mutedForeground,
                    fontSize = 13.sp
                )
                ShadcnInput(value = amountText, onValueChange = { input -> amountText = input.filter { it.isDigit() } }, label = "Deposit Amount in Won", prefix = "₩")
                KrwQuickSelector(currentAmount = amount, onAmountSelected = { amountText = if (it > 0) it.toString() else "" })

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = onDismiss, modifier = Modifier.padding(end = 8.dp))
                    ShadcnButtonText(
                        text = "Deposit",
                        variant = ButtonVariant.PRIMARY,
                        enabled = amount > 0,
                        onClick = {
                            if (amount > 0) {
                                onConfirm(amount)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditAllocationRatioDialog(
    isOpen: Boolean,
    currentNeeds: Int,
    currentWants: Int,
    currentSavings: Int,
    onDismiss: () -> Unit,
    onSave: (needs: Int, wants: Int, savings: Int) -> Unit
) {
    if (!isOpen) return

    var needs by remember(isOpen, currentNeeds) { mutableIntStateOf(currentNeeds) }
    var wants by remember(isOpen, currentWants) { mutableIntStateOf(currentWants) }
    var savings by remember(isOpen, currentSavings) { mutableIntStateOf(currentSavings) }

    val total = needs + wants + savings
    val isValid = total == 100

    Dialog(onDismissRequest = onDismiss) {
        ShadcnCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configure Allocation Ratio",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Customize your target budget distribution. The 3 categories must sum to exactly 100%.",
                    color = ShadcnTheme.colors.mutedForeground,
                    fontSize = 13.sp
                )

                // Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Presets",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetButton(title = "50/30/20", subtitle = "Standard", isSelected = needs == 50 && wants == 30 && savings == 20, modifier = Modifier.weight(1f)) {
                            needs = 50; wants = 30; savings = 20
                        }
                        PresetButton(title = "60/20/20", subtitle = "Living", isSelected = needs == 60 && wants == 20 && savings == 20, modifier = Modifier.weight(1f)) {
                            needs = 60; wants = 20; savings = 20
                        }
                        PresetButton(title = "70/20/10", subtitle = "Essentials", isSelected = needs == 70 && wants == 20 && savings == 10, modifier = Modifier.weight(1f)) {
                            needs = 70; wants = 20; savings = 10
                        }
                        PresetButton(title = "40/30/30", subtitle = "Savings", isSelected = needs == 40 && wants == 30 && savings == 30, modifier = Modifier.weight(1f)) {
                            needs = 40; wants = 30; savings = 30
                        }
                    }
                }

                // Ratio Adjusters
                RatioAdjusterRow(
                    title = "Needs (Essential)",
                    description = "Rent, food, transit, bills",
                    color = Color(0xFFF59E0B),
                    percentage = needs,
                    onMinus = { if (needs >= 5) needs -= 5 },
                    onPlus = { if (needs <= 95) needs += 5 }
                )

                RatioAdjusterRow(
                    title = "Wants (Discretionary)",
                    description = "Shopping, cafe, leisure",
                    color = Color(0xFFA855F7),
                    percentage = wants,
                    onMinus = { if (wants >= 5) wants -= 5 },
                    onPlus = { if (wants <= 95) wants += 5 }
                )

                RatioAdjusterRow(
                    title = "Savings & Investments",
                    description = "Emergency funds, deposits",
                    color = Color(0xFF10B981),
                    percentage = savings,
                    onMinus = { if (savings >= 5) savings -= 5 },
                    onPlus = { if (savings <= 95) savings += 5 }
                )

                // Total Summary Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isValid) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF450A0A).copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isValid) "Total: 100% (Balanced ✓)" else "Total: $total% (Must equal 100%)",
                        color = if (isValid) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShadcnButtonText(text = "Cancel", variant = ButtonVariant.GHOST, onClick = onDismiss, modifier = Modifier.padding(end = 8.dp))
                    ShadcnButtonText(
                        text = "Save Ratio",
                        variant = ButtonVariant.PRIMARY,
                        enabled = isValid,
                        onClick = {
                            if (isValid) {
                                onSave(needs, wants, savings)
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) ShadcnTheme.colors.primary.copy(alpha = 0.2f) else ShadcnTheme.colors.secondary)
            .border(1.5.dp, if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.cardBorder, shape)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.foreground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = subtitle, color = ShadcnTheme.colors.mutedForeground, fontSize = 9.sp)
    }
}

@Composable
private fun RatioAdjusterRow(
    title: String,
    description: String,
    color: Color,
    percentage: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ShadcnTheme.colors.secondary)
            .border(1.dp, ShadcnTheme.colors.cardBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = description, color = ShadcnTheme.colors.mutedForeground, fontSize = 11.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ShadcnTheme.colors.muted)
                    .clickable { onMinus() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "-", color = ShadcnTheme.colors.foreground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "$percentage%",
                color = ShadcnTheme.colors.foreground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(42.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ShadcnTheme.colors.muted)
                    .clickable { onPlus() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = ShadcnTheme.colors.foreground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
