package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import id.wahidiyah.miladiyyah.core.data.source.remote.CascadeAdjustment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

object HijriAdjuster {
    
    // Default fallback jika offline: Mulai 2026-09-01 koreksi -1
    private val defaultAdjustments = listOf(
        CascadeAdjustment("2026-09-01", -1, "Default Fallback")
    )

    private val _adjustmentsFlow = MutableStateFlow<List<CascadeAdjustment>>(defaultAdjustments)
    val adjustmentsFlow: StateFlow<List<CascadeAdjustment>> = _adjustmentsFlow.asStateFlow()

    fun updateAdjustments(newList: List<CascadeAdjustment>) {
        if (newList.isNotEmpty()) {
            // Urutkan berdasarkan tanggal mulai secara ascending
            _adjustmentsFlow.value = newList.sortedBy { it.startDate }
        }
    }

    fun getOffset(date: LocalDate): Int {
        val list = _adjustmentsFlow.value
        if (list.isEmpty()) return 0

        val dateString = date.toString() // Format "YYYY-MM-DD"
        var activeOffset = 0

        // Cari aturan aktif: Ambil koreksi dari tanggal mulai yang paling akhir 
        // yang lebih kecil atau sama dengan tanggal target
        for (item in list) {
            if (dateString >= item.startDate) {
                activeOffset = item.adjustment
            } else {
                break
            }
        }
        return activeOffset
    }
}
