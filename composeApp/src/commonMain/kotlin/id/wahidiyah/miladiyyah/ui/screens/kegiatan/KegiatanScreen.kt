package id.wahidiyah.miladiyyah.ui.screens.kegiatan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.AppRepository
import id.wahidiyah.miladiyyah.core.utils.UiState
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.launch

@Composable
fun KegiatanScreen() {
    var uiState by remember { mutableStateOf<UiState<String>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { uiState = AppRepository.getKegiatan() }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column { Text("Agenda Kegiatan", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Informasi resmi Wahidiyah", color = TextSecondary, fontSize = 14.sp) }
        }
        HorizontalDivider(color = Border)
        
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            when (uiState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandPrimary)
                is UiState.Empty -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                        Box(modifier = Modifier.size(88.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.DateRange, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp)) }
                        Spacer(modifier = Modifier.height(24.dp)); Text("Jadwal Kosong", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(modifier = Modifier.height(8.dp)); Text("Belum ada agenda kegiatan baru.", fontSize = 15.sp, color = TextSecondary)
                    }
                }
                is UiState.Error -> {
                    val err = uiState as UiState.Error
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp)); Text("Terjadi Kesalahan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(modifier = Modifier.height(8.dp)); Text(err.message, fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { scope.launch { uiState = UiState.Loading; uiState = AppRepository.getKegiatan() } }, colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)) { Text("Coba Lagi") }
                    }
                }
                is UiState.Success -> {
                    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(24.dp)) { Text((uiState as UiState.Success).data, fontSize = 15.sp, color = TextPrimary, lineHeight = 24.sp) }
                    }
                }
            }
        }
    }
}
