package id.wahidiyah.miladiyyah.ui.screens.kegiatan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanItem
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanRepository
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.launch

@Composable
fun KegiatanScreen() {
    var kegiatanList by remember { mutableStateOf<List<KegiatanItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            kegiatanList = KegiatanRepository.fetchKegiatan()
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SoftCream)) {
        // Header Banner
        Box(modifier = Modifier.fillMaxWidth().background(DeepForestGreen).padding(20.dp)) {
            Column {
                Text("Jadwal Kegiatan & Mujahadah", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Agenda resmi Pusat & Serentak Jamaah Wahidiyah", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepForestGreen)
            }
        } else if (kegiatanList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada jadwal kegiatan atau gagal memuat.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(kegiatanList) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(16.dp))
                                    Text(item.date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
