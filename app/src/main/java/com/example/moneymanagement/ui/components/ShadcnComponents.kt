package com.example.moneymanagement.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.ui.theme.ShadcnTheme
import com.example.moneymanagement.util.CurrencyFormatter

// --- CARD ---
@Composable
fun ShadcnCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = ShadcnTheme.colors.card,
    borderColor: Color = ShadcnTheme.colors.cardBorder,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .padding(contentPadding),
        content = content
    )
}

// --- BUTTON ---
enum class ButtonVariant { PRIMARY, SECONDARY, OUTLINE, GHOST, DESTRUCTIVE }

@Composable
fun ShadcnButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    shape: Shape = RoundedCornerShape(8.dp),
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val targetBgColor = when (variant) {
        ButtonVariant.PRIMARY -> if (isHovered) ShadcnTheme.colors.primary.copy(alpha = 0.88f) else ShadcnTheme.colors.primary
        ButtonVariant.SECONDARY -> if (isHovered) ShadcnTheme.colors.secondary.copy(alpha = 0.8f) else ShadcnTheme.colors.secondary
        ButtonVariant.OUTLINE -> if (isHovered) ShadcnTheme.colors.muted else Color.Transparent
        ButtonVariant.GHOST -> if (isHovered) ShadcnTheme.colors.muted else Color.Transparent
        ButtonVariant.DESTRUCTIVE -> if (isHovered) Color(0xFFDC2626) else Color(0xFFEF4444)
    }

    val targetTextColor = when (variant) {
        ButtonVariant.PRIMARY -> ShadcnTheme.colors.primaryForeground
        ButtonVariant.SECONDARY -> ShadcnTheme.colors.secondaryForeground
        ButtonVariant.OUTLINE -> ShadcnTheme.colors.foreground
        ButtonVariant.GHOST -> ShadcnTheme.colors.foreground
        ButtonVariant.DESTRUCTIVE -> Color.White
    }

    val animatedBg by animateColorAsState(if (enabled) targetBgColor else targetBgColor.copy(alpha = 0.4f))

    val borderModifier = if (variant == ButtonVariant.OUTLINE) {
        Modifier.border(1.dp, ShadcnTheme.colors.cardBorder, shape)
    } else Modifier

    Row(
        modifier = modifier
            .then(borderModifier)
            .clip(shape)
            .background(animatedBg)
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null) { onClick() }
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun ShadcnButtonText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true
) {
    val textColor = when (variant) {
        ButtonVariant.PRIMARY -> ShadcnTheme.colors.primaryForeground
        ButtonVariant.SECONDARY -> ShadcnTheme.colors.secondaryForeground
        ButtonVariant.OUTLINE -> ShadcnTheme.colors.foreground
        ButtonVariant.GHOST -> ShadcnTheme.colors.foreground
        ButtonVariant.DESTRUCTIVE -> Color.White
    }

    ShadcnButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        enabled = enabled
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// --- INPUT ---
@Composable
fun ShadcnInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    prefix: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = if (isFocused) ShadcnTheme.colors.primary else ShadcnTheme.colors.inputBorder
    val shape = RoundedCornerShape(10.dp)

    Column(modifier = modifier) {
        if (!label.isNullOrEmpty()) {
            Text(
                text = label,
                color = ShadcnTheme.colors.foreground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(ShadcnTheme.colors.input)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!prefix.isNullOrEmpty()) {
                Text(
                    text = prefix,
                    color = ShadcnTheme.colors.krwAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        color = ShadcnTheme.colors.mutedForeground.copy(alpha = 0.6f),
                        fontSize = 15.sp
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    singleLine = singleLine,
                    enabled = enabled,
                    keyboardOptions = keyboardOptions,
                    cursorBrush = SolidColor(ShadcnTheme.colors.primary),
                    interactionSource = interactionSource
                )
            }
        }
    }
}

