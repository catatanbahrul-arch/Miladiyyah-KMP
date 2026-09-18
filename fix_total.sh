#!/bin/bash

echo "🚀 Menjalankan Perbaikan Total: Kalender, ViewModel, dan UI Beranda..."

# 1. PASTIKAN FOLDER KALENDER DIBUAT
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar

# 2. BUAT MODEL KALENDER
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/MiladiyyahDate.kt
package id.wahidiyah.miladiyyah.core.domain.calendar

data class MiladiyyahDate(
    val dayOfWeek: String,
    val dayMasehi: Int,
    val monthMasehiName: String,
    val yearMasehi: Int,
    val dayHijriyah: Int,
    val monthHijriyahName: String,
    val yearHijriyah: Int,
    val pasaran: String
) {
    val formattedMasehi: String get() = "$dayMasehi $monthMasehiName $yearMasehi"
    val formattedHijriyah: String get() = "$dayHijriyah $monthHijriyahName $yearHijriyah H"
}
EOF

# 3. BUAT ENGINE KALENDER
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/CalendarEngine.kt
package id.wahidiyah.miladiyyah.core.domain.calendar

interface CalendarEngine {
    fun getTodayDate(): MiladiyyahDate
}
EOF

# 4. BUAT IMPLEMENTASI KALENDER
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/CalendarEngineImpl.kt
package id.wahidiyah.miladiyyah.core.domain.calendar

class CalendarEngineImpl : CalendarEngine {
    override fun getTodayDate(): MiladiyyahDate {
        return MiladiyyahDate(
            dayOfWeek = "Jumat", dayMasehi = 18, monthMasehiName = "September", yearMasehi = 2026,
            dayHijriyah = 26, monthHijriyahName = "Rabiul Akhir", yearHijriyah = 1448, pasaran = "Kliwon"
        )
    }
}
EOF

# 5. PERBARUI HOME UI STATE
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeUiState.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement

data class HomeUiState(
    val dayOfWeek: String = "",
    val masehiDate: String = "",
    val hijriyahDate: String = "",
    val pasaran: String = "",
    val location: String = "Kediri, Jawa Timur",
    val nextPrayerName: String = "Ashar",
    val nextPrayerTime: String = "14:48",
    val nextPrayerCountdown: String = "16 menit lagi",
    val activeImportantAnnouncements: List<Announcement> = emptyList(),
    val summaryActivitiesCount: Int = 2,
    val summaryDanaBoxTime: String = "19:00"
)
EOF

# 6. PERBARUI HOME VIEW MODEL
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeViewModel.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import id.wahidiyah.miladiyyah.core.domain.calendar.CalendarEngine
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
        val today = calendarEngine.getTodayDate()
        _uiState.update { currentState ->
            currentState.copy(
                masehiDate = today.formattedMasehi,
                hijriyahDate = today.formattedHijriyah,
                pasaran = today.pasaran,
                dayOfWeek = today.dayOfWeek
            )
        }
    }

    private fun observeAnnouncements() {
        scope.launch {
            repository.getActiveAnnouncements().collect { announcements ->
                val importantAnnouncements = announcements.filter { 
                    it.priority == AnnouncementPriority.IMPORTANT || it.priority == AnnouncementPriority.URGENT
                }
                _uiState.update { currentState ->
                    currentState.copy(activeImportantAnnouncements = importantAnnouncements)
                }
            }
        }
    }
}
EOF

# 7. PERBARUI HOME SCREEN (FINAL)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.repository.AnnouncementRepositoryImpl
import id.wahidiyah.miladiyyah.core.data.source.local.FakeLocalDataSource
import id.wahidiyah.miladiyyah.core.data.source.remote.RemoteDataSource
import id.wahidiyah.miladiyyah.core.domain.calendar.CalendarEngineImpl
import id.wahidiyah.miladiyyah.core.domain.repository.Announcement
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementPriority
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementState
import id.wahidiyah.miladiyyah.theme.*

class DummyRemote : RemoteDataSource {
    override suspend fun fetchAnnouncements(): List<Announcement> = emptyList()
}

@Composable
fun HomeScreen() {
    val fakeLocal = remember { FakeLocalDataSource() }
    val repository = remember { AnnouncementRepositoryImpl(fakeLocal, DummyRemote()) }
    val calendarEngine = remember { CalendarEngineImpl() }
    val viewModel = remember { HomeViewModel(repository, calendarEngine) }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        val fakeData = listOf(
            Announcement("1", "Perubahan Jadwal Mujahadah Kubro\nPelaksanaan dimajukan menjadi\n25-29 September 2026", AnnouncementPriority.IMPORTANT, AnnouncementState.ACTIVE)
        )
        fakeLocal.saveAnnouncements(fakeData)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        HomeHeader(uiState.location)
        DateSection(uiState)
        if (uiState.activeImportantAnnouncements.isNotEmpty()) {
            ImportantAnnouncementCard(uiState.activeImportantAnnouncements.first())
        }
        SmartPrayerCard(uiState)
        SummarySection(uiState)
        ActivitySection()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HomeHeader(location: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Miladiyyah", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
            Text(location, fontSize = 12.sp, color = TextSecondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi", tint = DeepForestGreen)
            Icon(Icons.Default.Settings, contentDescription = "Pengaturan", tint = DeepForestGreen)
        }
    }
}

@Composable
private fun DateSection(uiState: HomeUiState) {
    Column {
        Text("${uiState.dayOfWeek}, ${uiState.masehiDate}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = SubtleGold)
            Spacer(modifier = Modifier.width(6.dp))
            Text("${uiState.hijriyahDate} • ${uiState.pasaran}", fontSize = 12.sp, color = DeepForestGreen, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ImportantAnnouncementCard(announcement: Announcement) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ImportantOrange),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pengumuman Penting", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(announcement.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                Text("Lihat Selengkapnya >", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
private fun SmartPrayerCard(uiState: HomeUiState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepForestGreen),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Home, contentDescription = "Masjid", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Salat berikutnya", color = SoftCream, fontSize = 12.sp)
                Text(uiState.nextPrayerName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(uiState.nextPrayerTime, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(uiState.nextPrayerCountdown, color = SubtleGold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun SummarySection(uiState: HomeUiState) {
    Column {
        Text("Ringkasan Hari Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryItem(Icons.Default.List, "Kegiatan", "${uiState.summaryActivitiesCount}", Modifier.weight(1f), SubtleGold)
            SummaryItem(Icons.Default.Notifications, "Pengumuman", "Tidak ada", Modifier.weight(1f), UrgentRed)
            SummaryItem(Icons.Default.Favorite, "Dana Box", uiState.summaryDanaBoxTime, Modifier.weight(1f), NaturalGreen)
        }
    }
}

@Composable
private fun SummaryItem(icon: ImageVector, title: String, value: String, modifier: Modifier, iconColor: Color) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = WarmWhite), elevation = CardDefaults.cardElevation(1.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(title, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun ActivitySection() {
    Column {
        Text("Kegiatan Hari Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = WarmWhite), elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(50.dp).background(SoftCream, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = DeepForestGreen)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pengajian Rutin Wahidiyah", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                    Text("08:00 - 10:00", fontSize = 12.sp, color = TextSecondary)
                    Text("Masjid Baitul Wahid", fontSize = 12.sp, color = TextSecondary)
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}
EOF

echo "✅ SELURUH FILE BERHASIL DISELARASKAN!"
EOF
