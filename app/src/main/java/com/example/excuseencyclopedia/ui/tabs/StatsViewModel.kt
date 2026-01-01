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

// 그래프를 그리기 위한 데이터 클래스 추가
data class CategoryStat(
    val name: String,
    val count: Int,
    val percentage: Float // 0.0 ~ 1.0 (그래프 길이용)
)

data class StatsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val monthlyCount: Int = 0,
    val monthlyAverage: Double = 0.0,
    val monthlyTopCategory: String = "-",
    val totalCount: Int = 0,
    // ★ 추가됨: 카테고리별 통계 리스트 (그래프용)
    val categoryStats: List<CategoryStat> = emptyList(),
    // ★ 추가됨: 이달의 칭호 (재미 요소)
    val userTitle: String = "데이터 부족"
)

class StatsViewModel(private val repository: ExcuseRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAllExcusesStream(),
        _selectedDate
    ) { excuseList, selectedDate ->

        val total = excuseList.size
        val currentMonthStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val monthlyList = excuseList.filter { it.date.startsWith(currentMonthStr) }

        if (monthlyList.isEmpty()) {
            StatsUiState(selectedDate = selectedDate, totalCount = total)
        } else {
            val avg = monthlyList.map { it.score }.average()

            // 1. 카테고리 통계 상세 계산
            val categoryMap = monthlyList.groupingBy { it.category }.eachCount()
            val totalMonthly = monthlyList.size

            // 맵을 리스트로 변환하고 비율 계산 (많은 순 정렬)
            val catStats = categoryMap.map { (name, count) ->
                CategoryStat(name, count, count.toFloat() / totalMonthly)
            }.sortedByDescending { it.count }

            val topCat = catStats.firstOrNull()?.name ?: "-"

            // 2. 재미있는 칭호 부여 로직
            val title = when {
                monthlyList.size >= 10 && avg >= 4.0 -> "👑 전설의 혓바닥"
                monthlyList.size >= 10 -> "🏃 프로 도망러"
                avg >= 4.5 -> "🛡️ 철면피 마스터"
                avg <= 2.0 -> "🥺 소심한 핑계쟁이"
                monthlyList.size <= 3 -> "🌱 성실한 새싹"
                else -> "🤔 평범한 일반인"
            }

            StatsUiState(
                selectedDate = selectedDate,
                monthlyCount = monthlyList.size,
                monthlyAverage = avg,
                monthlyTopCategory = topCat,
                totalCount = total,
                categoryStats = catStats, // 리스트 전달
                userTitle = title
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = StatsUiState()
        )

    fun updateDate(newDate: LocalDate) {
        _selectedDate.value = newDate
    }
}