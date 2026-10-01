package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.ShadcnTheme

@Composable
fun KrwQuickSelector(
    currentAmount: Long,
    onAmountSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickAddOptions = listOf(
        Pair("+1만 (10k)", 10000L),
        Pair("+5만 (50k)", 50000L),
        Pair("+10만 (100k)", 100000L),
        Pair("+50만 (500k)", 500000L),
        Pair("+100만 (1M)", 1000000L)
    )

    Column(modifier = modifier) {
        Text(
            text = "빠른 금액 입력 (Quick Add Won)",
            color = ShadcnTheme.colors.mutedForeground,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickAddOptions.forEach { (label, value) ->
                QuickChip(
                    text = label,
                    onClick = { onAmountSelected(currentAmount + value) },
                    modifier = Modifier.weight(1f)
                )
            }

            QuickChip(
                text = "초기화 (Clear)",
                onClick = { onAmountSelected(0L) },
                isDestructive = true
            )
        }
    }
}

@Composable
private fun QuickChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    val shape = RoundedCornerShape(6.dp)
    val bgColor = if (isDestructive) Color(0xFF2A1515) else ShadcnTheme.colors.secondary
    val textColor = if (isDestructive) Color(0xFFFCA5A5) else ShadcnTheme.colors.foreground
    val borderColor = if (isDestructive) Color(0xFF450A0A) else ShadcnTheme.colors.cardBorder

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
