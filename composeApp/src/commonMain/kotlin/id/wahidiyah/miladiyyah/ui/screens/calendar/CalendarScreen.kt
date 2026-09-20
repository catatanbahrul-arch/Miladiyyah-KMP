package id.wahidiyah.miladiyyah.ui.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanItem
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanRepository
import id.wahidiyah.miladiyyah.core.data.sync.RemoteSyncCoordinator
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.HijriAdjuster
import id.wahidiyah.miladiyyah.core.domain.calendar.model.CalendarDay
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun Int.toArabicDigits(): String {
    val arabicNumbers = listOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { if (it.isDigit()) arabicNumbers[it.toString().toInt()] else it }.joinToString("")
}

@Composable
fun CalendarScreen(engine: CalendarEngine) {
    val dynamicAdjustments by HijriAdjuster.adjustmentsFlow.collectAsState()
    val today = remember(dynamicAdjustments) { engine.getToday() }
    var currentDisplayMonth by remember { mutableStateOf(today.gregorian.localDate) }
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }
    
    var allKegiatan by remember {
        mutableStateOf(RemoteSyncCoordinator.loadCachedKegiatan())
    }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scope.launch {
            allKegiatan = RemoteSyncCoordinator.syncKegiatan()
        }
    }
    
    if (selectedDay != null) {
        DateDetailScreen(day = selectedDay!!, onBack = { selectedDay = null })
        return
    }

    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val hijriMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")
    val dayNames = listOf("Ahad", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

    val daysInMonth = remember(currentDisplayMonth, dynamicAdjustments) {
        val firstDay = LocalDate(currentDisplayMonth.year, currentDisplayMonth.monthNumber, 1)
        val days = mutableListOf<CalendarDay>()
        
        val startOffset = if (firstDay.dayOfWeek.value == 7) 0 else firstDay.dayOfWeek.value
        
        for (i in 0 until startOffset) {
            val pastDate = firstDay.minus(DatePeriod(days = startOffset - i))
            days.add(engine.getCalendarDay(pastDate))
        }
        
        var current = firstDay
        while (current.monthNumber == firstDay.monthNumber) {
            days.add(engine.getCalendarDay(current))
            current = current.plus(DatePeriod(days = 1))
        }
        
        val remaining = 42 - days.size
        var nextMonthDay = current
        for(i in 0 until remaining) {
            days.add(engine.getCalendarDay(nextMonthDay))
            nextMonthDay = nextMonthDay.plus(DatePeriod(days = 1))
        }
        days
    }
    
    // Normalisasi format tanggal kegiatan agar cocok dengan engine (YYYY-MM-DD)
    val eventDateSet = remember(allKegiatan) {
        allKegiatan.map { item ->
            try {
                val parts = item.date.split("-")
                if (parts.size == 3) {
                    val y = parts[0]
                    val m = parts[1].toInt().toString() // Menghilangkan leading zero jika ada
                    val d = parts[2].toInt().toString()
                    "$y-$m-$d"
                } else item.date.trim()
            } catch (e: Exception) {
                item.date.trim()
            }
        }.toSet()
    }
    
    val filteredKegiatan = remember(currentDisplayMonth, allKegiatan) {
        allKegiatan.filter { item ->
            try {
                val parts = item.date.split("-")
                if (parts.size >= 2) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    year == currentDisplayMonth.year && month == currentDisplayMonth.monthNumber
                } else false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    val firstHijri = daysInMonth.firstOrNull { it.gregorian.month == currentDisplayMonth.monthNumber }?.hijri
    val lastHijri = daysInMonth.lastOrNull { it.gregorian.month == currentDisplayMonth.monthNumber }?.hijri
    
    val hijriRangeText = if (firstHijri != null && lastHijri != null) {
        if (firstHijri.month == lastHijri.month) {
            "${hijriMonthNames[firstHijri.month]} ${firstHijri.year}"
        } else {
            "${hijriMonthNames[firstHijri.month]} - ${hijriMonthNames[lastHijri.month]} ${lastHijri.year}"
        }
    } else ""

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().background(BrandAccentLight).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    val todayDayIndex = if (today.dayOfWeek == 7) 0 else today.dayOfWeek
                    Text("${dayNames[todayDayIndex]} ${today.pasaran.name}, ${today.gregorian.day} ${monthNames[today.gregorian.month]} ${today.gregorian.year}", color = BrandPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${today.hijri.day} ${hijriMonthNames[today.hijri.month]} ${today.hijri.year}", color = BrandPrimary, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Outlined.Info, contentDescription = "Info", tint = BrandPrimary)
                    Icon(Icons.Outlined.Settings, contentDescription = "Pengaturan", tint = BrandPrimary)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.minus(DatePeriod(months = 1)) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan Sebelumnya", tint = BrandPrimary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${monthNames[currentDisplayMonth.monthNumber]} ${currentDisplayMonth.year}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(hijriRangeText, fontSize = 12.sp, color = TextPrimary)
            }
            IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.plus(DatePeriod(months = 1)) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Bulan Berikutnya", tint = BrandPrimary)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            dayNames.forEach { dayName ->
                Text(dayName, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }

        HorizontalDivider(color = Border, thickness = 1.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .padding(horizontal = 4.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) {
                items(daysInMonth) { day ->
                    val isCurrentMonth = day.gregorian.month == currentDisplayMonth.monthNumber
                    val isToday = day.gregorian.localDate == today.gregorian.localDate
                    val isSunday = day.gregorian.localDate.dayOfWeek.value == 7
                    val isFriday = day.gregorian.localDate.dayOfWeek.value == 5
                    
                    val y = day.gregorian.year
                    val m = day.gregorian.month
                    val d = day.gregorian.day
                    val dateKey = "$y-$m-$d"
                    
                    val hasEvent = eventDateSet.contains(dateKey) && isCurrentMonth

                    val mainColor = if (!isCurrentMonth) Color.LightGray.copy(alpha=0.5f)
                        else if (isSunday) Error
                        else if (isFriday) BrandPrimary
                        else TextPrimary

                    Box(
                        modifier = Modifier
                            .aspectRatio(0.9f)
                            .background(
                                if (hasEvent) Color(0xFFC8E6C9).copy(alpha = 0.7f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                BorderStroke(
                                    if (isToday) 2.dp else 0.5.dp, 
                                    if (isToday) BrandPrimary else Color(0xFFF5F5F5)
                                ),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedDay = day }
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Text(day.hijri.day.toArabicDigits(), fontSize = 10.sp, color = if(isCurrentMonth) TextSecondary else mainColor)
                                Text(day.gregorian.day.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = mainColor)
                            }
                            
                            Text(day.pasaran.name, fontSize = 10.sp, color = if(isCurrentMonth) TextSecondary else mainColor)
                            
                            if (isCurrentMonth && (day.gregorian.day % 8 == 0 || day.gregorian.day == 19)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(modifier = Modifier.size(4.dp).background(if(day.gregorian.day == 19) Color(0xFF1976D2) else BrandAccent, CircleShape))
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Border, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Agenda Kegiatan Bulan Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (filteredKegiatan.isEmpty()) {
                Text("Tidak ada agenda kegiatan khusus pada bulan ini.", fontSize = 12.sp, color = TextSecondary)
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredKegiatan.forEach { item ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandAccentLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, Border, RoundedCornerShape(6.dp))
                                        .background(BrandAccentLight, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.date, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.title, 
                                    fontSize = 12.sp, 
                                    color = TextPrimary, 
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
