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
import id.wahidiyah.miladiyyah.AppCache
import id.wahidiyah.miladiyyah.KegiatanOnlineService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun KegiatanScreen() {
    val scope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    
    // Tampil 0 detik dari Cache lokal
    var kegiatanData by remember { mutableStateOf(AppCache.load("KEGIATAN_DATA") ?: "Belum ada data. Silakan perbarui.") }

    // Ngintip diam-diam ke server saat layar dibuka
    LaunchedEffect(Unit) {
        try {
            val newData = withContext(Dispatchers.IO) { KegiatanOnlineService.fetchKegiatanFromGAS() }
            if (newData != kegiatanData) {
                AppCache.save("KEGIATAN_DATA", newData)
                kegiatanData = newData
            }
        } catch (e: Exception) {}
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF009688)).padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Jadwal Kegiatan", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Otomatis tersimpan offline", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            isUpdating = true
                            try {
                                val newData = withContext(Dispatchers.IO) { KegiatanOnlineService.fetchKegiatanFromGAS() }
                                
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

        if (toastMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(toastMessage!!, color = Color(0xFF2E7D32), modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
            LaunchedEffect(toastMessage) {
                kotlinx.coroutines.delay(3000L)
                toastMessage = null
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(kegiatanData, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}
