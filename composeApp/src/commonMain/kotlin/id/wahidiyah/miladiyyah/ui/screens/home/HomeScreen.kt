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
    val calendarEngine = remember { CalendarEngine() }
    val tz = TimeZone.currentSystemDefault()
    var currentDateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(tz)) }

    LaunchedEffect(Unit) {
        while(true) { currentDateTime = Clock.System.now().toLocalDateTime(tz); delay(1000L) }
    }

    var dayOffset by remember { mutableStateOf(0) }
    val targetDate = Clock.System.todayIn(tz).plus(dayOffset, DateTimeUnit.DAY)
    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Ahad")
    
    val dateString = "${dayNames[targetDate.dayOfWeek.ordinal]}, ${targetDate.dayOfMonth} ${monthNames[targetDate.monthNumber]} ${targetDate.year}"
    val todayHijriBase = remember { calendarEngine.getToday().hijri }
    val tempHijriDay = todayHijriBase.day + dayOffset
    val hijriDay = when { tempHijriDay > 30 -> tempHijriDay % 30; tempHijriDay <= 0 -> 30 + (tempHijriDay % 30); else -> tempHijriDay }
    val hijriMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")
    val hijriString = "$hijriDay ${hijriMonthNames[todayHijriBase.month]} ${todayHijriBase.year} H"

    val prayers = try { PrayerTimeEngine.getPrayers(targetDate) } catch(e:Exception) { emptyList() }
    val nextPrayer = try { PrayerTimeEngine.getNextPrayer(currentDateTime.time) } catch(e:Exception) { null }
    val diffSeconds = if (nextPrayer != null) { val nextSec = nextPrayer.time.hour * 3600 + nextPrayer.time.minute * 60; val curSec = currentDateTime.time.hour * 3600 + currentDateTime.time.minute * 60 + currentDateTime.time.second; if (nextSec >= curSec) nextSec - curSec else (nextSec + 86400) - curSec } else 0
    val h = diffSeconds / 3600; val m = (diffSeconds % 3600) / 60; val s = diffSeconds % 60
    val countdownStr = "- ${h.toString().padStart(2,'0')} : ${m.toString().padStart(2,'0')} : ${s.toString().padStart(2,'0')}"

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        // App Identity Header
        Box(modifier = Modifier.fillMaxWidth().background(BrandPrimary, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(top = 24.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("WAHIDIYAH", color = Surface, fontSize = 18.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onUpdateLocation() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandAccentLight, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(PrayerTimeEngine.locationName, color = Surface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                if (nextPrayer != null) {
                    val timeStr = "${nextPrayer.time.hour.toString().padStart(2,'0')}:${nextPrayer.time.minute.toString().padStart(2,'0')}"
                    Text(nextPrayer.type.title.uppercase(), color = BrandAccentLight, fontSize = 13.sp, letterSpacing = 4.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(timeStr, color = Surface, fontSize = 56.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(countdownStr, color = Surface.copy(alpha = 0.8f), fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                }
            }
        }

        // Elegant Date Navigator
        Box(modifier = Modifier.fillMaxWidth().offset(y = (-32).dp).padding(horizontal = 20.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(CircleShape).clickable { dayOffset -= 1 }.padding(12.dp)) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = BrandPrimary) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dateString, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(hijriString, fontSize = 13.sp, color = BrandPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Box(modifier = Modifier.clip(CircleShape).clickable { dayOffset += 1 }.padding(12.dp)) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = BrandPrimary) }
                }
            }
        }

        // Qibla Shortcut
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-8).dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.weight(1f).clickable { onNavigateToKiblat() }) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kompas Kiblat", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
        }

        // Clean Prayer List
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text("Jadwal Salat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 12.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    prayers.forEachIndexed { index, prayer ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(prayer.type.title, fontSize = 15.sp, color = TextSecondary, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            val tStr = "${prayer.time.hour.toString().padStart(2,'0')}:${prayer.time.minute.toString().padStart(2,'0')}"
                            Text(tStr, fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(20.dp))
                            if (prayer.type == PrayerType.IMSAK || prayer.type == PrayerType.TERBIT || prayer.type == PrayerType.DHUHA) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Border, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandPrimaryLight, modifier = Modifier.size(16.dp))
                            }
                        }
                        if (index < prayers.size - 1) { HorizontalDivider(color = Background, thickness = 1.5.dp) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
