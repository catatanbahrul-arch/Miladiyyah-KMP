package id.wahidiyah.miladiyyah.ui.screens.home

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
    private val repository: AnnouncementRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeAnnouncements()
    }

    private fun observeAnnouncements() {
        scope.launch {
            // UI HANYA mengambil data dari Repository (yang membaca DB Lokal)
            repository.getActiveAnnouncements().collect { announcements ->
                
                // Aturan: Tampilkan di dashboard HANYA jika Penting/Urgent
                val importantAnnouncements = announcements.filter { 
                    it.priority == AnnouncementPriority.IMPORTANT || it.priority == AnnouncementPriority.URGENT
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        activeImportantAnnouncements = importantAnnouncements
                    )
                }
            }
        }
    }
}
