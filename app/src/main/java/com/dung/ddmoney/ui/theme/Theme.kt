package com.dung.ddmoney.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.material3.darkColorScheme

private val LuminousLightColorScheme = lightColorScheme(
    primary = LuminousPrimary,
    onPrimary = LuminousOnPrimary,
    primaryContainer = LuminousPrimaryContainer,
    onPrimaryContainer = LuminousOnPrimaryContainer,

    secondary = LuminousSecondary,
    onSecondary = LuminousOnSecondary,
    secondaryContainer = LuminousSecondaryContainer,
    onSecondaryContainer = LuminousOnSecondaryContainer,

    tertiary = LuminousTertiary,
    onTertiary = LuminousOnTertiary,
    tertiaryContainer = LuminousTertiaryContainer,
    onTertiaryContainer = LuminousOnTertiaryContainer,

    background = LuminousBackground,
    onBackground = LuminousOnBackground,

    surface = LuminousSurface,
    onSurface = LuminousOnSurface,
    surfaceVariant = LuminousSurfaceVariant,
    onSurfaceVariant = LuminousOnSurfaceVariant,

    error = LuminousError,
    onError = LuminousOnError,
    errorContainer = LuminousErrorContainer,
    onErrorContainer = LuminousOnErrorContainer,

    outline = LuminousOutline,
    outlineVariant = LuminousOutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
)

@Composable
fun DDMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LuminousLightColorScheme
    }
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
        typography = Typography,
        content = content
    )
}