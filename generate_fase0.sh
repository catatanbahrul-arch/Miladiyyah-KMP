#!/bin/bash

echo "🚀 Memulai pembentukan arsitektur Miladiyyah-KMP Fase 0..."

# 1. Buat Struktur Direktori
mkdir -p .github/workflows
mkdir -p gradle
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/repository
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/theme
mkdir -p composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah
mkdir -p composeApp/src/iosMain/kotlin/id/wahidiyah/miladiyyah

# 2. Buat .gitignore (KEAMANAN)
cat << 'EOF' > .gitignore
# IDE & Build
.idea/
.gradle/
build/
**/build/
local.properties

# iOS & macOS
.DS_Store
iosApp/build/
iosApp/DerivedData/
Pods/

# Keamanan (DILARANG KERAS MASUK REPO)
*.jks
*.keystore
secrets.properties
google-services.json
GoogleService-Info.plist
EOF

# 3. Buat libs.versions.toml (DEPENDENCY LOCK)
cat << 'EOF' > gradle/libs.versions.toml
[versions]
agp = "8.2.2"
kotlin = "1.9.23"
compose = "1.6.10"
coroutines = "1.8.0"
serialization = "1.6.3"
ktor = "2.3.9"
room = "2.7.0-alpha01"

[libraries]
compose-ui = { module = "org.jetbrains.compose.ui:ui", version.ref = "compose" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "compose" }
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidLibrary = { id = "com.android.library", version.ref = "agp" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "compose" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
EOF

# 4. Buat settings.gradle.kts
cat << 'EOF' > settings.gradle.kts
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Miladiyyah-KMP"
include(":composeApp")
EOF

# 5. Buat Root build.gradle.kts
cat << 'EOF' > build.gradle.kts
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
}
EOF

# 6. Buat Domain Interface (Offline-First Rules)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/repository/AnnouncementRepository.kt
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
EOF

# 7. Buat Design System (Tokens)
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/theme/Color.kt
package id.wahidiyah.miladiyyah.theme

// Ini hanyalah dummy representasi warna, Compose Multiplatform UI import akan diatur di fase selanjutnya
object MiladiyyahColors {
    val DeepForestGreen = 0xFF1B4D3E
    val DarkTealGreen = 0xFF0F3D35
    val SoftCream = 0xFFF9F6F0
    val WarmWhite = 0xFFFDFAF5
    val SubtleGold = 0xFFD4AF37
    val NaturalGreen = 0xFF4CAF50
}
EOF

# 8. Buat CI/CD Workflow Foundation
cat << 'EOF' > .github/workflows/ci.yml
name: Miladiyyah-KMP CI Foundation

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'
      - name: Validate Foundation
        run: echo "CI/CD Setup Validated. Build test will run after gradle wrapper is committed."
EOF

# 9. Buat ARCHITECTURE.md
cat << 'EOF' > ARCHITECTURE.md
# Arsitektur Miladiyyah-KMP

## 1. Clean Architecture Strict Rules
UI <-> Presentation <-> Domain <-> Repository <-> Local/Remote Data Source

## 2. Batasan Keras (Hard Rules)
- UI **TIDAK BOLEH** memanggil HTTP request.
- UI **TIDAK BOLEH** mengakses Firebase/GApps Script langsung.
- Offline-First: Selalu baca dari Local DB (Room). Internet hanya untuk sync background.
EOF

echo "✅ Fase 0 selesai dibentuk! Semua file telah di-generate."
EOF
