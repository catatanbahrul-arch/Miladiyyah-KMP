package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.domain.calendar.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CalendarEngine {
    
    fun getToday(): CalendarDay {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getCalendarDay(today)
    }

    fun getCalendarDay(date: LocalDate): CalendarDay {
        val epochDays = date.toEpochDays().toLong()
        
        // 1. Cek apakah ada penyesuaian (koreksi) NU Online untuk bulan/tanggal ini
        val hijriOffset = HijriAdjuster.getOffset(date)
        
        // 2. Terapkan offset tersebut langsung ke epoch days khusus untuk perhitungan Hijriyah
        //    (Ini memastikan jika +1 hari menyebabkan pindah bulan, algoritmanya tetap aman)
        val adjustedEpochForHijri = epochDays + hijriOffset
        
        return CalendarDay(
            gregorian = GregorianDate(date.year, date.monthNumber, date.dayOfMonth, date),
            hijri = HijriCalculator.calculate(adjustedEpochForHijri), // Dihitung dengan epoch yang terkoreksi
            pasaran = PasaranCalculator.calculate(epochDays),         // Pasaran tidak pernah berubah (mutlak)
            dayOfWeek = date.dayOfWeek.value
        )
    }
}
