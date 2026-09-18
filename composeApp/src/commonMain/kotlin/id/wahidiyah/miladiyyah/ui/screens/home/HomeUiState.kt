package id.wahidiyah.miladiyyah.ui.screens.home

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement

data class HomeUiState(
    val masehiDate: String = "18 September 2026",
    val hijriyahDate: String = "26 Rabiul Akhir 1448 H",
    val pasaran: String = "Jumat Kliwon",
    val location: String = "Kediri, Jawa Timur",
    val nextPrayerName: String = "Ashar",
    val nextPrayerTime: String = "14:48",
    val nextPrayerCountdown: String = "16 menit lagi",
    val activeImportantAnnouncements: List<Announcement> = emptyList(),
    val summaryActivitiesCount: Int = 2,
    val summaryDanaBoxTime: String = "19:00"
)
