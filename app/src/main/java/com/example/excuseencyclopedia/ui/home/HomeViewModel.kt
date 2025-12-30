package com.example.excuseencyclopedia.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.excuseencyclopedia.data.Excuse
import com.example.excuseencyclopedia.data.ExcuseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HomeUiState(
    val excuseList: List<Excuse> = listOf(),
    val selectedDate: LocalDate = LocalDate.now() // 선택된 날짜 추가
)

class HomeViewModel(private val repository: ExcuseRepository) : ViewModel() {

    // 사용자가 선택한 날짜 (기본값: 오늘)
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    // DB 데이터 + 선택된 날짜 -> 합쳐서 필터링된 결과 만들기
    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.getAllExcusesStream(),
        _selectedDate
    ) { excuseList, selectedDate ->
        // 날짜 포맷 맞추기 (LocalDate -> String "2024-12-30")
        val dateString = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        // 해당 날짜의 변명만 남김
        val filteredList = excuseList.filter { it.date == dateString }

        HomeUiState(
            excuseList = filteredList,
            selectedDate = selectedDate
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeUiState()
        )

    // 날짜 변경 함수
    fun updateDate(newDate: LocalDate) {
        _selectedDate.value = newDate
    }

    // 기존 삭제 함수
    fun deleteExcuse(excuse: Excuse) {
        viewModelScope.launch {
            repository.deleteExcuse(excuse)
        }
    }
}