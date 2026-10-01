package com.example.moneymanagement.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.data.model.Account
import com.example.moneymanagement.data.model.AccountType
import com.example.moneymanagement.data.repository.FinanceRepository
import com.example.moneymanagement.ui.components.*
import com.example.moneymanagement.ui.theme.ShadcnTheme
import com.example.moneymanagement.util.ColorParser
import com.example.moneymanagement.util.CurrencyFormatter
import java.io.File
import java.time.LocalDateTime
import com.example.moneymanagement.ui.theme.AppThemeMode
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    context: Context,
    repository: FinanceRepository,
    themeMode: AppThemeMode,
    onSelectThemeMode: (AppThemeMode) -> Unit,
    onOpenEditRatio: () -> Unit,
    onAddNewCard: () -> Unit,
    onManageCards: () -> Unit,
    onEditCard: (Account) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var importJsonText by remember { mutableStateOf("") }
    var showImportField by remember { mutableStateOf(false) }

    val needsRatio by repository.needsRatio.collectAsState()
    val wantsRatio by repository.wantsRatio.collectAsState()
    val savingsRatio by repository.savingsRatio.collectAsState()
    val issuers by repository.issuers.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (onBack != null) {
                        ShadcnButton(
                            onClick = onBack,
                            variant = ButtonVariant.OUTLINE,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShadcnTheme.colors.foreground, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column {
                        Text(text = "Settings & Data", color = ShadcnTheme.colors.foreground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Preferences, ratios & SQLite database", color = ShadcnTheme.colors.mutedForeground, fontSize = 13.sp)
                    }
                }
            }
        }

        // Budget Ratio Setting Card
        item {
            ShadcnCard(contentPadding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Budget Allocation Ratio", color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Customize your target income breakdown rule across Needs, Wants, and Savings.",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShadcnBadge(text = "Needs: $needsRatio%", variant = BadgeVariant.WARNING)
                            ShadcnBadge(text = "Wants: $wantsRatio%", variant = BadgeVariant.SECONDARY)
                            ShadcnBadge(text = "Savings: $savingsRatio%", variant = BadgeVariant.SUCCESS)
                        }

                        ShadcnButtonText(text = "Edit Ratio", variant = ButtonVariant.PRIMARY, onClick = onOpenEditRatio)
                    }
                }
            }
        }

        // Wallets & Cards Setting Card
        item {
            val accounts by repository.accounts.collectAsState()
            ShadcnCard(contentPadding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "My Cards & Wallets", color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${accounts.size} payment methods configured", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                        }
                        ShadcnButtonText(text = "+ Add Card", variant = ButtonVariant.PRIMARY, onClick = onAddNewCard)
                    }

                    // Cards list
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accounts.forEach { acc ->
                            val startCol = ColorParser.parse(acc.colorStartHex, Color(0xFF0064FF))
                            val endCol = ColorParser.parse(acc.colorEndHex, Color(0xFF0038A8))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShadcnTheme.colors.secondary)
                                    .border(1.dp, ShadcnTheme.colors.cardBorder, RoundedCornerShape(10.dp))
                                    .clickable { onEditCard(acc) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Brush.horizontalGradient(listOf(startCol, endCol))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (acc.type == AccountType.TRANSIT) Icons.Default.DirectionsTransit else Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = acc.name,
                                            color = ShadcnTheme.colors.foreground,
                                            fontSize = 13.sp,
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
                                        if (acc.isDefault) {
                                            ShadcnBadge(
                                                text = "PRIMARY",
                                                variant = BadgeVariant.DEFAULT,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = CurrencyFormatter.format(acc.balance),
                                        color = ShadcnTheme.colors.krwAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = ShadcnTheme.colors.mutedForeground,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    ShadcnButtonText(
                        text = "Manage All Cards & Set Primary",
                        variant = ButtonVariant.OUTLINE,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onManageCards
                    )
                }
            }
        }

        // Appearance
        item {
            ShadcnCard(contentPadding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(text = "Appearance & Currency", color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Color Theme Palette", color = ShadcnTheme.colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(AppThemeMode.AETHER, AppThemeMode.DARK, AppThemeMode.LIGHT).forEach { mode ->
                                val isSelected = themeMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ShadcnTheme.colors.primary.copy(alpha = 0.2f) else ShadcnTheme.colors.secondary)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.cardBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onSelectThemeMode(mode) }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text = "${mode.icon} ${mode.title}",
                                            color = if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.foreground,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Text(
                                            text = mode.subtitle,
                                            color = ShadcnTheme.colors.mutedForeground,
                                            fontSize = 9.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Primary Currency", color = ShadcnTheme.colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Korean Won (₩ KRW)", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                        }

                        ShadcnBadge(text = "₩ Korean Won", variant = BadgeVariant.KRW)
                    }
                }
            }
        }

        // Bank & Card Issuers Management
        item {
            var newIssuerInput by remember { mutableStateOf("") }
            ShadcnCard(contentPadding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Bank & Card Issuers", color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Configure preset issuers for your cards & wallets", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)
                        }
                        ShadcnButtonText(
                            text = "Reset",
                            variant = ButtonVariant.GHOST,
                            onClick = { repository.resetIssuers() }
                        )
                    }

                    // Add Issuer Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShadcnInput(
                            value = newIssuerInput,
                            onValueChange = { newIssuerInput = it },
                            placeholder = "New issuer (e.g. IBK Bank, Lotte Card)",
                            modifier = Modifier.weight(1f)
                        )
                        ShadcnButtonText(
                            text = "+ Add",
                            variant = ButtonVariant.PRIMARY,
                            enabled = newIssuerInput.isNotBlank(),
                            onClick = {
                                repository.addIssuer(newIssuerInput)
                                newIssuerInput = ""
                            }
                        )
                    }

                    // Issuer Chips
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        issuers.forEach { iss ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ShadcnTheme.colors.secondary)
                                    .border(1.dp, ShadcnTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                                    .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = iss,
                                    color = ShadcnTheme.colors.foreground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(ShadcnTheme.colors.mutedForeground.copy(alpha = 0.2f))
                                        .clickable { repository.removeIssuer(iss) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove $iss",
                                        tint = ShadcnTheme.colors.foreground,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Database & Backup
        item {
            ShadcnCard(contentPadding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(text = "Local SQLite Backup", color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "100% offline local database (/databases/moneymanagement.db)", color = ShadcnTheme.colors.mutedForeground, fontSize = 12.sp)

                    if (backupStatusMessage != null) {
                        ShadcnBadge(text = backupStatusMessage!!, variant = BadgeVariant.SUCCESS)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShadcnButtonText(
                            text = "📤 Export JSON",
                            variant = ButtonVariant.PRIMARY,
                            onClick = {
                                val json = repository.exportBackupJson()
                                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                val backupFile = File(context.filesDir, "backup_$timestamp.json")
                                backupFile.writeText(json)
                                backupStatusMessage = "Backup saved: ${backupFile.name}"
                            }
                        )

                        ShadcnButtonText(
                            text = "📥 Restore JSON",
                            variant = ButtonVariant.OUTLINE,
                            onClick = { showImportField = !showImportField }
                        )
                    }

                    if (showImportField) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ShadcnInput(value = importJsonText, onValueChange = { importJsonText = it }, label = "Paste JSON payload", placeholder = "{\n  \"exportedAt\": ...\n}", singleLine = false, modifier = Modifier.height(100.dp))
                            ShadcnButtonText(
                                text = "Run Restore",
                                variant = ButtonVariant.PRIMARY,
                                enabled = importJsonText.isNotBlank(),
                                onClick = {
                                    val success = repository.importBackupJson(importJsonText)
                                    backupStatusMessage = if (success) "Restored successfully!" else "Restore failed: Invalid format"
                                    if (success) {
                                        importJsonText = ""
                                        showImportField = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // About
        item {
            ShadcnCard(contentPadding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "About Application", color = ShadcnTheme.colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Shiota Wallet v1.0.0 (Kotlin & Jetpack Compose)", color = ShadcnTheme.colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Designed for modern personal money management in Korean Won (₩), featuring customizable allocation ratios, multi-wallet tracking, category limits, and digital savings goals.",
                        color = ShadcnTheme.colors.mutedForeground,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
