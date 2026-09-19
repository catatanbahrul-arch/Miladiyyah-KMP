package id.wahidiyah.miladiyyah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun SettingsScreen() {
    var adzanEnabled by remember { mutableStateOf(true) }
    var tarhimEnabled by remember { mutableStateOf(true) }
    var danaBoxEnabled by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(SoftCream)) {
        Box(modifier = Modifier.fillMaxWidth().background(DeepForestGreen).padding(20.dp)) {
            Column {
                Text("Pengaturan Notifikasi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Kelola pengingat salat, Tarhim, dan Dana Box", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            // 1. PENGINGAT ADZAN & POP-UP
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Adzan & Pengingat Salat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Pop-up (10 mnt sblm) & Suara Adzan pas masuk waktu", fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                        }
                    }
                    Switch(checked = adzanEnabled, onCheckedChange = { adzanEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DeepForestGreen))
                }
            }

            // 2. PENGINGAT TARHIM & SYAFA'AN (Setiap Hari)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Tarhim & Syafa'an", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Syafa'an (03:00) & Tarhim setiap hari sebelum Subuh", fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                        }
                    }
                    Switch(checked = tarhimEnabled, onCheckedChange = { tarhimEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DeepForestGreen))
                }
            }

            // 3. PENGINGAT DANA BOX
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Pengingat Dana Box", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Pagi (06:00) & Malam (19:00)", fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                        }
                    }
                    Switch(checked = danaBoxEnabled, onCheckedChange = { danaBoxEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DeepForestGreen))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("*Semua fitur pengingat ini otomatis aktif saat aplikasi diinstal untuk memudahkan Jamaah.", fontSize = 11.sp, color = SubtleGold, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}
