package id.wahidiyah.miladiyyah.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WahidiyahColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Surface,
    primaryContainer = BrandAccentLight,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = BrandAccent,
    onSecondary = BrandPrimaryDark,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    error = Error,
    onError = Surface
)

private val MiladiyyahTypography = Typography(
    displaySmall = androidx.compose.material3.Typography().displaySmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 36.sp,
        fontWeight = FontWeight.Light
    ),
    headlineSmall = androidx.compose.material3.Typography().headlineSmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black
    ),
    titleLarge = androidx.compose.material3.Typography().titleLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 22.sp,
        fontWeight = FontWeight.Black
    ),
    titleMedium = androidx.compose.material3.Typography().titleMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = androidx.compose.material3.Typography().bodyLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp
    ),
    bodyMedium = androidx.compose.material3.Typography().bodyMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp
    ),
    labelLarge = androidx.compose.material3.Typography().labelLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    ),
    labelMedium = androidx.compose.material3.Typography().labelMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    ),
    labelSmall = androidx.compose.material3.Typography().labelSmall.copy(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
)

private val MiladiyyahShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

@Composable
fun MiladiyyahTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WahidiyahColorScheme,
        typography = MiladiyyahTypography,
        shapes = MiladiyyahShapes,
        content = content
    )
}
