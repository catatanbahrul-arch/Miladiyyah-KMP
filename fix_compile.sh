#!/bin/bash

echo "🛠 Memperbaiki Error Kompilasi (Dependencies & Imports)..."

# 1. Update build.gradle.kts untuk menambahkan androidx.activity:activity-compose
cat << 'EOF' > composeApp/build.gradle.kts
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
        }
        androidMain.dependencies {
            // FIX: Menambahkan library ComponentActivity & setContent untuk Android
            implementation("androidx.activity:activity-compose:1.8.2")
        }
        iosMain.dependencies {}
    }
}

android {
    namespace = "id.wahidiyah.miladiyyah"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "id.wahidiyah.miladiyyah"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
EOF

# 2. Update Theme.kt untuk menyertakan import Color
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/theme/Theme.kt
package id.wahidiyah.miladiyyah.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MiladiyyahLightColorScheme = lightColorScheme(
    primary = DeepForestGreen,
    secondary = SubtleGold,
    background = SoftCream,
    surface = WarmWhite,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun MiladiyyahTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MiladiyyahLightColorScheme,
        content = content
    )
}
EOF

# 3. Update HomeScreen.kt untuk menyertakan import Coroutines
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
import id.wahidiyah.miladiyyah.core.domain.calendar.CalendarEngineImpl
import id.wahidiyah.miladiyyah.core.domain.repository.Announcement
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementPriority
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementState
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DummyRemote : RemoteDataSource {
    override suspend fun fetchAnnouncements(): List<Announcement> = emptyList()
}

@Composable
fun HomeScreen() {
    val fakeLocal = remember { 
        FakeLocalDataSource().apply {
            val fakeData = listOf(
                Announcement("1", "Perubahan Jadwal Mujahadah Kubro\nPelaksanaan dimajukan menjadi\n25-29 September 2026", AnnouncementPriority.IMPORTANT, AnnouncementState.ACTIVE)
            )
            GlobalScope.launch { saveAnnouncements(fakeData) }
        }
    }
    val repository = remember { AnnouncementRepositoryImpl(fakeLocal, DummyRemote()) }
    val calendarEngine = remember { CalendarEngineImpl() }
    val viewModel = remember { HomeViewModel(repository, calendarEngine) }
    
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
        HomeHeader(uiState.location)
        DateSection(uiState)
        if (uiState.activeImportantAnnouncements.isNotEmpty()) {
            ImportantAnnouncementCard(uiState.activeImportantAnnouncements.first())
        }
        SmartPrayerCard(uiState)
        SummarySection(uiState)
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
        Text("${uiState.dayOfWeek}, ${uiState.masehiDate}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
            Text(announcement.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                Text("Lihat Selengkapnya >", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
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
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
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
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = WarmWhite), elevation = CardDefaults.cardElevation(1.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = WarmWhite), elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(50.dp).background(SoftCream, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
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

echo "✅ File telah di-patch dengan import dan library yang benar!"
EOF
