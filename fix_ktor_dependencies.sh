#!/bin/bash

echo "🔧 Memperbaiki dependensi Ktor dan Serialization (Injeksi Langsung)..."

cat << 'EOF' > composeApp/build.gradle.kts
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    // INJEKSI LANGSUNG: Mengunci plugin serialisasi sesuai versi Kotlin kita (Bypass TOML)
    kotlin("plugin.serialization") version "1.9.23"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
            
            // INJEKSI LANGSUNG: Library Waktu
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            
            // INJEKSI LANGSUNG: Ktor & JSON Serialization (Membasmi error 'io' & 'serialization')
            implementation("io.ktor:ktor-client-core:2.3.9")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.9")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.9")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.8.2")
            implementation("io.ktor:ktor-client-okhttp:2.3.9")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.9")
        }
    }
}

android {
    namespace = "id.wahidiyah.miladiyyah"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "id.wahidiyah.miladiyyah"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
EOF

echo "✅ File build.gradle.kts berhasil diperbarui dengan injeksi langsung!"
EOF
