package id.wahidiyah.miladiyyah.core.data.source.local

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    // UI akan selalu observe fungsi ini (Single Source of Truth)
    fun getActiveAnnouncements(): Flow<List<Announcement>>
    
    // Fungsi untuk memperbarui database setelah sinkronisasi
    suspend fun saveAnnouncements(announcements: List<Announcement>)
}
