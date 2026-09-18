package id.wahidiyah.miladiyyah.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MiladiyyahLightColorScheme = lightColorScheme(
    primary = DeepForestGreen,
    secondary = SubtleGold,
    background = SoftCream,
    surface = WarmWhite,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun MiladiyyahTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MiladiyyahLightColorScheme,
        content = content
    )
}
