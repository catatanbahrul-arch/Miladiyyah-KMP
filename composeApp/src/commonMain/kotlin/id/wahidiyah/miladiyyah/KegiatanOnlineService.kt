package id.wahidiyah.miladiyyah

import id.wahidiyah.miladiyyah.core.utils.UiState
import kotlinx.coroutines.delay

object AppRepository {
    // Fungsi simulasi memanggil GAS Endpoint. Jika integrasi Ktor sudah ada, ini tinggal diganti fetch HTTP.
    suspend fun getPengumuman(): UiState<String> {
        return try {
            delay(1000) // Simulasi Network
            val cache = AppCache.load("PENGUMUMAN_DATA")
            if (cache.isNullOrEmpty()) UiState.Empty else UiState.Success(cache)
        } catch (e: Exception) {
            val cache = AppCache.load("PENGUMUMAN_DATA")
            if (cache != null) UiState.Success(cache) else UiState.Error("Tidak ada koneksi internet", true)
        }
    }

    suspend fun getKegiatan(): UiState<String> {
        return try {
            delay(1500)
            val cache = AppCache.load("KEGIATAN_DATA")
            if (cache.isNullOrEmpty()) UiState.Empty else UiState.Success(cache)
        } catch (e: Exception) {
            val cache = AppCache.load("KEGIATAN_DATA")
            if (cache != null) UiState.Success(cache) else UiState.Error("Gagal mengambil data", true)
        }
    }

    suspend fun getPustaka(): UiState<String> {
        return try {
            delay(1000)
            val cache = AppCache.load("PUSTAKA_DATA")
            if (cache.isNullOrEmpty()) UiState.Empty else UiState.Success(cache)
        } catch (e: Exception) {
            val cache = AppCache.load("PUSTAKA_DATA")
            if (cache != null) UiState.Success(cache) else UiState.Error("Sedang offline", true)
        }
    }
}
