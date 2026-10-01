package ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ui.theme.ShadcnTheme

@Composable
fun ShadcnProgress(
    progress: Float, // 0.0 to 1.0+
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
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
