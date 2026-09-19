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
    var isUpdating by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf(AppCache.load("KEGIATAN_DATA") ?: "Belum ada agenda kegiatan.") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Agenda Kegiatan", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Informasi resmi Wahidiyah", color = TextSecondary, fontSize = 14.sp) }
                Button(onClick = { scope.launch { isUpdating = true; try { val n = withContext(Dispatchers.IO) { KegiatanOnlineService.fetchKegiatanFromGAS() }; AppCache.save("KEGIATAN", n); data = n; toast = "Diperbarui" } catch(e:Exception){} finally{isUpdating=false} } }, colors = ButtonDefaults.buttonColors(containerColor = BrandAccentLight, contentColor = BrandPrimary), shape = RoundedCornerShape(12.dp)) {
                    if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandPrimary, strokeWidth = 2.dp) else Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
        HorizontalDivider(color = Border)
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (data.contains("Belum ada agenda")) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                    Box(modifier = Modifier.size(88.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.DateRange, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp)) }
                    Spacer(modifier = Modifier.height(24.dp)); Text("Jadwal Kosong", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(modifier = Modifier.height(8.dp)); Text("Agenda resmi akan muncul di sini.", fontSize = 15.sp, color = TextSecondary)
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) { Text(data, fontSize = 15.sp, color = TextPrimary, lineHeight = 24.sp) }
                }
            }
        }
    }
}
