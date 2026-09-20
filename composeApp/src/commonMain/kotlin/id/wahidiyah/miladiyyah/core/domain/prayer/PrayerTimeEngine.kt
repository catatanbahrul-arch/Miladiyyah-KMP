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

    // Elevasi lokasi dalam meter di atas permukaan laut.
    // Dipakai oleh FalakEngine khususnya untuk sunrise/Maghrib.
    var elevationMeters = 0.0

    var locationName by mutableStateOf("Mengambil lokasi...")


    // Titik tunggal perubahan koordinat.
    // Semua perhitungan waktu salat membaca latitude/longitude
    // dari object ini.
    fun updateLocation(
        latitude: Double,
        longitude: Double,
        name: String,
        elevationMeters: Double = this.elevationMeters
    ) {
        if (
            latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0
        ) {
            return
        }

        this.latitude = latitude
        this.longitude = longitude

        this.elevationMeters =
            if (
                elevationMeters.isFinite() &&
                elevationMeters >= 0.0
            ) {
                elevationMeters
            } else {
                0.0
            }

        this.locationName = name
    }

    fun getPrayers(date: LocalDate): List<PrayerTime> {
        val timeZone = TimeZone.currentSystemDefault()
        // Menggunakan offset dari waktu sekarang agar GMT tetap akurat
        val offset =
            timeZone.offsetAt(
                date.atStartOfDayIn(timeZone)
            ).totalSeconds / 3600.0
        val result =
            FalakEngine.calculate(
                date = date,
                lat = latitude,
                lng = longitude,
                timeZone = offset,
                elevationMeters = elevationMeters
            )

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
