package com.drink.watertracker.ui.theme

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

@Composable
fun WaterTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val dayTheme = ShinchanTheme.todayTheme()

    val colorScheme = lightColorScheme(
        primary = dayTheme.primary,
        onPrimary = Color.White,
        primaryContainer = dayTheme.primaryLight,
        onPrimaryContainer = dayTheme.primary,
        secondary = ShinchanTheme.ShincanPink,
        onSecondary = Color.White,
        tertiary = ShinchanTheme.ShincanGreen,
        background = dayTheme.background,
        onBackground = Color(0xFF1C1B1F),
        surface = dayTheme.surface,
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = dayTheme.gradientStart,
        onSurfaceVariant = Color(0xFF49454F),
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = dayTheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
