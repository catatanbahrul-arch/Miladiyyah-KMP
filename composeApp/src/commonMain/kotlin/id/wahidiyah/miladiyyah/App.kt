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
import id.wahidiyah.miladiyyah.core.data.source.remote.CalendarNetworkService
import id.wahidiyah.miladiyyah.core.domain.calendar.engine.HijriAdjuster
import id.wahidiyah.miladiyyah.core.data.sync.RemoteSyncCoordinator
import id.wahidiyah.miladiyyah.core.utils.AppCache
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppScreen { BERANDA, KALENDER, SALAT, KEGIATAN, MENU, PUSTAKA, PENGATURAN, KIBLAT }

@Composable
fun App(onUpdateLocation: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf(AppScreen.BERANDA) }
    var showSplash by remember { mutableStateOf(true) }
    var autoSyncEnabled by remember {
        mutableStateOf(AppCache.loadBoolean("SETTING_AUTO_SYNC", true))
    }
    val scope = rememberCoroutineScope()
    val networkService = remember { CalendarNetworkService() }

    suspend fun syncHijriCorrections() {
        try {
            val result =
                networkService.fetchCascadeAdjustments()

            if (result.success) {
                HijriAdjuster.updateAdjustments(result.data)
                RemoteSyncCoordinator.syncHijri(result.data)

                println(
                    "[HIJRI-SYNC] Sync sukses: " +
                        "${result.data.size} records"
                )
            } else {
                val cached =
                    RemoteSyncCoordinator.loadCachedHijri()

                if (cached.isNotEmpty()) {
                    HijriAdjuster.updateAdjustments(cached)

                    println(
                        "[HIJRI-SYNC] Remote gagal -> " +
                            "memakai cache: ${cached.size} records"
                    )
                } else {
                    println(
                        "[HIJRI-SYNC] Remote gagal dan cache kosong -> " +
                            "baseline engine dipertahankan."
                    )
                }
            }
        } catch (e: Exception) {
            val cached =
                RemoteSyncCoordinator.loadCachedHijri()

            if (cached.isNotEmpty()) {
                HijriAdjuster.updateAdjustments(cached)
            }

            println(
                "[HIJRI-SYNC] Exception: ${e.message}"
            )
        }
    }

    LaunchedEffect(Unit) {
        if (autoSyncEnabled) {
            syncHijriCorrections()
        }
    }

    LaunchedEffect(currentScreen, autoSyncEnabled) {
        if (currentScreen == AppScreen.KALENDER && autoSyncEnabled) {
            syncHijriCorrections()
            while (true) {
                delay(10 * 60 * 1000L)
                syncHijriCorrections()
            }
        }
    }

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
                        NavigationBarItem(icon = { Icon(Icons.Default.Home, "Beranda") }, label = { Text("Beranda") }, selected = currentScreen == AppScreen.BERANDA, onClick = { currentScreen = AppScreen.BERANDA }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.DateRange, "Kalender") }, label = { Text("Kalender") }, selected = currentScreen == AppScreen.KALENDER, onClick = { currentScreen = AppScreen.KALENDER }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.Notifications, "Sholat") }, label = { Text("Sholat") }, selected = currentScreen == AppScreen.SALAT, onClick = { currentScreen = AppScreen.SALAT }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.List, "Kegiatan") }, label = { Text("Kegiatan") }, selected = currentScreen == AppScreen.KEGIATAN, onClick = { currentScreen = AppScreen.KEGIATAN }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                        NavigationBarItem(icon = { Icon(Icons.Default.Menu, "Menu") }, label = { Text("Menu") }, selected = currentScreen == AppScreen.MENU, onClick = { currentScreen = AppScreen.MENU }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                    }
                }
            ) { innerPadding ->
                Surface(modifier = Modifier.padding(innerPadding), color = Background) {
                    when (currentScreen) {
                        AppScreen.BERANDA -> HomeScreen(onNavigateToSalat = { currentScreen = AppScreen.SALAT }, onNavigateToPustaka = { currentScreen = AppScreen.PUSTAKA }, onUpdateLocation = onUpdateLocation)
                        AppScreen.KALENDER -> CalendarScreen(id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine())
                        AppScreen.SALAT -> SalatScreen(
                            onNavigateToKiblat = {
                                currentScreen = AppScreen.KIBLAT
                            },
                            onBack = {
                                currentScreen = AppScreen.BERANDA
                            },
                            onUpdateLocation = onUpdateLocation,
                            onNavigateToSettings = {
                                currentScreen = AppScreen.PENGATURAN
                            }
                        )
                        AppScreen.KEGIATAN -> KegiatanScreen()
                        AppScreen.MENU -> MenuScreen(onNavigate = { screen -> currentScreen = screen })
                        AppScreen.PUSTAKA -> PustakaScreen()
                        AppScreen.PENGATURAN -> SettingsScreen(
                            autoSyncEnabled = autoSyncEnabled,
                            onAutoSyncChanged = { enabled ->
                                autoSyncEnabled = enabled
                            },
                            onSyncNow = {
                                scope.launch {
                                    syncHijriCorrections()
                                }
                            }
                        )
                        AppScreen.KIBLAT -> QiblaScreen()
                    }
                }
            }
        }
    }
}
