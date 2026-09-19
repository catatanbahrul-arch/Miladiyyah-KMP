package id.wahidiyah.miladiyyah.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Spacer disiapkan untuk Logo Image sebenarnya yang akan Anda upload
            Box(modifier = Modifier.size(120.dp).background(BrandAccentLight, shape = androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                Text("W", color = BrandPrimary, fontSize = 64.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("WAHIDIYAH", color = BrandPrimaryDark, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Aplikasi Resmi Jamaah", color = TextSecondary, fontSize = 14.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
        }
    }
}
