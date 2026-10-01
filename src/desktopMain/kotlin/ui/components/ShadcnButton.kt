package ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.ShadcnTheme

enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    GHOST,
    DESTRUCTIVE
}

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
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
