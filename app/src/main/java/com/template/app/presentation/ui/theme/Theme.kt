package com.template.app.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightAccentFill,
    onPrimary = Color.White,
    primaryContainer = LightAccentTintBg,
    onPrimaryContainer = LightAccentText,
    secondary = LightAccentFill,
    onSecondary = Color.White,
    background = LightBaseBg,
    onBackground = LightTextPrimary,
    surface = LightBaseBg,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceFill,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightDivider,
    error = LightDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccentFill,
    onPrimary = Color.White,
    primaryContainer = DarkAccentTintBg,
    onPrimaryContainer = DarkAccentText,
    secondary = DarkAccentFill,
    onSecondary = Color.White,
    background = DarkBaseBg,
    onBackground = DarkTextPrimary,
    surface = DarkBaseBg,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceFill,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkDivider,
    error = DarkDanger,
    onError = Color.White
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled dynamic color to enforce Vela branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
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
