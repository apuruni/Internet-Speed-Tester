package com.example.composespeedtest.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Teal200,
    primaryVariant = Purple700,
    secondary = Pink,
    background = DarkColor,
    surface = DarkColor2,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onSurface = LightColor2,
    onBackground = LightColor2
)

private val LightColorPalette = lightColors(
    primary = Teal200,
    primaryVariant = Teal200,
    secondary = Pink,
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onSurface = Color(0xFF1B1D22),
    onBackground = Color(0xFF1B1D22)
)

@Composable
fun ComposeSpeedTestTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}