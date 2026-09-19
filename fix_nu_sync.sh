#!/bin/bash

echo "⚙️ Memasang Layer Sinkronisasi Kalender (NU Online MABIMS Standard)..."

# 1. BUAT HIJRI ADJUSTER (Tabel Koreksi NU Online)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/engine/HijriAdjuster.kt
package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import kotlinx.datetime.LocalDate

/**
 * Layer penyesuaian (Adjustment Layer) untuk menyinkronkan kalender Tabular murni
 * dengan ketetapan Imkanur Rukyat MABIMS (Standar Kemenag & NU Online).
 */
object HijriAdjuster {
    
    // Tabel deviasi/koreksi (Offset hari). 
    // Format: "YYYY-MM" (Bulan Masehi) to Int (Jumlah hari yang ditambah/dikurangi)
    private val nuOnlineAdjustments = mapOf(
        // Berdasarkan referensi NU Online: 19 Sept 2026 = 7 Rabiul Akhir.
        // Tabular murni menghasilkan 6 Rabiul Akhir. Maka butuh koreksi +1 hari.
        "2026-09" to 1,
        
        // Catatan: Di Fase produksi/sinkronisasi nanti, tabel ini bisa diisi ribuan data
        // dari lokal database (Room) yang disinkronkan dari server di background.
    )

    /**
     * Mengambil nilai koreksi berdasarkan tahun dan bulan Masehi.
     */
    fun getOffset(date: LocalDate): Int {
        val monthString = date.monthNumber.toString().padStart(2, '0')
        val key = "${date.year}-${monthString}"
        return nuOnlineAdjustments[key] ?: 0
    }
}
EOF

# 2. UPDATE CALENDAR ENGINE (Untuk menerapkan koreksi)
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
EOF

echo "✅ Layer Sinkronisasi NU Online berhasil dipasang!"
EOF
