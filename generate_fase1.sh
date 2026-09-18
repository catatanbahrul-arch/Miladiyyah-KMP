#!/bin/bash

echo "🚀 Memulai eksekusi FASE 1: Compose UI Foundation & Navigation..."

# Buat folder struktur UI
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/calendar
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/prayer
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/activity
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/menu
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/navigation

# 1. Update Warna Tema (Design System Locked)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/theme/Color.kt
package id.wahidiyah.miladiyyah.theme

import androidx.compose.ui.graphics.Color

val DeepForestGreen = Color(0xFF1B4D3E)
val DarkTealGreen = Color(0xFF0F3D35)
val SoftCream = Color(0xFFF9F6F0)
val WarmWhite = Color(0xFFFDFAF5)
val SubtleGold = Color(0xFFD4AF37)
val NaturalGreen = Color(0xFF4CAF50)

val UrgentRed = Color(0xFFD32F2F)
val ImportantOrange = Color(0xFFF57C00)
val TextPrimary = Color(0xFF1C1C1E)
val TextSecondary = Color(0xFF8E8E93)
EOF

# 2. Buat Skema Tema Material 3
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/theme/Theme.kt
package id.wahidiyah.miladiyyah.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
        // Typography akan diatur nanti menggunakan font Plus Jakarta Sans
        content = content
    )
}
EOF

# 3. Buat Kerangka Screen: Beranda
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/home/HomeScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Beranda - Normal State (Tenang & Bersih)")
    }
}
EOF

# 4. Buat Kerangka Screen: Kalender
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/calendar/CalendarScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CalendarScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Kalender Masehi, Hijriyah, Pasaran")
    }
}
EOF

# 5. Buat Kerangka Screen: Salat
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/prayer/PrayerScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.prayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PrayerScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Waktu Salat & Hitung Mundur")
    }
}
EOF

# 6. Buat Kerangka Screen: Kegiatan
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/activity/ActivityScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ActivityScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Daftar Kegiatan Komunitas")
    }
}
EOF

# 7. Buat Kerangka Screen: Menu
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/ui/screens/menu/MenuScreen.kt
package id.wahidiyah.miladiyyah.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MenuScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Menu (Pustaka, Dana Box, Sync, Settings)")
    }
}
EOF

# 8. Buat Main App Entry Point & Simple State Navigation
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/App.kt
package id.wahidiyah.miladiyyah

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import id.wahidiyah.miladiyyah.theme.MiladiyyahTheme
import id.wahidiyah.miladiyyah.ui.screens.home.HomeScreen
import id.wahidiyah.miladiyyah.ui.screens.calendar.CalendarScreen
import id.wahidiyah.miladiyyah.ui.screens.prayer.PrayerScreen
import id.wahidiyah.miladiyyah.ui.screens.activity.ActivityScreen
import id.wahidiyah.miladiyyah.ui.screens.menu.MenuScreen

enum class BottomTab { BERANDA, KALENDER, SALAT, KEGIATAN, MENU }

@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(BottomTab.BERANDA) }

    MiladiyyahTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda") },
                        selected = selectedTab == BottomTab.BERANDA,
                        onClick = { selectedTab = BottomTab.BERANDA }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Kalender") },
                        label = { Text("Kalender") },
                        selected = selectedTab == BottomTab.KALENDER,
                        onClick = { selectedTab = BottomTab.KALENDER }
                    )
                    NavigationBarItem(
                        // Placeholder icon untuk Salat
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Salat") },
                        label = { Text("Salat") },
                        selected = selectedTab == BottomTab.SALAT,
                        onClick = { selectedTab = BottomTab.SALAT }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Kegiatan") },
                        label = { Text("Kegiatan") },
                        selected = selectedTab == BottomTab.KEGIATAN,
                        onClick = { selectedTab = BottomTab.KEGIATAN }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                        label = { Text("Menu") },
                        selected = selectedTab == BottomTab.MENU,
                        onClick = { selectedTab = BottomTab.MENU }
                    )
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    BottomTab.BERANDA -> HomeScreen()
                    BottomTab.KALENDER -> CalendarScreen()
                    BottomTab.SALAT -> PrayerScreen()
                    BottomTab.KEGIATAN -> ActivityScreen()
                    BottomTab.MENU -> MenuScreen()
                }
            }
        }
    }
}
EOF

echo "✅ FASE 1 Selesai! Kerangka UI Navigasi Berhasil Dibuat."
EOF
