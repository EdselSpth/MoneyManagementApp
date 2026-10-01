package com.example.moneymanagement.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val title: String, val subtitle: String, val icon: String) {
    AETHER("Aether Genshin", "Warm Gold & Espresso", "⚔️"),
    DARK("Dark Zinc", "Deep Charcoal & Cyan", "🌙"),
    LIGHT("Clean Slate", "Bright White & Sky Blue", "☀️")
}

object ShadcnColors {
    // Dark Zinc Theme
    val DarkBackground = Color(0xFF09090B) // zinc-950
    val DarkForeground = Color(0xFFF4F4F5) // zinc-100
    val DarkCard = Color(0xFF121215)
    val DarkCardBorder = Color(0xFF27272A) // zinc-800
    val DarkMuted = Color(0xFF1C1C21)
    val DarkMutedForeground = Color(0xFFA1A1AA) // zinc-400
    val DarkInput = Color(0xFF1E1E24)
    val DarkInputBorder = Color(0xFF2E2E36)
    val DarkPrimary = Color(0xFF38BDF8) // sky-400 / cyan accent
    val DarkPrimaryForeground = Color(0xFF09090B)
    val DarkSecondary = Color(0xFF27272A)
    val DarkSecondaryForeground = Color(0xFFF4F4F5)

    val EmeraldIncome = Color(0xFF10B981)
    val RoseExpense = Color(0xFFF43F5E)
    val AmberWarning = Color(0xFFF59E0B)
    val IndigoKrw = Color(0xFF6366F1)

    // Light Slate Theme
    val LightBackground = Color(0xFFF8FAFC)
    val LightForeground = Color(0xFF0F172A)
    val LightCard = Color(0xFFFFFFFF)
    val LightCardBorder = Color(0xFFE2E8F0)
    val LightMuted = Color(0xFFF1F5F9)
    val LightMutedForeground = Color(0xFF64748B)
    val LightInput = Color(0xFFF8FAFC)
    val LightInputBorder = Color(0xFFCBD5E1)
    val LightPrimary = Color(0xFF0284C7)
    val LightPrimaryForeground = Color(0xFFFFFFFF)
    val LightSecondary = Color(0xFFF1F5F9)
    val LightSecondaryForeground = Color(0xFF0F172A)

    // Aether Genshin Impact Official Palette:
    // #faf0d9: Cream / Alabaster Light Gold (RGB: 250, 240, 217)
    // #dcb37b: Warm Golden Amber / Wheat Gold (RGB: 220, 179, 123)
    // #a08066: Muted Sandstone / Earthy Bronze (RGB: 160, 128, 102)
    // #5e4a4b: Deep Warm Cocoa / Dark Taupe (RGB: 94, 74, 75)
    // #332829: Espresso Charcoal / Deep Obsidian Umber (RGB: 51, 40, 41)
    val AetherAlabaster = Color(0xFFFAF0D9)
    val AetherGold = Color(0xFFDCB37B)
    val AetherBronze = Color(0xFFA08066)
    val AetherCocoa = Color(0xFF5E4A4B)
    val AetherEspresso = Color(0xFF332829)
    val AetherCardBg = Color(0xFF423435)      // Slightly elevated from espresso
    val AetherInputBg = Color(0xFF3B2E2F)
}

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

val AppAetherColors = CustomColors(
    background = ShadcnColors.AetherEspresso,        // #332829
    foreground = ShadcnColors.AetherAlabaster,       // #FAF0D9
    card = ShadcnColors.AetherCardBg,               // #423435
    cardBorder = ShadcnColors.AetherCocoa,          // #5E4A4B
    muted = ShadcnColors.AetherInputBg,             // #3B2E2F
    mutedForeground = ShadcnColors.AetherBronze,    // #A08066
    input = ShadcnColors.AetherInputBg,
    inputBorder = ShadcnColors.AetherCocoa,
    primary = ShadcnColors.AetherGold,              // #DCB37B
    primaryForeground = ShadcnColors.AetherEspresso,
    secondary = ShadcnColors.AetherCocoa,           // #5E4A4B
    secondaryForeground = ShadcnColors.AetherAlabaster,
    income = Color(0xFF34D399),                     // Warm Emerald
    expense = Color(0xFFFB7185),                    // Warm Rose Coral
    warning = ShadcnColors.AetherGold,
    krwAccent = ShadcnColors.AetherGold
)

object ShadcnTheme {
    val colors: CustomColors
        @Composable
        get() = LocalCustomColors.current
}

@Composable
fun AppTheme(
    themeMode: AppThemeMode = AppThemeMode.AETHER,
    content: @Composable () -> Unit
) {
    val customColors = when (themeMode) {
        AppThemeMode.DARK -> AppDarkColors
        AppThemeMode.LIGHT -> AppLightColors
        AppThemeMode.AETHER -> AppAetherColors
    }

    val isDark = themeMode != AppThemeMode.LIGHT

    val materialColors = if (isDark) {
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
