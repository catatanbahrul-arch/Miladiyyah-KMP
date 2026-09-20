package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.data.source.remote.CascadeAdjustment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

object HijriAdjuster {
    // Fallback audit September 2026: baseline 7 -> reference 8 = +1.
    // Data GAS yang berhasil dimuat akan menggantikan fallback.
    private val defaultAdjustments = listOf(
        CascadeAdjustment("2026-09-01", 1, "Fallback September 2026")
    )

    private val _adjustmentsFlow = MutableStateFlow(defaultAdjustments)
    val adjustmentsFlow: StateFlow<List<CascadeAdjustment>> = _adjustmentsFlow.asStateFlow()

    fun updateAdjustments(newList: List<CascadeAdjustment>) {
        if (newList.isEmpty()) {
            println("[HIJRI-SYNC] Empty response; correction saat ini dipertahankan.")
            return
        }
        val normalized = newList
            .filter { Regex("""^\\d{4}-\\d{2}-\\d{2}$""").matches(it.startDate) }
            .filter { it.adjustment in -3..3 }
            .sortedBy { it.startDate }
        if (normalized.isEmpty()) {
            println("[HIJRI-SYNC] Tidak ada correction valid; correction saat ini dipertahankan.")
            return
        }
        _adjustmentsFlow.value = normalized
        println("[HIJRI-SYNC] HijriAdjuster updated=${normalized.size}")
    }

    fun getOffset(date: LocalDate): Int {
        val target = date.toString()
        var active = 0
        for (item in _adjustmentsFlow.value) {
            if (target >= item.startDate) active = item.adjustment else break
        }
        return active
    }

    fun getActiveAdjustment(date: LocalDate): CascadeAdjustment? {
        val target = date.toString()
        return _adjustmentsFlow.value.filter { it.startDate <= target }.maxByOrNull { it.startDate }
    }
}
