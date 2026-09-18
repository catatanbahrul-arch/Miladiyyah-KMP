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
