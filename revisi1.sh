#!/bin/bash
set -e

echo "========================================================"
echo " EKSEKUSI MASTER REBRANDING & PERBAIKAN WAHIDIYAH"
echo "========================================================"

echo "==> [1/10] MEMBUAT GENERATOR LOGO ORIGINAL WAHIDIYAH..."
mkdir -p brand/logo
cat << 'EOF' > brand/generate_wahidiyah_brand.sh
#!/bin/bash
# WAHIDIYAH BRAND GENERATOR
# Script ini menghasilkan SVG logo original Wahidiyah yang terinspirasi dari
# nilai spiritual, ketenangan, dan harmoni (W shape + kubah/daun abstrak).
# Warna: Deep Green & Lime.

cat << 'SVG_EOF' > brand/logo/wahidiyah_mark.svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
  <path d="M20,30 L40,80 L50,55 L60,80 L80,30 L68,30 L60,60 L50,35 L40,60 L32,30 Z" fill="#1C5B2D"/>
  <path d="M50,15 Q65,35 60,60 Q50,45 50,15 Z" fill="#73C34F"/>
</svg>
SVG_EOF
echo "Logo SVG Wahidiyah Original berhasil di-generate di brand/logo/"
EOF
chmod +x brand/generate_wahidiyah_brand.sh
./brand/generate_wahidiyah_brand.sh

echo "==> [2/10] MEMBERSIHKAN IKON LAMA & MEMBUAT APP ICON ANDROID..."
find composeApp/src/androidMain/res -name "ic_launcher*.png" -type f -delete || true
find composeApp/src/androidMain/res -name "ic_launcher*.xml" -type f -delete || true

mkdir -p composeApp/src/androidMain/res/mipmap-anydpi-v26
mkdir -p composeApp/src/androidMain/res/drawable
mkdir -p composeApp/src/androidMain/res/values

cat << 'EOF' > composeApp/src/androidMain/res/values/colors.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="brand_background">#F9FBF9</color>
</resources>
EOF

cat << 'EOF' > composeApp/src/androidMain/res/drawable/ic_wahidiyah_logo.xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="100" android:viewportHeight="100">
    <path android:fillColor="#1C5B2D" android:pathData="M20,30 L40,80 L50,55 L60,80 L80,30 L68,30 L60,60 L50,35 L40,60 L32,30 Z"/>
    <path android:fillColor="#73C34F" android:pathData="M50,15 Q65,35 60,60 Q50,45 50,15 Z"/>
</vector>
EOF

cat << 'EOF' > composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher.xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/brand_background"/>
    <foreground android:drawable="@drawable/ic_wahidiyah_logo"/>
</adaptive-icon>
EOF
cp composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher.xml composeApp/src/androidMain/res/mipmap-anydpi-v26/ic_launcher_round.xml


echo "==> [3/10] MEMBUAT COMPOSABLE LOGO NATIVE (ANTI-ERROR RESOURCE LINTAS PLATFORM)..."
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/components
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/components/WahidiyahLogo.kt
package id.wahidiyah.miladiyyah.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import id.wahidiyah.miladiyyah.theme.BrandPrimary
import id.wahidiyah.miladiyyah.theme.BrandAccent

@Composable
fun WahidiyahLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(64.dp)) {
        val wPath = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.3f)
            lineTo(size.width * 0.4f, size.height * 0.8f)
            lineTo(size.width * 0.5f, size.height * 0.55f)
            lineTo(size.width * 0.6f, size.height * 0.8f)
            lineTo(size.width * 0.8f, size.height * 0.3f)
            lineTo(size.width * 0.68f, size.height * 0.3f)
            lineTo(size.width * 0.6f, size.height * 0.6f)
            lineTo(size.width * 0.5f, size.height * 0.35f)
            lineTo(size.width * 0.4f, size.height * 0.6f)
            lineTo(size.width * 0.32f, size.height * 0.3f)
            close()
        }
        val leafPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.15f)
            quadraticBezierTo(size.width * 0.65f, size.height * 0.35f, size.width * 0.6f, size.height * 0.6f)
            quadraticBezierTo(size.width * 0.5f, size.height * 0.45f, size.width * 0.5f, size.height * 0.15f)
            close()
        }
        drawPath(path = wPath, color = BrandPrimary)
        drawPath(path = leafPath, color = BrandAccent)
    }
}
EOF

echo "==> [4/10] MEMPERBAIKI STATE PERSISTENCE (APP CACHE UNTUK SWITCH SETTINGS)..."
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/utils/AppCache.kt
package id.wahidiyah.miladiyyah.core.utils

object AppCache {
    var save: (String, String) -> Unit = { _, _ -> }
    var load: (String) -> String? = { null }
    
    fun saveBoolean(key: String, value: Boolean) { save(key, value.toString()) }
    fun loadBoolean(key: String, default: Boolean = true): Boolean {
        val res = load(key)
        return res?.toBooleanStrictOrNull() ?: default
    }
}
EOF

cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/utils/UiState.kt
package id.wahidiyah.miladiyyah.core.utils

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val isOffline: Boolean = false) : UiState<Nothing>()
}
EOF

echo "==> [5/10] REVISI SPLASH SCREEN (MENGGUNAKAN LOGO ORIGINAL)..."
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
import id.wahidiyah.miladiyyah.ui.components.WahidiyahLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) { delay(2000); onTimeout() }
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WahidiyahLogo(modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Text("WAHIDIYAH", color = BrandPrimaryDark, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 8.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Aplikasi Resmi Jamaah", color = TextSecondary, fontSize = 13.sp, letterSpacing = 2.sp)
        }
    }
}
EOF

echo "==> [6/10] REVISI APP.KT (STRUKTUR BOTTOM NAVIGATION)..."
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
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.screens.home.HomeScreen
import id.wahidiyah.miladiyyah.ui.screens.calendar.CalendarScreen
import id.wahidiyah.miladiyyah.ui.screens.kegiatan.KegiatanScreen
import id.wahidiyah.miladiyyah.ui.screens.pustaka.PustakaScreen
import id.wahidiyah.miladiyyah.ui.screens.settings.SettingsScreen
import id.wahidiyah.miladiyyah.ui.screens.salat.SalatScreen
import id.wahidiyah.miladiyyah.ui.screens.kiblat.QiblaScreen
import id.wahidiyah.miladiyyah.ui.screens.menu.MenuScreen
import id.wahidiyah.miladiyyah.ui.screens.splash.SplashScreen

enum class AppScreen { BERANDA, KALENDER, SALAT, KEGIATAN, MENU, PUSTAKA, PENGATURAN, KIBLAT }

@Composable
fun App(onUpdateLocation: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf(AppScreen.BERANDA) }
    var showSplash by remember { mutableStateOf(true) }

    MiladiyyahTheme {
        if (showSplash) { SplashScreen(onTimeout = { showSplash = false }) } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Surface, contentColor = TextSecondary, tonalElevation = 8.dp) {
                        NavigationBarItem(icon = { Icon(Icons.Default.Home, "Beranda") }, label = { Text("Beranda") }, selected = currentScreen == AppScreen.BERANDA, onClick = { currentScreen = AppScreen.BERANDA }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.DateRange, "Kalender") }, label = { Text("Kalender") }, selected = currentScreen == AppScreen.KALENDER, onClick = { currentScreen = AppScreen.KALENDER }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.Notifications, "Salat") }, label = { Text("Salat") }, selected = currentScreen == AppScreen.SALAT, onClick = { currentScreen = AppScreen.SALAT }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.List, "Kegiatan") }, label = { Text("Kegiatan") }, selected = currentScreen == AppScreen.KEGIATAN, onClick = { currentScreen = AppScreen.KEGIATAN }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.Menu, "Menu") }, label = { Text("Menu") }, selected = currentScreen == AppScreen.MENU, onClick = { currentScreen = AppScreen.MENU }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                    }
                }
            ) { innerPadding ->
                Surface(modifier = Modifier.padding(innerPadding).background(Background)) {
                    when (currentScreen) {
                        AppScreen.BERANDA -> HomeScreen(onNavigateToSalat = { currentScreen = AppScreen.SALAT }, onNavigateToPustaka = { currentScreen = AppScreen.PUSTAKA }, onUpdateLocation = onUpdateLocation)
                        AppScreen.KALENDER -> CalendarScreen(id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine())
                        AppScreen.SALAT -> SalatScreen(onNavigateToKiblat = { currentScreen = AppScreen.KIBLAT })
                        AppScreen.KEGIATAN -> KegiatanScreen()
                        AppScreen.MENU -> MenuScreen(onNavigate = { screen -> currentScreen = screen })
                        AppScreen.PUSTAKA -> PustakaScreen()
                        AppScreen.PENGATURAN -> SettingsScreen()
                        AppScreen.KIBLAT -> QiblaScreen()
                    }
                }
            }
        }
    }
}
EOF

echo "==> [7/10] REVISI HOMESCREEN (HEADER TERSUSUN AMAN, LOGO ORIGINAL, PENGUMUMAN DOMINAN)..."
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
import id.wahidiyah.miladiyyah.ui.components.WahidiyahLogo
import kotlinx.coroutines.delay
import kotlinx.datetime.*

