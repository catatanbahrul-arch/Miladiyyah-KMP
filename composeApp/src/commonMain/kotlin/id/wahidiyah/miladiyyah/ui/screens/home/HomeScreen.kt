package id.wahidiyah.miladiyyah.ui.screens.home

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

val TealHeader = Color(0xFF009688)
val SoftBackground = Color(0xFFF5F5F5)

@Composable
fun HomeScreen(onNavigateToKiblat: () -> Unit = {}, onUpdateLocation: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val calendarEngine = remember { CalendarEngine() }
    val todayHijriBase = remember { calendarEngine.getToday().hijri }
    
    // Logika Navigasi Panah Kanan/Kiri
    var dayOffset by remember { mutableStateOf(0) }
    
    val tz = TimeZone.currentSystemDefault()
    val currentLocalDate = Clock.System.todayIn(tz)
    val targetDate = currentLocalDate.plus(dayOffset, DateTimeUnit.DAY)
    
    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Ahad")
    val targetDayIndex = targetDate.dayOfWeek.ordinal
    val dateString = "${dayNames[targetDayIndex]}, ${targetDate.dayOfMonth} ${monthNames[targetDate.monthNumber]} ${targetDate.year}"
    
    // Perhitungan kasaran maju/mundur bulan Hijriyah (Anggap 30 hari)
    val tempHijriDay = todayHijriBase.day + dayOffset
    val hijriDay = when {
        tempHijriDay > 30 -> tempHijriDay % 30
        tempHijriDay <= 0 -> 30 + (tempHijriDay % 30)
        else -> tempHijriDay
    }
    val hijriMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")
    val hijriString = "$hijriDay ${hijriMonthNames[todayHijriBase.month]} ${todayHijriBase.year} H"

    var currentDateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(tz)) }

    LaunchedEffect(Unit) {
        while(true) {
            currentDateTime = Clock.System.now().toLocalDateTime(tz)
            delay(1000L)
        }
    }

    // Ambil jadwal sesuai tanggal yang sedang dipilih
    val prayers = try { PrayerTimeEngine.getPrayers(targetDate) } catch(e:Exception) { emptyList() }
    
    // Next prayer tetap berdasarkan waktu asli hari ini, bukan hari yang dipilih
    val nextPrayer = try { PrayerTimeEngine.getNextPrayer(currentDateTime.time) } catch(e:Exception) { null }
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
        
        Box(modifier = Modifier.fillMaxWidth().background(TealHeader).padding(top = 24.dp, bottom = 40.dp, start = 16.dp, end = 16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    // Nama Lokasi Dinamis Hasil Reverse Geocoding
                    Text(PrayerTimeEngine.locationName, color = Color.White, fontSize = 14.sp)
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
                    Row(modifier = Modifier.clickable { onUpdateLocation() }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update", color = Color.White, fontSize = 14.sp)
                    }
                    Row(modifier = Modifier.clickable { onNavigateToKiblat() }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Arah Kiblat", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().offset(y = (-24).dp).padding(horizontal = 16.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier.fillMaxWidth()) {
                // Tombol Navigasi Kanan Kiri
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(CircleShape).clickable { dayOffset -= 1 }.padding(12.dp)) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Sebelumnya", tint = TealHeader)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dateString, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(hijriString, fontSize = 13.sp, color = Color.Gray)
                    }
                    Box(modifier = Modifier.clip(CircleShape).clickable { dayOffset += 1 }.padding(12.dp)) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Selanjutnya", tint = TealHeader)
                    }
                }
            }
        }

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
                
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
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
