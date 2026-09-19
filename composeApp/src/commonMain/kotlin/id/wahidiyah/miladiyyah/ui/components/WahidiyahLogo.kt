package id.wahidiyah.miladiyyah.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import id.wahidiyah.miladiyyah.theme.BrandPrimary
import id.wahidiyah.miladiyyah.theme.BrandAccent

@Composable
fun WahidiyahLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(64.dp)) {
        val wPath = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.3f)
            lineTo(size.width * 0.4f, size.height * 0.8f)
            lineTo(size.width * 0.5f, size.height * 0.55f)
            lineTo(size.width * 0.6f, size.height * 0.8f)
            lineTo(size.width * 0.8f, size.height * 0.3f)
            lineTo(size.width * 0.68f, size.height * 0.3f)
            lineTo(size.width * 0.6f, size.height * 0.6f)
            lineTo(size.width * 0.5f, size.height * 0.35f)
            lineTo(size.width * 0.4f, size.height * 0.6f)
            lineTo(size.width * 0.32f, size.height * 0.3f)
            close()
        }
        val leafPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.15f)
            quadraticBezierTo(size.width * 0.65f, size.height * 0.35f, size.width * 0.6f, size.height * 0.6f)
            quadraticBezierTo(size.width * 0.5f, size.height * 0.45f, size.width * 0.5f, size.height * 0.15f)
            close()
        }
        drawPath(path = wPath, color = BrandPrimary)
        drawPath(path = leafPath, color = BrandAccent)
    }
}
