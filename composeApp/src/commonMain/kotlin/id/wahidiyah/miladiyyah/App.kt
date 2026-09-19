package id.wahidiyah.miladiyyah

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class AppScreen { BERANDA, KALENDER, SALAT, KEGIATAN, MENU, PUSTAKA, KIBLAT }
const val GAS_API_KALENDER = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec"

@Composable
fun App(onUpdateLocation: () -> Unit = {}) {
    var currentScreen by remember { mutableStateOf(AppScreen.BERANDA) }
    val scope = rememberCoroutineScope()
    val networkService = remember { CalendarNetworkService() }

    LaunchedEffect(Unit) { scope.launch(Dispatchers.IO) { try { val adj = withTimeoutOrNull(5000L) { networkService.fetchCascadeAdjustments(GAS_API_KALENDER) }; if (adj != null) HijriAdjuster.updateAdjustments(adj) } catch (e: Exception) { } } }
    
    MiladiyyahTheme {
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
