package com.nexora.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object NexoraColors {
    val Amoled = Color(0xFF020706)
    val Ink = Color(0xFF071310)
    val Glass = Color(0xE6131F1B)
    val GlassHigh = Color(0xF21A2B26)
    val Stroke = Color(0xFF273A35)
    val Primary = Color(0xFF67E8D1)
    val PrimaryDeep = Color(0xFF0F766E)
    val MintSoft = Color(0xFFB7F7EB)
    val TextMain = Color(0xFFF6FFFC)
    val TextMuted = Color(0xFFA9BBB6)
    val BubbleMine = Color(0xFF145C54)
    val BubbleOther = Color(0xFF17231F)
}

private val NexoraDarkScheme = darkColorScheme(
    primary = NexoraColors.Primary,
    onPrimary = Color(0xFF03211D),
    primaryContainer = Color(0xFF123F39),
    onPrimaryContainer = NexoraColors.MintSoft,
    secondary = Color(0xFF8AB4FF),
    onSecondary = Color(0xFF061A33),
    background = NexoraColors.Amoled,
    onBackground = NexoraColors.TextMain,
    surface = NexoraColors.Ink,
    onSurface = NexoraColors.TextMain,
    surfaceVariant = NexoraColors.GlassHigh,
    onSurfaceVariant = NexoraColors.TextMuted,
    outline = NexoraColors.Stroke,
    error = Color(0xFFFFB4AB),
)

private val NexoraTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 38.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp),
)

@Composable
fun NexoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexoraDarkScheme,
        typography = NexoraTypography,
        content = content,
    )
}
