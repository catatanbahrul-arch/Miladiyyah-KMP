#!/bin/bash

echo "🚀 Membangun Fitur Jadwal Kegiatan Terintegrasi Google Sheets..."

# 1. BUAT MODEL & NETWORK SERVICE UNTUK KEGIATAN
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/source/remote/KegiatanNetworkService.kt
package id.wahidiyah.miladiyyah.core.data.source.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class KegiatanItem(val date: String, val title: String)

@Serializable
data class KegiatanResponse(val status: String, val data: List<KegiatanItem>)

object KegiatanRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec?action=kegiatan"

    suspend fun fetchKegiatan(): List<KegiatanItem> {
        return try {
            val timeStamp = Clock.System.now().toEpochMilliseconds()
            val url = "$BASE_URL&t=$timeStamp"
            val response: KegiatanResponse = client.get(url).body()
            if (response.status == "success") {
                response.data
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Gagal memuat jadwal kegiatan: ${e.message}")
            emptyList()
        }
    }
}
EOF

# 2. BUAT UI SCREEN UNTUK TAB KEGIATAN
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/kegiatan
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/kegiatan/KegiatanScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.kegiatan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanItem
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanRepository
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.launch

@Composable
fun KegiatanScreen() {
    var kegiatanList by remember { mutableStateOf<List<KegiatanItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            kegiatanList = KegiatanRepository.fetchKegiatan()
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SoftCream)) {
        // Header Banner
        Box(modifier = Modifier.fillMaxWidth().background(DeepForestGreen).padding(20.dp)) {
            Column {
                Text("Jadwal Kegiatan & Mujahadah", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Agenda resmi Pusat & Serentak Jamaah Wahidiyah", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepForestGreen)
            }
        } else if (kegiatanList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada jadwal kegiatan atau gagal memuat.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(kegiatanList) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(16.dp))
                                    Text(item.date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
EOF

# 3. HUBUNGKAN TAB KEGIATAN DI APP.KT
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/App.kt
package id.wahidiyah.miladiyyah

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch

enum class BottomTab { BERANDA, KALENDER, SALAT, KEGIATAN, MENU }

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
                    BottomTab.KEGIATAN -> KegiatanScreen()
                    else -> Box {}
                }
            }
        }
    }
}
EOF

echo "✅ Fitur Jadwal Kegiatan berhasil diintegrasikan secara penuh!"
EOF
