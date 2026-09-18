#!/bin/bash
echo "⚙️ TAHAP 1: Setup Library kotlinx-datetime & Domain Models..."

# 1. Update libs.versions.toml untuk menambahkan kotlinx-datetime
cat << 'EOF' > gradle/libs.versions.toml
[versions]
agp = "8.2.2"
kotlin = "1.9.23"
compose = "1.6.10"
coroutines = "1.8.0"
serialization = "1.6.3"
ktor = "2.3.9"
room = "2.7.0-alpha01"
datetime = "0.5.0"

[libraries]
compose-ui = { module = "org.jetbrains.compose.ui:ui", version.ref = "compose" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "compose" }
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidLibrary = { id = "com.android.library", version.ref = "agp" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "compose" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
EOF

# 2. Update build.gradle.kts untuk memasang library
cat << 'EOF' > composeApp/build.gradle.kts
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
}
kotlin {
    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "17" } }
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "ComposeApp"; isStatic = true }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
            implementation(libs.kotlinx.datetime) // Library waktu deterministik KMP
        }
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.8.2")
        }
        iosMain.dependencies {}
    }
}
android {
    namespace = "id.wahidiyah.miladiyyah"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    defaultConfig { applicationId = "id.wahidiyah.miladiyyah"; minSdk = 24; targetSdk = 34; versionCode = 1; versionName = "1.0.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
EOF

# 3. Buat Data Class Domain Murni (CalendarDay, HijriDate, dll)
mkdir -p composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/model
cat << 'EOF' > composeApp/src/commonMain/kotlin/id/wahidiyah/miladiyyah/core/domain/calendar/model/CalendarModels.kt
package id.wahidiyah.miladiyyah.core.domain.calendar.model

import kotlinx.datetime.LocalDate

data class GregorianDate(val year: Int, val month: Int, val day: Int, val localDate: LocalDate)
data class HijriDate(val year: Int, val month: Int, val day: Int)
data class Pasaran(val name: String, val index: Int)

data class CalendarDay(
    val gregorian: GregorianDate,
    val hijri: HijriDate,
    val pasaran: Pasaran,
    val dayOfWeek: Int // 1 (Senin) - 7 (Minggu)
)
EOF

echo "✅ Tahap 1 Selesai!"
EOF
