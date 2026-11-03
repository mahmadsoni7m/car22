package tj.big.card.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object BigColors {
    val Teal = Color(0xFF0A74F5)
    val Navy = Color(0xFF141B34)
    val Mint = Color(0xFF5AA9FF)
    val Orange = Color(0xFFFF7525)
    val Background = Color(0xFFF2F6FF)
    val Surface = Color(0xFFFFFFFF)
    val Border = Color(0xFFE1E9F7)
    val Success = Color(0xFF2E9E5B)
    val Error = Color(0xFFE0463C)
    val Muted = Color(0xFF6B7690)
    val MintSoft = Color(0xFFE4EEFF)
    val Field = Color(0xFFF4F5F8)
    val SilverLight = Color(0xFFFAFBFC)
    val SilverDark = Color(0xFFCFD9DE)
}

/** 8dp spacing system. */
object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 16.dp
    val l = 24.dp
    val xl = 32.dp
}

private val BigColorScheme = lightColorScheme(
    primary = BigColors.Teal,
    onPrimary = Color.White,
    primaryContainer = BigColors.MintSoft,
    onPrimaryContainer = BigColors.Navy,
    secondary = BigColors.Mint,
    onSecondary = Color.White,
    tertiary = BigColors.Navy,
    background = BigColors.Background,
    onBackground = BigColors.Navy,
    surface = BigColors.Surface,
    onSurface = BigColors.Navy,
    surfaceVariant = BigColors.Background,
    onSurfaceVariant = BigColors.Muted,
    outline = BigColors.Border,
    outlineVariant = BigColors.Border,
    error = BigColors.Error,
    onError = Color.White
)

private val BigShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val BigTypography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun BigTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BigColorScheme,
        shapes = BigShapes,
        typography = BigTypography,
        content = content
    )
}
