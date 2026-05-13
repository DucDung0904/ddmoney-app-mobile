package com.dung.ddmoney.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LuminousLightColorScheme = lightColorScheme(
    primary          = LuminousPrimary,           // OceanBlue600 #185FA5
    onPrimary        = LuminousOnPrimary,
    primaryContainer = LuminousPrimaryContainer,  // OceanBlue400
    onPrimaryContainer = LuminousOnPrimaryContainer,

    secondary          = LuminousSecondary,
    onSecondary        = LuminousOnSecondary,
    secondaryContainer = LuminousSecondaryContainer,
    onSecondaryContainer = LuminousOnSecondaryContainer,

    tertiary          = LuminousTertiary,          // SavingsTeal600 #1D9E75
    onTertiary        = LuminousOnTertiary,
    tertiaryContainer = LuminousTertiaryContainer,
    onTertiaryContainer = LuminousOnTertiaryContainer,

    background = LuminousBackground,
    onBackground = LuminousOnBackground,

    surface          = LuminousSurface,
    onSurface        = LuminousOnSurface,
    surfaceVariant   = LuminousSurfaceVariant,
    onSurfaceVariant = LuminousOnSurfaceVariant,   // NeutralGray600 #888780

    error          = LuminousError,                // ExpenseRed600 #E24B4A
    onError        = LuminousOnError,
    errorContainer = LuminousErrorContainer,
    onErrorContainer = LuminousOnErrorContainer,

    outline        = LuminousOutline,              // NeutralGray600
    outlineVariant = LuminousOutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary          = DarkPrimary,               // OceanBlue400
    onPrimary        = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,      // OceanBlue800
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary          = DarkSecondary,           // SavingsTeal400
    onSecondary        = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    tertiary          = DarkTertiary,             // InvestAmber400
    onTertiary        = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface          = DarkSurface,
    onSurface        = DarkOnSurface,
    surfaceVariant   = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    error          = DarkError,                   // ExpenseRed400
    onError        = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,

    outline        = DarkOutline,
    outlineVariant = DarkOutlineVariant,
)

@Composable
fun DDMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LuminousLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}