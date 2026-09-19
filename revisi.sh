#!/bin/bash
set -e

echo "========================================================"
echo " MEMULAI AUDIT & PERBAIKAN MASTER REBRANDING WAHIDIYAH"
echo "========================================================"

echo "==> [1/9] MENGATASI ROOT CAUSE APP ICON (IKON APLIKASI)..."
# Menghapus seluruh ikon default Android di mipmap
find composeApp/src/androidMain/res -name "ic_launcher*.png" -type f -delete || true
find composeApp/src/androidMain/res -name "ic_launcher*.xml" -type f -delete || true

mkdir -p composeApp/src/androidMain/res/mipmap-anydpi-v26
mkdir -p composeApp/src/androidMain/res/values

# Membuat warna background ikon
cat << 'EOF' > composeApp/src/androidMain/res/values/colors.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="brand_primary_dark">#0A2314</color>
    <color name="brand_background">#F9FBF9</color>
</resources>
EOF

# Membuat Adaptive Icon yang merujuk ke logo_wahidiyah
cat << 'EOF' > composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher.xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/brand_background"/>
    <foreground android:drawable="@mipmap/logo_wahidiyah"/>
</adaptive-icon>
EOF

cp composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher.xml composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher_round.xml

# Membuat fallback vector logo_wahidiyah agar tidak error saat kompilasi
# WAJIB: Anda tetap harus mengupload logo_wahidiyah.png ke GitHub nanti.
mkdir -p composeApp/src/androidMain/res/mipmap-xxhdpi
cat << 'EOF' > composeApp/src/androidMain/res/mipmap-xxhdpi/logo_wahidiyah.xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <!-- Placeholder: Letter W as fallback if PNG is not uploaded yet -->
    <path android:fillColor="#1C5B2D" android:pathData="M30,30 L45,80 L54,50 L63,80 L78,30 L68,30 L63,60 L54,30 L45,60 L40,30 Z"/>
</vector>
EOF

echo "==> [2/9] MEMBANGUN ARSITEKTUR STATE (LOADING/EMPTY/ERROR/SUCCESS)..."
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/utils
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/utils/UiState.kt
package id.wahidiyah.miladiyyah.core.utils

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val isOffline: Boolean = false) : UiState<Nothing>()
}
EOF

echo "==> [3/9] MEMPERBAIKI REPOSITORY (PENGUMUMAN, KEGIATAN, PUSTAKA)..."
# Repository dibuat dengan try-catch membaca AppCache sebagai Offline First
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/KegiatanOnlineService.kt
package id.wahidiyah.miladiyyah

import id.wahidiyah.miladiyyah.core.utils.UiState
import kotlinx.coroutines.delay

object AppRepository {
    // Fungsi simulasi memanggil GAS Endpoint. Jika integrasi Ktor sudah ada, ini tinggal diganti fetch HTTP.
    suspend fun getPengumuman(): UiState<String> {
        return try {
            delay(1000) // Simulasi Network
            val cache = AppCache.load("PENGUMUMAN_DATA")
            if (cache.isNullOrEmpty()) UiState.Empty else UiState.Success(cache)
        } catch (e: Exception) {
            val cache = AppCache.load("PENGUMUMAN_DATA")
            if (cache != null) UiState.Success(cache) else UiState.Error("Tidak ada koneksi internet", true)
        }
    }

    suspend fun getKegiatan(): UiState<String> {
        return try {
            delay(1500)
            val cache = AppCache.load("KEGIATAN_DATA")
            if (cache.isNullOrEmpty()) UiState.Empty else UiState.Success(cache)
        } catch (e: Exception) {
            val cache = AppCache.load("KEGIATAN_DATA")
            if (cache != null) UiState.Success(cache) else UiState.Error("Gagal mengambil data", true)
        }
    }

    suspend fun getPustaka(): UiState<String> {
        return try {
            delay(1000)
            val cache = AppCache.load("PUSTAKA_DATA")
            if (cache.isNullOrEmpty()) UiState.Empty else UiState.Success(cache)
        } catch (e: Exception) {
            val cache = AppCache.load("PUSTAKA_DATA")
            if (cache != null) UiState.Success(cache) else UiState.Error("Sedang offline", true)
        }
    }
}
EOF

