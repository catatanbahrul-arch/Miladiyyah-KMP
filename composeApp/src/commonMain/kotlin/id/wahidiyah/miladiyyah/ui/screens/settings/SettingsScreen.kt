package id.wahidiyah.miladiyyah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.alarm.AlarmScheduler

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var adzanEnabled by remember { mutableStateOf(true) }
    var tarhimEnabled by remember { mutableStateOf(true) }
    var tasyafuanEnabled by remember { mutableStateOf(true) }
    var danaBoxEnabled by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { try { AlarmScheduler.scheduleAll(context) } catch (e: Exception) {} }

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 20.dp, vertical = 16.dp)) {
            Column {
                Text("Pengaturan & Menu", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Personalisasi aplikasi Wahidiyah", color = TextSecondary, fontSize = 13.sp)
            }
        }
        HorizontalDivider(color = Border)

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("NOTIFIKASI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
            
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(1.dp)) {
                Column {
                    SettingsItem(icon = Icons.Outlined.NotificationsActive, title = "Adzan & Waktu Salat", subtitle = "Peringatan 10 menit & Suara Adzan", checked = adzanEnabled) { adzanEnabled = it }
                    HorizontalDivider(color = Border.copy(alpha = 0.5f), modifier = Modifier.padding(start = 56.dp))
                    SettingsItem(icon = Icons.Outlined.NotificationsActive, title = "Pengingat Tarhim", subtitle = "Setiap hari sebelum Subuh", checked = tarhimEnabled) { tarhimEnabled = it }
                }
            }

            Text("PENGINGAT KHUSUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
            
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(1.dp)) {
                Column {
                    SettingsItem(icon = Icons.Outlined.Info, title = "Pengingat Syafa'an", subtitle = "Setiap jam 03:00 pagi", checked = tasyafuanEnabled) { tasyafuanEnabled = it }
                    HorizontalDivider(color = Border.copy(alpha = 0.5f), modifier = Modifier.padding(start = 56.dp))
                    SettingsItem(icon = Icons.Outlined.VolunteerActivism, title = "Pengingat Dana Box", subtitle = "Setiap jam 06:00 & 19:00", checked = danaBoxEnabled) { danaBoxEnabled = it }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary, checkedThumbColor = Surface))
    }
}
