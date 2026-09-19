package id.wahidiyah.miladiyyah.core.domain.prayer

import kotlin.math.*
import kotlinx.datetime.*

data class PrayerTimeResult(val imsak: LocalTime, val subuh: LocalTime, val terbit: LocalTime, val dhuha: LocalTime, val dzuhur: LocalTime, val ashar: LocalTime, val maghrib: LocalTime, val isya: LocalTime)

object FalakEngine {
    private fun dsin(d: Double) = sin(d * PI / 180.0)
    private fun dcos(d: Double) = cos(d * PI / 180.0)
    private fun dtan(d: Double) = tan(d * PI / 180.0)
    private fun darcsin(x: Double) = asin(x) * 180.0 / PI
    private fun darccos(x: Double) = acos(x) * 180.0 / PI
    private fun darctan2(y: Double, x: Double) = atan2(y, x) * 180.0 / PI
    private fun fixAngle(a: Double) = (a % 360.0).let { if (it < 0) it + 360.0 else it }
    private fun fixHour(a: Double) = (a % 24.0).let { if (it < 0) it + 24.0 else it }

    fun calculate(date: LocalDate, lat: Double, lng: Double, timeZone: Double): PrayerTimeResult {
        val fajrAngle = 20.0
        val ishaAngle = 18.0
        val ihtiyat = 2.0 / 60.0

        val jDate = julianDate(date.year, date.monthNumber, date.dayOfMonth) - lng / (15.0 * 24.0)
        val d = jDate - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g))
        val e = 23.439 - 0.00000036 * d
        val eqTime = q / 15.0 - fixHour(darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0)
        val declination = darcsin(dsin(e) * dsin(l))

        val midDay = fixHour(12.0 + timeZone - lng / 15.0 - eqTime)

        val getAngleTime = { angle: Double ->
            val t = 1.0 / 15.0 * darccos((-dsin(angle) - dsin(lat) * dsin(declination)) / (dcos(lat) * dcos(declination)))
            if (t.isNaN()) 0.0 else t
        }

        val asrAngle = darctan2(1.0, 1.0 + dtan(abs(lat - declination)))
        
        val fajrTime = midDay - getAngleTime(fajrAngle) + ihtiyat
        val asrTime = midDay + getAngleTime(-asrAngle) + ihtiyat
        val maghribTime = midDay + getAngleTime(0.833) + ihtiyat
        val ishaTime = midDay + getAngleTime(ishaAngle) + ihtiyat
        val dzuhurTime = midDay + ihtiyat
        val imsakTime = fajrTime - (10.0 / 60.0)
        
        // Terbit (Sunrise) & Dhuha (+20 menit dari matahari terbit)
        val sunriseTime = midDay - getAngleTime(0.833)
        val terbitTime = sunriseTime
        val dhuhaTime = sunriseTime + (20.0 / 60.0)

        return PrayerTimeResult(
            toLocalTime(imsakTime), toLocalTime(fajrTime), toLocalTime(terbitTime),
            toLocalTime(dhuhaTime), toLocalTime(dzuhurTime), toLocalTime(asrTime),
            toLocalTime(maghribTime), toLocalTime(ishaTime)
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year; var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun toLocalTime(hours: Double): LocalTime {
        var h = hours
        if (h.isNaN()) h = 0.0
        h = fixHour(h + 0.5/60.0)
        return LocalTime(floor(h).toInt(), floor((h - floor(h)) * 60).toInt())
    }
}
