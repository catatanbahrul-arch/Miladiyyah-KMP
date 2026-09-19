package id.wahidiyah.miladiyyah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun SettingsScreen() {
    var adzanEnabled by remember { mutableStateOf(true) }
    var tarhimEnabled by remember { mutableStateOf(true) }
    
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column { Text("Pengaturan", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Personalisasi aplikasi Wahidiyah", color = TextSecondary, fontSize = 14.sp) }
        }
        HorizontalDivider(color = Border)
        Column(modifier = Modifier.padding(24.dp)) {
            Text("NOTIFIKASI", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 16.dp, start = 8.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Adzan & Salat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Peringatan otomatis", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = adzanEnabled, onCheckedChange = { adzanEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Pengingat Tarhim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Sebelum waktu Subuh", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tarhimEnabled, onCheckedChange = { tarhimEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                }
            }
        }
    }
}
