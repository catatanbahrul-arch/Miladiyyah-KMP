package id.wahidiyah.miladiyyah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 20.dp)) {
            Column {
                Text("Pengaturan", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Personalisasi Wahidiyah", color = TextSecondary, fontSize = 13.sp)
            }
        }
        HorizontalDivider(color = Border)

        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text("ALARM SALAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(1.dp)) {
                    Column {
                        SettingsItem(icon = Icons.Default.Notifications, title = "Adzan & Waktu Salat", subtitle = "Peringatan otomatis", checked = adzanEnabled) { adzanEnabled = it }
                        HorizontalDivider(color = Background, modifier = Modifier.padding(start = 64.dp), thickness = 1.5.dp)
                        SettingsItem(icon = Icons.Default.Notifications, title = "Pengingat Tarhim", subtitle = "Sebelum masuk waktu Subuh", checked = tarhimEnabled) { tarhimEnabled = it }
                    }
                }
            }

            Column {
                Text("PENGINGAT KHUSUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(1.dp)) {
                    Column {
                        SettingsItem(icon = Icons.Default.Info, title = "Pengingat Syafa'an", subtitle = "Pukul 03:00 pagi", checked = tasyafuanEnabled) { tasyafuanEnabled = it }
                        HorizontalDivider(color = Background, modifier = Modifier.padding(start = 64.dp), thickness = 1.5.dp)
                        SettingsItem(icon = Icons.Default.Favorite, title = "Pengingat Dana Box", subtitle = "Pukul 06:00 & 19:00", checked = danaBoxEnabled) { danaBoxEnabled = it }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = TextSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary, checkedThumbColor = Surface))
    }
}
