#!/bin/bash

echo "🎨 Membangun Premium UI Dashboard Beranda Miladiyyah..."

# 1. Update HomeScreen.kt dengan High-Fidelity UI Compose
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.data.repository.AnnouncementRepositoryImpl
import id.wahidiyah.miladiyyah.core.data.source.local.FakeLocalDataSource
import id.wahidiyah.miladiyyah.core.data.source.remote.RemoteDataSource
import id.wahidiyah.miladiyyah.core.domain.repository.Announcement
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementPriority
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementState
import id.wahidiyah.miladiyyah.theme.*

// Dummy Remote (Fase 0/1)
class DummyRemote : RemoteDataSource {
    override suspend fun fetchAnnouncements(): List<Announcement> = emptyList()
}

@Composable
fun HomeScreen() {
    // Inisiasi View Model sementara (Fake Data untuk menampilkan UI Penting)
    val fakeLocal = remember { 
        FakeLocalDataSource().apply {
            // Kita inject 1 pengumuman penting agar state UI-nya terlihat
            val fakeData = listOf(
                Announcement("1", "Perubahan Jadwal Mujahadah Kubro\nPelaksanaan dimajukan menjadi\n25-29 September 2026", AnnouncementPriority.IMPORTANT, AnnouncementState.ACTIVE)
            )
            // Menggunakan coroutine sederhana (di real-app dilakukan di interactor)
            kotlinx.coroutines.GlobalScope.launch { saveAnnouncements(fakeData) }
        }
    }
    val repository = remember { AnnouncementRepositoryImpl(fakeLocal, DummyRemote()) }
    val viewModel = remember { HomeViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // 1. HEADER (Top Bar Custom)
        HomeHeader(uiState.location)

        // 2. TANGGAL HARI INI
        DateSection(uiState)

        // 3. PENGUMUMAN PENTING (Dinamis)
        if (uiState.activeImportantAnnouncements.isNotEmpty()) {
            ImportantAnnouncementCard(uiState.activeImportantAnnouncements.first())
        }

        // 4. SMART PRAYER CARD
        SmartPrayerCard(uiState)

        // 5. RINGKASAN HARI INI
        SummarySection(uiState)

        // 6. KEGIATAN HARI INI
        ActivitySection()

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HomeHeader(location: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Miladiyyah", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
            Text(location, fontSize = 12.sp, color = TextSecondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifikasi", tint = DeepForestGreen)
            Icon(Icons.Default.Settings, contentDescription = "Pengaturan", tint = DeepForestGreen)
        }
    }
}

@Composable
private fun DateSection(uiState: HomeUiState) {
    Column {
        Text("Jumat, ${uiState.masehiDate}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = SubtleGold)
            Spacer(modifier = Modifier.width(6.dp))
            Text("${uiState.hijriyahDate} • ${uiState.pasaran}", fontSize = 12.sp, color = DeepForestGreen, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ImportantAnnouncementCard(announcement: Announcement) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ImportantOrange),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pengumuman Penting", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = announcement.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Lihat Selengkapnya >",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SmartPrayerCard(uiState: HomeUiState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepForestGreen),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder untuk Ikon Masjid
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, contentDescription = "Masjid", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Salat berikutnya", color = SoftCream, fontSize = 12.sp)
                Text(uiState.nextPrayerName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(uiState.nextPrayerTime, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(uiState.nextPrayerCountdown, color = SubtleGold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun SummarySection(uiState: HomeUiState) {
    Column {
        Text("Ringkasan Hari Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryItem(Icons.Default.List, "Kegiatan", "${uiState.summaryActivitiesCount}", Modifier.weight(1f), SubtleGold)
            SummaryItem(Icons.Default.Notifications, "Pengumuman", "Tidak ada", Modifier.weight(1f), UrgentRed)
            SummaryItem(Icons.Default.Favorite, "Dana Box", uiState.summaryDanaBoxTime, Modifier.weight(1f), NaturalGreen)
        }
    }
}

@Composable
private fun SummaryItem(icon: ImageVector, title: String, value: String, modifier: Modifier, iconColor: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(title, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun ActivitySection() {
    Column {
        Text("Kegiatan Hari Ini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarmWhite),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image Placeholder
                Box(
                    modifier = Modifier.size(50.dp).background(SoftCream, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = DeepForestGreen)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pengajian Rutin Wahidiyah", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepForestGreen)
                    Text("08:00 - 10:00", fontSize = 12.sp, color = TextSecondary)
                    Text("Masjid Baitul Wahid", fontSize = 12.sp, color = TextSecondary)
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}
EOF

echo "✅ UI Dashboard Premium telah ter-generate!"
EOF
