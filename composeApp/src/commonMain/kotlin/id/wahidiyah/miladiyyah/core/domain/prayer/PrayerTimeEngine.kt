package id.wahidiyah.miladiyyah.core.domain.prayer

import kotlinx.datetime.*

enum class PrayerType(val title: String) {
    TARHIM("Tarhim"), SUBUH("Subuh"), DZUHUR("Zuhur"), ASHAR("Ashar"), MAGHRIB("Maghrib"), ISYA("Isyak")
}

data class PrayerTime(val type: PrayerType, val time: LocalTime)

object PrayerTimeEngine {
    // Koordinat Default Jika GPS Mati (Pusat Kediri / Nganjuk)
    var latitude = -7.8480
    var longitude = 112.0178

    fun getTodayPrayers(): List<PrayerTime> {
        val nowInstant = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        val now = nowInstant.toLocalDateTime(timeZone)
        
        // Membaca GMT daerah HP secara otomatis (misal: WIB = +7.0)
        val offset = timeZone.offsetAt(nowInstant).totalSeconds / 3600.0

        // Menghitung jadwal shalat dari titik koordinat GPS
        val result = FalakEngine.calculate(now.date, latitude, longitude, offset)

        return listOf(
            PrayerTime(PrayerType.TARHIM, result.imsak),
            PrayerTime(PrayerType.SUBUH, result.subuh),
            PrayerTime(PrayerType.DZUHUR, result.dzuhur),
            PrayerTime(PrayerType.ASHAR, result.ashar),
            PrayerTime(PrayerType.MAGHRIB, result.maghrib),
            PrayerTime(PrayerType.ISYA, result.isya)
        )
    }

    fun getNextPrayer(now: LocalTime): PrayerTime {
        val prayers = getTodayPrayers()
        return prayers.firstOrNull { (it.time.hour * 60 + it.time.minute) > (now.hour * 60 + now.minute) } ?: prayers.first()
    }
}