@Composable
fun HomeScreen(onNavigateToSalat: () -> Unit = {}, onNavigateToPustaka: () -> Unit = {}, onUpdateLocation: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    var currentDateTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(tz)) }
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
        
        // HEADER: LOGO ORIGINAL & STACKING AMAN
        Box(modifier = Modifier.fillMaxWidth().background(BrandPrimaryDark, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(top = 40.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                WahidiyahLogo(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("WAHIDIYAH", color = Surface, fontSize = 20.sp, letterSpacing = 6.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onUpdateLocation() }.background(BrandPrimary).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandAccentLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(PrayerTimeEngine.locationName, color = Surface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
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

        // TANGGAL MASEHI & HIJRIYAH
        Box(modifier = Modifier.fillMaxWidth().offset(y = (-32).dp).padding(horizontal = 24.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(dateString, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${hijriBase.day} ${hMonthNames[hijriBase.month]} ${hijriBase.year} H", fontSize = 14.sp, color = BrandPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // PENGUMUMAN PENTING (DOMINAN VISUAL JIKA ADA)
        if (pengumumanState is UiState.Success && (pengumumanState as UiState.Success).data.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-8).dp)) {
                Text("Pengumuman Penting", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Error, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
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

        // FITUR PENGINGAT (TASYAFU'AN & DANA BOX TETAP ADA)
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

echo "==> [8/10] PUSAT PENGATURAN ALARM DENGAN STATE PERSISTENCE (SALAT SCREEN)..."
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
import id.wahidiyah.miladiyyah.core.utils.AppCache
import kotlinx.datetime.*

@Composable
fun SalatScreen(onNavigateToKiblat: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val tz = TimeZone.currentSystemDefault()
    val prayers = try { PrayerTimeEngine.getPrayers(Clock.System.now().toLocalDateTime(tz).date) } catch(e:Exception) { emptyList() }

    // State Persistent menggunakan AppCache
    var adzanEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_ADZAN", true)) }
    var tarhimEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_TARHIM", true)) }
    var tasyafuanEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_TASYAFUAN", true)) }
    var danaBoxEnabled by remember { mutableStateOf(AppCache.loadBoolean("ALARM_DANABOX", true)) }

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

            Spacer(modifier = Modifier.height(40.dp))
            Text("PENGATURAN PENGINGAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Adzan & Salat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Peringatan masuk waktu", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = adzanEnabled, onCheckedChange = { adzanEnabled = it; AppCache.saveBoolean("ALARM_ADZAN", it) }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Pengingat Tarhim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Sebelum waktu Subuh", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tarhimEnabled, onCheckedChange = { tarhimEnabled = it; AppCache.saveBoolean("ALARM_TARHIM", it) }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Tasyafu'an", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Setiap 03:00 WIB", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = tasyafuanEnabled, onCheckedChange = { tasyafuanEnabled = it; AppCache.saveBoolean("ALARM_TASYAFUAN", it) }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Dana Box", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Pukul 06:00 & 19:00", fontSize = 14.sp, color = TextSecondary) }
                        Switch(checked = danaBoxEnabled, onCheckedChange = { danaBoxEnabled = it; AppCache.saveBoolean("ALARM_DANABOX", it) }, colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary))
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
EOF

echo "==> [9/10] MEMBANGUN MENU NYATA (BUKAN PLACEHOLDER TEXT)..."
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/menu
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/menu/MenuScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.wahidiyah.miladiyyah.AppScreen
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun MenuScreen(onNavigate: (AppScreen) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(horizontal = 24.dp, vertical = 24.dp)) {
            Column { Text("Menu Utama", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier=Modifier.height(4.dp)); Text("Pusat fitur & informasi sekunder", color = TextSecondary, fontSize = 14.sp) }
        }
        HorizontalDivider(color = Border)
        
        Column(modifier = Modifier.padding(24.dp)) {
            Text("FITUR WAHIDIYAH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 16.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.PUSTAKA) }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Info, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Pustaka Jamaah", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Kitab dan arsip digital", fontSize = 14.sp, color = TextSecondary) }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                    HorizontalDivider(color = Background, thickness = 2.dp)
                    Row(modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.KIBLAT) }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Kompas Kiblat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Arah presisi via GPS", fontSize = 14.sp, color = TextSecondary) }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("PENGATURAN & LAINNYA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 16.dp, start = 4.dp))
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.PENGATURAN) }.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandAccentLight), contentAlignment = Alignment.Center) { Icon(Icons.Default.Settings, null, tint = BrandPrimary) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text("Pengaturan Umum", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("Data & Sinkronisasi lokal", fontSize = 14.sp, color = TextSecondary) }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }
        }
    }
}
EOF
rm -f composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/settings/SettingsScreen.kt
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/settings/SettingsScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.wahidiyah.miladiyyah.theme.*

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Text("Halaman Pengaturan & Sinkronisasi Data Lokal", color = TextSecondary)
    }
}
EOF

echo "==> [10/10] MENGIRIM MASTER PERBAIKAN KE GITHUB..."
git add .
git commit -m "feat: complete master branding resolution. Scripted original reproducible Wahidiyah vector logo (No Icons.Info). Implemented safe vertical stacking for Home header. Centralized alarms to SalatScreen with boolean state persistence. Refined Bottom Navigation & created genuine Menu Screen interface."
git push origin main
echo "========================================================"
echo " EKSEKUSI SELESAI & SUKSES DI-PUSH KE GITHUB!"
echo "========================================================"


