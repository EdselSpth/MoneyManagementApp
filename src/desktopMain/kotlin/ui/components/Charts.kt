package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.ShadcnTheme
import util.CurrencyFormatter

data class ChartSlice(
    val label: String,
    val value: Long,
    val color: Color,
    val percentage: Float
)

@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    totalAmount: Long,
    modifier: Modifier = Modifier,
    centerLabel: String = "총 지출 (Total Expense)"
) {
    if (slices.isEmpty() || totalAmount == 0L) {
        Box(
            modifier = modifier.height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "이번 달 지출 내역이 없습니다\n(No expenses recorded this month)",
                color = ShadcnTheme.colors.mutedForeground,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The Donut Graphic
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                var currentAngle = -90f
                val strokeWidth = 24.dp.toPx()

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
                Text(
                    text = centerLabel,
                    color = ShadcnTheme.colors.mutedForeground,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = CurrencyFormatter.format(totalAmount),
                    color = ShadcnTheme.colors.foreground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Legend list
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.take(6).forEach { slice ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                        Text(
                            text = slice.label,
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(slice.percentage * 100).toInt()}%",
                            color = ShadcnTheme.colors.mutedForeground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = CurrencyFormatter.format(slice.value),
                            color = ShadcnTheme.colors.foreground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CashflowComparisonBar(
    income: Long,
    expense: Long,
    modifier: Modifier = Modifier
) {
    val total = (income + expense).coerceAtLeast(1L)
    val incomeRatio = (income.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val expenseRatio = (expense.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ShadcnTheme.colors.income))
                Text(
                    text = "수입: ${CurrencyFormatter.format(income)}",
                    color = ShadcnTheme.colors.income,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ShadcnTheme.colors.expense))
                Text(
                    text = "지출: ${CurrencyFormatter.format(expense)}",
                    color = ShadcnTheme.colors.expense,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Split Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(ShadcnTheme.colors.muted)
        ) {
            if (incomeRatio > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(incomeRatio.coerceAtLeast(0.01f))
                        .background(ShadcnTheme.colors.income)
                )
            }
            if (expenseRatio > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(expenseRatio.coerceAtLeast(0.01f))
                        .background(ShadcnTheme.colors.expense)
                )
            }
        }
    }
}
