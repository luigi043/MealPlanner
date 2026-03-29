package com.mealplanner.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = NeutralWhite,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenPrimary,
    secondary = GreenSecondary,
    onSecondary = NeutralWhite,
    secondaryContainer = Color(0xFFE8F5EF),
    onSecondaryContainer = GreenPrimary,
    tertiary = OrangeAccent,
    onTertiary = NeutralWhite,
    tertiaryContainer = OrangeLight,
    onTertiaryContainer = Color(0xFF8B2500),
    error = ErrorRed,
    errorContainer = Color(0xFFFFDAD6),
    background = NeutralWhite,
    onBackground = NeutralDark,
    surface = NeutralWhite,
    onSurface = NeutralDark,
    surfaceVariant = SurfaceDim,
    onSurfaceVariant = NeutralMedium,
    outline = Color(0xFFBDBDBD)
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenTertiary,
    onPrimary = Color(0xFF00391E),
    primaryContainer = Color(0xFF00522C),
    onPrimaryContainer = GreenContainer,
    secondary = GreenSecondary,
    onSecondary = Color(0xFF003822),
    tertiary = OrangeAccent,
    onTertiary = Color(0xFF4A1800),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

@Composable
fun MealPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MealPlannerTypography,
        content = content
    )
}