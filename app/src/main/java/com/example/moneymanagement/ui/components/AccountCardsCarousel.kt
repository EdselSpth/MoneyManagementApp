package com.example.moneymanagement.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.data.model.Account
import com.example.moneymanagement.data.model.AccountType
import com.example.moneymanagement.ui.theme.ShadcnTheme
import com.example.moneymanagement.util.ColorParser
import com.example.moneymanagement.util.CurrencyFormatter

/**
 * Modern FinTech Card Carousel
 * Displays Korean payment methods:
 * - T-Money Transit Card (with low balance alert)
 * - Toss Bank Card (Primary FinTech account)
 * - Shinhan / Kakao Debit Cards (Salary & Living)
 * - Cash Wallet
 */
@Composable
fun AccountCardsCarousel(
    accounts: List<Account>,
    onCardClick: (Account) -> Unit,
    onTopUpClick: (Account) -> Unit,
    onAddNewCard: () -> Unit,
    onManageCards: () -> Unit,
    modifier: Modifier = Modifier,
    onTransferClick: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = "My Cards & Wallets",
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ShadcnBadge(
                    text = "${accounts.size} Cards",
                    variant = BadgeVariant.SECONDARY
                )
            }

            ShadcnButtonText(
                text = "Manage",
                variant = ButtonVariant.OUTLINE,
                onClick = onManageCards
            )
        }

        // Action Row for Cards & Wallets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onTransferClick != null && accounts.size >= 2) {
                ShadcnButton(
                    onClick = onTransferClick,
                    modifier = Modifier.weight(1f),
                    variant = ButtonVariant.SECONDARY,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    CategoryIcon(
                        iconName = "SwapHoriz",
                        tint = ShadcnTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Transfer Funds",
                        color = ShadcnTheme.colors.foreground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ShadcnButton(
                onClick = onAddNewCard,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.PRIMARY,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = ShadcnTheme.colors.primaryForeground,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Card / Wallet",
                    color = ShadcnTheme.colors.primaryForeground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Horizontal Card Carousel
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(accounts, key = { it.id }) { account ->
                FinTechCardItem(
                    account = account,
                    onClick = { onCardClick(account) },
                    onTopUp = { onTopUpClick(account) }
                )
            }

            item {
                AddCardPlaceholderItem(onClick = onAddNewCard)
            }
        }
    }
}

@Composable
fun AddCardPlaceholderItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(160.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ShadcnTheme.colors.secondary.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = ShadcnTheme.colors.cardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ShadcnTheme.colors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Card",
                    tint = ShadcnTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Add Card / Wallet",
                color = ShadcnTheme.colors.foreground,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Toss, Kakao, Transit",
                color = ShadcnTheme.colors.mutedForeground,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun FinTechCardItem(
    account: Account,
    onClick: () -> Unit,
    onTopUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = ColorParser.parse(account.colorStartHex, Color(0xFF0064FF))
    val endColor = ColorParser.parse(account.colorEndHex, Color(0xFF0038A8))
    val isTransit = account.type == AccountType.TRANSIT
    val isLowTransitBalance = isTransit && account.balance < 10000L

    Box(
        modifier = modifier
            .width(280.dp)
            .height(168.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = startColor.copy(alpha = 0.35f),
                spotColor = endColor.copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(startColor, endColor),
                    start = Offset(0f, 0f),
                    end = Offset(700f, 400f)
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Issuer & Contactless NFC wave symbol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isTransit) {
                        Icon(
                            Icons.Default.DirectionsTransit,
                            contentDescription = "Transit",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = account.issuer.uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (account.isDefault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRIMARY",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Contactless waves graphic
                    ContactlessWaveGlyph(modifier = Modifier.size(16.dp))
                }
            }

            // Middle: EMV Chip & Masked Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Realistic EMV Chip
                EmvChipGraphic(modifier = Modifier.size(width = 36.dp, height = 26.dp))

                // Card Number display
                Text(
                    text = if (account.type == AccountType.CASH) "WALLET CASH"
                    else "•••• •••• •••• ${account.lastFour.ifEmpty { "1084" }}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
            }

            // Bottom Row: Account Name & Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = account.name,
                        color = Color.White.copy(alpha = 0.80f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = CurrencyFormatter.format(account.balance),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Top Up quick pill button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable { onTopUp() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Top Up",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Top-Up",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Low balance alert banner for Transit card
        if (isLowTransitBalance) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEF4444))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "Low Transit Balance (< ₩10,000)",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Realistic golden metallic EMV chip graphic
 */
@Composable
private fun EmvChipGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val chipColor = Color(0xFFFFD700)
        val circuitColor = Color(0xFFB8860B)

        // Chip body
        drawRoundRect(
            color = chipColor,
            size = size,
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Circuit lines
        val stroke = Stroke(width = 1.2.dp.toPx())
        // Horizontal divider
        drawLine(
            color = circuitColor,
            start = Offset(0f, h * 0.5f),
            end = Offset(w, h * 0.5f),
            strokeWidth = 1.2.dp.toPx()
        )
        // Center inner rectangle
        drawRoundRect(
            color = circuitColor,
            topLeft = Offset(w * 0.35f, h * 0.25f),
            size = Size(w * 0.30f, h * 0.50f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = stroke
        )
    }
}

/**
 * Contactless wave NFC graphic
 */
@Composable
private fun ContactlessWaveGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val waveColor = Color.White.copy(alpha = 0.85f)
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)

        // Wave 1
        drawArc(
            color = waveColor,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.1f, h * 0.1f),
            size = Size(w * 0.4f, h * 0.8f),
            style = stroke
        )
        // Wave 2
        drawArc(
            color = waveColor,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.35f, h * 0.25f),
            size = Size(w * 0.35f, h * 0.5f),
            style = stroke
        )
    }
}
