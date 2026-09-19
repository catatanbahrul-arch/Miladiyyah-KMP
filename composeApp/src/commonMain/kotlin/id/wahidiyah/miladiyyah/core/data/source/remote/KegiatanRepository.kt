package id.wahidiyah.miladiyyah.core.data.source.remote

import kotlinx.coroutines.delay

object KegiatanRepository {
    // Fungsi ini nantinya akan memanggil link GAS_API_KEGIATAN
    // Untuk saat ini kita simulasikan proses download JSON dari Google Sheet
    suspend fun fetchKegiatanFromGAS(): String {
        delay(1500L) // Simulasi loading internet 1.5 detik
        
        // Anggap ini adalah hasil keluaran JSON dari Google Sheet Anda
        return """
            [
              {"tanggal": "25 Sep 2026", "acara": "Mujahadah Kubro", "lokasi": "Kediri"},
              {"tanggal": "10 Okt 2026", "acara": "Pembinaan Remaja", "lokasi": "Nganjuk"}
            ]
        """.trimIndent()
    }
}
