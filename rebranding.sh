#!/bin/bash
set -e

echo "==> [1/6] Menerapkan Nama Aplikasi: WAHIDIYAH..."
sed -i 's/android:label="Miladiyyah"/android:label="Wahidiyah"/g' composeApp/src/androidMain/AndroidManifest.xml

echo "==> [2/6] Membangun Design System & Color Tokens berdasarkan Logo..."
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/theme

cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/theme/Color.kt
package id.wahidiyah.miladiyyah.theme

import androidx.compose.ui.graphics.Color

// Palette diekstrak dari Logo Wahidiyah Resmi
val BrandPrimaryDark = Color(0xFF0A2314)   // Hijau Sangat Tua (Dasar W)
val BrandPrimary = Color(0xFF1C5B2D)       // Hijau Daun (Warna Utama Logo)
val BrandAccent = Color(0xFF73C34F)        // Hijau Lime (Aksen Daun Terang)
val BrandAccentLight = Color(0xFFEBF5E6)   // Hijau Sangat Pudar (Background Card/Icon)

// Neutral Tokens (Elegan & Bersih)
val Background = Color(0xFFF9FBF9)         // Off-white dengan hint hijau sangat tipis
val Surface = Color(0xFFFFFFFF)            // Putih Murni
val Border = Color(0xFFE6EBE6)             // Abu-abu hijau halus
val TextPrimary = Color(0xFF111A13)        // Hitam pekat elegan
val TextSecondary = Color(0xFF5A665D)      // Abu-abu gelap (untuk subtitle)
val TextMuted = Color(0xFF9EAA9F)          // Abu-abu terang

// Status Tokens
val Error = Color(0xFFC62828)
val Success = Color(0xFF2E7D32)
EOF

cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/theme/Theme.kt
package id.wahidiyah.miladiyyah.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WahidiyahColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Surface,
    primaryContainer = BrandAccentLight,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = BrandAccent,
    onSecondary = BrandPrimaryDark,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    error = Error,
    onError = Surface
)

@Composable
fun MiladiyyahTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = WahidiyahColorScheme, content = content)
}
EOF

echo "==> [3/6] Membangun Splash Screen Premium Wahidiyah..."
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/splash

cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/splash/SplashScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Spacer disiapkan untuk Logo Image sebenarnya yang akan Anda upload
            Box(modifier = Modifier.size(120.dp).background(BrandAccentLight, shape = androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                Text("W", color = BrandPrimary, fontSize = 64.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("WAHIDIYAH", color = BrandPrimaryDark, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Aplikasi Resmi Jamaah", color = TextSecondary, fontSize = 14.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
        }
    }
}
EOF

echo "==> [4/6] Merombak App Shell, Navigasi & Beranda..."
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
import id.wahidiyah.miladiyyah.ui.screens.kiblat.QiblaScreen
import id.wahidiyah.miladiyyah.ui.screens.splash.SplashScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class BottomTab { BERANDA, KALENDER, PUSTAKA, KEGIATAN, MENU, KIBLAT }
const val GAS_API_KALENDER = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec"

@Composable
fun App(onUpdateLocation: () -> Unit = {}) {
    var showSplash by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(BottomTab.BERANDA) }
    val scope = rememberCoroutineScope()
    val networkService = remember { CalendarNetworkService() }

    LaunchedEffect(Unit) { scope.launch(Dispatchers.IO) { try { val adj = withTimeoutOrNull(5000L) { networkService.fetchCascadeAdjustments(GAS_API_KALENDER) }; if (adj != null) HijriAdjuster.updateAdjustments(adj) } catch (e: Exception) { } } }
    
    MiladiyyahTheme {
        if (showSplash) { SplashScreen(onTimeout = { showSplash = false }) } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Surface, contentColor = TextSecondary, tonalElevation = 8.dp) {
                        NavigationBarItem(icon = { Icon(Icons.Default.Home, "Beranda") }, label = { Text("Beranda") }, selected = selectedTab == BottomTab.BERANDA, onClick = { selectedTab = BottomTab.BERANDA }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.DateRange, "Kalender") }, label = { Text("Kalender") }, selected = selectedTab == BottomTab.KALENDER, onClick = { selectedTab = BottomTab.KALENDER }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.Info, "Pustaka") }, label = { Text("Pustaka") }, selected = selectedTab == BottomTab.PUSTAKA, onClick = { selectedTab = BottomTab.PUSTAKA }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.List, "Kegiatan") }, label = { Text("Kegiatan") }, selected = selectedTab == BottomTab.KEGIATAN, onClick = { selectedTab = BottomTab.KEGIATAN }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.Menu, "Menu") }, label = { Text("Menu") }, selected = selectedTab == BottomTab.MENU, onClick = { selectedTab = BottomTab.MENU }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                    }
                }
            ) { innerPadding ->
                Surface(modifier = Modifier.padding(innerPadding).background(Background)) {
                    when (selectedTab) {
                        BottomTab.BERANDA -> HomeScreen(onNavigateToKiblat = { selectedTab = BottomTab.KIBLAT }, onUpdateLocation = onUpdateLocation)
                        BottomTab.KALENDER -> CalendarScreen(id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine())
                        BottomTab.PUSTAKA -> PustakaScreen()
                        BottomTab.KEGIATAN -> KegiatanScreen()
                        BottomTab.MENU -> SettingsScreen()
                        BottomTab.KIBLAT -> QiblaScreen()
                    }
                }
            }
        }
    }
}
EOF

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
import kotlinx.coroutines.delay
import kotlinx.datetime.*

