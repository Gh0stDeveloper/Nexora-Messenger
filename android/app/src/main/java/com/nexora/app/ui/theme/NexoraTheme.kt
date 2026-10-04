package com.nexora.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexoraLightScheme = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F5EF),
    onPrimaryContainer = Color(0xFF073B36),
    secondary = Color(0xFF2563EB),
    onSecondary = Color.White,
    background = Color(0xFFF4F7F6),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE7ECEB),
    onSurfaceVariant = Color(0xFF60706D),
    outline = Color(0xFFB8C5C1),
    error = Color(0xFFB42318),
)

private val NexoraDarkScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF102A43),
    background = Color(0xFF08110F),
    onBackground = Color(0xFFF4FBF9),
    surface = Color(0xFF101B18),
    onSurface = Color(0xFFF4FBF9),
    surfaceVariant = Color(0xFF1C2A27),
    onSurfaceVariant = Color(0xFFC4D4D0),
    outline = Color(0xFF3A4A46),
    error = Color(0xFFFFB4AB),
)

@Composable
fun NexoraTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) NexoraDarkScheme else NexoraLightScheme
    MaterialTheme(
        colorScheme = scheme,
        content = content,
    )
}
