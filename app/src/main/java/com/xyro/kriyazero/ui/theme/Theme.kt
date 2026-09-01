package com.xyro.kriyazero.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF5146D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E2FF),
    onPrimaryContainer = Color(0xFF17104A),
    secondary = Color(0xFF47636E),
    secondaryContainer = Color(0xFFCBE8F4),
    background = Color(0xFFF8F8FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9E9F0),
    onSurface = Color(0xFF1B1B21),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC5C0FF),
    primaryContainer = Color(0xFF3930B0),
    secondary = Color(0xFFAFCCD8),
    background = Color(0xFF121217),
    surface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFF45464F),
)

@Composable
fun KriyaZeroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
