# Miladiyyah KMP

Aplikasi kalender dan informasi offline-first untuk Jamaah Wahidiyah, dibangun menggunakan **Kotlin Multiplatform (KMP)** dan **Compose Multiplatform**.

## 🏗 Arsitektur
Proyek ini secara ketat menerapkan **Clean Architecture** dengan batasan-batasan berikut:
- **UI / Presentation Layer (`commonMain`)**: Berisi UI (Compose), ViewModels, dan State. **Tidak boleh** ada pemanggilan HTTP/Network di sini.
- **Domain Layer (`commonMain`)**: Berisi Entity, Business Rules, dan antarmuka (interface) Repository.
- **Data Layer (`commonMain`)**: Implementasi Repository, penanganan *Offline-first*, abstraksi Local Data Source (Room) dan Remote Data Source (Ktor).

## 📡 Aturan Offline-First
Aplikasi dirancang agar **selalu** membaca dan merender UI dari Local Database (`LocalDataSource`).
1. UI observe data dari Database Lokal melalui `Flow`.
2. Sinkronisasi (Ktor) dipanggil di *background*.
3. Data dari server disimpan ke Database Lokal.
4. UI otomatis diperbarui secara reaktif dari Database Lokal.
Jika internet tidak ada, UI tetap berjalan lancar dengan data terakhir.

## 🛠 Panduan Build & Pengembangan
- **Android**: Buka proyek ini di Android Studio (Iguana/Jellyfish ke atas). Jalankan konfigurasi `composeApp` ke emulator atau device fisik.
- **iOS**: Buka direktori `iosApp` di Xcode (macOS), dan jalankan ke Simulator atau iPhone. *Catatan: Proses compile Kotlin ke iOS Framework akan berjalan otomatis via Gradle saat build di Xcode.*

## 📦 Dependency Policy
Setiap penambahan *library* harus mematuhi aturan:
1. Prioritaskan *library* KMP murni (misal: `Ktor`, `Room KMP`, `kotlinx.serialization`).
2. Jangan menambahkan *dependency* Android-only di `commonMain`. Gunakan `expect/actual` jika memerlukan akses API spesifik platform (seperti GPS Location).

---
*Proyek ini dibangun secara modular sejak awal untuk memastikan skalabilitas dan performa tinggi di ekosistem Android dan Apple.*
