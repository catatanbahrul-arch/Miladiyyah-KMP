#!/bin/bash

echo "🚀 Meningkatkan Sistem Sinkronisasi menjadi Agresif dan Anti-Cache..."

# 1. UPDATE NETWORK SERVICE (Membypass Cache Google dengan Timestamp)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/source/remote/CalendarNetworkService.kt
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
data class GasAdjustment(val month: String, val adjustment: Int, val desc: String)

@Serializable
data class GasResponse(val status: String, val data: List<GasAdjustment>)

class CalendarNetworkService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    suspend fun fetchAdjustments(url: String): Map<String, Int> {
        return try {
            // ANTI-CACHE: Menambahkan waktu saat ini ke URL agar Google/Ktor mengira ini link baru 
            // dan dipaksa membaca ulang langsung dari Google Sheets.
            val timeStamp = Clock.System.now().toEpochMilliseconds()
            val noCacheUrl = if (url.contains("?")) "$url&t=$timeStamp" else "$url?t=$timeStamp"
            
            val response: GasResponse = client.get(noCacheUrl).body()
            if (response.status == "success") {
                response.data.associate { it.month to it.adjustment }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            println("Gagal sinkronisasi kalender: ${e.message}")
            emptyMap()
        }
    }
}
EOF

# 2. UPDATE APP.KT (Sinkronisasi setiap kali Tab Kalender ditekan)
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
import kotlinx.coroutines.launch

enum class BottomTab { BERANDA, KALENDER, SALAT, KEGIATAN, MENU }

const val GAS_API_URL = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec"

@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(BottomTab.BERANDA) }
    val calendarEngine = remember { CalendarEngine() }
    val scope = rememberCoroutineScope()
    val networkService = remember { CalendarNetworkService() }

    // FUNGSI SINKRONISASI
    val syncCalendarData = {
        scope.launch {
            try {
                val adjustments = networkService.fetchAdjustments(GAS_API_URL)
                HijriAdjuster.updateAdjustments(adjustments)
            } catch (e: Exception) {
                println("Gagal memuat penyesuaian kalender: ${e.message}")
            }
        }
    }

    // 1. Sinkronisasi saat aplikasi pertama kali dibuka
    LaunchedEffect(Unit) {
        syncCalendarData()
    }

    // 2. Sinkronisasi ULANG secara diam-diam setiap kali tab Kalender ditekan
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
                    else -> Box {}
                }
            }
        }
    }
}
EOF

echo "✅ Sistem Anti-Cache terpasang! Aplikasi akan selalu mengambil data terbaru."
EOF