// --- DATE PICKER FIELD ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadcnDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "Date",
    quickOptions: List<Pair<String, String>> = emptyList()
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val parsedDate = remember(value) {
        try {
            LocalDate.parse(value)
        } catch (_: Exception) {
            LocalDate.now()
        }
    }

    val displayString = remember(parsedDate) {
        val today = LocalDate.now()
        val formatted = parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)"))
        when (parsedDate) {
            today -> "$formatted • Today"
            today.minusDays(1) -> "$formatted • Yesterday"
            today.plusDays(1) -> "$formatted • Tomorrow"
            else -> formatted
        }
    }

    Column(modifier = modifier) {
        if (!label.isNullOrEmpty()) {
            Text(
                text = label,
                color = ShadcnTheme.colors.foreground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        val shape = RoundedCornerShape(10.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(ShadcnTheme.colors.input)
                .border(1.dp, ShadcnTheme.colors.inputBorder, shape)
                .clickable { showDatePicker = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = ShadcnTheme.colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = displayString,
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ShadcnTheme.colors.secondary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Pick",
                    color = ShadcnTheme.colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (quickOptions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickOptions.forEach { (chipLabel, dateVal) ->
                    val isSelected = value == dateVal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ShadcnTheme.colors.primary.copy(alpha = 0.2f) else ShadcnTheme.colors.muted)
                            .border(1.dp, if (isSelected) ShadcnTheme.colors.primary else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { onValueChange(dateVal) }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chipLabel,
                            color = if (isSelected) ShadcnTheme.colors.primary else ShadcnTheme.colors.mutedForeground,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = remember(parsedDate) {
            parsedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                ShadcnButtonText(
                    text = "Select",
                    variant = ButtonVariant.PRIMARY,
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onValueChange(selectedLocalDate.toString())
                        }
                        showDatePicker = false
                    }
                )
            },
            dismissButton = {
                ShadcnButtonText(
                    text = "Cancel",
                    variant = ButtonVariant.GHOST,
                    onClick = { showDatePicker = false }
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = ShadcnTheme.colors.card
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = ShadcnTheme.colors.card,
                    titleContentColor = ShadcnTheme.colors.foreground,
                    headlineContentColor = ShadcnTheme.colors.foreground,
                    weekdayContentColor = ShadcnTheme.colors.mutedForeground,
                    subheadContentColor = ShadcnTheme.colors.mutedForeground,
                    yearContentColor = ShadcnTheme.colors.foreground,
                    currentYearContentColor = ShadcnTheme.colors.primary,
                    selectedYearContentColor = ShadcnTheme.colors.primaryForeground,
                    selectedYearContainerColor = ShadcnTheme.colors.primary,
                    dayContentColor = ShadcnTheme.colors.foreground,
                    selectedDayContentColor = ShadcnTheme.colors.primaryForeground,
                    selectedDayContainerColor = ShadcnTheme.colors.primary,
                    todayContentColor = ShadcnTheme.colors.primary,
                    todayDateBorderColor = ShadcnTheme.colors.primary
                )
            )
        }
    }
}

// --- BADGE ---
enum class BadgeVariant { DEFAULT, SECONDARY, OUTLINE, SUCCESS, DESTRUCTIVE, WARNING, KRW }

@Composable
fun ShadcnBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.DEFAULT
) {
    val (bgColor, textColor, borderColor) = when (variant) {
        BadgeVariant.DEFAULT -> Triple(ShadcnTheme.colors.primary, ShadcnTheme.colors.primaryForeground, Color.Transparent)
        BadgeVariant.SECONDARY -> Triple(ShadcnTheme.colors.secondary, ShadcnTheme.colors.secondaryForeground, Color.Transparent)
        BadgeVariant.OUTLINE -> Triple(Color.Transparent, ShadcnTheme.colors.foreground, ShadcnTheme.colors.cardBorder)
        BadgeVariant.SUCCESS -> Triple(Color(0xFF064E3B), Color(0xFF6EE7B7), Color(0xFF047857))
        BadgeVariant.DESTRUCTIVE -> Triple(Color(0xFF450A0A), Color(0xFFFCA5A5), Color(0xFF991B1B))
        BadgeVariant.WARNING -> Triple(Color(0xFF451A03), Color(0xFFFCD34D), Color(0xFFB45309))
        BadgeVariant.KRW -> Triple(Color(0xFF1E1B4B), Color(0xFFA5B4FC), Color(0xFF4338CA))
    }

    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

// --- PROGRESS ---
@Composable
fun ShadcnProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 9.dp,
    trackColor: Color = ShadcnTheme.colors.muted,
    colorOverride: Color? = null
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = clampedProgress)

    val indicatorColor = colorOverride ?: when {
        progress >= 1.0f -> ShadcnTheme.colors.expense
        progress >= 0.75f -> ShadcnTheme.colors.warning
        else -> ShadcnTheme.colors.income
    }

    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(shape)
                .background(indicatorColor)
        )
    }
}

