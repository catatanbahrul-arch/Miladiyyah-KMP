package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val TealHeader = Color(0xFF009688)
val SoftBackground = Color(0xFFF5F5F5)

@Composable
fun HomeScreen(onNavigateToKiblat: () -> Unit = {}, onUpdateLocation: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val calendarEngine = remember { CalendarEngine() }
    val today = remember { calendarEngine.getToday() }
    
    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val hijriMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")
    val dateString = "${today.gregorian.day} ${monthNames[today.gregorian.month]} ${today.gregorian.year}"
    val hijriString = "${today.hijri.day} ${hijriMonthNames[today.hijri.month]} ${today.hijri.year} H"

    var currentDateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) }

    LaunchedEffect(Unit) {
        while(true) {
            currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            delay(1000L)
        }
    }

    val prayers = try { PrayerTimeEngine.getTodayPrayers() } catch(e:Exception) { emptyList() }
    val nextPrayer = try { PrayerTimeEngine.getNextPrayer(currentDateTime.time) } catch(e:Exception) { null }
    
    // Perhitungan Hitung Mundur Detik
    val diffSeconds = if (nextPrayer != null) {
        val nextSec = nextPrayer.time.hour * 3600 + nextPrayer.time.minute * 60
        val curSec = currentDateTime.time.hour * 3600 + currentDateTime.time.minute * 60 + currentDateTime.time.second
        if (nextSec >= curSec) nextSec - curSec else (nextSec + 86400) - curSec
    } else 0
    val h = diffSeconds / 3600
    val m = (diffSeconds % 3600) / 60
    val s = diffSeconds % 60
    val countdownStr = "- ${h.toString().padStart(2,'0')} : ${m.toString().padStart(2,'0')} : ${s.toString().padStart(2,'0')}"

    Column(modifier = Modifier.fillMaxSize().background(SoftBackground).verticalScroll(scrollState)) {
        
        // 1. BAGIAN HEADER (HIJAU TEAL)
        Box(modifier = Modifier.fillMaxWidth().background(TealHeader).padding(top = 24.dp, bottom = 40.dp, start = 16.dp, end = 16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Titik Lokasi GPS", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                if (nextPrayer != null) {
                    val timeStr = "${nextPrayer.time.hour.toString().padStart(2,'0')}:${nextPrayer.time.minute.toString().padStart(2,'0')}"
                    Text("${nextPrayer.type.title} $timeStr WIB", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(countdownStr, color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.clickable { onUpdateLocation() }, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update", color = Color.White, fontSize = 14.sp)
                    }
                    Row(modifier = Modifier.clickable { onNavigateToKiblat() }, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Arah Kiblat", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        // 2. KARTU TANGGAL MENUMPANG DI ATAS HEADER
        Box(modifier = Modifier.fillMaxWidth().offset(y = (-24).dp).padding(horizontal = 16.dp)) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = TealHeader)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dateString, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(hijriString, fontSize = 13.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TealHeader)
                }
            }
        }

        // 3. DAFTAR JADWAL SALAT
        Column(modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-10).dp)) {
            prayers.forEachIndexed { index, prayer ->
                val iconEmoji = when(prayer.type) {
                    PrayerType.IMSAK -> "🌙"
                    PrayerType.SUBUH -> "⛅"
                    PrayerType.TERBIT -> "🌅"
                    PrayerType.DHUHA -> "🌤️"
                    PrayerType.DZUHUR -> "☀️"
                    PrayerType.ASHAR -> "🌥️"
                    PrayerType.MAGHRIB -> "🌇"
                    PrayerType.ISYA -> "🌌"
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(iconEmoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(prayer.type.title, fontSize = 15.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                    
                    val tStr = "${prayer.time.hour.toString().padStart(2,'0')}:${prayer.time.minute.toString().padStart(2,'0')}"
                    Text(tStr, fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    if (prayer.type == PrayerType.IMSAK || prayer.type == PrayerType.TERBIT || prayer.type == PrayerType.DHUHA) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
                if (index < prayers.size - 1) {
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
