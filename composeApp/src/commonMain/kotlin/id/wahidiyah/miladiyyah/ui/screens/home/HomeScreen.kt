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
import kotlinx.coroutines.delay
import kotlinx.datetime.*

@Composable
fun HomeScreen(onNavigateToKiblat: () -> Unit = {}, onUpdateLocation: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    var currentDateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(tz)) }
    LaunchedEffect(Unit) { while(true) { currentDateTime = Clock.System.now().toLocalDateTime(tz); delay(1000L) } }

    var dayOffset by remember { mutableStateOf(0) }
    val targetDate = Clock.System.todayIn(tz).plus(dayOffset, DateTimeUnit.DAY)
    val prayers = try { PrayerTimeEngine.getPrayers(targetDate) } catch(e:Exception) { emptyList() }
    val nextPrayer = try { PrayerTimeEngine.getNextPrayer(currentDateTime.time) } catch(e:Exception) { null }
    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Ahad")
    
    val hijriBase = CalendarEngine().getToday().hijri
    val tempDay = hijriBase.day + dayOffset
    val hDay = when { tempDay > 30 -> tempDay % 30; tempDay <= 0 -> 30 + (tempDay % 30); else -> tempDay }
    val hMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        // Hero Header Wahidiyah
        Box(modifier = Modifier.fillMaxWidth().background(BrandPrimaryDark, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(top = 28.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("WAHIDIYAH", color = Surface, fontSize = 16.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onUpdateLocation() }.background(BrandPrimary).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(PrayerTimeEngine.locationName, color = Surface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
                if (nextPrayer != null) {
                    val diff = (nextPrayer.time.hour * 3600 + nextPrayer.time.minute * 60) - (currentDateTime.time.hour * 3600 + currentDateTime.time.minute * 60 + currentDateTime.time.second)
                    val dSecs = if(diff >= 0) diff else diff + 86400
                    Text(nextPrayer.type.title.uppercase(), color = BrandAccent, fontSize = 14.sp, letterSpacing = 6.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${nextPrayer.time.hour.toString().padStart(2,'0')}:${nextPrayer.time.minute.toString().padStart(2,'0')}", color = Surface, fontSize = 64.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("- ${dSecs/3600}:${((dSecs%3600)/60).toString().padStart(2,'0')}:${(dSecs%60).toString().padStart(2,'0')}", color = Surface.copy(alpha=0.7f), fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
                }
            }
        }

        // Floating Date Navigator
        Box(modifier = Modifier.fillMaxWidth().offset(y = (-32).dp).padding(horizontal = 24.dp)) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(CircleShape).clickable { dayOffset -= 1 }.padding(12.dp)) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = BrandPrimary) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${dayNames[targetDate.dayOfWeek.ordinal]}, ${targetDate.dayOfMonth} ${monthNames[targetDate.monthNumber]}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$hDay ${hMonthNames[hijriBase.month]} ${hijriBase.year} H", fontSize = 13.sp, color = BrandPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Box(modifier = Modifier.clip(CircleShape).clickable { dayOffset += 1 }.padding(12.dp)) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = BrandPrimary) }
                }
            }
        }

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-8).dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.weight(1f).clickable { onNavigateToKiblat() }) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Arah Kiblat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }

        // Clean Prayer List
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text("Jadwal Salat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 16.dp, start = 8.dp))
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
        }
    }
}