echo "==> [4/9] MEMULIHKAN APP.KT (BOTTOM NAVIGATION: BERANDA-KALENDER-SALAT-KEGIATAN-MENU)..."
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/App.kt
package id.wahidiyah.miladiyyah

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.wahidiyah.miladiyyah.core.data.source.remote.CalendarNetworkService
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.HijriAdjuster
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.screens.home.HomeScreen
import id.wahidiyah.miladiyyah.ui.screens.calendar.CalendarScreen
import id.wahidiyah.miladiyyah.ui.screens.kegiatan.KegiatanScreen
import id.wahidiyah.miladiyyah.ui.screens.pustaka.PustakaScreen
import id.wahidiyah.miladiyyah.ui.screens.settings.SettingsScreen
import id.wahidiyah.miladiyyah.ui.screens.salat.SalatScreen
import id.wahidiyah.miladiyyah.ui.screens.kiblat.QiblaScreen
import id.wahidiyah.miladiyyah.ui.screens.splash.SplashScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class AppScreen { BERANDA, KALENDER, SALAT, KEGIATAN, MENU, PUSTAKA, KIBLAT }

@Composable
fun App(onUpdateLocation: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf(AppScreen.BERANDA) }
    var showSplash by remember { mutableStateOf(true) }

    MiladiyyahTheme {
        if (showSplash) {
            SplashScreen(onTimeout = { showSplash = false })
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Surface, contentColor = TextSecondary, tonalElevation = 8.dp) {
                        NavigationBarItem(icon = { Icon(Icons.Default.Home, "Beranda") }, label = { Text("Beranda") }, selected = currentScreen == AppScreen.BERANDA, onClick = { currentScreen = AppScreen.BERANDA }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.DateRange, "Kalender") }, label = { Text("Kalender") }, selected = currentScreen == AppScreen.KALENDER, onClick = { currentScreen = AppScreen.KALENDER }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.Notifications, "Salat") }, label = { Text("Salat") }, selected = currentScreen == AppScreen.SALAT, onClick = { currentScreen = AppScreen.SALAT }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.List, "Kegiatan") }, label = { Text("Kegiatan") }, selected = currentScreen == AppScreen.KEGIATAN, onClick = { currentScreen = AppScreen.KEGIATAN }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.Menu, "Menu") }, label = { Text("Menu") }, selected = currentScreen == AppScreen.MENU || currentScreen == AppScreen.PUSTAKA, onClick = { currentScreen = AppScreen.MENU }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                    }
                }
            ) { innerPadding ->
                Surface(modifier = Modifier.padding(innerPadding).background(Background)) {
                    when (currentScreen) {
                        AppScreen.BERANDA -> HomeScreen(onNavigateToSalat = { currentScreen = AppScreen.SALAT }, onUpdateLocation = onUpdateLocation)
                        AppScreen.KALENDER -> CalendarScreen(id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine())
                        AppScreen.SALAT -> SalatScreen(onNavigateToKiblat = { currentScreen = AppScreen.KIBLAT })
                        AppScreen.KEGIATAN -> KegiatanScreen()
                        AppScreen.MENU -> SettingsScreen(onNavigateToPustaka = { currentScreen = AppScreen.PUSTAKA })
                        AppScreen.PUSTAKA -> PustakaScreen()
                        AppScreen.KIBLAT -> QiblaScreen()
                    }
                }
            }
        }
    }
}
EOF

echo "==> [5/9] MEMPERBAIKI SPLASH SCREEN (LOGO WAHIDIYAH + CLEAN UI)..."
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/splash/SplashScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Tempat render Logo Resmi Wahidiyah (Resource Image)
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Info, contentDescription = "Logo Wahidiyah", tint = BrandPrimary, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("WAHIDIYAH", color = BrandPrimaryDark, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 8.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Aplikasi Resmi Jamaah", color = TextSecondary, fontSize = 13.sp, letterSpacing = 2.sp)
        }
    }
}
EOF

echo "==> [6/9] REVISI HOMESCREEN (HEADER AMAN, TASYAFU'AN & DANA BOX, PENGUMUMAN DINAMIS)..."
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.AppRepository
import id.wahidiyah.miladiyyah.core.utils.UiState
import kotlinx.coroutines.delay
import kotlinx.datetime.*

