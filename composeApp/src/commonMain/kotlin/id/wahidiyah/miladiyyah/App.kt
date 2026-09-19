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
