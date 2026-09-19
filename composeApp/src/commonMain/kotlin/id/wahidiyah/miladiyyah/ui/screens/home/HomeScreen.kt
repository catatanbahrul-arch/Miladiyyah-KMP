package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.List
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
            
            if (pengumuman != null && (!pengumuman!!.title.isEmpty() || !pengumuman!!.content.isEmpty())) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (pengumuman!!.title.isNotEmpty()) pengumuman!!.title else "Pengumuman Penting",
                                fontSize = 15.sp,
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

                        if (!pengumuman!!.link.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFFFE082), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* Aksi buka link */ },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lihat Himbauan Selengkapnya",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = ">",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kalender Hijriyah", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Akurat MABIMS", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.List, contentDescription = null, tint = DeepForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Jadwal Kegiatan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Mujahadah Serempak", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