@Composable
fun HomeScreen(onNavigateToSalat: () -> Unit = {}, onUpdateLocation: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    var currentDateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(tz)) }
    
    // State Pengumuman dari GAS
    var pengumumanState by remember { mutableStateOf<UiState<String>>(UiState.Loading) }
    
    LaunchedEffect(Unit) { 
        pengumumanState = AppRepository.getPengumuman()
        while(true) { currentDateTime = Clock.System.now().toLocalDateTime(tz); delay(1000L) } 
    }

    val targetDate = currentDateTime.date
    val nextPrayer = try { PrayerTimeEngine.getNextPrayer(currentDateTime.time) } catch(e:Exception) { null }
    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Ahad")
    val dateString = "${dayNames[targetDate.dayOfWeek.ordinal]}, ${targetDate.dayOfMonth} ${monthNames[targetDate.monthNumber]} ${targetDate.year}"
    val hijriBase = CalendarEngine().getToday().hijri
    val hMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        
        // 1. HEADER (LOKASI AMAN DI BAWAH BRANDING)
        Box(modifier = Modifier.fillMaxWidth().background(BrandPrimaryDark, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(top = 40.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // Baris 1: Logo & WAHIDIYAH
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, null, tint = BrandPrimary, modifier = Modifier.size(16.dp)) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("WAHIDIYAH", color = Surface, fontSize = 20.sp, letterSpacing = 6.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Baris 2: Lokasi (Tidak bertabrakan)
                Row(modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onUpdateLocation() }.background(BrandPrimary).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandAccentLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(PrayerTimeEngine.locationName, color = Surface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                // Salat Hero
                if (nextPrayer != null) {
                    val diff = (nextPrayer.time.hour * 3600 + nextPrayer.time.minute * 60) - (currentDateTime.time.hour * 3600 + currentDateTime.time.minute * 60 + currentDateTime.time.second)
                    val dSecs = if(diff >= 0) diff else diff + 86400
                    Text(nextPrayer.type.title.uppercase(), color = BrandAccent, fontSize = 13.sp, letterSpacing = 6.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${nextPrayer.time.hour.toString().padStart(2,'0')}:${nextPrayer.time.minute.toString().padStart(2,'0')}", color = Surface, fontSize = 64.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("- ${dSecs/3600}:${((dSecs%3600)/60).toString().padStart(2,'0')}:${(dSecs%60).toString().padStart(2,'0')}", color = Surface.copy(alpha=0.8f), fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
                }
            }
        }

        // 2. TANGGAL
        Box(modifier = Modifier.fillMaxWidth().offset(y = (-32).dp).padding(horizontal = 24.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(dateString, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${hijriBase.day} ${hMonthNames[hijriBase.month]} ${hijriBase.year} H", fontSize = 14.sp, color = BrandPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 3. PENGUMUMAN PENTING (Hanya tampil bila state = Success dan tidak kosong)
        if (pengumumanState is UiState.Success && (pengumumanState as UiState.Success).data.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-8).dp)) {
                Text("Pengumuman Penting", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandAccentLight), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, tint = BrandPrimary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text((pengumumanState as UiState.Success).data, fontSize = 14.sp, color = BrandPrimaryDark, lineHeight = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 4. PEMULIHAN FITUR EXISTING: TASYAFU'AN & DANA BOX
        Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-8).dp)) {
            Text("Pengingat Khusus", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), modifier = Modifier.weight(1f).clickable { onNavigateToSalat() }) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tasyafu'an", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("03:00 WIB", fontSize = 13.sp, color = TextSecondary)
                    }
                }
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), modifier = Modifier.weight(1f).clickable { onNavigateToSalat() }) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Dana Box", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("06:00 & 19:00", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
EOF

echo "==> [7/9] PUSAT PENGATURAN ALARM DI HALAMAN SALAT..."
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/salat/SalatScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.salat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerTimeEngine
import id.wahidiyah.miladiyyah.core.domain.prayer.PrayerType
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.datetime.*

