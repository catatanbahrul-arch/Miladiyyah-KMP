package id.wahidiyah.miladiyyah

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.wahidiyah.miladiyyah.theme.*
import id.wahidiyah.miladiyyah.ui.screens.calendar.CalendarScreen
import id.wahidiyah.miladiyyah.ui.screens.home.HomeScreen
import id.wahidiyah.miladiyyah.ui.screens.kegiatan.KegiatanScreen
import id.wahidiyah.miladiyyah.ui.screens.kiblat.QiblaScreen
import id.wahidiyah.miladiyyah.ui.screens.menu.MenuScreen
import id.wahidiyah.miladiyyah.ui.screens.pustaka.PustakaScreen
import id.wahidiyah.miladiyyah.ui.screens.salat.SalatScreen
import id.wahidiyah.miladiyyah.ui.screens.settings.SettingsScreen
import id.wahidiyah.miladiyyah.ui.screens.splash.SplashScreen

enum class AppScreen { BERANDA, KALENDER, SALAT, KEGIATAN, MENU, PUSTAKA, PENGATURAN, KIBLAT }

@Composable
fun App(onUpdateLocation: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf(AppScreen.BERANDA) }
    var showSplash by remember { mutableStateOf(true) }

    MiladiyyahTheme {
        if (showSplash) {
            SplashScreen(onTimeout = { showSplash = false })
        } else {
            Scaffold(
                containerColor = Background,
                bottomBar = {
                    NavigationBar(
                        containerColor = Surface,
                        contentColor = TextSecondary,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, "Beranda") },
                            label = { Text("Beranda") },
                            selected = currentScreen == AppScreen.BERANDA,
                            onClick = { currentScreen = AppScreen.BERANDA },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = BrandAccentLight,
                                selectedIconColor = BrandPrimary,
                                selectedTextColor = BrandPrimary
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.DateRange, "Kalender") },
                            label = { Text("Kalender") },
                            selected = currentScreen == AppScreen.KALENDER,
                            onClick = { currentScreen = AppScreen.KALENDER },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = BrandAccentLight,
                                selectedIconColor = BrandPrimary,
                                selectedTextColor = BrandPrimary
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Notifications, "Salat") },
                            label = { Text("Salat") },
                            selected = currentScreen == AppScreen.SALAT,
                            onClick = { currentScreen = AppScreen.SALAT },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = BrandAccentLight,
                                selectedIconColor = BrandPrimary,
                                selectedTextColor = BrandPrimary
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.List, "Kegiatan") },
                            label = { Text("Kegiatan") },
                            selected = currentScreen == AppScreen.KEGIATAN,
                            onClick = { currentScreen = AppScreen.KEGIATAN },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = BrandAccentLight,
                                selectedIconColor = BrandPrimary,
                                selectedTextColor = BrandPrimary
                            )
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Menu, "Menu") },
                            label = { Text("Menu") },
                            selected = currentScreen == AppScreen.MENU,
                            onClick = { currentScreen = AppScreen.MENU },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = BrandAccentLight,
                                selectedIconColor = BrandPrimary,
                                selectedTextColor = BrandPrimary
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier.padding(innerPadding),
                    color = Background
                ) {
                    when (currentScreen) {
                        AppScreen.BERANDA -> HomeScreen(
                            onNavigateToSalat = { currentScreen = AppScreen.SALAT },
                            onNavigateToPustaka = { currentScreen = AppScreen.PUSTAKA },
                            onUpdateLocation = onUpdateLocation
                        )
                        AppScreen.KALENDER -> CalendarScreen(
                            id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine()
                        )
                        AppScreen.SALAT -> SalatScreen(
                            onNavigateToKiblat = { currentScreen = AppScreen.KIBLAT }
                        )
                        AppScreen.KEGIATAN -> KegiatanScreen()
                        AppScreen.MENU -> MenuScreen(
                            onNavigate = { screen -> currentScreen = screen }
                        )
                        AppScreen.PUSTAKA -> PustakaScreen()
                        AppScreen.PENGATURAN -> SettingsScreen()
                        AppScreen.KIBLAT -> QiblaScreen()
                    }
                }
            }
        }
    }
}
