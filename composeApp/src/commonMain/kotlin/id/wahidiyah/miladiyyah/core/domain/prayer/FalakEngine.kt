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
 * Engine hisab waktu sholat Miladiyyah.
 *
 * Baseline LFNU yang terdokumentasi:
 * - Subuh  : -20°
 * - Isya   : -18°
 * - Dhuha  : +4.5°
 * - Dzuhur : istiwa + 1 menit
 * - Ashar  : faktor panjang bayangan 1
 * - Imsak  : 10 menit sebelum Subuh
 * - Ihtiyath default: 0 menit
 * - Markaz : koordinat GPS pengguna
 * - Elevasi: dipakai untuk Terbit/Maghrib
 * - Tampilan: round-up ke menit berikutnya
 *
 * Catatan:
 * Source internal NU Online tidak dipublikasikan penuh.
 * Implementasi ini mengikuti parameter LFNU yang dapat
 * diverifikasi dari dokumentasi publik, bukan klaim bahwa
 * source internal NU Online identik 100%.
 */
object FalakEngine {

    // ============================================================
    // PARAMETER LFNU
    // ============================================================

    private const val FAJR_ALTITUDE = -20.0
    private const val ISHA_ALTITUDE = -18.0
    private const val DHUHA_ALTITUDE = 4.5
    private const val DHUHUR_PLUS_MINUTES = 1.0

    /**
     * Imsak = 10 menit sebelum Subuh.
     */
    private const val IMSAK_BEFORE_SUBUH_MINUTES = 10.0

    /**
     * Refraksi horizon:
     * 34.5 arc-minute = 0.575°.
     *
     * Dipakai bersama semidiameter Matahari dan dip/elevasi
     * untuk menentukan tinggi Matahari saat Terbit/Maghrib.
     */
    private const val HORIZON_REFRACTION = 34.5 / 60.0

    /**
     * LFNU-style solar semidiameter:
     *
     * SD = 0.267 / (1 - 0.017 * cos(M))
     *
     * M = mean anomaly Matahari.
     *
     * Jadi semidiameter tidak lagi dipatok satu angka tetap
     * sepanjang tahun.
     */
    private const val BASE_SOLAR_SEMI_DIAMETER = 0.267
    private const val EARTH_ORBIT_ECCENTRICITY_TERM = 0.017

    /**
     * Dip / kerendahan ufuk:
     * dip = 1.76 * sqrt(elevasi) arc-minute.
     */
    private const val DIP_COEFFICIENT_ARCMIN = 1.76

