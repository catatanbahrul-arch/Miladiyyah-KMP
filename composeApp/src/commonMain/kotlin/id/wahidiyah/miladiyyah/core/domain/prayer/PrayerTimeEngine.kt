package id.wahidiyah.miladiyyah.core.domain.prayer

import kotlinx.datetime.LocalTime

enum class PrayerType(val title: String) {
    TARHIM("Tarhim"), SUBUH("Subuh"), DZUHUR("Zuhur"), ASHAR("Ashar"), MAGHRIB("Maghrib"), ISYA("Isyak")
}

data class PrayerTime(val type: PrayerType, val time: LocalTime)

object PrayerTimeEngine {
    fun getTodayPrayers(): List<PrayerTime> {
        val subuh = LocalTime(4, 15)
        val subuhMins = subuh.hour * 60 + subuh.minute
        
        // Tarhim otomatis 10 menit sebelum Subuh setiap hari
        val tarhimMins = subuhMins - 10
        val tarhim = LocalTime(tarhimMins / 60, tarhimMins % 60)
        
        return listOf(
            PrayerTime(PrayerType.TARHIM, tarhim),
            PrayerTime(PrayerType.SUBUH, subuh),
            PrayerTime(PrayerType.DZUHUR, LocalTime(11, 32)),
            PrayerTime(PrayerType.ASHAR, LocalTime(14, 41)),
            PrayerTime(PrayerType.MAGHRIB, LocalTime(17, 33)),
            PrayerTime(PrayerType.ISYA, LocalTime(18, 42))
        )
    }

    fun getNextPrayer(now: LocalTime): PrayerTime {
        // Tidak ada lagi pengecekan Ramadhan, Tarhim nyala setiap hari
        val prayers = getTodayPrayers()
        return prayers.firstOrNull { (it.time.hour * 60 + it.time.minute) > (now.hour * 60 + now.minute) } ?: prayers.first()
    }
}
