package id.wahidiyah.miladiyyah.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {
    // Membaca data lokal, tidak block network (Offline-first)
    fun getActiveAnnouncements(): Flow<List<Announcement>>
    
    // Trigger sinkronisasi background
    suspend fun syncAnnouncements()
}

enum class AnnouncementPriority { NORMAL, IMPORTANT, URGENT }
enum class AnnouncementState { ACTIVE, EXPIRED, ARCHIVED }

data class Announcement(
    val id: String,
    val title: String,
    val priority: AnnouncementPriority,
    val state: AnnouncementState
)