@Composable
fun HomeScreen(onNavigateToKiblat: () -> Unit = {}, onUpdateLocation: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    var currentDateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(tz)) }
    LaunchedEffect(Unit) { while(true) { currentDateTime = Clock.System.now().toLocalDateTime(tz); delay(1000L) } }

    var dayOffset by remember { mutableStateOf(0) }
    val targetDate = Clock.System.todayIn(tz).plus(dayOffset, DateTimeUnit.DAY)
    val prayers = try { PrayerTimeEngine.getPrayers(targetDate) } catch(e:Exception) { emptyList() }
    val nextPrayer = try { PrayerTimeEngine.getNextPrayer(currentDateTime.time) } catch(e:Exception) { null }
    val monthNames = listOf("", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Ahad")
    
    val hijriBase = CalendarEngine().getToday().hijri
    val tempDay = hijriBase.day + dayOffset
    val hDay = when { tempDay > 30 -> tempDay % 30; tempDay <= 0 -> 30 + (tempDay % 30); else -> tempDay }
    val hMonthNames = listOf("", "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir", "Jumadil Awal", "Jumadil Akhir", "Rajab", "Syaban", "Ramadhan", "Syawal", "Dzulqaidah", "Dzulhijjah")

    Column(modifier = Modifier.fillMaxSize().background(Background).verticalScroll(scrollState)) {
        // Hero Header Wahidiyah
        Box(modifier = Modifier.fillMaxWidth().background(BrandPrimaryDark, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(top = 28.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("WAHIDIYAH", color = Surface, fontSize = 16.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onUpdateLocation() }.background(BrandPrimary).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(PrayerTimeEngine.locationName, color = Surface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
                if (nextPrayer != null) {
                    val diff = (nextPrayer.time.hour * 3600 + nextPrayer.time.minute * 60) - (currentDateTime.time.hour * 3600 + currentDateTime.time.minute * 60 + currentDateTime.time.second)
                    val dSecs = if(diff >= 0) diff else diff + 86400
                    Text(nextPrayer.type.title.uppercase(), color = BrandAccent, fontSize = 14.sp, letterSpacing = 6.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${nextPrayer.time.hour.toString().padStart(2,'0')}:${nextPrayer.time.minute.toString().padStart(2,'0')}", color = Surface, fontSize = 64.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("- ${dSecs/3600}:${((dSecs%3600)/60).toString().padStart(2,'0')}:${(dSecs%60).toString().padStart(2,'0')}", color = Surface.copy(alpha=0.7f), fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
                }
            }
        }

        // Floating Date Navigator
        Box(modifier = Modifier.fillMaxWidth().offset(y = (-32).dp).padding(horizontal = 24.dp)) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(CircleShape).clickable { dayOffset -= 1 }.padding(12.dp)) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = BrandPrimary) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${dayNames[targetDate.dayOfWeek.ordinal]}, ${targetDate.dayOfMonth} ${monthNames[targetDate.monthNumber]}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$hDay ${hMonthNames[hijriBase.month]} ${hijriBase.year} H", fontSize = 13.sp, color = BrandPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Box(modifier = Modifier.clip(CircleShape).clickable { dayOffset += 1 }.padding(12.dp)) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = BrandPrimary) }
                }
            }
        }

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-8).dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.weight(1f).clickable { onNavigateToKiblat() }) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Arah Kiblat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }

        // Clean Prayer List
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text("Jadwal Salat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 16.dp, start = 8.dp))
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
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
EOF

echo "==> [5/6] Merombak Layar Kegiatan, Pustaka & Pengaturan (UI Bersih & Empty State)..."
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/kegiatan/KegiatanScreen.kt
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
EOF

cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/pustaka/PustakaScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.pustaka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun PustakaScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column { Text("Pustaka Jamaah", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Kitab dan arsip digital", color = TextSecondary, fontSize = 14.sp) }
        }
        HorizontalDivider(color = Border)
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(88.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(40.dp)) }
                Spacer(modifier = Modifier.height(24.dp)); Text("Arsip Offline", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(modifier = Modifier.height(12.dp)); Text("Materi Wahidiyah tersimpan di perangkat.\nSiap diakses kapan saja.", textAlign = TextAlign.Center, color = TextSecondary, fontSize = 15.sp, lineHeight = 24.sp)
            }
        }
    }
}
EOF

cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/settings/SettingsScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
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
fun SettingsScreen() {
    var adzanEnabled by remember { mutableStateOf(true) }
    var tarhimEnabled by remember { mutableStateOf(true) }
    
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column { Text("Pengaturan", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Personalisasi aplikasi Wahidiyah", color = TextSecondary, fontSize = 14.sp) }
        }
        HorizontalDivider(color = Border)
        Column(modifier = Modifier.padding(24.dp)) {
            Text("NOTIFIKASI", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandPrimary, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 16.dp, start = 8.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Adzan & Salat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Peringatan otomatis", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = adzanEnabled, onCheckedChange = { adzanEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Pengingat Tarhim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Sebelum waktu Subuh", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tarhimEnabled, onCheckedChange = { tarhimEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                }
            }
        }
    }
}
EOF

echo "==> [6/6] Menyapu bersih sisa warna lawas yang mengganggu kompilasi..."
for file in composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/calendar/*.kt composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/kiblat/*.kt; do
  if [ -f "$file" ]; then
    sed -i 's/DeepForestGreen/BrandPrimaryDark/g; s/BrandPrimaryLight/BrandAccentLight/g; s/UrgentRed/Error/g; s/SubtleGold/BrandAccent/g; s/SoftCream/Background/g; s/WarmWhite/Surface/g' "$file"
  fi
done

echo "Menyimpan seluruh Master Rebranding ke GitHub..."
git add .
git commit -m "design: master UI/UX rebranding implementing official WAHIDIYAH brand identity with centralized minimalist design system"
git push origin main

echo "MASTER REBRANDING WAHIDIYAH SELESAI & SUKSES DIKIRIM!"
EOF
