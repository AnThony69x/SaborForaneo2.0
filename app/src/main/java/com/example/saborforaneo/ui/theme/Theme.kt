package com.example.saborforaneo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun crearEsquemaClaro(colorPrimario: Color): androidx.compose.material3.ColorScheme {
    return lightColorScheme(
        primary = colorPrimario,
        onPrimary = Color.White,
        primaryContainer = colorPrimario.copy(alpha = 0.1f),
        onPrimaryContainer = colorPrimario,
        secondary = colorPrimario.copy(alpha = 0.7f),
        onSecondary = Color.White,
        secondaryContainer = colorPrimario.copy(alpha = 0.1f),
        onSecondaryContainer = colorPrimario,
        tertiary = colorPrimario.copy(alpha = 0.5f),
        onTertiary = Color.White,
        tertiaryContainer = colorPrimario.copy(alpha = 0.1f),
        onTertiaryContainer = colorPrimario,
        error = Color(0xFFB00020),
        onError = Color.White,
        errorContainer = Color(0xFFFCD8DF),
        onErrorContainer = Color(0xFF8C0009),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E)
    )
}

private fun crearEsquemaOscuro(colorPrimario: Color): androidx.compose.material3.ColorScheme {
    return darkColorScheme(
        primary = colorPrimario,
        onPrimary = Color(0xFF003258),
        primaryContainer = colorPrimario.copy(alpha = 0.2f),
        onPrimaryContainer = colorPrimario,
        secondary = colorPrimario.copy(alpha = 0.7f),
        onSecondary = Color(0xFF003353),
        secondaryContainer = colorPrimario.copy(alpha = 0.2f),
        onSecondaryContainer = colorPrimario,
        tertiary = colorPrimario.copy(alpha = 0.5f),
        onTertiary = Color(0xFF36003C),
        tertiaryContainer = colorPrimario.copy(alpha = 0.2f),
        onTertiaryContainer = colorPrimario,
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99)
    )
}

@Composable
fun SaborForaneoTheme(
    temaOscuro: Boolean = isSystemInDarkTheme(),
    colorPrimario: Long = 0xFF6B9FBF,
    contenido: @Composable () -> Unit
) {
    val colorPrimarioCompuesto = Color(colorPrimario)

    val colorScheme = if (temaOscuro) {
        crearEsquemaOscuro(colorPrimarioCompuesto)
    } else {
        crearEsquemaClaro(colorPrimarioCompuesto)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !temaOscuro
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = contenido
    )
}