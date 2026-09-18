#!/bin/bash

echo "🚀 Memulai eksekusi FASE 4: Presentation Layer & Dashboard UI..."

# 1. Buat State Holder (HomeUiState)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeUiState.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement

data class HomeUiState(
    val masehiDate: String = "18 September 2026",
    val hijriyahDate: String = "26 Rabiul Akhir 1448 H",
    val pasaran: String = "Jumat Kliwon",
    val location: String = "Kediri, Jawa Timur",
    val nextPrayerName: String = "Ashar",
    val nextPrayerTime: String = "14:48",
    val nextPrayerCountdown: String = "16 menit lagi",
    val activeImportantAnnouncements: List<Announcement> = emptyList(),
    val summaryActivitiesCount: Int = 2,
    val summaryDanaBoxTime: String = "19:00"
)
EOF

# 2. Buat HomeViewModel (Logika Pengambilan Data dari Database Lokal)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeViewModel.kt
package id.wahidiyah.miladiyyah.ui.screens.home

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
    private val repository: AnnouncementRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeAnnouncements()
    }

    private fun observeAnnouncements() {
        scope.launch {
            // UI HANYA mengambil data dari Repository (yang membaca DB Lokal)
            repository.getActiveAnnouncements().collect { announcements ->
                
                // Aturan: Tampilkan di dashboard HANYA jika Penting/Urgent
                val importantAnnouncements = announcements.filter { 
                    it.priority == AnnouncementPriority.IMPORTANT || it.priority == AnnouncementPriority.URGENT
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        activeImportantAnnouncements = importantAnnouncements
                    )
                }
            }
        }
    }
}
EOF

# 3. Update HomeScreen.kt (Implementasi Hierarki Visual Sesuai Master Prompt)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.repository.AnnouncementRepositoryImpl
import id.wahidiyah.miladiyyah.core.data.source.local.FakeLocalDataSource
import id.wahidiyah.miladiyyah.core.data.source.remote.RemoteDataSource
import id.wahidiyah.miladiyyah.core.domain.repository.Announcement
import id.wahidiyah.miladiyyah.theme.ImportantOrange
import id.wahidiyah.miladiyyah.theme.SoftCream

// Placeholder untuk Fase 0 agar project bisa ter-compile
class DummyRemote : RemoteDataSource {
    override suspend fun fetchAnnouncements(): List<Announcement> = emptyList()
}

@Composable
fun HomeScreen() {
    // Dependency Injection Manual (DI) untuk Fase 0
    val fakeLocal = remember { FakeLocalDataSource() }
    val dummyRemote = remember { DummyRemote() }
    val repository = remember { AnnouncementRepositoryImpl(fakeLocal, dummyRemote) }
    val viewModel = remember { HomeViewModel(repository) }
    
    // Obeserve State dari ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TANGGAL HARI INI (Hirarki Tertinggi)
        Column {
            Text(uiState.location, color = Color.Gray, fontSize = 12.sp)
            Text("Jumat, ${uiState.masehiDate}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("${uiState.hijriyahDate} • ${uiState.pasaran}", color = Color.DarkGray, fontSize = 14.sp)
        }

        // 2. STATE DINAMIS: Pengumuman Penting (Hanya muncul jika ada isinya)
        if (uiState.activeImportantAnnouncements.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ImportantOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PENGUMUMAN PENTING", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(uiState.activeImportantAnnouncements.first().title, color = Color.White)
                }
            }
        }

        // 3. SMART PRAYER CARD (Salat Berikutnya)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Salat Berikutnya", color = Color.Gray, fontSize = 12.sp)
                    Text(uiState.nextPrayerName, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1B4D3E)) // DeepForestGreen
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(uiState.nextPrayerTime, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(uiState.nextPrayerCountdown, color = ImportantOrange, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // 4. RINGKASAN HARI INI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard("Kegiatan", "${uiState.summaryActivitiesCount}", modifier = Modifier.weight(1f))
            SummaryCard("Pengumuman", "Tidak ada", modifier = Modifier.weight(1f))
            SummaryCard("Dana Box", uiState.summaryDanaBoxTime, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(title, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
EOF

echo "✅ FASE 4 Selesai! ViewModel dan Dashboard UI berhasil diimplementasikan."
EOF
