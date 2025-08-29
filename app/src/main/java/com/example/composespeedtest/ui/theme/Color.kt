package com.example.composespeedtest.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core brand hues (neon on deep blue)
val Purple700 = Color(0xFF7C4DFF)
val Teal200 = Color(0xFF64FFDA)
val Pink = Color(0xFFFF57C1)

// Accent greens for success/gauges
val Green200 = Color(0xFF7CF7C4)
val Green300 = Color(0xFF4BE4A1)
val Green500 = Color(0xFF22C55E)

// Dark palette
val DarkColor = Color(0xFF0D1220)     // deepest
val DarkColor2 = Color(0xFF131A2C)    // base background
val LightColor = Color(0xFF7F8BAA)
val LightColor2 = Color(0xFFB3C0E0)

// Status/alerts
val Red200 = Color(0xFFFF9AA2)
val Red500 = Color(0xFFFF4D67)
val RedGradient = Brush.linearGradient(
    colors = listOf(Red500, Red200),
    start = Offset.Zero,
    end = Offset.Infinite
)

// Gradients
val GreenGradient = Brush.linearGradient(
    colors = listOf(Green300, Green200),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, 0f)
)

val IndigoGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F1730), Color(0xFF0B1023))
)

val DarkGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1B2340), Color(0xFF0E1326))
)