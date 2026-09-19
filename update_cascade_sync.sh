#!/bin/bash

echo "⚙️ Memperbarui Logika Kalender menjadi Sistem Berantai (Cascade)..."

# 1. UPDATE NETWORK SERVICE & MODEL DATA
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
data class CascadeAdjustment(val startDate: String, val adjustment: Int, val desc: String)

@Serializable
data class GasCascadeResponse(val status: String, val data: List<CascadeAdjustment>)

class CalendarNetworkService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    suspend fun fetchCascadeAdjustments(url: String): List<CascadeAdjustment> {
        return try {
            val timeStamp = Clock.System.now().toEpochMilliseconds()
            val noCacheUrl = if (url.contains("?")) "$url&t=$timeStamp" else "$url?t=$timeStamp"
            
            val response: GasCascadeResponse = client.get(noCacheUrl).body()
            if (response.status == "success") {
                response.data
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Gagal sinkronisasi kalender berantai: ${e.message}")
            emptyList()
        }
    }
}
EOF

# 2. UPDATE HIJRI ADJUSTER UNTUK MENGHITUNG BERANTAI (CASCADE)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine/HijriAdjuster.kt
package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.data.source.remote.CascadeAdjustment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

object HijriAdjuster {
    
    // Default fallback jika offline: Mulai 2026-09-01 koreksi -1
    private val defaultAdjustments = listOf(
        CascadeAdjustment("2026-09-01", -1, "Default Fallback")
    )

    private val _adjustmentsFlow = MutableStateFlow<List<CascadeAdjustment>>(defaultAdjustments)
    val adjustmentsFlow: StateFlow<List<CascadeAdjustment>> = _adjustmentsFlow.asStateFlow()

    fun updateAdjustments(newList: List<CascadeAdjustment>) {
        if (newList.isNotEmpty()) {
            // Urutkan berdasarkan tanggal mulai secara ascending
            _adjustmentsFlow.value = newList.sortedBy { it.startDate }
        }
    }

    fun getOffset(date: LocalDate): Int {
        val list = _adjustmentsFlow.value
        if (list.isEmpty()) return 0

        val dateString = date.toString() // Format "YYYY-MM-DD"
        var activeOffset = 0

        // Cari aturan aktif: Ambil koreksi dari tanggal mulai yang paling akhir 
        // yang lebih kecil atau sama dengan tanggal target
        for (item in list) {
            if (dateString >= item.startDate) {
                activeOffset = item.adjustment
            } else {
                break
            }
        }
        return activeOffset
    }
}
EOF

# 3. UPDATE APP.KT UNTUK MEMANGGIL API CASCADE BARU
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

    val syncCalendarData = {
        scope.launch {
            try {
                val adjustments = networkService.fetchCascadeAdjustments(GAS_API_URL)
                HijriAdjuster.updateAdjustments(adjustments)
            } catch (e: Exception) {
                println("Gagal memuat sinkronisasi berantai: ${e.message}")
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
                    else -> Box {}
                }
            }
        }
    }
}
EOF

echo "✅ Sistem Berantai (Cascade) berhasil di-deploy ke dalam kode KMP!"
EOF
