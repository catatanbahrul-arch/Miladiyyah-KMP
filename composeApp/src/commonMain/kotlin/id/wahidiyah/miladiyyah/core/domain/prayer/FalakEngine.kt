package id.wahidiyah.miladiyyah.core.domain.prayer

import kotlin.math.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class PrayerTimeResult(
    val imsak: LocalTime,
    val subuh: LocalTime,
    val terbit: LocalTime,
    val dhuha: LocalTime,
    val dzuhur: LocalTime,
    val ashar: LocalTime,
    val maghrib: LocalTime,
    val isya: LocalTime
)

/**
 * Engine hisab waktu sholat untuk Miladiyyah.
 *
 * Baseline Indonesia:
 * - Subuh : -20°
 * - Isya  : -18°
 * - Dzuhur: istiwa + 1 menit
 * - Ihtiyath otomatis: 0 menit
 * - GPS menjadi markaz perhitungan
 * - Elevasi dipakai terutama untuk Maghrib dan Terbit
 * - Tampilan akhir dibulatkan ke menit berikutnya
 *
 * Catatan:
 * Source internal NU Online tidak dipublikasikan secara penuh.
 * Implementasi ini mengikuti parameter LFNU yang terdokumentasi
 * secara publik, bukan klaim bahwa bytecode/source-nya identik.
 */
object FalakEngine {

    // ============================================================
    // PARAMETER LFNU
    // ============================================================

    private const val FAJR_ALTITUDE = -20.0
    private const val ISHA_ALTITUDE = -18.0

    /**
     * Matahari terbit/terbenam:
     *
     * semi-diameter matahari
     * + refraksi horizon
     * + dip/kerendahan ufuk.
     *
     * Nilai dasar:
     * semi diameter ≈ 16 menit busur
     * refraksi horizon ≈ 34 menit busur
     *
     * total ≈ 0.8333°
     */
    private const val SOLAR_SEMI_DIAMETER = 0.2666667
    private const val HORIZON_REFRACTION = 0.5666667

    private const val DHUHUR_PLUS_MINUTES = 1.0

    /**
     * Dip/kerendahan ufuk:
     *
     * dip = 1.76 * sqrt(height) arc-minute
     *
     * kemudian dikonversi ke derajat.
     */
    private const val DIP_COEFFICIENT_ARCMIN = 1.76

    /**
     * Fungsi lama project dipertahankan:
     * Imsak = 10 menit sebelum Subuh.
     *
     * Tidak mengubah aturan pengguna yang sudah ada.
     */
    private const val IMSAK_BEFORE_SUBUH_MINUTES = 10.0

    /**
     * Untuk Dhuha kita mempertahankan perilaku aplikasi
     * yang sudah ada sampai tersedia dokumentasi publik
     * NU Online yang cukup eksplisit mengenai parameter Dhuha.
     *
     * Existing behavior:
     * Dhuha = sunrise + 20 menit.
     */
    private const val DHUHA_AFTER_SUNRISE_MINUTES = 20.0

    // ============================================================
    // UTILITAS ANGLE
    // ============================================================

    private fun degToRad(value: Double): Double =
        value * PI / 180.0

    private fun radToDeg(value: Double): Double =
        value * 180.0 / PI

    private fun sinD(value: Double): Double =
        sin(degToRad(value))

    private fun cosD(value: Double): Double =
        cos(degToRad(value))

    private fun normalizeDegrees(value: Double): Double {
        var result = value % 360.0

        if (result < 0.0) {
            result += 360.0
        }

        return result
    }

    private fun normalizeHours(value: Double): Double {
        var result = value % 24.0

        if (result < 0.0) {
            result += 24.0
        }

        return result
    }

    // ============================================================
    // DATA SOLAR
    // ============================================================

    private data class SolarPosition(
        val declinationDegrees: Double,
        val equationOfTimeHours: Double
    )

    // ============================================================
    // JULIAN DAY
    // ============================================================

    private fun julianDate(
        year: Int,
        month: Int,
        day: Int
    ): Double {

        var y = year
        var m = month

        if (m <= 2) {
            y -= 1
            m += 12
        }

        val a =
            floor(y / 100.0)

        val b =
            2.0 -
                a +
                floor(a / 4.0)

        return floor(
            365.25 *
                (y + 4716)
        ) +
            floor(
                30.6001 *
                    (m + 1)
            ) +
            day +
            b -
            1524.5
    }

