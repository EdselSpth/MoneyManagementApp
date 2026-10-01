package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class CustomColors(
    val background: Color,
    val foreground: Color,
    val card: Color,
    val cardBorder: Color,
    val muted: Color,
    val mutedForeground: Color,
    val input: Color,
    val inputBorder: Color,
    val primary: Color,
    val primaryForeground: Color,
    val secondary: Color,
    val secondaryForeground: Color,
    val income: Color,
    val expense: Color,
    val warning: Color,
    val krwAccent: Color
)

val LocalCustomColors = staticCompositionLocalOf<CustomColors> {
    error("No CustomColors provided")
}

val AppDarkColors = CustomColors(
    background = ShadcnColors.DarkBackground,
    foreground = ShadcnColors.DarkForeground,
    card = ShadcnColors.DarkCard,
    cardBorder = ShadcnColors.DarkCardBorder,
    muted = ShadcnColors.DarkMuted,
    mutedForeground = ShadcnColors.DarkMutedForeground,
    input = ShadcnColors.DarkInput,
    inputBorder = ShadcnColors.DarkInputBorder,
    primary = ShadcnColors.DarkPrimary,
    primaryForeground = ShadcnColors.DarkPrimaryForeground,
    secondary = ShadcnColors.DarkSecondary,
    secondaryForeground = ShadcnColors.DarkSecondaryForeground,
    income = ShadcnColors.EmeraldIncome,
    expense = ShadcnColors.RoseExpense,
    warning = ShadcnColors.AmberWarning,
    krwAccent = ShadcnColors.IndigoKrw
)

val AppLightColors = CustomColors(
    background = ShadcnColors.LightBackground,
    foreground = ShadcnColors.LightForeground,
    card = ShadcnColors.LightCard,
    cardBorder = ShadcnColors.LightCardBorder,
    muted = ShadcnColors.LightMuted,
    mutedForeground = ShadcnColors.LightMutedForeground,
    input = ShadcnColors.LightInput,
    inputBorder = ShadcnColors.LightInputBorder,
    primary = ShadcnColors.LightPrimary,
    primaryForeground = ShadcnColors.LightPrimaryForeground,
    secondary = ShadcnColors.LightSecondary,
    secondaryForeground = ShadcnColors.LightSecondaryForeground,
    income = ShadcnColors.EmeraldIncome,
    expense = ShadcnColors.RoseExpense,
    warning = ShadcnColors.AmberWarning,
    krwAccent = ShadcnColors.IndigoKrw
)

object ShadcnTheme {
    val colors: CustomColors
        @Composable
        get() = LocalCustomColors.current
}

@Composable
fun AppTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) AppDarkColors else AppLightColors

    val materialColors = if (darkTheme) {
        darkColorScheme(
            background = customColors.background,
            surface = customColors.card,
            onBackground = customColors.foreground,
            onSurface = customColors.foreground,
            primary = customColors.primary,
            onPrimary = customColors.primaryForeground
        )
    } else {
        lightColorScheme(
            background = customColors.background,
            surface = customColors.card,
            onBackground = customColors.foreground,
            onSurface = customColors.foreground,
            primary = customColors.primary,
            onPrimary = customColors.primaryForeground
        )
    }

    CompositionLocalProvider(
        LocalCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
