package id.wahidiyah.miladiyyah.ui.screens.kegiatan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.AppCache
import id.wahidiyah.miladiyyah.KegiatanOnlineService
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun KegiatanScreen() {
    val scope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var kegiatanData by remember { mutableStateOf(AppCache.load("KEGIATAN_DATA") ?: "Belum ada agenda kegiatan.") }

    LaunchedEffect(Unit) {
        try {
            val newData = withContext(Dispatchers.IO) { KegiatanOnlineService.fetchKegiatanFromGAS() }
            if (newData != kegiatanData) {
                AppCache.save("KEGIATAN_DATA", newData)
                kegiatanData = newData
            }
        } catch (e: Exception) {}
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        
        // Header
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Kegiatan & Agenda", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Informasi resmi Wahidiyah", color = TextSecondary, fontSize = 13.sp)
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            isUpdating = true
                            try {
                                val newData = withContext(Dispatchers.IO) { KegiatanOnlineService.fetchKegiatanFromGAS() }
                                if (newData == kegiatanData) {
                                    toastMessage = "Data sudah versi terbaru."
                                } else {
                                    AppCache.save("KEGIATAN_DATA", newData)
                                    kegiatanData = newData
                                    toastMessage = "Kegiatan berhasil diperbarui."
                                }
                            } catch (e: Exception) { toastMessage = "Gagal terhubung ke server." } 
                            finally { isUpdating = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccentLight, contentColor = BrandPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Perbarui", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        HorizontalDivider(color = Border)

        if (toastMessage != null) {
            Card(colors = CardDefaults.cardColors(containerColor = BrandAccentLight), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(toastMessage!!, color = BrandPrimaryDark, modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            LaunchedEffect(toastMessage) { kotlinx.coroutines.delay(3000L); toastMessage = null }
        }

        // Empty State / Content
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            if (kegiatanData.contains("Belum ada agenda")) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Event, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum Ada Kegiatan", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Agenda resmi akan muncul di sini.", fontSize = 14.sp, color = TextSecondary)
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(kegiatanData, fontSize = 14.sp, color = TextSecondary, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}
