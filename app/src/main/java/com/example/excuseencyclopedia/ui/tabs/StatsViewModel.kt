package com.example.excuseencyclopedia.ui.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.excuseencyclopedia.data.ExcuseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class StatsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    // 월간 통계
    val monthlyCount: Int = 0,
    val monthlyAverage: Double = 0.0,
    val monthlyTopCategory: String = "없음",
    // 전체 통계
    val totalCount: Int = 0
)

class StatsViewModel(private val repository: ExcuseRepository) : ViewModel() {

    // 사용자가 선택한 날짜 (월 단위 이동용)
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAllExcusesStream(),
        _selectedDate
    ) { excuseList, selectedDate ->

        // 1. 전체 개수
        val total = excuseList.size

        // 2. 이번 달 데이터 필터링
        // 예: "2024-12" 로 시작하는 날짜만 찾음
        val currentMonthStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val monthlyList = excuseList.filter { it.date.startsWith(currentMonthStr) }

        // 3. 월간 통계 계산
        if (monthlyList.isEmpty()) {
            StatsUiState(
                selectedDate = selectedDate,
                monthlyCount = 0,
                monthlyAverage = 0.0,
                monthlyTopCategory = "기록 없음",
                totalCount = total
            )
        } else {
            // 평균 점수
            val avg = monthlyList.map { it.score }.average()

            // 최다 카테고리
            val topCat = monthlyList.groupingBy { it.category }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key ?: "없음"

            StatsUiState(
                selectedDate = selectedDate,
                monthlyCount = monthlyList.size,
                monthlyAverage = avg,
                monthlyTopCategory = topCat,
                totalCount = total
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = StatsUiState()
        )

    // 달 변경 함수
    fun updateDate(newDate: LocalDate) {
        _selectedDate.value = newDate
    }
}