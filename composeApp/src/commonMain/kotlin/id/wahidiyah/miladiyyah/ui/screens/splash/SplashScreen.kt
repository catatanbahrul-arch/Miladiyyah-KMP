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
import id.wahidiyah.miladiyyah.ui.components.WahidiyahLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) { delay(2000); onTimeout() }
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WahidiyahLogo(modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Text("WAHIDIYAH", color = BrandPrimaryDark, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 8.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Aplikasi Resmi Jamaah", color = TextSecondary, fontSize = 13.sp, letterSpacing = 2.sp)
        }
    }
}