    // ============================================================
    // UTILITAS SUDUT
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
     * Perhitungan apparent solar longitude, declination,
     * right ascension, dan equation of time.
     *
     * Struktur perhitungan memakai formulasi astronomi
     * apparent Sun yang umum dipakai dalam hisab ephemeris.
     */
    private fun solarPosition(
        julianDay: Double
    ): SolarPosition {

        val t =
            (julianDay - 2451545.0) /
                36525.0

        val t2 = t * t
        val t3 = t2 * t

        val meanLongitude =
            normalizeDegrees(
                280.46646 +
                    36000.76983 * t +
                    0.0003032 * t2
            )

        val meanAnomaly =
            normalizeDegrees(
                357.52911 +
                    35999.05029 * t -
                    0.0001537 * t2
            )

        val meanAnomalyRad =
            degToRad(meanAnomaly)

        val equationOfCenter =
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

        val trueLongitude =
            meanLongitude +
                equationOfCenter

        val omega =
            125.04 -
                1934.136 * t

        val apparentLongitude =
            trueLongitude -
                0.00569 -
                0.00478 *
                sinD(omega)

        val meanObliquity =
            23.439291111 -
                0.0130041667 * t -
                0.000000164 * t2 +
                0.000000504 * t3

        val apparentObliquity =
            meanObliquity +
                0.00256 *
                cosD(omega)

        val declination =
            radToDeg(
                asin(
                    (
                        sinD(apparentObliquity) *
                            sinD(apparentLongitude)
                        ).coerceIn(-1.0, 1.0)
                )
            )

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
            declinationDegrees =
                declination,
            equationOfTimeHours =
                equationOfTime
        )
    }

    // ============================================================
    // SOLAR SEMI-DIAMETER DINAMIS
    // ============================================================

    /**
     * Menghitung semidiameter Matahari untuk tanggal/waktu event.
     *
     * SD = 0.267 / (1 - 0.017 * cos(M))
     */
    private fun solarSemiDiameterDegrees(
        julianDay: Double
    ): Double {

        val t =
            (julianDay - 2451545.0) /
                36525.0

        val meanAnomaly =
            357.52911 +
                35999.05029 * t -
                0.0001537 * t * t

        val denominator =
            1.0 -
                EARTH_ORBIT_ECCENTRICITY_TERM *
                cosD(meanAnomaly)

        if (
            !denominator.isFinite() ||
            abs(denominator) < 1e-12
        ) {
            return BASE_SOLAR_SEMI_DIAMETER
        }

        val result =
            BASE_SOLAR_SEMI_DIAMETER /
                denominator

        return if (result.isFinite()) {
            result
        } else {
            BASE_SOLAR_SEMI_DIAMETER
        }
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
    // HORIZON ALTITUDE
    // ============================================================

    private fun horizonAltitude(
        elevationMeters: Double,
        julianDay: Double
    ): Double {

        val dip =
            calculateDipDegrees(
                elevationMeters
            )

        val solarSemiDiameter =
            solarSemiDiameterDegrees(
                julianDay
            )

        val apparentHorizon =
            solarSemiDiameter +
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
    // EVENT TIME + ITERASI
    // ============================================================

    /**
     * Penyelesaian event Matahari dilakukan iteratif.
     *
     * Tujuannya bukan mengubah parameter LFNU,
     * tetapi mengurangi error kecil karena posisi Matahari
     * sebelumnya dihitung pada jam perkiraan tetap.
     */
    private fun calculateEventTime(
        date: LocalDate,
        localHourForEphemeris: Double,
        latitude: Double,
        longitude: Double,
        timeZone: Double,
        altitudeDegrees: Double,
        morning: Boolean
    ): Double {

        var estimateHours =
            localHourForEphemeris

        repeat(3) {

            val eventJulianDay =
                julianDateAtLocalHour(
                    date = date,
                    localHour = estimateHours,
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

            estimateHours =
                if (morning) {
                    solarNoon -
                        hourAngle
                } else {
                    solarNoon +
                        hourAngle
                }
        }

        return estimateHours
    }

    // ============================================================
    // ASHAR
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
                normalized * 60.0 -
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

        val sunriseHorizonAltitude =
            horizonAltitude(
                elevationMeters =
                    safeElevation,
                julianDay =
                    julianDateAtLocalHour(
                        date = date,
                        localHour = 6.0,
                        timeZone = timeZone
                    )
            )

        val terbitRaw =
            calculateEventTime(
                date = date,
                localHourForEphemeris = 6.0,
                latitude = safeLatitude,
                longitude = safeLongitude,
                timeZone = timeZone,
                altitudeDegrees =
                    sunriseHorizonAltitude,
                morning = true
            )

        // --------------------------------------------------------
        // DHUHA
        // --------------------------------------------------------

        val dhuhaRaw =
            calculateEventTime(
                date = date,
                localHourForEphemeris = 6.0,
                latitude = safeLatitude,
                longitude = safeLongitude,
                timeZone = timeZone,
                altitudeDegrees = DHUHA_ALTITUDE,
                morning = true
            )

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
                altitudeDegrees =
                    asharAltitude,
                morning = false
            )

        // --------------------------------------------------------
        // MAGHRIB
        // --------------------------------------------------------

        val sunsetHorizonAltitude =
            horizonAltitude(
                elevationMeters =
                    safeElevation,
                julianDay =
                    julianDateAtLocalHour(
                        date = date,
                        localHour = 18.0,
                        timeZone = timeZone
                    )
            )

        val maghribRaw =
            calculateEventTime(
                date = date,
                localHourForEphemeris = 18.0,
                latitude = safeLatitude,
                longitude = safeLongitude,
                timeZone = timeZone,
                altitudeDegrees =
                    sunsetHorizonAltitude,
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
