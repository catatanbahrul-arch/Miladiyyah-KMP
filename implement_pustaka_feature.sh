#!/bin/bash

echo "⚙️ Membangun Fitur Pustaka (Buku & Materi PDF)..."

# 1. BUAT REPOSITORY PUSTAKA
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/source/remote/PustakaNetworkService.kt
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
data class PustakaItem(val title: String, val content: String, val link: String)

@Serializable
data class PustakaResponse(val status: String, val data: List<PustakaItem>)

object PustakaRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec?action=pustaka"

    suspend fun fetchPustaka(): List<PustakaItem> {
        return try {
            val timeStamp = Clock.System.now().toEpochMilliseconds()
            val url = "$BASE_URL&t=$timeStamp"
            val response: PustakaResponse = client.get(url).body()
            if (response.status == "success") {
                response.data
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Gagal memuat pustaka: ${e.message}")
            emptyList()
        }
    }
}
EOF

# 2. BUAT TAMPILAN (UI) UNTUK HALAMAN PUSTAKA
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/pustaka
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/pustaka/PustakaScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.pustaka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.source.remote.PustakaItem
import id.wahidiyah.miladiyyah.core.data.source.remote.PustakaRepository
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.launch

@Composable
fun PustakaScreen() {
    var pustakaList by remember { mutableStateOf<List<PustakaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            pustakaList = PustakaRepository.fetchPustaka()
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SoftCream)) {
        // Header Halaman Pustaka
        Box(modifier = Modifier.fillMaxWidth().background(DeepForestGreen).padding(20.dp)) {
            Column {
                Text("Pustaka & Materi", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Kumpulan Buku, Kitab, & Dokumen Resmi PDF", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepForestGreen)
            }
        } else if (pustakaList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada dokumen atau materi di Pustaka.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pustakaList) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Book, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
                            if (item.content.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.content,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }

                            if (item.link.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { /* Aksi buka link file */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Buka / Download File", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
EOF

# 3. UPDATE APP.KT MENGGANTI MENU "SALAT" MENJADI "PUSTAKA"
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
                    NavigationBarItem(icon = { Icon(Icons.Default.Book, "") }, label = { Text("Pustaka") }, selected = selectedTab == BottomTab.PUSTAKA, onClick = { selectedTab = BottomTab.PUSTAKA })
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

echo "✅ Fitur Pustaka Berhasil Menggantikan Menu Salat di Navigasi Bawah!"
EOF
