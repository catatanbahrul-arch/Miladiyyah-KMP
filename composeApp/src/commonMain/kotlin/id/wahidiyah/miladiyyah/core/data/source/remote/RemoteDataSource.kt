package id.wahidiyah.miladiyyah.core.data.source.remote

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement

interface RemoteDataSource {
    // Fungsi untuk mengambil data dari server
    suspend fun fetchAnnouncements(): List<Announcement>
}
