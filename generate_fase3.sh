#!/bin/bash

echo "🚀 Memulai eksekusi FASE 3: Clean Architecture & Offline-First Layer..."

# Buat folder untuk struktur Data Layer
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/source/local
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/source/remote
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/repository

# 1. Buat Interface Local Data Source (Representasi Room DB nanti)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/source/local/LocalDataSource.kt
package id.wahidiyah.miladiyyah.core.data.source.local

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    // UI akan selalu observe fungsi ini (Single Source of Truth)
    fun getActiveAnnouncements(): Flow<List<Announcement>>
    
    // Fungsi untuk memperbarui database setelah sinkronisasi
    suspend fun saveAnnouncements(announcements: List<Announcement>)
}
EOF

# 2. Buat Interface Remote Data Source (Representasi Ktor/Network API nanti)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/source/remote/RemoteDataSource.kt
package id.wahidiyah.miladiyyah.core.data.source.remote

import id.wahidiyah.miladiyyah.core.domain.repository.Announcement

interface RemoteDataSource {
    // Fungsi untuk mengambil data dari server
    suspend fun fetchAnnouncements(): List<Announcement>
}
EOF

# 3. Buat Implementasi Repository (Jantung Logika Offline-First)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/repository/AnnouncementRepositoryImpl.kt
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
EOF

# 4. Buat Fake Data Source (Untuk Development Awal sesuai Master Prompt)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/data/source/local/FakeLocalDataSource.kt
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
EOF

echo "✅ FASE 3 Selesai! Arsitektur Data Layer & Offline-First berhasil diimplementasikan."
EOF
