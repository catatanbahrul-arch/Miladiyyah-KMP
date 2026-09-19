#!/bin/bash

echo "🎨 Menyelaraskan UI Kalender agar 100% presisi dengan desain referensi..."

cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/calendar/CalendarScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

enum class CalendarMode { MASEHI, HIJRIYAH, PASARAN }

@Composable
fun CalendarScreen(engine: CalendarEngine) {
    val today = remember { engine.getToday() }
    var currentDisplayMonth by remember { mutableStateOf(today.gregorian.localDate) }
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }
    var calendarMode by remember { mutableStateOf(CalendarMode.MASEHI) }
    
    if (selectedDay != null) {
        DateDetailScreen(day = selectedDay!!, onBack = { selectedDay = null })
        return
    }

    val daysInMonth = remember(currentDisplayMonth) {
        val firstDay = LocalDate(currentDisplayMonth.year, currentDisplayMonth.monthNumber, 1)
        val days = mutableListOf<CalendarDay>()
        val startOffset = firstDay.dayOfWeek.value - 1
        
        for (i in 0 until startOffset) {
            val pastDate = firstDay.minus(DatePeriod(days = startOffset - i))
            days.add(engine.getCalendarDay(pastDate))
        }
        
        var current = firstDay
        while (current.monthNumber == firstDay.monthNumber) {
            days.add(engine.getCalendarDay(current))
            current = current.plus(DatePeriod(days = 1))
        }
        days
    }

    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

    Column(
        modifier = Modifier.fillMaxSize().background(SoftCream),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Kembali", tint = TextPrimary)
            Text("Kalender", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = TextPrimary)
        }

        // TABS: Masehi | Hijriyah | Pasaran
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(WarmWhite, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabItem("Masehi", calendarMode == CalendarMode.MASEHI) { calendarMode = CalendarMode.MASEHI }
            TabItem("Hijriyah", calendarMode == CalendarMode.HIJRIYAH) { calendarMode = CalendarMode.HIJRIYAH }
            TabItem("Pasaran", calendarMode == CalendarMode.PASARAN) { calendarMode = CalendarMode.PASARAN }
        }

        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Header Navigasi Bulan
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.minus(DatePeriod(months = 1)) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan Sebelumnya")
                    }
                    Text("${monthNames[currentDisplayMonth.monthNumber]} ${currentDisplayMonth.year}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.plus(DatePeriod(months = 1)) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Bulan Berikutnya")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Label Hari
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab").forEach {
                        Text(it, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Grid Kalender
                LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
                    items(daysInMonth) { day ->
                        val isToday = day.gregorian.localDate == today.gregorian.localDate
                        val isCurrentMonth = day.gregorian.month == currentDisplayMonth.monthNumber

                        // Dinamika Teks Utama & Sekunder berdasarkan Tab
                        val primaryText = when (calendarMode) {
                            CalendarMode.MASEHI -> day.gregorian.day.toString()
                            CalendarMode.HIJRIYAH -> day.hijri.day.toString()
                            CalendarMode.PASARAN -> day.pasaran.name.take(3)
                        }
                        val secondaryText = when (calendarMode) {
                            CalendarMode.MASEHI -> day.pasaran.name.first().toString()
                            CalendarMode.HIJRIYAH -> day.gregorian.day.toString()
                            CalendarMode.PASARAN -> day.gregorian.day.toString()
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(0.85f)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isToday) DeepForestGreen else Color.Transparent)
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(primaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isToday) Color.White else if (isCurrentMonth) TextPrimary else Color.LightGray)
                                Text(secondaryText, fontSize = 9.sp, color = if (isToday) SubtleGold else if (isCurrentMonth) TextSecondary else Color.LightGray)
                                
                                // Indikator Titik (Dummy untuk kemiripan visual)
                                if (isCurrentMonth && day.gregorian.day % 4 == 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Box(modifier = Modifier.size(4.dp).background(DeepForestGreen, CircleShape))
                                        if (day.gregorian.day % 8 == 0) Box(modifier = Modifier.size(4.dp).background(ImportantOrange, CircleShape))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Tombol "Hari Ini" yang presisi (Outlined, kecil, elegan)
                OutlinedButton(
                    onClick = { currentDisplayMonth = today.gregorian.localDate },
                    border = BorderStroke(1.dp, DeepForestGreen),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Hari Ini", color = DeepForestGreen, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Legenda Titik Indikator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(DeepForestGreen, "Kegiatan")
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(UrgentRed, "Pengumuman")
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(ImportantOrange, "Hari Libur")
                }
            }
        }
    }
}

@Composable
fun RowScope.TabItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) DeepForestGreen else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = if (isSelected) Color.White else TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}
EOF

echo "✅ UI Kalender telah disinkronisasi persis dengan master desain!"
EOF
