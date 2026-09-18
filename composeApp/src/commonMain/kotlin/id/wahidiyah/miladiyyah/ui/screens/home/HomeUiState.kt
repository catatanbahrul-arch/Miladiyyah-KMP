package id.wahidiyah.miladiyyah.ui.screens.home

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement

data class HomeUiState(
    val dayOfWeek: String = "",
    val masehiDate: String = "",
    val hijriyahDate: String = "",
    val pasaran: String = "",
    val location: String = "Kediri, Jawa Timur",
    val nextPrayerName: String = "Ashar",
    val nextPrayerTime: String = "14:48",
    val nextPrayerCountdown: String = "16 menit lagi",
    val activeImportantAnnouncements: List<Announcement> = emptyList(),
    val summaryActivitiesCount: Int = 2,
    val summaryDanaBoxTime: String = "19:00"
)
