package com.example.moneymanagement.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.MobileNavScreen
import com.example.moneymanagement.ui.theme.ShadcnTheme

/**
 * Apple-style Floating Dock Navigation Bar
 * Characterized by:
 * - Floating pill island with frosted glass blur & soft shadow
 * - Strictly uniform button widths (Modifier.weight(1f)) for balanced symmetry
 * - Custom SF Symbol inspired vector icons (bespoke drawn with Canvas)
 * - Micro-interactions: icon bounce pop on selection, glowing background, indicator dots
 */
@Composable
fun AppleFloatingNavBar(
    currentScreen: MobileNavScreen,
    onScreenSelected: (MobileNavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Island Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(32.dp))
                // Translucent Apple dock background
                .background(ShadcnTheme.colors.card.copy(alpha = 0.94f))
                .border(
                    width = 1.dp,
                    color = ShadcnTheme.colors.cardBorder.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MobileNavScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    AppleNavItem(
                        screen = screen,
                        isSelected = isSelected,
                        onClick = { onScreenSelected(screen) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppleNavItem(
    screen: MobileNavScreen,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "iconScale"
    )

    val activeColor = ShadcnTheme.colors.primary
    val inactiveColor = ShadcnTheme.colors.mutedForeground

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        label = "iconTint"
    )

    val itemBgColor by animateColorAsState(
        targetValue = if (isSelected) activeColor.copy(alpha = 0.12f) else Color.Transparent,
        label = "itemBgColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(itemBgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Bespoke Apple-style icon with isolated scale animation
            AppleNavIcon(
                screen = screen,
                tint = iconTint,
                isSelected = isSelected,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale)
            )

            // Label with guaranteed 1-line truncation and centered alignment
            Text(
                text = screen.title,
                fontSize = 11.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = iconTint
            )
        }
    }
}

@Composable
private fun AppleNavIcon(
    screen: MobileNavScreen,
    tint: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val krwAccent = ShadcnTheme.colors.krwAccent
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (screen) {
            MobileNavScreen.HOME -> {
                // SF Symbols Home: Sleek geometric house with curved roof and doorway
                val path = Path().apply {
                    // Roof apex
                    moveTo(w * 0.5f, h * 0.12f)
                    // Right roof slope
                    lineTo(w * 0.90f, h * 0.44f)
                    // Right wall
                    lineTo(w * 0.82f, h * 0.44f)
                    lineTo(w * 0.82f, h * 0.88f)
                    // Right floor
                    lineTo(w * 0.58f, h * 0.88f)
                    // Doorway
                    lineTo(w * 0.58f, h * 0.60f)
                    lineTo(w * 0.42f, h * 0.60f)
                    lineTo(w * 0.42f, h * 0.88f)
                    // Left floor & wall
                    lineTo(w * 0.18f, h * 0.88f)
                    lineTo(w * 0.18f, h * 0.44f)
                    lineTo(w * 0.10f, h * 0.44f)
                    close()
                }

                if (isSelected) {
                    drawPath(path = path, color = tint, style = Fill)
                } else {
                    drawPath(
                        path = path,
                        color = tint,
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Chimney
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.70f, h * 0.18f),
                    size = Size(w * 0.12f, h * 0.20f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            MobileNavScreen.TRANSACTIONS -> {
                // SF Symbols Cards / Wallet: Sleek dual card / transaction stack
                // Back card (subtle)
                drawRoundRect(
                    color = tint.copy(alpha = if (isSelected) 0.5f else 0.4f),
                    topLeft = Offset(w * 0.20f, h * 0.14f),
                    size = Size(w * 0.70f, h * 0.48f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = if (isSelected) Fill else Stroke(width = 1.8.dp.toPx())
                )

                // Front card (prominent)
                val frontRect = Rect(Offset(w * 0.10f, h * 0.32f), Size(w * 0.76f, h * 0.54f))
                val frontPath = Path().apply {
                    addRoundRect(RoundRect(frontRect, CornerRadius(4.dp.toPx(), 4.dp.toPx())))
                }

                if (isSelected) {
                    drawPath(path = frontPath, color = tint, style = Fill)
                    // Magnetic stripe on front card
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.35f),
                        topLeft = Offset(w * 0.10f, h * 0.44f),
                        size = Size(w * 0.76f, h * 0.12f),
                        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                    )
                    // Chip dot
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = 2.2.dp.toPx(),
                        center = Offset(w * 0.30f, h * 0.68f)
                    )
                } else {
                    drawPath(
                        path = frontPath,
                        color = tint,
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Magnetic stripe line
                    drawLine(
                        color = tint,
                        start = Offset(w * 0.10f, h * 0.48f),
                        end = Offset(w * 0.86f, h * 0.48f),
                        strokeWidth = 1.8.dp.toPx()
                    )
                }
            }

            MobileNavScreen.BUDGETS -> {
                // SF Symbols Gauge / Target Meter: Donut ring with indicator
                val strokeW = if (isSelected) 3.5.dp.toPx() else 2.5.dp.toPx()
                val radius = (w * 0.38f)

                // Background track
                drawCircle(
                    color = tint.copy(alpha = if (isSelected) 0.3f else 0.25f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeW)
                )

                // Active arc (budget progress 75%)
                drawArc(
                    color = tint,
                    startAngle = -90f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Center goal dot or star
                drawCircle(
                    color = tint,
                    radius = if (isSelected) 3.5.dp.toPx() else 2.5.dp.toPx(),
                    center = center
                )
            }

            MobileNavScreen.ANALYTICS -> {
                // SF Symbols Bar Chart: 3 rounded vertical pill bars + upward sparkline
                val barW = w * 0.18f
                val spacing = w * 0.09f
                val cornerR = CornerRadius(3.dp.toPx(), 3.dp.toPx())

                // Bar 1 (left - low)
                drawRoundRect(
                    color = tint.copy(alpha = if (isSelected) 0.7f else 0.5f),
                    topLeft = Offset(w * 0.12f, h * 0.52f),
                    size = Size(barW, h * 0.36f),
                    cornerRadius = cornerR,
                    style = if (isSelected) Fill else Stroke(width = 2.dp.toPx())
                )

                // Bar 2 (center - mid/high)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.12f + barW + spacing, h * 0.24f),
                    size = Size(barW, h * 0.64f),
                    cornerRadius = cornerR,
                    style = if (isSelected) Fill else Stroke(width = 2.dp.toPx())
                )

                // Bar 3 (right - mid)
                drawRoundRect(
                    color = tint.copy(alpha = if (isSelected) 0.85f else 0.65f),
                    topLeft = Offset(w * 0.12f + (barW + spacing) * 2, h * 0.38f),
                    size = Size(barW, h * 0.50f),
                    cornerRadius = cornerR,
                    style = if (isSelected) Fill else Stroke(width = 2.dp.toPx())
                )

                // Subtle trend line across the tops
                if (isSelected) {
                    val trendPath = Path().apply {
                        moveTo(w * 0.18f, h * 0.44f)
                        quadraticTo(w * 0.50f, h * 0.14f, w * 0.85f, h * 0.28f)
                    }
                    drawPath(
                        path = trendPath,
                        color = krwAccent,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
