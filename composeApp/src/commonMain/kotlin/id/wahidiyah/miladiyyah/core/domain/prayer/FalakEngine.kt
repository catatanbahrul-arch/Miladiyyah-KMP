package id.wahidiyah.miladiyyah.core.domain.prayer

import kotlin.math.*
import kotlinx.datetime.*

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

object FalakEngine {

    /*
     * LFNU / NU Online alignment baseline for Indonesia:
     *
     * - Subuh : solar altitude -20°
     * - Isya  : solar altitude -18°
     * - Default ihtiyath : 0 minute
     * - Dhuhur: istiwa + 1 minute
     * - Sunrise / Maghrib include observer elevation
     * - Final display rounds upward to the next minute
     *
     * NOTE:
     * The exact internal implementation of NU Online is not public.
     * Therefore this is the documented LFNU-compatible baseline,
     * not a claim of binary-identical source code.
     */

    private const val FAJR_ANGLE = 20.0
    private const val ISHA_ANGLE = 18.0

    // Standard apparent solar radius + atmospheric refraction baseline.
    private const val SUN_HORIZON_ANGLE = 0.833

    // NU Online documents Dhuhur as istiwa + 1 minute.
    private const val DHUHUR_ADDITION_MINUTES = 1.0

    // Existing project behavior: imsak = 10 minutes before Subuh.
    private const val IMSAK_BEFORE_SUBUH_MINUTES = 10.0

    // Existing project behavior retained until a documented NU-specific
    // Dhuha formula is verified against enough NU Online samples.
    private const val DHUHA_AFTER_SUNRISE_MINUTES = 20.0

    private fun dsin(d: Double) = sin(d * PI / 180.0)
    private fun dcos(d: Double) = cos(d * PI / 180.0)
    private fun dtan(d: Double) = tan(d * PI / 180.0)

    private fun darcsin(x: Double) =
        asin(x.coerceIn(-1.0, 1.0)) * 180.0 / PI

    private fun darccos(x: Double) =
        acos(x.coerceIn(-1.0, 1.0)) * 180.0 / PI

    private fun darctan2(y: Double, x: Double) =
        atan2(y, x) * 180.0 / PI

    private fun fixAngle(a: Double) =
        (a % 360.0).let {
            if (it < 0) it + 360.0 else it
        }

    private fun fixHour(a: Double) =
        (a % 24.0).let {
            if (it < 0) it + 24.0 else it
        }

    fun calculate(
        date: LocalDate,
        lat: Double,
        lng: Double,
        timeZone: Double,
        elevationMeters: Double = 0.0
    ): PrayerTimeResult {

        val safeElevation =
            if (elevationMeters.isFinite() && elevationMeters > 0.0) {
                elevationMeters
            } else {
                0.0
            }

        val jDate =
            julianDate(
                date.year,
                date.monthNumber,
                date.dayOfMonth
            ) - lng / (15.0 * 24.0)

        val d =
            jDate - 2451545.0

        val g =
            fixAngle(
                357.529 + 0.98560028 * d
            )

        val q =
            fixAngle(
                280.459 + 0.98564736 * d
            )

        val l =
            fixAngle(
                q +
                    1.915 * dsin(g) +
                    0.020 * dsin(2 * g)
            )

        val e =
            23.439 - 0.00000036 * d

        val eqTime =
            q / 15.0 -
                fixHour(
                    darctan2(
                        dcos(e) * dsin(l),
                        dcos(l)
                    ) / 15.0
                )

        val declination =
            darcsin(
                dsin(e) * dsin(l)
            )

        val midDay =
            fixHour(
                12.0 +
                    timeZone -
                    lng / 15.0 -
                    eqTime
            )

        val getAngleTime = { angle: Double ->
            val denominator =
                dcos(lat) * dcos(declination)

            val numerator =
                -dsin(angle) -
                    dsin(lat) * dsin(declination)

            val ratio =
                if (abs(denominator) < 1e-12) {
                    Double.NaN
                } else {
                    numerator / denominator
                }

            val safeRatio =
                when {
                    ratio < -1.0 -> -1.0
                    ratio > 1.0 -> 1.0
                    else -> ratio
                }

            val t =
                (1.0 / 15.0) *
                    darccos(safeRatio)

            if (t.isNaN()) 0.0 else t
        }

        /*
         * Elevation correction:
         *
         * Observer at elevation h sees the geometric horizon below
         * the sea-level horizontal plane. A standard horizon-dip
         * approximation is:
         *
         *     dip ≈ 0.0347 × sqrt(h)
         *
         * degrees.
         *
         * Applied only to sunrise/sunset geometry, matching the
         * documented emphasis of NU Online on elevation for
         * Maghrib and sunrise.
         */
        val elevationDip =
            0.0347 * sqrt(safeElevation)

        val horizonAngle =
            SUN_HORIZON_ANGLE + elevationDip

        val asrAngle =
            darctan2(
                1.0,
                1.0 + dtan(
                    abs(lat - declination)
                )
            )

        // Default ihtiyath deliberately removed: 0 minute.
        val fajrTime =
            midDay - getAngleTime(FAJR_ANGLE)

        val asrTime =
            midDay + getAngleTime(-asrAngle)

        val maghribTime =
            midDay + getAngleTime(horizonAngle)

        val ishaTime =
            midDay + getAngleTime(ISHA_ANGLE)

        // LFNU documented baseline: istiwa + 1 minute.
        val dzuhurTime =
            midDay +
                DHUHUR_ADDITION_MINUTES / 60.0

        val imsakTime =
            fajrTime -
                IMSAK_BEFORE_SUBUH_MINUTES / 60.0

        val sunriseTime =
            midDay -
                getAngleTime(horizonAngle)

        val terbitTime =
            sunriseTime

        val dhuhaTime =
            sunriseTime +
                DHUHA_AFTER_SUNRISE_MINUTES / 60.0

        return PrayerTimeResult(
            toLocalTimeRoundedUp(imsakTime),
            toLocalTimeRoundedUp(fajrTime),
            toLocalTimeRoundedUp(terbitTime),
            toLocalTimeRoundedUp(dhuhaTime),
            toLocalTimeRoundedUp(dzuhurTime),
            toLocalTimeRoundedUp(asrTime),
            toLocalTimeRoundedUp(maghribTime),
            toLocalTimeRoundedUp(ishaTime)
        )
    }

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
            2 - a + floor(a / 4.0)

        return floor(
            365.25 * (y + 4716)
        ) +
            floor(
                30.6001 * (m + 1)
            ) +
            day +
            b -
            1524.5
    }

    private fun toLocalTimeRoundedUp(
        hours: Double
    ): LocalTime {

        if (!hours.isFinite()) {
            return LocalTime(0, 0)
        }

        val normalized =
            fixHour(hours)

        /*
         * NU Online documents "round up":
         * 17:59:03 -> 18:00
         * 04:41:57 -> 04:42
         *
         * A tiny epsilon prevents floating-point residue from turning
         * an exact minute into the following minute.
         */
        val totalMinutes =
            ceil(
                normalized * 60.0 - 1e-9
            ).toInt()

        val minutesInDay =
            24 * 60

        val safeTotalMinutes =
            ((totalMinutes % minutesInDay) +
                minutesInDay) %
                minutesInDay

        val hour =
            safeTotalMinutes / 60

        val minute =
            safeTotalMinutes % 60

        return LocalTime(
            hour,
            minute
        )
    }
}