// --- QUICK AMOUNT CHIPS ---
@Composable
fun KrwQuickSelector(
    currentAmount: Long,
    onAmountSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickAddOptions = listOf(
        Pair("+10K", 10000L),
        Pair("+50K", 50000L),
        Pair("+100K", 100000L),
        Pair("+500K", 500000L),
        Pair("+1M", 1000000L)
    )

    Column(modifier = modifier) {
        Text(
            text = "Quick Add Amount (Won)",
            color = ShadcnTheme.colors.mutedForeground,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickAddOptions.forEach { (label, value) ->
                val shape = RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(ShadcnTheme.colors.secondary)
                        .border(1.dp, ShadcnTheme.colors.cardBorder, shape)
                        .clickable { onAmountSelected(currentAmount + value) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = label, color = ShadcnTheme.colors.foreground, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            val clearShape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier
                    .clip(clearShape)
                    .background(Color(0xFF2A1515))
                    .border(1.dp, Color(0xFF450A0A), clearShape)
                    .clickable { onAmountSelected(0L) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Reset", color = Color(0xFFFCA5A5), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// --- CATEGORY ICON ---
@Composable
fun CategoryIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    val vector: ImageVector = when (iconName) {
        "Work" -> Icons.Default.Work
        "Laptop" -> Icons.Default.Laptop
        "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
        "Gift" -> Icons.Default.CardGiftcard
        "Restaurant" -> Icons.Default.Restaurant
        "Home" -> Icons.Default.Home
        "DirectionsTransit" -> Icons.Default.DirectionsTransit
        "Receipt" -> Icons.Default.Receipt
        "LocalHospital" -> Icons.Default.LocalHospital
        "Coffee" -> Icons.Default.LocalCafe
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "Movie" -> Icons.Default.Movie
        "Palette" -> Icons.Default.Palette
        "Savings" -> Icons.Default.Savings
        "Star" -> Icons.Default.Star
        else -> Icons.Default.Category
    }

    Icon(
        imageVector = vector,
        contentDescription = iconName,
        tint = tint,
        modifier = modifier
    )
}

// --- CHARTS ---
data class ChartSlice(val label: String, val value: Long, val color: Color, val percentage: Float)

@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    totalAmount: Long,
    modifier: Modifier = Modifier,
    centerLabel: String = "Total Expense"
) {
    if (slices.isEmpty() || totalAmount == 0L) {
        Box(modifier = modifier.height(140.dp), contentAlignment = Alignment.Center) {
            Text(text = "No expenses recorded this month", color = ShadcnTheme.colors.mutedForeground, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(136.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                var currentAngle = -90f
                val strokeWidth = 20.dp.toPx()
                slices.forEach { slice ->
                    val sweep = slice.percentage * 360f
                    if (sweep > 0.5f) {
                        drawArc(
                            color = slice.color,
                            startAngle = currentAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        currentAngle += sweep
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = centerLabel, color = ShadcnTheme.colors.mutedForeground, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                Text(text = CurrencyFormatter.format(totalAmount), color = ShadcnTheme.colors.foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slices.take(5).forEach { slice ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(slice.color))
                        Text(text = slice.label, color = ShadcnTheme.colors.foreground, fontSize = 13.sp, maxLines = 1)
                    }
                    Text(text = "${(slice.percentage * 100).toInt()}%", color = ShadcnTheme.colors.mutedForeground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CashflowComparisonBar(income: Long, expense: Long, modifier: Modifier = Modifier) {
    val total = (income + expense).coerceAtLeast(1L)
    val incomeRatio = (income.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val expenseRatio = (expense.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Income: ${CurrencyFormatter.format(income)}", color = ShadcnTheme.colors.income, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Expense: ${CurrencyFormatter.format(expense)}", color = ShadcnTheme.colors.expense, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(ShadcnTheme.colors.muted)
        ) {
            if (incomeRatio > 0.01f) {
                Box(modifier = Modifier.fillMaxHeight().weight(incomeRatio.coerceAtLeast(0.01f)).background(ShadcnTheme.colors.income))
            }
            if (expenseRatio > 0.01f) {
                Box(modifier = Modifier.fillMaxHeight().weight(expenseRatio.coerceAtLeast(0.01f)).background(ShadcnTheme.colors.expense))
            }
        }
    }
}
