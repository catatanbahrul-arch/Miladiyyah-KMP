#!/bin/bash

echo "🚀 Menyinkronkan ulang seluruh Domain & UI Kalender (Final Fix)..."

# 1. Bersihkan file lama yang menyebabkan konflik
rm -f composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/MiladiyyahDate.kt
rm -f composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/CalendarEngineImpl.kt

mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/model
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine

# 2. MODELS
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/model/CalendarModels.kt
package id.wahidiyah.miladiyyah.core.domain.calendar.model

import kotlinx.datetime.LocalDate

data class GregorianDate(val year: Int, val month: Int, val day: Int, val localDate: LocalDate)
data class HijriDate(val year: Int, val month: Int, val day: Int)
data class Pasaran(val name: String, val index: Int)

data class CalendarDay(
    val gregorian: GregorianDate,
    val hijri: HijriDate,
    val pasaran: Pasaran,
    val dayOfWeek: Int
)
EOF

# 3. PASARAN CALCULATOR
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine/PasaranCalculator.kt
package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.domain.calendar.model.Pasaran

object PasaranCalculator {
    private val pasaranNames = listOf("Legi", "Pahing", "Pon", "Wage", "Kliwon")
    private const val EPOCH_PASARAN_INDEX = 3 // 1 Jan 1970 = Kamis Wage

    fun calculate(epochDays: Long): Pasaran {
        val index = ((epochDays + EPOCH_PASARAN_INDEX) % 5 + 5) % 5
        return Pasaran(pasaranNames[index.toInt()], index.toInt())
    }
}
EOF

# 4. HIJRI CALCULATOR
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine/HijriCalculator.kt
package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.domain.calendar.model.HijriDate

object HijriCalculator {
    private const val HIJRI_EPOCH_OFFSET = 492148L

    fun calculate(epochDays: Long): HijriDate {
        var days = epochDays + HIJRI_EPOCH_OFFSET
        var cycles = days / 10631
        days %= 10631
        
        if (days < 0) {
            days += 10631
            cycles -= 1
        }
        
        var year = (cycles * 30).toInt() + 1
        var yDays = days.toInt()

        while (true) {
            val isLeap = (11 * year + 14) % 30 < 11
            val daysInYear = if (isLeap) 355 else 354
            if (yDays < daysInYear) break
            yDays -= daysInYear
            year++
        }

        var month = 1
        while (true) {
            val isLeap = (11 * year + 14) % 30 < 11
            val daysInMonth = if (month == 12 && isLeap) 30 else if (month % 2 != 0) 30 else 29
            if (yDays < daysInMonth) break
            yDays -= daysInMonth
            month++
        }

        return HijriDate(year, month, yDays + 1)
    }
}
EOF

# 5. CALENDAR ENGINE
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine/CalendarEngine.kt
package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.domain.calendar.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CalendarEngine {
    fun getToday(): CalendarDay {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getCalendarDay(today)
    }

    fun getCalendarDay(date: LocalDate): CalendarDay {
        val epochDays = date.toEpochDays().toLong()
        return CalendarDay(
            gregorian = GregorianDate(date.year, date.monthNumber, date.dayOfMonth, date),
            hijri = HijriCalculator.calculate(epochDays),
            pasaran = PasaranCalculator.calculate(epochDays),
            dayOfWeek = date.dayOfWeek.value
        )
    }
}
EOF

# 6. HOME VIEW MODEL
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeViewModel.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementPriority
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: AnnouncementRepository,
    private val calendarEngine: CalendarEngine
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayDate()
        observeAnnouncements()
    }

    private fun loadTodayDate() {
        val today = calendarEngine.getToday()
        val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val hijriMonths = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")
        val dayNames = listOf("", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

        _uiState.update { currentState ->
            currentState.copy(
                masehiDate = "${today.gregorian.day} ${monthNames[today.gregorian.month]} ${today.gregorian.year}",
                hijriyahDate = "${today.hijri.day} ${hijriMonths[today.hijri.month]} ${today.hijri.year} H",
                pasaran = today.pasaran.name,
                dayOfWeek = dayNames[today.dayOfWeek]
            )
        }
    }

    private fun observeAnnouncements() {
        scope.launch {
            repository.getActiveAnnouncements().collect { announcements ->
                val importantAnnouncements = announcements.filter { it.priority == AnnouncementPriority.IMPORTANT || it.priority == AnnouncementPriority.URGENT }
                _uiState.update { it.copy(activeImportantAnnouncements = importantAnnouncements) }
            }
        }
    }
}
EOF

# 7. CALENDAR SCREEN (FIX DATE PERIOD)
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
import kotlinx.datetime.DatePeriod
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

    Column(modifier = Modifier.fillMaxSize().background(SoftCream).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.minus(DatePeriod(months = 1)) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Bulan Sebelumnya")
            }
            Text("${monthNames[currentDisplayMonth.monthNumber]} ${currentDisplayMonth.year}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { currentDisplayMonth = currentDisplayMonth.plus(DatePeriod(months = 1)) }) {
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
            Text("Kembali ke Hari Ini", color = Color.White)
        }
    }
}
EOF

# 8. DATE DETAIL SCREEN (FIX M3 HorizontalDivider)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/calendar/DateDetailScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.calendar.model.CalendarDay
import id.wahidiyah.miladiyyah.theme.*

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

echo "✅ SELURUH FILE KALENDER & UI BERHASIL DIBENARKAN!"
EOF
