package id.wahidiyah.miladiyyah.core.domain.prayer

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.datetime.*

enum class PrayerType(val title: String) {
    IMSAK("Imsak"), SUBUH("Subuh"), TERBIT("Terbit"), DHUHA("Dhuha"), 
    DZUHUR("Zuhur"), ASHAR("Ashar"), MAGHRIB("Maghrib"), ISYA("Isya'")
}

data class PrayerTime(val type: PrayerType, val time: LocalTime)

object PrayerTimeEngine {
    var latitude = -7.8480
    var longitude = 112.0178
    var locationName by mutableStateOf("Mengambil lokasi...") // State dinamis untuk nama kota


    // Titik tunggal perubahan koordinat.
    // Semua perhitungan waktu salat membaca latitude/longitude
    // dari object ini.
    fun updateLocation(
        latitude: Double,
        longitude: Double,
        name: String
    ) {
        if (
            latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0
        ) {
            return
        }

        this.latitude = latitude
        this.longitude = longitude
        this.locationName = name
    }

    fun getPrayers(date: LocalDate): List<PrayerTime> {
        val timeZone = TimeZone.currentSystemDefault()
        // Menggunakan offset dari waktu sekarang agar GMT tetap akurat
        val offset = timeZone.offsetAt(Clock.System.now()).totalSeconds / 3600.0
        val result = FalakEngine.calculate(date, latitude, longitude, offset)

        return listOf(
            PrayerTime(PrayerType.IMSAK, result.imsak),
            PrayerTime(PrayerType.SUBUH, result.subuh),
            PrayerTime(PrayerType.TERBIT, result.terbit),
            PrayerTime(PrayerType.DHUHA, result.dhuha),
            PrayerTime(PrayerType.DZUHUR, result.dzuhur),
            PrayerTime(PrayerType.ASHAR, result.ashar),
            PrayerTime(PrayerType.MAGHRIB, result.maghrib),
            PrayerTime(PrayerType.ISYA, result.isya)
        )
    }

    fun getNextPrayer(now: LocalTime): PrayerTime? {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val prayers = getPrayers(today).filter { it.type != PrayerType.TERBIT && it.type != PrayerType.DHUHA }
        return prayers.firstOrNull { (it.time.hour * 60 + it.time.minute) > (now.hour * 60 + now.minute) } ?: prayers.firstOrNull()
    }
}
