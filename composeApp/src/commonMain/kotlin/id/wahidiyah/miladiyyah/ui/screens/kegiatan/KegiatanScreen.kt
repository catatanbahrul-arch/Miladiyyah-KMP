package id.wahidiyah.miladiyyah.ui.screens.kegiatan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            if (newData != kegiatanData) { AppCache.save("KEGIATAN_DATA", newData); kegiatanData = newData }
        } catch (e: Exception) {}
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Agenda Kegiatan", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Informasi resmi Wahidiyah", color = TextSecondary, fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        scope.launch {
                            isUpdating = true; try { val newData = withContext(Dispatchers.IO) { KegiatanOnlineService.fetchKegiatanFromGAS() }; if (newData == kegiatanData) { toastMessage = "Data mutakhir." } else { AppCache.save("KEGIATAN_DATA", newData); kegiatanData = newData; toastMessage = "Diperbarui." } } catch (e: Exception) { toastMessage = "Gagal terhubung." } finally { isUpdating = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccentLight, contentColor = BrandPrimary), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isUpdating) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandPrimary, strokeWidth = 2.dp) } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        HorizontalDivider(color = Border)
        if (toastMessage != null) {
            Card(colors = CardDefaults.cardColors(containerColor = BrandAccentLight), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(24.dp)) { Text(toastMessage!!, color = BrandPrimaryDark, modifier = Modifier.padding(16.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            LaunchedEffect(toastMessage) { kotlinx.coroutines.delay(3000L); toastMessage = null }
        }

        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (kegiatanData.contains("Belum ada agenda")) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.DateRange, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(36.dp)) }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Kosong", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Belum ada jadwal kegiatan resmi.", fontSize = 14.sp, color = TextSecondary)
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Surface), modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(24.dp)) { Text(kegiatanData, fontSize = 14.sp, color = TextPrimary, lineHeight = 24.sp) }
                }
            }
        }
    }
}
