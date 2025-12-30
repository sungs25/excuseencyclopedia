package com.example.excuseencyclopedia.ui.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.excuseencyclopedia.data.Excuse
import com.example.excuseencyclopedia.data.ExcuseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// 캘린더 화면용 상태
data class CalendarUiState(
    val excuseList: List<Excuse> = emptyList()
)

class CalendarViewModel(private val repository: ExcuseRepository) : ViewModel() {

    // 모든 변명을 가져옵니다. (날짜별로 점을 찍기 위해)
    val uiState: StateFlow<CalendarUiState> = repository.getAllExcusesStream()
        .map { CalendarUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CalendarUiState()
        )
}