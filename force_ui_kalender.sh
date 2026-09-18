#!/bin/bash

echo "🚀 MEMAKSA UPDATE UI KALENDER & NAVIGASI..."

# 1. Paksa Update App.kt (Navigasi)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/App.kt
package id.wahidiyah.miladiyyah

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.theme.MiladiyyahTheme
import id.wahidiyah.miladiyyah.ui.screens.home.HomeScreen
import id.wahidiyah.miladiyyah.ui.screens.calendar.CalendarScreen

enum class BottomTab { BERANDA, KALENDER, SALAT, KEGIATAN, MENU }

@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(BottomTab.BERANDA) }
    val calendarEngine = remember { CalendarEngine() }

    MiladiyyahTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(icon = { Icon(Icons.Default.Home, "") }, label = { Text("Beranda") }, selected = selectedTab == BottomTab.BERANDA, onClick = { selectedTab = BottomTab.BERANDA })
                    NavigationBarItem(icon = { Icon(Icons.Default.DateRange, "") }, label = { Text("Kalender") }, selected = selectedTab == BottomTab.KALENDER, onClick = { selectedTab = BottomTab.KALENDER })
                    NavigationBarItem(icon = { Icon(Icons.Default.Notifications, "") }, label = { Text("Salat") }, selected = selectedTab == BottomTab.SALAT, onClick = { selectedTab = BottomTab.SALAT })
                    NavigationBarItem(icon = { Icon(Icons.Default.List, "") }, label = { Text("Kegiatan") }, selected = selectedTab == BottomTab.KEGIATAN, onClick = { selectedTab = BottomTab.KEGIATAN })
                    NavigationBarItem(icon = { Icon(Icons.Default.Menu, "") }, label = { Text("Menu") }, selected = selectedTab == BottomTab.MENU, onClick = { selectedTab = BottomTab.MENU })
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    BottomTab.BERANDA -> HomeScreen()
                    BottomTab.KALENDER -> CalendarScreen(calendarEngine)
                    else -> Box {}
                }
            }
        }
    }
}
EOF

# 2. Paksa Update CalendarScreen.kt (UI Grid Kalender)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/calendar/CalendarScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import id.wahidiyah.miladiyyah.core.domain.calendar.model.CalendarDay
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
fun CalendarScreen(engine: CalendarEngine) {
    val today = remember { engine.getToday() }
    var currentDisplayMonth by remember { mutableStateOf(today.gregorian.localDate) }
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }
    
    if (selectedDay != null) {
        DateDetailScreen(day = selectedDay!!, onBack = { selectedDay = null })
        return
    }

    val daysInMonth = remember(currentDisplayMonth) {
        val firstDay = LocalDate(currentDisplayMonth.year, currentDisplayMonth.monthNumber, 1)
        val days = mutableListOf<CalendarDay>()
        val startOffset = firstDay.dayOfWeek.value - 1
        for (i in 0 until startOffset) days.add(engine.getCalendarDay(firstDay.minus(startOffset - i, DateTimeUnit.DAY)))
        
        var current = firstDay
        while (current.monthNumber == firstDay.monthNumber) {
            days.add(engine.getCalendarDay(current))
            current = current.plus(1, DateTimeUnit.DAY)
        }
        days
    }

    Column(modifier = Modifier.fillMaxSize().background(SoftCream).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.minus(1, DateTimeUnit.MONTH) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Bulan Sebelumnya")
            }
            Text("${getMonthName(currentDisplayMonth.monthNumber)} ${currentDisplayMonth.year}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.plus(1, DateTimeUnit.MONTH) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Bulan Berikutnya")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach {
                Text(it, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(daysInMonth) { day ->
                val isToday = day.gregorian.localDate == today.gregorian.localDate
                val isCurrentMonth = day.gregorian.month == currentDisplayMonth.monthNumber

                Box(
                    modifier = Modifier.aspectRatio(0.8f).padding(2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isToday) DeepForestGreen else if (isCurrentMonth) Color.White else Color.Transparent)
                        .clickable { selectedDay = day },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${day.gregorian.day}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isToday) Color.White else if (isCurrentMonth) TextPrimary else TextSecondary)
                        Text("${day.pasaran.name.first()}", fontSize = 10.sp, color = if (isToday) SubtleGold else TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { currentDisplayMonth = today.gregorian.localDate },
            colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kembali ke Hari Ini")
        }
    }
}

fun getMonthName(month: Int): String = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")[month]
EOF

echo "✅ File Kalender & Navigasi Berhasil Di-update!"
EOF
