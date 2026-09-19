package id.wahidiyah.miladiyyah.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WahidiyahColorScheme = lightColorScheme(
    primary = BrandPrimary, onPrimary = Surface, primaryContainer = BrandAccentLight,
    onPrimaryContainer = BrandPrimaryDark, secondary = BrandAccent, onSecondary = Surface,
    background = Background, onBackground = TextPrimary, surface = Surface,
    onSurface = TextPrimary, error = Error, onError = Surface
)

@Composable
fun MiladiyyahTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = WahidiyahColorScheme, content = content)
}
