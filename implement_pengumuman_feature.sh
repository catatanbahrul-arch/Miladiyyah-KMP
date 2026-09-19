#!/bin/bash

echo "⚙️ Menambahkan Fitur Pengumuman Penting di Beranda..."

# 1. BUAT REPOSITORY UNTUK PENGUMUMAN
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/source/remote/PengumumanNetworkService.kt
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
data class PengumumanData(val title: String, val content: String, val link: String)

@Serializable
data class PengumumanResponse(val status: String, val data: PengumumanData?)

object PengumumanRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec?action=pengumuman"

    suspend fun fetchPengumuman(): PengumumanData? {
        return try {
            val timeStamp = Clock.System.now().toEpochMilliseconds()
            val url = "$BASE_URL&t=$timeStamp"
            val response: PengumumanResponse = client.get(url).body()
            if (response.status == "success") {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            println("Gagal memuat pengumuman: ${e.message}")
            null
        }
    }
}
EOF

# 2. UPDATE HOMESCREEN.KT AGAR MENAMPILKAN PENGUMUMAN JIKA TIDAK KOSONG DI SHEET
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.source.remote.PengumumanData
import id.wahidiyah.miladiyyah.core.data.source.remote.PengumumanRepository
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()
    var pengumuman by remember { mutableStateOf<PengumumanData?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            pengumuman = PengumumanRepository.fetchPengumuman()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .verticalScroll(scrollState)
    ) {
        // Header Beranda
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepForestGreen)
                .padding(20.dp)
        ) {
            Column {
                Text("Jamaah Wahidiyah", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Aplikasi Kalender & Kegiatan Miladiyyah", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            // KARTU PENGUMUMAN PENTING (Hanya muncul jika di Google Sheet terisi / tidak kosong)
            if (pengumuman != null && (!pengumuman!!.title.isEmpty() || !pengumuman!!.content.isEmpty())) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)), // Warna kuning lembut penanda pengumuman penting
                    border = BorderStroke(1.dp, Color(0xFFFFE082)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (pengumuman!!.title.isNotEmpty()) pengumuman!!.title else "Pengumuman Penting",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57F17)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pengumuman!!.content,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                        
                        if (pengumuman!!.link.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { /* Bisa diarahkan buka link dokumen resmi */ },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepForestGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Buka Dokumen Resmi", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Menu Utama / Kartu Sambutan
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selamat Datang, Jamaah!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Gunakan aplikasi Miladiyyah untuk memantau kalender Hijriyah berstandar MABIMS / NU Online secara akurat, serta mengecek jadwal kegiatan mujahadah serempak di menu bawah.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
EOF

echo "✅ Fitur Pengumuman Dinamis Berbasis Google Sheets Berhasil Dipasang!"
EOF
