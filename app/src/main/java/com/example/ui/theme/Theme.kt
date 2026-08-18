package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ImmersiveDarkColorScheme = darkColorScheme(
    primary = ImmersiveAccent,
    onPrimary = ImmersiveOnAccent,
    primaryContainer = ImmersiveAccentContainer,
    onPrimaryContainer = ImmersiveAccentGlow,
    secondary = ImmersiveAccent,
    onSecondary = ImmersiveOnAccent,
    secondaryContainer = ImmersiveAccentContainer,
    onSecondaryContainer = ImmersiveTextPrimary,
    tertiary = ImmersiveAccentGlow,
    onTertiary = ImmersiveOnAccent,
    background = ImmersiveBackground,
    onBackground = ImmersiveTextPrimary,
    surface = ImmersiveSurface,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = ImmersiveTextSecondary,
    outline = ImmersiveBorder,
    outlineVariant = ImmersiveBorder.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Immersive UI defaults to the deep immersive theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = ImmersiveDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ImmersiveSurfaceVariant.toArgb()
            window.navigationBarColor = ImmersiveSurfaceVariant.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
