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

private val DDMoneyLightColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = LuminousOnPrimary,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary800,

    secondary = AppSecondary,
    onSecondary = LuminousOnSecondary,
    secondaryContainer = Secondary100,
    onSecondaryContainer = Secondary600,

    tertiary = Success,
    onTertiary = LuminousOnTertiary,
    tertiaryContainer = PriorityLowBg,
    onTertiaryContainer = PriorityLowText,

    background = AppBackground,
    onBackground = AppTextPrimary,

    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = Gray100,
    onSurfaceVariant = AppTextSecondary,

    error = Error,
    onError = LuminousOnError,
    errorContainer = PriorityHighBg,
    onErrorContainer = PriorityHighText,

    outline = AppBorder,
    outlineVariant = Gray200,
)

private val DDMoneyDarkColorScheme = darkColorScheme(
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
    val colorScheme = if (darkTheme) DDMoneyDarkColorScheme else DDMoneyLightColorScheme
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
