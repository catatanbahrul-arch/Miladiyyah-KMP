#!/bin/bash

echo "🚀 Memulai eksekusi FASE 2: KMP Module Configuration & Entry Points..."

# Buat folder untuk Android platform files
mkdir -p composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah
# Buat folder untuk iOS platform files
mkdir -p composeApp/src/iosMain/kotlin/id/wahidiyah/miladiyyah

# 1. Buat composeApp/build.gradle.kts (Jantung KMP)
cat << 'EOF' > composeApp/build.gradle.kts
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
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
        }
        androidMain.dependencies {
            // Dependency khusus Android jika ada nantinya (contoh: Ktor OkHttp, Room Android)
        }
        iosMain.dependencies {
            // Dependency khusus iOS jika ada nantinya (contoh: Ktor Darwin)
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

# 2. Buat AndroidManifest.xml
cat << 'EOF' > composeApp/src/androidMain/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

# 3. Buat Android MainActivity.kt (Entry Point Android)
cat << 'EOF' > composeApp/src/androidMain/kotlin/id/wahidiyah/miladiyyah/MainActivity.kt
package id.wahidiyah.miladiyyah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Memanggil shared UI dari commonMain (App.kt)
        setContent {
            App()
        }
    }
}
EOF

# 4. Buat iOS MainViewController.kt (Entry Point iOS)
cat << 'EOF' > composeApp/src/iosMain/kotlin/id/wahidiyah/miladiyyah/MainViewController.kt
package id.wahidiyah.miladiyyah

import androidx.compose.ui.window.ComposeUIViewController

// Fungsi ini akan dipanggil dari Swift di Xcode (iOS App)
fun MainViewController() = ComposeUIViewController { 
    App() 
}
EOF

# 5. Buat file gradle.properties untuk stabilitas build KMP
cat << 'EOF' > gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
kotlin.code.style=official
android.useAndroidX=true
android.nonTransitiveRClass=true
org.jetbrains.compose.experimental.uikit.enabled=true
EOF

echo "✅ FASE 2 Selesai! Gradle KMP & Platform Entry Points berhasil dikonfigurasi."
EOF
