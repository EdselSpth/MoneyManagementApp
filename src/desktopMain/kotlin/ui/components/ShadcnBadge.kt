package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.ShadcnTheme

enum class BadgeVariant {
    DEFAULT,
    SECONDARY,
    OUTLINE,
    SUCCESS,
    DESTRUCTIVE,
    WARNING,
    KRW
}

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

    val shape = RoundedCornerShape(999.dp) // pill

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