    /**
     * Julian day pada jam lokal tertentu.
     *
     * timeZone:
     * contoh WIB = +7.
     */
    private fun julianDateAtLocalHour(
        date: LocalDate,
        localHour: Double,
        timeZone: Double
    ): Double {

        val base =
            julianDate(
                date.year,
                date.monthNumber,
                date.dayOfMonth
            )

        val utcHour =
            localHour -
                timeZone

        return base +
            utcHour / 24.0
    }

    // ============================================================
    // APPARENT SOLAR POSITION
    // ============================================================

    /**
     * Perhitungan apparent solar longitude,
     * declination dan equation of time.
     *
     * Digunakan terpisah untuk setiap waktu utama,
     * sehingga tidak memakai satu nilai Matahari yang
     * dipaksakan untuk semua waktu sholat.
     */
    private fun solarPosition(
        julianDay: Double
    ): SolarPosition {

        val t =
            (julianDay - 2451545.0) /
                36525.0

        val t2 = t * t
        val t3 = t2 * t

        // Mean solar longitude.
        val meanLongitude =
            normalizeDegrees(
                280.46646 +
                    36000.76983 * t +
                    0.0003032 * t2
            )

        // Mean anomaly.
        val meanAnomaly =
            normalizeDegrees(
                357.52911 +
                    35999.05029 * t -
                    0.0001537 * t2
            )

        val meanAnomalyRad =
            degToRad(meanAnomaly)

        // Equation of center.
        val center =
            sin(meanAnomalyRad) *
                (
                    1.914602 -
                        0.004817 * t -
                        0.000014 * t2
                    ) +
                sin(2.0 * meanAnomalyRad) *
                (
                    0.019993 -
                        0.000101 * t
                    ) +
                sin(3.0 * meanAnomalyRad) *
                0.000289

        // True longitude.
        val trueLongitude =
            meanLongitude +
                center

        // Apparent longitude correction.
        val omega =
            125.04 -
                1934.136 * t

        val apparentLongitude =
            trueLongitude -
                0.00569 -
                0.00478 *
                sinD(omega)

        // Mean obliquity of the ecliptic.
        val meanObliquity =
            23.439291111 -
                0.0130041667 * t -
                0.000000164 * t2 +
                0.000000504 * t3

        // Apparent obliquity correction.
        val apparentObliquity =
            meanObliquity +
                0.00256 *
                cosD(omega)

        // Apparent solar declination.
        val declination =
            radToDeg(
                asin(
                    (
                        sinD(apparentObliquity) *
                            sinD(apparentLongitude)
                        ).coerceIn(-1.0, 1.0)
                )
            )

        // Apparent solar right ascension.
        var rightAscension =
            radToDeg(
                atan2(
                    cosD(apparentObliquity) *
                        sinD(apparentLongitude),
                    cosD(apparentLongitude)
                )
            )

        rightAscension =
            normalizeDegrees(
                rightAscension
            )

        // Equation of time.
        var equationOfTime =
            meanLongitude / 15.0 -
                rightAscension / 15.0

        while (equationOfTime > 12.0) {
            equationOfTime -= 24.0
        }

        while (equationOfTime < -12.0) {
            equationOfTime += 24.0
        }

        return SolarPosition(
            declinationDegrees = declination,
            equationOfTimeHours = equationOfTime
        )
    }

    // ============================================================
    // DIP / KERENDAHAN UFUK
    // ============================================================

    private fun calculateDipDegrees(
        elevationMeters: Double
    ): Double {

        val safeElevation =
            if (
                elevationMeters.isFinite() &&
                elevationMeters > 0.0
            ) {
                elevationMeters
            } else {
                0.0
            }

        if (safeElevation <= 0.0) {
            return 0.0
        }

        val dipArcMinutes =
            DIP_COEFFICIENT_ARCMIN *
                sqrt(safeElevation)

        return dipArcMinutes / 60.0
    }

    // ============================================================
    // SUNSET / SUNRISE ALTITUDE
    // ============================================================

