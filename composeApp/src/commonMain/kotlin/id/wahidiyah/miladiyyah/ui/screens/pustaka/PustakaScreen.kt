package id.wahidiyah.miladiyyah.ui.screens.pustaka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun PustakaScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column { Text("Pustaka Jamaah", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Kitab dan arsip digital", color = TextSecondary, fontSize = 14.sp) }
        }
        HorizontalDivider(color = Border)
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(88.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp)) }
                Spacer(modifier = Modifier.height(24.dp)); Text("Arsip Offline", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(modifier = Modifier.height(12.dp)); Text("Materi Wahidiyah tersimpan di perangkat.\nSiap diakses kapan saja.", textAlign = TextAlign.Center, color = TextSecondary, fontSize = 15.sp, lineHeight = 24.sp)
            }
        }
    }
}
