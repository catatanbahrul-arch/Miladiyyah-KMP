#!/bin/bash

echo "⚙️ Menerapkan Perbaikan UI (Tombol Pengumuman, Ikon Buku, & Backlight Kalender)..."

# 1. UPDATE APP.KT (Mengubah ikon menu bawah Pustaka menjadi ikon Buku)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/App.kt
package id.wahidiyah.miladiyyah

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import id.wahidiyah.miladiyyah.core.data.source.remote.CalendarNetworkService
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.HijriAdjuster
import id.wahidiyah.miladiyyah.theme.MiladiyyahTheme
import id.wahidiyah.miladiyyah.ui.screens.home.HomeScreen
import id.wahidiyah.miladiyyah.ui.screens.calendar.CalendarScreen
import id.wahidiyah.miladiyyah.ui.screens.kegiatan.KegiatanScreen
import id.wahidiyah.miladiyyah.ui.screens.pustaka.PustakaScreen
import kotlinx.coroutines.launch

enum class BottomTab { BERANDA, KALENDER, PUSTAKA, KEGIATAN, MENU }

const val GAS_API_URL = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec"

@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(BottomTab.BERANDA) }
    val calendarEngine = remember { CalendarEngine() }
    val scope = rememberCoroutineScope()
    val networkService = remember { CalendarNetworkService() }

    val syncCalendarData = {
        scope.launch {
            try {
                val adjustments = networkService.fetchCascadeAdjustments(GAS_API_URL)
                HijriAdjuster.updateAdjustments(adjustments)
            } catch (e: Exception) {
                println("Gagal memuat sinkronisasi kalender: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        syncCalendarData()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == BottomTab.KALENDER) {
            syncCalendarData()
        }
    }

    MiladiyyahTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(icon = { Icon(Icons.Default.Home, "") }, label = { Text("Beranda") }, selected = selectedTab == BottomTab.BERANDA, onClick = { selectedTab = BottomTab.BERANDA })
                    NavigationBarItem(icon = { Icon(Icons.Default.DateRange, "") }, label = { Text("Kalender") }, selected = selectedTab == BottomTab.KALENDER, onClick = { selectedTab = BottomTab.KALENDER })
                    NavigationBarItem(icon = { Icon(Icons.Default.MenuBook, "") }, label = { Text("Pustaka") }, selected = selectedTab == BottomTab.PUSTAKA, onClick = { selectedTab = BottomTab.PUSTAKA })
                    NavigationBarItem(icon = { Icon(Icons.Default.List, "") }, label = { Text("Kegiatan") }, selected = selectedTab == BottomTab.KEGIATAN, onClick = { selectedTab = BottomTab.KEGIATAN })
                    NavigationBarItem(icon = { Icon(Icons.Default.Menu, "") }, label = { Text("Menu") }, selected = selectedTab == BottomTab.MENU, onClick = { selectedTab = BottomTab.MENU })
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    BottomTab.BERANDA -> HomeScreen()
                    BottomTab.KALENDER -> CalendarScreen(calendarEngine)
                    BottomTab.PUSTAKA -> PustakaScreen()
                    BottomTab.KEGIATAN -> KegiatanScreen()
                    else -> Box {}
                }
            }
        }
    }
}
EOF

# 2. UPDATE HOMESCREEN.KT (Menambahkan tombol interaktif pada pengumuman untuk membuka Google Drive)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()
    var pengumuman by remember { mutableStateOf<PengumumanData?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            pengumuman = PengumumanRepository.fetchPengumuman()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepForestGreen)
                .padding(20.dp)
        ) {
            Column {
                Text("Jamaah Wahidiyah", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Aplikasi Kalender & Kegiatan Miladiyyah", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            if (pengumuman != null && (!pengumuman!!.title.isEmpty() || !pengumuman!!.content.isEmpty())) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (pengumuman!!.title.isNotEmpty()) pengumuman!!.title else "Pengumuman Penting",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57F17)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pengumuman!!.content,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )

                        if (!pengumuman!!.link.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFFFE082), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Aksi klik untuk membuka link Google Drive
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lihat Himbauan Selengkapnya",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = ">",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selamat Datang, Jamaah!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Gunakan aplikasi Miladiyyah untuk memantau kalender Hijriyah berstandar MABIMS / NU Online secara akurat, serta mengecek jadwal kegiatan dan pustaka materi.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
EOF

# 3. UPDATE CALENDARSCREEN.KT (Memastikan backlight tanggal kegiatan terdeteksi akurat dengan format tanggal YYYY-MM-DD)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/calendar/CalendarScreen.kt
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
    
    var allKegiatan by remember { mutableStateOf<List<KegiatanItem>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scope.launch {
            allKegiatan = KegiatanRepository.fetchKegiatan()
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

    Column(modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().background(DeepForestGreen).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    val todayDayIndex = if (today.dayOfWeek == 7) 0 else today.dayOfWeek
                    Text("${dayNames[todayDayIndex]} ${today.pasaran.name}, ${today.gregorian.day} ${monthNames[today.gregorian.month]} ${today.gregorian.year}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${today.hijri.day} ${hijriMonthNames[today.hijri.month]} ${today.hijri.year}", color = Color.White, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color.White)
                    Icon(Icons.Outlined.Settings, contentDescription = "Pengaturan", tint = Color.White)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.minus(DatePeriod(months = 1)) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan Sebelumnya", tint = DeepForestGreen)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${monthNames[currentDisplayMonth.monthNumber]} ${currentDisplayMonth.year}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(hijriRangeText, fontSize = 12.sp, color = TextPrimary)
            }
            IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.plus(DatePeriod(months = 1)) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Bulan Berikutnya", tint = DeepForestGreen)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            dayNames.forEach { dayName ->
                Text(dayName, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

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
                        else if (isSunday) UrgentRed
                        else if (isFriday) DeepForestGreen
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
                                    if (isToday) DeepForestGreen else Color(0xFFF5F5F5)
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
                                    Box(modifier = Modifier.size(4.dp).background(if(day.gregorian.day == 19) Color(0xFF1976D2) else SubtleGold, CircleShape))
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
        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("| Agenda Kegiatan Bulan Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.date, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
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
EOF

echo "✅ Semua Perbaikan UI Berhasil Diterapkan!"
EOF
