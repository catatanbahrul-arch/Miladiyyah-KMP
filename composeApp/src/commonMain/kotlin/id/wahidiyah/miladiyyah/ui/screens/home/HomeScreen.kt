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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.AppRepository
import id.wahidiyah.miladiyyah.core.utils.UiState
import kotlinx.coroutines.delay
import kotlinx.datetime.*

@Composable
fun HomeScreen(onNavigateToSalat: () -> Unit = {}, onUpdateLocation: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    var currentDateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(tz)) }
    
    // State Pengumuman dari GAS
    var pengumumanState by remember { mutableStateOf<UiState<String>>(UiState.Loading) }
    
    LaunchedEffect(Unit) { 
        pengumumanState = AppRepository.getPengumuman()
        while(true) { currentDateTime = Clock.System.now().toLocalDateTime(tz); delay(1000L) } 
    }

    val targetDate = currentDateTime.date
    val nextPrayer = try { PrayerTimeEngine.getNextPrayer(currentDateTime.time) } catch(e:Exception) { null }
    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Ahad")
    val dateString = "${dayNames[targetDate.dayOfWeek.ordinal]}, ${targetDate.dayOfMonth} ${monthNames[targetDate.monthNumber]} ${targetDate.year}"
    val hijriBase = CalendarEngine().getToday().hijri
    val hMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        
        // 1. HEADER (LOKASI AMAN DI BAWAH BRANDING)
        Box(modifier = Modifier.fillMaxWidth().background(BrandPrimaryDark, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(top = 40.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // Baris 1: Logo & WAHIDIYAH
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, null, tint = BrandPrimary, modifier = Modifier.size(16.dp)) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("WAHIDIYAH", color = Surface, fontSize = 20.sp, letterSpacing = 6.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Baris 2: Lokasi (Tidak bertabrakan)
                Row(modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onUpdateLocation() }.background(BrandPrimary).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandAccentLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(PrayerTimeEngine.locationName, color = Surface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                // Salat Hero
                if (nextPrayer != null) {
                    val diff = (nextPrayer.time.hour * 3600 + nextPrayer.time.minute * 60) - (currentDateTime.time.hour * 3600 + currentDateTime.time.minute * 60 + currentDateTime.time.second)
                    val dSecs = if(diff >= 0) diff else diff + 86400
                    Text(nextPrayer.type.title.uppercase(), color = BrandAccent, fontSize = 13.sp, letterSpacing = 6.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${nextPrayer.time.hour.toString().padStart(2,'0')}:${nextPrayer.time.minute.toString().padStart(2,'0')}", color = Surface, fontSize = 64.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("- ${dSecs/3600}:${((dSecs%3600)/60).toString().padStart(2,'0')}:${(dSecs%60).toString().padStart(2,'0')}", color = Surface.copy(alpha=0.8f), fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
                }
            }
        }

        // 2. TANGGAL
        Box(modifier = Modifier.fillMaxWidth().offset(y = (-32).dp).padding(horizontal = 24.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(dateString, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${hijriBase.day} ${hMonthNames[hijriBase.month]} ${hijriBase.year} H", fontSize = 14.sp, color = BrandPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 3. PENGUMUMAN PENTING (Hanya tampil bila state = Success dan tidak kosong)
        if (pengumumanState is UiState.Success && (pengumumanState as UiState.Success).data.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-8).dp)) {
                Text("Pengumuman Penting", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandAccentLight), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, tint = BrandPrimary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text((pengumumanState as UiState.Success).data, fontSize = 14.sp, color = BrandPrimaryDark, lineHeight = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 4. PEMULIHAN FITUR EXISTING: TASYAFU'AN & DANA BOX
        Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-8).dp)) {
            Text("Pengingat Khusus", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), modifier = Modifier.weight(1f).clickable { onNavigateToSalat() }) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tasyafu'an", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("03:00 WIB", fontSize = 13.sp, color = TextSecondary)
                    }
                }
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), modifier = Modifier.weight(1f).clickable { onNavigateToSalat() }) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Dana Box", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("06:00 & 19:00", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
