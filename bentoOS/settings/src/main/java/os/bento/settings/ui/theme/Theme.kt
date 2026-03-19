package os.bento.settings.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import os.bento.settings.R

object BentoColors {
    val bg              = Color(0xFF0D0D0D)
    val surface         = Color(0xFF161616)
    val surfaceElevated = Color(0xFF1E1E1E)
    val border          = Color(0xFF1E1E1E)
    val borderBright    = Color(0xFF252525)
    val textPrimary     = Color(0xFFE0E0E0)
    val textSecondary   = Color(0xFF666666)
    val accent          = Color(0xFF6C6CFF)
    val accentDim       = Color(0x336C6CFF)
    val accentBright    = Color(0xFF8A8AFF)
    val success         = Color(0xFF34C759)
    val warning         = Color(0xFFFFCC00)
    val error           = Color(0xFFFF3B30)
    val glass           = Color(0x26FFFFFF)
    val glassBorder     = Color(0x33FFFFFF)
}

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

val MonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
)

private val BentoDarkColorScheme = darkColorScheme(
    primary          = BentoColors.accent,
    secondary        = BentoColors.accentBright,
    background       = BentoColors.bg,
    surface          = BentoColors.surface,
    onPrimary        = Color.Black,
    onSecondary      = Color.Black,
    onBackground     = BentoColors.textPrimary,
    onSurface        = BentoColors.textPrimary,
    error            = BentoColors.error,
    outline          = BentoColors.border,
)

@Composable
fun BentoSettingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BentoDarkColorScheme,
        content = content
    )
}
