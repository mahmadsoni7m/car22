package tj.soni.card.ui.theme

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

object SoniColors {
    val Teal = Color(0xFF087C70)
    val Navy = Color(0xFF102D3A)
    val Mint = Color(0xFF36B99A)
    val Background = Color(0xFFF1F6F8)
    val Surface = Color(0xFFFFFFFF)
    val Border = Color(0xFFDCE8EA)
    val Success = Color(0xFF21865C)
    val Error = Color(0xFFC74747)
    val Muted = Color(0xFF5E7681)
    val MintSoft = Color(0xFFE3F4F0)
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

private val SoniColorScheme = lightColorScheme(
    primary = SoniColors.Teal,
    onPrimary = Color.White,
    primaryContainer = SoniColors.MintSoft,
    onPrimaryContainer = SoniColors.Navy,
    secondary = SoniColors.Mint,
    onSecondary = Color.White,
    tertiary = SoniColors.Navy,
    background = SoniColors.Background,
    onBackground = SoniColors.Navy,
    surface = SoniColors.Surface,
    onSurface = SoniColors.Navy,
    surfaceVariant = SoniColors.Background,
    onSurfaceVariant = SoniColors.Muted,
    outline = SoniColors.Border,
    outlineVariant = SoniColors.Border,
    error = SoniColors.Error,
    onError = Color.White
)

private val SoniShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val SoniTypography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun SoniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SoniColorScheme,
        shapes = SoniShapes,
        typography = SoniTypography,
        content = content
    )
}
