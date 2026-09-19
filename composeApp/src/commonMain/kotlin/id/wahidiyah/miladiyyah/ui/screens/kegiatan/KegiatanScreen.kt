package id.wahidiyah.miladiyyah.ui.screens.kegiatan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanRepository
import id.wahidiyah.miladiyyah.core.utils.AppCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun KegiatanScreen() {
    val scope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    
    // 1. MEMORI 0 DETIK: Langsung muat data lama dari Cache HP saat layar disentuh
    var kegiatanData by remember { mutableStateOf(AppCache.load("KEGIATAN_DATA") ?: "Belum ada data. Silakan perbarui.") }

    // 2. SILENT BACKGROUND PEEK: Mengintip diam-diam ke server GAS saat layar dibuka
    LaunchedEffect(Unit) {
        try {
            val newData = withContext(Dispatchers.IO) { KegiatanRepository.fetchKegiatanFromGAS() }
            // Jika ada perubahan data di Google Sheet, langsung ganti tanpa loading
            if (newData != kegiatanData) {
                AppCache.save("KEGIATAN_DATA", newData)
                kegiatanData = newData
            }
        } catch (e: Exception) {
            // Sinyal jelek? Diam saja, tetap pakai data lama di layar.
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        
        // HEADER KEGIATAN
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF009688)).padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Jadwal Kegiatan", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Otomatis tersimpan offline", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                
                // 3. SMART MANUAL UPDATE: Tombol pintar untuk cek perubahan manual
                Button(
                    onClick = {
                        scope.launch {
                            isUpdating = true
                            try {
                                val newData = withContext(Dispatchers.IO) { KegiatanRepository.fetchKegiatanFromGAS() }
                                
                                // KOMPARASI PINTAR: Bandingkan data baru vs data lama
                                if (newData == kegiatanData) {
                                    toastMessage = "Data sudah versi terbaru! (Tidak ada kuota terbuang)"
                                } else {
                                    AppCache.save("KEGIATAN_DATA", newData)
                                    kegiatanData = newData
                                    toastMessage = "Kegiatan berhasil diperbarui!"
                                }
                            } catch (e: Exception) {
                                toastMessage = "Gagal terhubung ke server."
                            } finally {
                                isUpdating = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF009688), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF009688), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Perbarui", color = Color(0xFF009688), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TAMPILAN NOTIFIKASI PINTAR (Pengganti Toast sementara untuk KMP)
        if (toastMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(toastMessage!!, color = Color(0xFF2E7D32), modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
            // Hilangkan notifikasi setelah 3 detik
            LaunchedEffect(toastMessage) {
                kotlinx.coroutines.delay(3000L)
                toastMessage = null
            }
        }

        // KONTEN
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Nantinya ini di-parsing dari JSON. Sementara kita tampilkan teks mentahnya untuk bukti.
                Text(kegiatanData, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}
