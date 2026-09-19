package id.wahidiyah.miladiyyah.core.domain.calendar.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

object HijriAdjuster {
    
    // Gunakan StateFlow agar UI Compose tahu saat data baru dari internet selesai diunduh
    private val _adjustmentsFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
    val adjustmentsFlow: StateFlow<Map<String, Int>> = _adjustmentsFlow.asStateFlow()

    fun updateAdjustments(newAdjustments: Map<String, Int>) {
        if (newAdjustments.isNotEmpty()) {
            _adjustmentsFlow.value = newAdjustments
        }
    }

    fun getOffset(date: LocalDate): Int {
        val monthString = date.monthNumber.toString().padStart(2, '0')
        val key = "${date.year}-${monthString}"
        return _adjustmentsFlow.value[key] ?: 0
    }
}
