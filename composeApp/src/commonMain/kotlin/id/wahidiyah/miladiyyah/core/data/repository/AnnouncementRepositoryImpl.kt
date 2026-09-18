package id.wahidiyah.miladiyyah.core.data.repository

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementRepository
import id.wahidiyah.miladiyyah.core.data.source.local.LocalDataSource
import id.wahidiyah.miladiyyah.core.data.source.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow

class AnnouncementRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : AnnouncementRepository {

    // 1. RULE: UI SELALU MEMBACA DATA LOKAL
    override fun getActiveAnnouncements(): Flow<List<Announcement>> {
        return localDataSource.getActiveAnnouncements()
    }

    // 2. RULE: INTERNET HANYA DIGUNAKAN UNTUK SINKRONISASI KE DATABASE LOKAL
    override suspend fun syncAnnouncements() {
        try {
            // Tarik data dari internet (Ktor)
            val remoteData = remoteDataSource.fetchAnnouncements()
            
            // Simpan ke database lokal (Room)
            // UI akan otomatis ter-update karena observe flow dari database lokal
            localDataSource.saveAnnouncements(remoteData)
        } catch (e: Exception) {
            // Jika tidak ada internet atau server error, kita telan error-nya (jangan crash).
            // UI tetap berjalan normal dengan data terakhir yang ada di database lokal.
        }
    }
}
