package com.nexora.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexoraDarkScheme = darkColorScheme(
    primary = Color(0xFF7AA7FF),
    secondary = Color(0xFF8C7BFF),
    background = Color(0xFF050816),
    surface = Color(0xFF0B1026),
    surfaceVariant = Color(0xFF111936),
    onPrimary = Color(0xFF061229),
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFD8DDF5),
)

@Composable
fun NexoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexoraDarkScheme,
        content = content,
    )
}
