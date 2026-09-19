package id.wahidiyah.miladiyyah

import kotlinx.coroutines.delay

object AppCache {
    var save: (String, String) -> Unit = { _, _ -> }
    var load: (String) -> String? = { null }
}

object KegiatanOnlineService {
    suspend fun fetchKegiatanFromGAS(): String {
        delay(1500L) // Simulasi loading internet 1.5 detik
        return """
            [
              {"tanggal": "25 Sep 2026", "acara": "Mujahadah Kubro", "lokasi": "Kediri"},
              {"tanggal": "10 Okt 2026", "acara": "Pembinaan Remaja", "lokasi": "Nganjuk"}
            ]
        """.trimIndent()
    }
}