    private fun horizonAltitude(
        elevationMeters: Double
    ): Double {

        val dip =
            calculateDipDegrees(
                elevationMeters
            )

        val apparentHorizon =
            SOLAR_SEMI_DIAMETER +
                HORIZON_REFRACTION +
                dip

        return -apparentHorizon
    }

    // ============================================================
    // SOLAR NOON
    // ============================================================

    private fun solarNoonHours(
        longitude: Double,
        timeZone: Double,
        equationOfTimeHours: Double
    ): Double {

        return normalizeHours(
            12.0 +
                timeZone -
                longitude / 15.0 -
                equationOfTimeHours
        )
    }

    // ============================================================
    // HOUR ANGLE
    // ============================================================

    private fun hourAngleHours(
        latitude: Double,
        declination: Double,
        solarAltitude: Double
    ): Double {

        val numerator =
            sinD(solarAltitude) -
                sinD(latitude) *
                sinD(declination)

        val denominator =
            cosD(latitude) *
                cosD(declination)

        if (
            !numerator.isFinite() ||
            !denominator.isFinite() ||
            abs(denominator) < 1e-12
        ) {
            return Double.NaN
        }

        val cosine =
            (
                numerator /
                    denominator
                ).coerceIn(
                    -1.0,
                    1.0
                )

        return radToDeg(
            acos(cosine)
        ) / 15.0
    }

    // ============================================================
    // EVENT TIME
    // ============================================================

    private fun calculateEventTime(
        date: LocalDate,
        localHourForEphemeris: Double,
        latitude: Double,
        longitude: Double,
        timeZone: Double,
        altitudeDegrees: Double,
        morning: Boolean
    ): Double {

        val eventJulianDay =
            julianDateAtLocalHour(
                date = date,
                localHour = localHourForEphemeris,
                timeZone = timeZone
            )

        val solar =
            solarPosition(
                eventJulianDay
            )

        val solarNoon =
            solarNoonHours(
                longitude = longitude,
                timeZone = timeZone,
                equationOfTimeHours =
                    solar.equationOfTimeHours
            )

        val hourAngle =
            hourAngleHours(
                latitude = latitude,
                declination =
                    solar.declinationDegrees,
                solarAltitude =
                    altitudeDegrees
            )

        if (!hourAngle.isFinite()) {
            return Double.NaN
        }

        return if (morning) {
            solarNoon -
                hourAngle
        } else {
            solarNoon +
                hourAngle
        }
    }

    // ============================================================
    // ASHAR ALTITUDE
    // ============================================================

    private fun asharAltitude(
        latitude: Double,
        declination: Double
    ): Double {

        val solarNoonZenithDistance =
            abs(
                latitude -
                    declination
            )

        val tangentValue =
            tan(
                degToRad(
                    solarNoonZenithDistance
                )
            )

        return radToDeg(
            atan(
                1.0 /
                    (tangentValue + 1.0)
            )
        )
    }

    // ============================================================
    // ROUND UP
    // ============================================================

    /**
     * NU Online menjelaskan bahwa hasil detik dibulatkan
     * ke menit berikutnya.
     *
     * Contoh:
     * 17:59:03 -> 18:00
     * 04:41:57 -> 04:42
     */
    private fun toLocalTimeRoundedUp(
        hours: Double
    ): LocalTime {

        if (!hours.isFinite()) {
            return LocalTime(
                hour = 0,
                minute = 0
            )
        }

        val normalized =
            normalizeHours(hours)

        val totalMinutes =
            ceil(
                normalized *
                    60.0 -
                    1e-9
            ).toInt()

        val minutesPerDay =
            24 * 60

        val safeMinutes =
            (
                totalMinutes %
                    minutesPerDay +
                    minutesPerDay
                ) %
                minutesPerDay

        val hour =
            safeMinutes / 60

        val minute =
            safeMinutes % 60

        return LocalTime(
            hour = hour,
            minute = minute
        )
    }

    // ============================================================
    // MAIN CALCULATION
    // ============================================================