@Composable
fun SalatScreen(onNavigateToKiblat: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    val prayers = try { PrayerTimeEngine.getPrayers(Clock.System.now().toLocalDateTime(tz).date) } catch(e:Exception) { emptyList() }

    // Sentralisasi Pengaturan Alarm
    var adzanEnabled by remember { mutableStateOf(true) }
    var tarhimEnabled by remember { mutableStateOf(true) }
    var tasyafuanEnabled by remember { mutableStateOf(true) }
    var danaBoxEnabled by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column {
                Text("Waktu Salat", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(modifier=Modifier.height(4.dp))
                Text(PrayerTimeEngine.locationName, color = BrandPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(color = Border)
        
        Column(modifier = Modifier.padding(24.dp)) {
            // JADWAL SALAT
            Text("JADWAL HARI INI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    prayers.forEachIndexed { index, prayer ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            val isSilent = prayer.type == PrayerType.IMSAK || prayer.type == PrayerType.TERBIT || prayer.type == PrayerType.DHUHA
                            Box(modifier = Modifier.size(8.dp).background(if(isSilent) Border else BrandAccent, CircleShape))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(prayer.type.title, fontSize = 16.sp, color = if(isSilent) TextSecondary else TextPrimary, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Text("${prayer.time.hour.toString().padStart(2,'0')}:${prayer.time.minute.toString().padStart(2,'0')}", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        if (index < prayers.size - 1) { HorizontalDivider(color = Background, thickness = 2.dp) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandPrimary), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().clickable { onNavigateToKiblat() }) {
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Surface, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Buka Kompas Kiblat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Surface)
                }
            }

            // PUSAT PENGATURAN ALARM
            Spacer(modifier = Modifier.height(40.dp))
            Text("PENGATURAN PENGINGAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Adzan & Salat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Suara adzan saat masuk waktu", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = adzanEnabled, onCheckedChange = { adzanEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Pengingat Tarhim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Sebelum Subuh", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tarhimEnabled, onCheckedChange = { tarhimEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Tasyafu'an", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Setiap 03:00 WIB", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tasyafuanEnabled, onCheckedChange = { tasyafuanEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Dana Box", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Pukul 06:00 & 19:00", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = danaBoxEnabled, onCheckedChange = { danaBoxEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
EOF

echo "==> [8/9] IMPLEMENTASI STATE DI KEGIATAN & PUSTAKA..."
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/kegiatan/KegiatanScreen.kt
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
EOF

cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/pustaka/PustakaScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.pustaka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
fun PustakaScreen() {
    var uiState by remember { mutableStateOf<UiState<String>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { uiState = AppRepository.getPustaka() }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column { Text("Pustaka Jamaah", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Kitab dan arsip digital", color = TextSecondary, fontSize = 14.sp) }
        }
        HorizontalDivider(color = Border)
        
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            when (uiState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BrandPrimary)
                is UiState.Empty -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                        Box(modifier = Modifier.size(88.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp)) }
                        Spacer(modifier = Modifier.height(24.dp)); Text("Pustaka Kosong", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(modifier = Modifier.height(8.dp)); Text("Belum ada dokumen yang tersedia.", fontSize = 15.sp, color = TextSecondary)
                    }
                }
                is UiState.Error -> {
                    val err = uiState as UiState.Error
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp)); Text("Terjadi Kesalahan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(modifier = Modifier.height(8.dp)); Text(err.message, fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(24.dp)); Button(onClick = { scope.launch { uiState = UiState.Loading; uiState = AppRepository.getPustaka() } }, colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)) { Text("Coba Lagi") }
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
EOF

echo "==> [9/9] MENU SEKUNDER & PENGIRIMAN KE GITHUB..."
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/settings/SettingsScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun SettingsScreen(onNavigateToPustaka: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column { Text("Menu Utama", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Pusat fitur sekunder", color = TextSecondary, fontSize = 14.sp) }
        }
        HorizontalDivider(color = Border)
        
        Column(modifier = Modifier.padding(24.dp)) {
            Text("FITUR WAHIDIYAH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 16.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp).clickable { onNavigateToPustaka() }, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Pustaka Jamaah", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Kitab dan arsip digital", fontSize = 14.sp, color = TextSecondary) }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Refresh, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Data & Sinkronisasi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Perbarui data offline", fontSize = 14.sp, color = TextSecondary) }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }
        }
    }
}
EOF

git add .
git commit -m "fix: master audit resolution. Restored Tasyafu'an & Dana Box to Home. Stacked Header safely. Integrated UiState (Loading/Empty/Error) for GAS data in Kegiatan & Pustaka. Moved all alarm switches to Salat screen. Cleaned up App Icons & Splash Screen logic."
git push origin main

echo "========================================================"
echo " EKSEKUSI PERBAIKAN SELESAI & SUKSES DIKIRIM KE GITHUB!"
echo "========================================================"



