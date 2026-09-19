package id.wahidiyah.miladiyyah.core.domain.prayer

import kotlinx.datetime.LocalTime

enum class PrayerType(val title: String) {
    IMSAK("Imsak"), 
    SUBUH("Subuh"), 
    DZUHUR("Zuhur"), 
    ASHAR("Ashar"), 
    MAGHRIB("Maghrib"), 
    ISYA("Isyak")
}

data class PrayerTime(val type: PrayerType, val time: LocalTime)

object PrayerTimeEngine {
    // Catatan: Ini adalah data simulasi awal yang nantinya akan disambungkan dengan library GPS Kemenag/MABIMS.
    // Saat ini diset mendekati waktu Nganjuk/Kediri agar UI bisa langsung berjalan dan dites.
    fun getTodayPrayers(): List<PrayerTime> {
        val subuh = LocalTime(4, 15)
        
        // LOGIKA IMSAK OTOMATIS: 10 Menit sebelum Subuh
        val subuhMins = subuh.hour * 60 + subuh.minute
        val imsakMins = subuhMins - 10
        val imsak = LocalTime(imsakMins / 60, imsakMins % 60)
        
        return listOf(
            PrayerTime(PrayerType.IMSAK, imsak),
            PrayerTime(PrayerType.SUBUH, subuh),
            PrayerTime(PrayerType.DZUHUR, LocalTime(11, 35)),
            PrayerTime(PrayerType.ASHAR, LocalTime(14, 48)),
            PrayerTime(PrayerType.MAGHRIB, LocalTime(17, 35)),
            PrayerTime(PrayerType.ISYA, LocalTime(18, 45))
        )
    }

    // Mendapatkan jadwal salat berikutnya berdasarkan waktu saat ini dan status Ramadhan
    fun getNextPrayer(now: LocalTime, isRamadhan: Boolean): PrayerTime {
        val prayers = getTodayPrayers().filter { 
            // Jika bukan Ramadhan, sembunyikan Imsak
            if (!isRamadhan) it.type != PrayerType.IMSAK else true 
        }
        
        return prayers.firstOrNull { 
            (it.time.hour * 60 + it.time.minute) > (now.hour * 60 + now.minute) 
        } ?: prayers.first() // Jika sudah lewat Isya, kembali ke jadwal pertama esok hari
    }
}
