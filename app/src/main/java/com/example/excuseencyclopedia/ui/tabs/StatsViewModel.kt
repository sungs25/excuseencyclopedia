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

// 그래프 데이터 클래스
data class CategoryStat(
    val name: String,
    val count: Int,
    val percentage: Float
)

data class StatsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val monthlyCount: Int = 0,
    val monthlyAverage: Double = 0.0,
    val monthlyTopCategory: String = "-",
    val totalCount: Int = 0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val userTitle: String = "핑계 신생아" // 기본 칭호
)

class StatsViewModel(val repository: ExcuseRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAllExcusesStream(),
        _selectedDate
    ) { excuseList, selectedDate ->

        val total = excuseList.size
        val currentMonthStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val monthlyList = excuseList.filter { it.date.startsWith(currentMonthStr) }

        if (monthlyList.isEmpty()) {
            StatsUiState(
                selectedDate = selectedDate,
                totalCount = total,
                userTitle = "🥚 핑계 신생아" // 데이터 없을 때
            )
        } else {
            val avg = monthlyList.map { it.score }.average()
            val count = monthlyList.size

            // 1. 카테고리 통계 계산
            val categoryMap = monthlyList.groupingBy { it.category }.eachCount()

            val catStats = categoryMap.map { (name, cnt) ->
                CategoryStat(name, cnt, cnt.toFloat() / count)
            }.sortedByDescending { it.count }

            val topCat = catStats.firstOrNull()?.name ?: "-"

            // ★ 2. [업데이트됨] 진화하는 핑계러 칭호 로직
            val title = when {
                count >= 60 -> "👴 전설의 핑계 깎는 노인"
                count >= 50 -> "🤖 핑계 자판기"
                count >= 40 -> "💨 숨 쉬듯 핑계"
                count >= 30 -> "🧠 논리 창조가"
                count >= 20 -> "✨ 임기응변 유망주"
                count >= 10 -> "🚪 입문 핑계러"
                count >= 5 -> "🌱 귀여운 핑계 새싹"
                else -> "🥚 핑계 신생아" // 5회 미만
            }

            StatsUiState(
                selectedDate = selectedDate,
                monthlyCount = count,
                monthlyAverage = avg,
                monthlyTopCategory = topCat,
                totalCount = total,
                categoryStats = catStats,
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