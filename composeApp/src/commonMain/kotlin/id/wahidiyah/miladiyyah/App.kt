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
import id.wahidiyah.miladiyyah.ui.screens.kiblat.QiblaScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
enum class BottomTab { BERANDA, KALENDER, PUSTAKA, KEGIATAN, MENU, KIBLAT }
const val GAS_API_KALENDER = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec"
const val GAS_API_PENGUMUMAN = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec"
const val GAS_API_PUSTAKA = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec"
const val GAS_API_KEGIATAN = "https://script.google.com/macros/s/AKfycbyuM5B2TNnOvlJKIDeQCiec8-Q-jI0vDOv--n4xiLEu38hykX4wniweG4Jm5mE1H9Ew/exec"
@Composable
fun App(onUpdateLocation: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(BottomTab.BERANDA) }
    val scope = rememberCoroutineScope()
    val networkService = remember { CalendarNetworkService() }
    val syncCalendarData = { scope.launch(Dispatchers.IO) { try { val adjustments = withTimeoutOrNull(5000L) { networkService.fetchCascadeAdjustments(GAS_API_KALENDER) }; if (adjustments != null) HijriAdjuster.updateAdjustments(adjustments) } catch (e: Exception) { } } }
    LaunchedEffect(Unit) { syncCalendarData() }
    MiladiyyahTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Surface, contentColor = TextSecondary, tonalElevation = 8.dp) {
                    NavigationBarItem(icon = { Icon(if (selectedTab == BottomTab.BERANDA) Icons.Filled.Home else Icons.Outlined.Home, "Beranda") }, label = { Text("Beranda") }, selected = selectedTab == BottomTab.BERANDA, onClick = { selectedTab = BottomTab.BERANDA }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                    NavigationBarItem(icon = { Icon(if (selectedTab == BottomTab.KALENDER) Icons.Filled.DateRange else Icons.Outlined.DateRange, "Kalender") }, label = { Text("Kalender") }, selected = selectedTab == BottomTab.KALENDER, onClick = { selectedTab = BottomTab.KALENDER }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                    NavigationBarItem(icon = { Icon(if (selectedTab == BottomTab.PUSTAKA) Icons.Filled.Info else Icons.Outlined.Info, "Pustaka") }, label = { Text("Pustaka") }, selected = selectedTab == BottomTab.PUSTAKA, onClick = { selectedTab = BottomTab.PUSTAKA }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                    NavigationBarItem(icon = { Icon(if (selectedTab == BottomTab.KEGIATAN) Icons.Filled.List else Icons.Outlined.List, "Kegiatan") }, label = { Text("Kegiatan") }, selected = selectedTab == BottomTab.KEGIATAN, onClick = { selectedTab = BottomTab.KEGIATAN }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                    NavigationBarItem(icon = { Icon(if (selectedTab == BottomTab.MENU) Icons.Filled.Menu else Icons.Outlined.Menu, "Menu") }, label = { Text("Menu") }, selected = selectedTab == BottomTab.MENU, onClick = { selectedTab = BottomTab.MENU }, colors = NavigationBarItemDefaults.colors(indicatorColor = BrandAccentLight, selectedIconColor = BrandPrimary, selectedTextColor = BrandPrimary))
                }
            }
        ) { innerPadding -> Surface(modifier = Modifier.padding(innerPadding).background(Background)) { when (selectedTab) { BottomTab.BERANDA -> HomeScreen(onNavigateToKiblat = { selectedTab = BottomTab.KIBLAT }, onUpdateLocation = onUpdateLocation); BottomTab.KALENDER -> CalendarScreen(id.wahidiyah.miladiyyah.core.domain.calendar.engine.CalendarEngine()); BottomTab.PUSTAKA -> PustakaScreen(); BottomTab.KEGIATAN -> KegiatanScreen(); BottomTab.MENU -> SettingsScreen(); BottomTab.KIBLAT -> QiblaScreen() } } }
    }
}
