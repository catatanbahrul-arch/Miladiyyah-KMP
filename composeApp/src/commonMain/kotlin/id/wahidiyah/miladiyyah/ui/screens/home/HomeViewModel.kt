package id.wahidiyah.miladiyyah.ui.screens.home

import id.wahidiyah.miladiyyah.core.domain.calendar.CalendarEngine
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementPriority
import id.wahidiyah.miladiyyah.core.domain.repository.AnnouncementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: AnnouncementRepository,
    private val calendarEngine: CalendarEngine
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayDate()
        observeAnnouncements()
    }

    private fun loadTodayDate() {
        val today = calendarEngine.getTodayDate()
        _uiState.update { currentState ->
            currentState.copy(
                masehiDate = today.formattedMasehi,
                hijriyahDate = today.formattedHijriyah,
                pasaran = today.pasaran,
                dayOfWeek = today.dayOfWeek
            )
        }
    }

    private fun observeAnnouncements() {
        scope.launch {
            repository.getActiveAnnouncements().collect { announcements ->
                val importantAnnouncements = announcements.filter { 
                    it.priority == AnnouncementPriority.IMPORTANT || it.priority == AnnouncementPriority.URGENT
                }
                _uiState.update { currentState ->
                    currentState.copy(activeImportantAnnouncements = importantAnnouncements)
                }
            }
        }
    }
}
