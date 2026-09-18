package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.domain.calendar.model.Pasaran

object PasaranCalculator {
    private val pasaranNames = listOf("Legi", "Pahing", "Pon", "Wage", "Kliwon")
    private const val EPOCH_PASARAN_INDEX = 3 // 1 Jan 1970 = Kamis Wage

    fun calculate(epochDays: Long): Pasaran {
        val index = ((epochDays + EPOCH_PASARAN_INDEX) % 5 + 5) % 5
        return Pasaran(pasaranNames[index.toInt()], index.toInt())
    }
}
