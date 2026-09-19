package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.source.remote.PengumumanData
import id.wahidiyah.miladiyyah.core.data.source.remote.PengumumanRepository
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HomeScreen(onNavigateToKiblat: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    var pengumuman by remember { mutableStateOf<PengumumanData?>(null) }
    val scope = rememberCoroutineScope()

    val calendarEngine = remember { CalendarEngine() }
    val today = remember { calendarEngine.getToday() }
    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val hijriMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")
    val dayNames = listOf("Ahad", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    val todayDayIndex = if (today.dayOfWeek == 7) 0 else today.dayOfWeek
    
    val dateString = "${dayNames[todayDayIndex]}, ${today.gregorian.day} ${monthNames[today.gregorian.month]} ${today.gregorian.year}"
    val hijriString = "${today.hijri.day} ${hijriMonthNames[today.hijri.month]} ${today.hijri.year} H • ${today.pasaran.name}"

    var currentTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time) }

    LaunchedEffect(Unit) {
        scope.launch { pengumuman = PengumumanRepository.fetchPengumuman() }
        while(true) {
            currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
            delay(1000L)
        }
    }

    val prayers = try { PrayerTimeEngine.getTodayPrayers() } catch(e:Exception) { emptyList() }
    val nextPrayer = try { PrayerTimeEngine.getNextPrayer(currentTime) } catch(e:Exception) { null }
    val currentMins = currentTime.hour * 60 + currentTime.minute
    val diffMins = if (nextPrayer != null) {
        val nextMins = nextPrayer.time.hour * 60 + nextPrayer.time.minute
        if (nextMins >= currentMins) nextMins - currentMins else (nextMins + 1440) - currentMins
    } else 0
    val isWarningTime = diffMins in 0..10

    Column(modifier = Modifier.fillMaxSize().background(SoftCream).verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
            Column {
                Text("Miladiyyah", color = DeepForestGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("App Resmi Jamaah", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(dateString, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SubtleGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(hijriString, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToKiblat() },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kompas Arah Kiblat", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Deteksi & Sinkronisasi GPS", fontSize = 12.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = DeepForestGreen)
                }
            }

            if (isWarningTime && nextPrayer != null) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), border = BorderStroke(1.dp, Color(0xFFEF9A9A)), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Persiapan Waktu ${nextPrayer.type.title}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                            Text("Kurang $diffMins menit lagi.", fontSize = 12.sp, color = Color(0xFFC62828))
                        }
                    }
                }
            }

            if (nextPrayer != null) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DeepForestGreen), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(50.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jadwal Berikutnya", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            Text(nextPrayer.type.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.Bottom) {
                                val timeString = "${nextPrayer.time.hour.toString().padStart(2, '0')}:${nextPrayer.time.minute.toString().padStart(2, '0')}"
                                Text(timeString, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("$diffMins menit lagi", color = SubtleGold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 3.dp))
                            }
                        }
                    }
                }
            }

            // LISTING SEMUA JADWAL SALAT HARI INI
            if (prayers.isNotEmpty()) {
                Text("Jadwal Salat Hari Ini", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen, modifier = Modifier.padding(top = 8.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        prayers.forEachIndexed { index, prayer ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prayer.type.title, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                val timeString = "${prayer.time.hour.toString().padStart(2, '0')}:${prayer.time.minute.toString().padStart(2, '0')}"
                                Text(timeString, fontSize = 14.sp, color = DeepForestGreen, fontWeight = FontWeight.Bold)
                            }
                            if (index < prayers.size - 1) {
                                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
