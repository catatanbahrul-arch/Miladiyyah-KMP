package id.wahidiyah.miladiyyah.core.domain.calendar

class CalendarEngineImpl : CalendarEngine {
    override fun getTodayDate(): MiladiyyahDate {
        return MiladiyyahDate(
            dayOfWeek = "Jumat", dayMasehi = 18, monthMasehiName = "September", yearMasehi = 2026,
            dayHijriyah = 26, monthHijriyahName = "Rabiul Akhir", yearHijriyah = 1448, pasaran = "Kliwon"
        )
    }
}
