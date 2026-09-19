package id.wahidiyah.miladiyyah.ui.screens.salat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.core.utils.AppCache
import kotlinx.datetime.*

@Composable
fun SalatScreen(onNavigateToKiblat: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    val prayers = try { PrayerTimeEngine.getPrayers(Clock.System.now().toLocalDateTime(tz).date) } catch(e:Exception) { emptyList() }

    // State Persistent menggunakan AppCache
    var adzanEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_ADZAN", true)) }
    var tarhimEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_TARHIM", true)) }
    var tasyafuanEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_TASYAFUAN", true)) }
    var danaBoxEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_DANABOX", true)) }

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column {
                Text("Waktu Salat", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(modifier=Modifier.height(4.dp))
                Text(PrayerTimeEngine.locationName, color = BrandPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(color = Border)
        
        Column(modifier = Modifier.padding(24.dp)) {
            Text("JADWAL HARI INI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    prayers.forEachIndexed { index, prayer ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            val isSilent = prayer.type == PrayerType.IMSAK || prayer.type == PrayerType.TERBIT || prayer.type == PrayerType.DHUHA
                            Box(modifier = Modifier.size(8.dp).background(if(isSilent) Border else BrandAccent, CircleShape))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(prayer.type.title, fontSize = 16.sp, color = if(isSilent) TextSecondary else TextPrimary, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Text("${prayer.time.hour.toString().padStart(2,'0')}:${prayer.time.minute.toString().padStart(2,'0')}", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        if (index < prayers.size - 1) { HorizontalDivider(color = Background, thickness = 2.dp) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text("PENGATURAN PENGINGAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Adzan & Salat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Peringatan masuk waktu", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = adzanEnabled, onCheckedChange = { adzanEnabled = it; AppCache.saveBoolean("ALARM_ADZAN", it) }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Pengingat Tarhim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Sebelum waktu Subuh", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tarhimEnabled, onCheckedChange = { tarhimEnabled = it; AppCache.saveBoolean("ALARM_TARHIM", it) }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Tasyafu'an", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Setiap 03:00 WIB", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tasyafuanEnabled, onCheckedChange = { tasyafuanEnabled = it; AppCache.saveBoolean("ALARM_TASYAFUAN", it) }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Dana Box", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Pukul 06:00 & 19:00", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = danaBoxEnabled, onCheckedChange = { danaBoxEnabled = it; AppCache.saveBoolean("ALARM_DANABOX", it) }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
