package id.wahidiyah.miladiyyah.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.AppScreen
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun MenuScreen(onNavigate: (AppScreen) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column { Text("Menu Utama", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Pusat fitur & informasi sekunder", color = TextSecondary, fontSize = 14.sp) }
        }
        HorizontalDivider(color = Border)
        
        Column(modifier = Modifier.padding(24.dp)) {
            Text("FITUR WAHIDIYAH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 16.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.PUSTAKA) }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Pustaka Jamaah", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Kitab dan arsip digital", fontSize = 14.sp, color = TextSecondary) }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.KIBLAT) }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Kompas Kiblat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Arah presisi via GPS", fontSize = 14.sp, color = TextSecondary) }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("PENGATURAN & LAINNYA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 16.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.PENGATURAN) }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Settings, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Pengaturan Umum", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Data & Sinkronisasi lokal", fontSize = 14.sp, color = TextSecondary) }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }
        }
    }
}
