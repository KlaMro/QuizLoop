package com.example.quizloop.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = QuizAccent,
    onPrimary = QuizLight,
    primaryContainer = QuizWarm,
    onPrimaryContainer = QuizDark,
    secondary = QuizCyan,
    onSecondary = QuizDark,
    secondaryContainer = QuizSurfaceVariant,
    onSecondaryContainer = QuizDark,
    tertiary = QuizWarm,
    onTertiary = QuizDark,
    tertiaryContainer = QuizLight,
    onTertiaryContainer = QuizDark,
    background = QuizLight,
    onBackground = QuizDark,
    surface = QuizSurface,
    onSurface = QuizOnSurface,
    surfaceVariant = QuizSurfaceVariant,
    onSurfaceVariant = QuizOnSurface,
    outline = QuizOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = QuizAccent,
    onPrimary = QuizLight,
    primaryContainer = QuizWarm,
    onPrimaryContainer = QuizLight,
    secondary = QuizCyan,
    onSecondary = QuizLight,
    tertiary = QuizWarm,
    onTertiary = QuizLight,
    background = QuizDark,
    onBackground = QuizLight,
    surface = QuizDark,
    onSurface = QuizLight,
    surfaceVariant = QuizSurfaceVariant,
    onSurfaceVariant = QuizLight,
    outline = QuizOutline
)

@Composable
fun QuizLoopTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QuizLoopTypography,
        content = content
    )
}