    fun calculate(
        date: LocalDate,
        lat: Double,
        lng: Double,
        timeZone: Double,
        elevationMeters: Double = 0.0
    ): PrayerTimeResult {

        val safeLatitude =
            lat.coerceIn(
                -90.0,
                90.0
            )

        val safeLongitude =
            lng.coerceIn(
                -180.0,
                180.0
            )

        val safeElevation =
            if (
                elevationMeters.isFinite() &&
                elevationMeters >= 0.0
            ) {
                elevationMeters
            } else {
                0.0
            }

        // --------------------------------------------------------
        // SUBUH
        // --------------------------------------------------------

        val subuhRaw =
            calculateEventTime(
                date = date,
                localHourForEphemeris = 5.0,
                latitude = safeLatitude,
                longitude = safeLongitude,
                timeZone = timeZone,
                altitudeDegrees = FAJR_ALTITUDE,
                morning = true
            )

        // --------------------------------------------------------
        // IMSAK
        // --------------------------------------------------------

        val imsakRaw =
            subuhRaw -
                IMSAK_BEFORE_SUBUH_MINUTES /
                60.0

        // --------------------------------------------------------
        // TERBIT
        // --------------------------------------------------------

        val horizonAltitude =
            horizonAltitude(
                safeElevation
            )

        val terbitRaw =
            calculateEventTime(
                date = date,
                localHourForEphemeris = 6.0,
                latitude = safeLatitude,
                longitude = safeLongitude,
                timeZone = timeZone,
                altitudeDegrees = horizonAltitude,
                morning = true
            )

        // --------------------------------------------------------
        // DHUHA
        // --------------------------------------------------------

        val dhuhaRaw =
            terbitRaw +
                DHUHA_AFTER_SUNRISE_MINUTES /
                60.0

        // --------------------------------------------------------
        // DZUHUR / ISTIWA
        // --------------------------------------------------------

        val noonJulianDay =
            julianDateAtLocalHour(
                date = date,
                localHour = 12.0,
                timeZone = timeZone
            )

        val noonSolar =
            solarPosition(
                noonJulianDay
            )

        val solarNoon =
            solarNoonHours(
                longitude = safeLongitude,
                timeZone = timeZone,
                equationOfTimeHours =
                    noonSolar.equationOfTimeHours
            )

        val dzuhurRaw =
            solarNoon +
                DHUHUR_PLUS_MINUTES /
                60.0

        // --------------------------------------------------------
        // ASHAR
        // --------------------------------------------------------

        val asharJulianDay =
            julianDateAtLocalHour(
                date = date,
                localHour = 15.0,
                timeZone = timeZone
            )

        val asharSolar =
            solarPosition(
                asharJulianDay
            )

        val asharAltitude =
            asharAltitude(
                latitude = safeLatitude,
                declination =
                    asharSolar.declinationDegrees
            )

        val asharRaw =
            calculateEventTime(
                date = date,
                localHourForEphemeris = 15.0,
                latitude = safeLatitude,
                longitude = safeLongitude,
                timeZone = timeZone,
                altitudeDegrees = asharAltitude,
                morning = false
            )

        // --------------------------------------------------------
        // MAGHRIB
        // --------------------------------------------------------

        val maghribRaw =
            calculateEventTime(
                date = date,
                localHourForEphemeris = 18.0,
                latitude = safeLatitude,
                longitude = safeLongitude,
                timeZone = timeZone,
                altitudeDegrees = horizonAltitude,
                morning = false
            )

        // --------------------------------------------------------
        // ISYA
        // --------------------------------------------------------

        val isyaRaw =
            calculateEventTime(
                date = date,
                localHourForEphemeris = 19.0,
                latitude = safeLatitude,
                longitude = safeLongitude,
                timeZone = timeZone,
                altitudeDegrees = ISHA_ALTITUDE,
                morning = false
            )

        return PrayerTimeResult(
            imsak =
                toLocalTimeRoundedUp(
                    imsakRaw
                ),

            subuh =
                toLocalTimeRoundedUp(
                    subuhRaw
                ),

            terbit =
                toLocalTimeRoundedUp(
                    terbitRaw
                ),

            dhuha =
                toLocalTimeRoundedUp(
                    dhuhaRaw
                ),

            dzuhur =
                toLocalTimeRoundedUp(
                    dzuhurRaw
                ),

            ashar =
                toLocalTimeRoundedUp(
                    asharRaw
                ),

            maghrib =
                toLocalTimeRoundedUp(
                    maghribRaw
                ),

            isya =
                toLocalTimeRoundedUp(
                    isyaRaw
                )
        )
    }
}
