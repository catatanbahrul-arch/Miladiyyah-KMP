package id.wahidiyah.miladiyyah.core.domain.calendar.model

import kotlinx.datetime.LocalDate

data class GregorianDate(val year: Int, val month: Int, val day: Int, val localDate: LocalDate)
data class HijriDate(val year: Int, val month: Int, val day: Int)
data class Pasaran(val name: String, val index: Int)

data class CalendarDay(
    val gregorian: GregorianDate,
    val hijri: HijriDate,
    val pasaran: Pasaran,
    val dayOfWeek: Int
)
