package id.wahidiyah.miladiyyah.core.domain.prayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.*

enum class PrayerType(val title: String) {
    IMSAK("Imsak"),
    SUBUH("Subuh"),
    TERBIT("Terbit"),
    DHUHA("Dhuha"),
    DZUHUR("Zuhur"),
    ASHAR("Ashar"),
    MAGHRIB("Maghrib"),
    ISYA("Isya'")
}

data class PrayerTime(
    val type: PrayerType,
    val time: LocalTime
)

object PrayerTimeEngine {
    // Nilai default dipertahankan hanya sebagai fallback internal.
    // Tidak boleh dipakai sebelum lokasi perangkat divalidasi.
    var latitude = -7.8480
    var longitude = 112.0178

    var elevationMeters = 0.0

    var locationName by mutableStateOf("Lokasi belum siap")
    var locationReady by mutableStateOf(false)

    var locationAccuracyMeters = Double.POSITIVE_INFINITY
        private set

    var locationUpdatedAtEpochMillis = 0L
        private set

    var locationSource = "NONE"
        private set

    // Semua perhitungan waktu salat membaca lokasi terakhir
    // yang sudah divalidasi.
    fun updateLocation(
        latitude: Double,
        longitude: Double,
        name: String,
        elevationMeters: Double = this.elevationMeters,
        accuracyMeters: Double = Double.POSITIVE_INFINITY,
        updatedAtEpochMillis: Long = System.currentTimeMillis(),
        source: String = "UNKNOWN"
    ) {
        if (
            latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0
        ) {
            return
        }

        if (updatedAtEpochMillis <= 0L) {
            return
        }

        this.latitude = latitude
        this.longitude = longitude

        this.elevationMeters =
            if (
                elevationMeters.isFinite() &&
                elevationMeters >= 0.0
            ) {
                elevationMeters
            } else {
                0.0
            }

        this.locationAccuracyMeters =
            if (
                accuracyMeters.isFinite() &&
                accuracyMeters >= 0.0
            ) {
                accuracyMeters
            } else {
                Double.POSITIVE_INFINITY
            }

        this.locationUpdatedAtEpochMillis =
            updatedAtEpochMillis

        this.locationSource =
            source.ifBlank { "UNKNOWN" }

        this.locationName =
            name.ifBlank { "Lokasi GPS" }

        this.locationReady = true
    }

    fun clearLocation() {
        locationReady = false
        locationAccuracyMeters = Double.POSITIVE_INFINITY
        locationUpdatedAtEpochMillis = 0L
        locationSource = "NONE"
        locationName = "Menunggu lokasi GPS..."
    }

    fun getPrayers(date: LocalDate): List<PrayerTime> {
        // Jangan menghitung menggunakan koordinat default.
        if (!locationReady) {
            return emptyList()
        }

        val timeZone =
            TimeZone.currentSystemDefault()

        val offset =
            timeZone.offsetAt(
                date.atStartOfDayIn(timeZone)
            ).totalSeconds / 3600.0

        val result =
            FalakEngine.calculate(
                date = date,
                lat = latitude,
                lng = longitude,
                timeZone = offset,
                elevationMeters = elevationMeters
            )

        return listOf(
            PrayerTime(PrayerType.IMSAK, result.imsak),
            PrayerTime(PrayerType.SUBUH, result.subuh),
            PrayerTime(PrayerType.TERBIT, result.terbit),
            PrayerTime(PrayerType.DHUHA, result.dhuha),
            PrayerTime(PrayerType.DZUHUR, result.dzuhur),
            PrayerTime(PrayerType.ASHAR, result.ashar),
            PrayerTime(PrayerType.MAGHRIB, result.maghrib),
            PrayerTime(PrayerType.ISYA, result.isya)
        )
    }

    fun getNextPrayer(now: LocalTime): PrayerTime? {
        if (!locationReady) {
            return null
        }

        val today =
            Clock.System.todayIn(
                TimeZone.currentSystemDefault()
            )

        val prayers =
            getPrayers(today)
                .filter {
                    it.type != PrayerType.TERBIT &&
                        it.type != PrayerType.DHUHA
                }

        return prayers.firstOrNull {
            (
                it.time.hour * 60 +
                    it.time.minute
            ) >
                (
                    now.hour * 60 +
                        now.minute
                )
        } ?: prayers.firstOrNull()
    }
}
