package com.madderate.wukongdemo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors()
private val DarkColorPalette = darkColors()

@Composable
fun WukongTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun WukongBasicTheme(content: @Composable () -> Unit) {
    WukongTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background, content = content)
    }
}
