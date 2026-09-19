#!/bin/bash

echo "🎨 Menerapkan UI Kalender All-in-One (Desain Referensi NU Online)..."

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.calendar.model.CalendarDay
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

// Helper untuk mengubah angka Latin (19) menjadi angka Arab (١٩)
fun Int.toArabicDigits(): String {
    val arabicNumbers = listOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { if (it.isDigit()) arabicNumbers[it.toString().toInt()] else it }.joinToString("")
}

@Composable
fun CalendarScreen(engine: CalendarEngine) {
    val today = remember { engine.getToday() }
    var currentDisplayMonth by remember { mutableStateOf(today.gregorian.localDate) }
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }
    
    if (selectedDay != null) {
        DateDetailScreen(day = selectedDay!!, onBack = { selectedDay = null })
        return
    }

    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val hijriMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")
    val dayNames = listOf("Ahad", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu") // Ahad di depan

    val daysInMonth = remember(currentDisplayMonth) {
        val firstDay = LocalDate(currentDisplayMonth.year, currentDisplayMonth.monthNumber, 1)
        val days = mutableListOf<CalendarDay>()
        
        // Aturan offset untuk Ahad (Minggu) sebagai kolom pertama (Index 0)
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
        
        // Penuhi sisa grid agar selalu 42 kotak (6 baris)
        val remaining = 42 - days.size
        var nextMonthDay = current
        for(i in 0 until remaining) {
            days.add(engine.getCalendarDay(nextMonthDay))
            nextMonthDay = nextMonthDay.plus(DatePeriod(days = 1))
        }
        days
    }
    
    // Kalkulasi rentang bulan Hijriyah (Contoh: Rabiul Awal - Rabiul Akhir 1448)
    val firstHijri = daysInMonth.firstOrNull { it.gregorian.month == currentDisplayMonth.monthNumber }?.hijri
    val lastHijri = daysInMonth.lastOrNull { it.gregorian.month == currentDisplayMonth.monthNumber }?.hijri
    
    val hijriRangeText = if (firstHijri != null && lastHijri != null) {
        if (firstHijri.month == lastHijri.month) {
            "${hijriMonthNames[firstHijri.month]} ${firstHijri.year}"
        } else {
            "${hijriMonthNames[firstHijri.month]} - ${hijriMonthNames[lastHijri.month]} ${lastHijri.year}"
        }
    } else ""

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 1. TOP GREEN BAR (Info Hari Ini)
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

        // 2. BULAN NAVIGATOR
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

        // 3. NAMA HARI
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            dayNames.forEach { dayName ->
                Text(dayName, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

        // 4. GRID KALENDER ALL-IN-ONE
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().weight(1f) // Ambil sisa ruang untuk grid
        ) {
            items(daysInMonth) { day ->
                val isCurrentMonth = day.gregorian.month == currentDisplayMonth.monthNumber
                val isToday = day.gregorian.localDate == today.gregorian.localDate
                val isSunday = day.gregorian.localDate.dayOfWeek.value == 7
                val isFriday = day.gregorian.localDate.dayOfWeek.value == 5
                
                // Pewarnaan Spesifik (Minggu Merah, Jumat Hijau)
                val mainColor = if (!isCurrentMonth) Color.LightGray.copy(alpha=0.5f)
                    else if (isSunday) UrgentRed
                    else if (isFriday) DeepForestGreen
                    else TextPrimary

                Box(
                    modifier = Modifier
                        .aspectRatio(0.9f)
                        .border(
                            BorderStroke(
                                if (isToday) 2.dp else 0.5.dp, 
                                if (isToday) DeepForestGreen else Color(0xFFF5F5F5) // Subtle border
                            )
                        )
                        .clickable { selectedDay = day }
                        .padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Kiri: Angka Arab Hijriyah | Kanan: Angka Masehi
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Text(day.hijri.day.toArabicDigits(), fontSize = 10.sp, color = if(isCurrentMonth) TextSecondary else mainColor)
                            Text(day.gregorian.day.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = mainColor)
                        }
                        
                        // Tengah Bawah: Nama Pasaran
                        Text(day.pasaran.name, fontSize = 10.sp, color = if(isCurrentMonth) TextSecondary else mainColor)
                        
                        // Bawah: Titik Penanda (Dummy event dots)
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
        
        // 5. EVENT LIST (HARI BESAR)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("| Hari Besar & Libur Nasional", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Dummy List Item (Meniru Gambar)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier
                    .border(1.dp, Color(0xFFBBDEFB), RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(8.dp), 
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sep", fontSize = 10.sp, color = Color(0xFF1976D2))
                        Text("19", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Hari Lahir Lembaga Pendidikan Ma'arif NU", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Sabtu, 19 September 2026 / 7 Rabiul Akhir 1448", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DateDetailScreen(day: CalendarDay, onBack: () -> Unit) {
    val dayNames = listOf("", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    val hijriMonths = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")
    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

    Column(modifier = Modifier.fillMaxSize().background(SoftCream)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Kembali") }
            Text("Detail Tanggal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(dayNames[day.dayOfWeek], fontSize = 14.sp, color = TextSecondary)
                Text("${day.gregorian.day} ${monthNames[day.gregorian.month]} ${day.gregorian.year}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SoftCream)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Hijriyah", color = TextSecondary)
                    Text("${day.hijri.day} ${hijriMonths[day.hijri.month]} ${day.hijri.year} H", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pasaran", color = TextSecondary)
                    Text(day.pasaran.name, fontWeight = FontWeight.Bold)
                }
            }
        }
        EmptyStateCard("Jadwal Salat belum disinkronkan.")
        EmptyStateCard("Tidak ada kegiatan pada tanggal ini.")
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, color = TextSecondary, fontSize = 14.sp)
        }
    }
}
EOF

echo "✅ UI Kalender telah dikonversi persis 100% mengikuti referensi visual All-In-One!"
EOF
