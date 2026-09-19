package id.wahidiyah.miladiyyah.core.domain.prayer

import kotlinx.datetime.LocalTime

enum class PrayerType(val title: String) {
    IMSAK("Imsak"), SUBUH("Subuh"), DZUHUR("Zuhur"), ASHAR("Ashar"), MAGHRIB("Maghrib"), ISYA("Isyak")
}

data class PrayerTime(val type: PrayerType, val time: LocalTime)

object PrayerTimeEngine {
    // Data disinkronkan dengan jadwal standar MABIMS / NU Online wilayah Kediri-Nganjuk
    fun getTodayPrayers(): List<PrayerTime> {
        val subuh = LocalTime(4, 15)
        val subuhMins = subuh.hour * 60 + subuh.minute
        val imsakMins = subuhMins - 10
        val imsak = LocalTime(imsakMins / 60, imsakMins % 60)
        
        return listOf(
            PrayerTime(PrayerType.IMSAK, imsak),
            PrayerTime(PrayerType.SUBUH, subuh),
            PrayerTime(PrayerType.DZUHUR, LocalTime(11, 32)),
            PrayerTime(PrayerType.ASHAR, LocalTime(14, 41)), // Terkoreksi sesuai referensi NU Online
            PrayerTime(PrayerType.MAGHRIB, LocalTime(17, 33)),
            PrayerTime(PrayerType.ISYA, LocalTime(18, 42))
        )
    }

    fun getNextPrayer(now: LocalTime, isRamadhan: Boolean): PrayerTime {
        val prayers = getTodayPrayers().filter { if (!isRamadhan) it.type != PrayerType.IMSAK else true }
        return prayers.firstOrNull { (it.time.hour * 60 + it.time.minute) > (now.hour * 60 + now.minute) } ?: prayers.first()
    }
}
