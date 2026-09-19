package id.wahidiyah.miladiyyah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
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

    // Memicu penjadwalan alarm dengan aman dari dalam layar (bukan saat aplikasi baru dibuka)
    LaunchedEffect(Unit) {
        try {
            AlarmScheduler.scheduleAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SoftCream).verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().background(DeepForestGreen).padding(20.dp)) {
            Column {
                Text("Pengaturan Notifikasi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Kelola pengingat Salat, Tarhim, Syafa'an & Dana Box", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Adzan & Pengingat Salat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Pop-up (10mnt sblm) & Suara Adzan", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(checked = adzanEnabled, onCheckedChange = { adzanEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = DeepForestGreen))
                }
            }

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pengingat Tarhim", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Setiap hari sebelum Subuh", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(checked = tarhimEnabled, onCheckedChange = { tarhimEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = DeepForestGreen))
                }
            }

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pengingat Syafa'an", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Setiap jam 03:00 pagi", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(checked = tasyafuanEnabled, onCheckedChange = { tasyafuanEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = DeepForestGreen))
                }
            }

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pengingat Dana Box", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Pagi (06:00) & Malam (19:00)", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(checked = danaBoxEnabled, onCheckedChange = { danaBoxEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = DeepForestGreen))
                }
            }
        }
    }
}
