plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)

    // Kotlin Serialization
    kotlin("plugin.serialization") version "1.9.23"
}

kotlin {
    // ============================================================
    // ANDROID TARGET
    // ============================================================
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // ============================================================
    // iOS TARGETS
    // ============================================================
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

    // ============================================================
    // SOURCE SETS
    // ============================================================
    sourceSets {

        // ========================================================
        // COMMON MAIN
        // ========================================================
        commonMain.dependencies {

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            // Compose Multiplatform Resources
            implementation(compose.components.resources)

            // Coroutines
            implementation(libs.coroutines.core)

            // ====================================================
            // Kotlinx DateTime
            // ====================================================
            implementation(
                "org.jetbrains.kotlinx:kotlinx-datetime:0.6.0"
            )

            // ====================================================
            // Ktor Client
            // ====================================================
            implementation(
                "io.ktor:ktor-client-core:2.3.9"
            )

            implementation(
                "io.ktor:ktor-client-content-negotiation:2.3.9"
            )

            implementation(
                "io.ktor:ktor-serialization-kotlinx-json:2.3.9"
            )

            // ====================================================
            // Kotlinx Serialization JSON
            // ====================================================
            implementation(
                "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3"
            )
        }

        // ========================================================
        // ANDROID MAIN
        // ========================================================
        androidMain.dependencies {

            implementation(
                "androidx.activity:activity-compose:1.8.2"
            )

            implementation(
                "io.ktor:ktor-client-okhttp:2.3.9"
            )

            // Android GPS / current location / location settings
            implementation(
                "com.google.android.gms:play-services-location:21.4.0"
            )
        }

        // ========================================================
        // iOS MAIN
        // ========================================================
        iosMain.dependencies {

            implementation(
                "io.ktor:ktor-client-darwin:2.3.9"
            )
        }
    }
}


// =================================================================
// COMPOSE MULTIPLATFORM RESOURCES
// =================================================================
//
// Resource directory:
//
// composeApp/src/commonMain/composeResources/
//
// Generated resource package:
//
// id.wahidiyah.miladiyyah.generated.resources
//
// Contoh import:
//
// import id.wahidiyah.miladiyyah.generated.resources.Res
//
// Contoh penggunaan:
//
// Res.drawable.wahidiyah_logo_home_splash
//
// =================================================================

compose.resources {
    packageOfResClass =
        "id.wahidiyah.miladiyyah.generated.resources"
}


// =================================================================
// ANDROID CONFIGURATION
// =================================================================

android {

    namespace = "id.wahidiyah.miladiyyah"

    compileSdk = 34

    // ============================================================
    // Android source directories
    // ============================================================

    sourceSets["main"].manifest.srcFile(
        "src/androidMain/AndroidManifest.xml"
    )

    sourceSets["main"].res.srcDirs(
        "src/androidMain/res"
    )

    sourceSets["main"].resources.srcDirs(
        "src/commonMain/resources"
    )

    // ============================================================
    // DEFAULT CONFIGURATION
    // ============================================================

    defaultConfig {

        applicationId =
            "id.wahidiyah.miladiyyah"

        minSdk = 24

        targetSdk = 34

        versionCode = 1

        versionName = "1.0.0"
    }

    // ============================================================
    // PACKAGING
    // ============================================================

    packaging {
        resources {
            excludes +=
                "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // ============================================================
    // BUILD TYPES
    // ============================================================

    buildTypes {

        getByName("release") {

            isMinifyEnabled = false
        }
    }

    // ============================================================
    // JAVA COMPATIBILITY
    // ============================================================

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }
}
