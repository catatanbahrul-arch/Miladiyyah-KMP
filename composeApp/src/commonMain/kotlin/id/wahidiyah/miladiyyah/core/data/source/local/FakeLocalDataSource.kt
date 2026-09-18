package id.wahidiyah.miladiyyah.core.data.source.local

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Placeholder In-Memory Database sebelum Room sepenuhnya di-inject
class FakeLocalDataSource : LocalDataSource {
    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())

    override fun getActiveAnnouncements(): Flow<List<Announcement>> {
        return _announcements.asStateFlow()
    }

    override suspend fun saveAnnouncements(announcements: List<Announcement>) {
        _announcements.value = announcements
    }
}
