package id.wahidiyah.miladiyyah.ui.screens.settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Text("Halaman Pengaturan & Sinkronisasi Data Lokal", color = TextSecondary)
    }
}
