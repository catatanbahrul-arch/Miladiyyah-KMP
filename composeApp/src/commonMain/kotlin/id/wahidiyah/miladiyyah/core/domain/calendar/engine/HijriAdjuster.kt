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
