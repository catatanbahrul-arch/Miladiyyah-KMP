#!/bin/bash
echo "⚙️ TAHAP 2: Implementasi Mesin Aritmatika Kalender (Offline)..."

mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine

# 1. Pasaran Calculator (Anchor: 1 Jan 1970 = Wage)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine/PasaranCalculator.kt
package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.domain.calendar.model.Pasaran

object PasaranCalculator {
    // Siklus Pasaran
    private val pasaranNames = listOf("Legi", "Pahing", "Pon", "Wage", "Kliwon")
    
    // ANCHOR: 1 Januari 1970 (Unix Epoch 0) adalah hari KAMIS WAGE (Index 3)
    private const val EPOCH_PASARAN_INDEX = 3

    fun calculate(epochDays: Long): Pasaran {
        // Matematika modulo yang aman untuk angka negatif (sebelum 1970)
        val index = ((epochDays + EPOCH_PASARAN_INDEX) % 5 + 5) % 5
        return Pasaran(pasaranNames[index.toInt()], index.toInt())
    }
}
EOF

# 2. Hijri Calculator (Tabular Islamic Calendar - Civil Epoch)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine/HijriCalculator.kt
package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.domain.calendar.model.HijriDate

object HijriCalculator {
    // Epoch Hijriyah (1 Muharram 1 H) dalam Julian Day = 1948440 (16 Juli 622 M)
    // Unix Epoch (1 Jan 1970) dalam Julian Day = 2440588
    // Selisih hari Unix Epoch ke Hijri Epoch = -492148
    private const val HIJRI_EPOCH_OFFSET = 492148L

    fun calculate(epochDays: Long): HijriDate {
        var days = epochDays + HIJRI_EPOCH_OFFSET
        
        // 1 siklus = 30 tahun = 10631 hari
        val cycles = days / 10631
        days %= 10631
        
        // Antisipasi tanggal sebelum epoch Hijriyah
        if (days < 0) {
            val negativeCycles = (days / 10631) - 1
            days -= negativeCycles * 10631
            var adjustedCycles = cycles + negativeCycles
        }
        
        var year = (cycles * 30).toInt() + 1
        var yDays = days.toInt()

        // Kalkulasi tahun dalam siklus 30 tahun
        while (true) {
            val isLeap = isHijriLeapYear(year)
            val daysInYear = if (isLeap) 355 else 354
            if (yDays < daysInYear) break
            yDays -= daysInYear
            year++
        }

        var month = 1
        // Kalkulasi bulan (Ganjil 30 hari, Genap 29 hari. Dzulhijjah 30 jika kabisat)
        while (true) {
            val daysInMonth = getDaysInHijriMonth(month, isHijriLeapYear(year))
            if (yDays < daysInMonth) break
            yDays -= daysInMonth
            month++
        }

        return HijriDate(year, month, yDays + 1)
    }

    private fun isHijriLeapYear(year: Int): Boolean {
        // Formula tahun kabisat Tabular (Siklus 30 tahun)
        return (11 * year + 14) % 30 < 11
    }

    private fun getDaysInHijriMonth(month: Int, isLeapYear: Boolean): Int {
        if (month == 12 && isLeapYear) return 30
        return if (month % 2 != 0) 30 else 29
    }
}
EOF

# 3. Calendar Engine (Pintu Utama)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine/CalendarEngine.kt
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
        
        val gregorian = GregorianDate(date.year, date.monthNumber, date.dayOfMonth, date)
        val hijri = HijriCalculator.calculate(epochDays)
        val pasaran = PasaranCalculator.calculate(epochDays)
        
        return CalendarDay(
            gregorian = gregorian,
            hijri = hijri,
            pasaran = pasaran,
            dayOfWeek = date.dayOfWeek.value // 1=Senin, 7=Minggu
        )
    }
}
EOF
echo "✅ Tahap 2 Selesai!"
EOF
