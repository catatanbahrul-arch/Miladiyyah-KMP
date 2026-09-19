package id.wahidiyah.miladiyyah.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import id.wahidiyah.miladiyyah.generated.resources.Res
import id.wahidiyah.miladiyyah.generated.resources.wahidiyah_logo_home_splash

@Composable
fun WahidiyahLogo(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(
            Res.drawable.wahidiyah_logo_home_splash
        ),
        contentDescription = "Logo Wahidiyah",
        modifier = modifier.aspectRatio(1f),
        contentScale = ContentScale.Fit
    )
}
