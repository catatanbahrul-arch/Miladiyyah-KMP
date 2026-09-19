package id.wahidiyah.miladiyyah.core.domain.prayer

import kotlinx.datetime.*

enum class PrayerType(val title: String) {
    IMSAK("Imsak"), SUBUH("Subuh"), TERBIT("Terbit"), DHUHA("Dhuha"), 
    DZUHUR("Zuhur"), ASHAR("Ashar"), MAGHRIB("Maghrib"), ISYA("Isya'")
}

data class PrayerTime(val type: PrayerType, val time: LocalTime)

object PrayerTimeEngine {
    var latitude = -7.8480
    var longitude = 112.0178

    fun getTodayPrayers(): List<PrayerTime> {
        val nowInstant = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        val now = nowInstant.toLocalDateTime(timeZone)
        val offset = timeZone.offsetAt(nowInstant).totalSeconds / 3600.0
        val result = FalakEngine.calculate(now.date, latitude, longitude, offset)

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

    fun getNextPrayer(now: LocalTime): PrayerTime {
        val prayers = getTodayPrayers().filter { it.type != PrayerType.TERBIT && it.type != PrayerType.DHUHA }
        return prayers.firstOrNull { (it.time.hour * 60 + it.time.minute) > (now.hour * 60 + now.minute) } ?: prayers.first()
    }
}
