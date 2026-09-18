package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.domain.calendar.model.HijriDate

object HijriCalculator {
    private const val HIJRI_EPOCH_OFFSET = 492148L

    fun calculate(epochDays: Long): HijriDate {
        var days = epochDays + HIJRI_EPOCH_OFFSET
        var cycles = days / 10631
        days %= 10631
        
        if (days < 0) {
            days += 10631
            cycles -= 1
        }
        
        var year = (cycles * 30).toInt() + 1
        var yDays = days.toInt()

        while (true) {
            val isLeap = (11 * year + 14) % 30 < 11
            val daysInYear = if (isLeap) 355 else 354
            if (yDays < daysInYear) break
            yDays -= daysInYear
            year++
        }

        var month = 1
        while (true) {
            val isLeap = (11 * year + 14) % 30 < 11
            val daysInMonth = if (month == 12 && isLeap) 30 else if (month % 2 != 0) 30 else 29
            if (yDays < daysInMonth) break
            yDays -= daysInMonth
            month++
        }

        return HijriDate(year, month, yDays + 1)
    }
}
