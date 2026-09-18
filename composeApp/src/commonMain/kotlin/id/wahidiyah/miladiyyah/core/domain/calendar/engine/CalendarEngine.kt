package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.domain.calendar.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CalendarEngine {
    fun getToday(): CalendarDay {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getCalendarDay(today)
    }

    fun getCalendarDay(date: LocalDate): CalendarDay {
        val epochDays = date.toEpochDays().toLong()
        return CalendarDay(
            gregorian = GregorianDate(date.year, date.monthNumber, date.dayOfMonth, date),
            hijri = HijriCalculator.calculate(epochDays),
            pasaran = PasaranCalculator.calculate(epochDays),
            dayOfWeek = date.dayOfWeek.value
        )
    }
}
