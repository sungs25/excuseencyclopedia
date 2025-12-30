package com.example.excuseencyclopedia.ui.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.excuseencyclopedia.data.ExcuseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val totalCount: Int = 0,
    val averageScore: Double = 0.0,
    val topCategory: String = "데이터 없음",
    val topCategoryCount: Int = 0
)

class StatsViewModel(repository: ExcuseRepository) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = repository.getAllExcusesStream()
        .map { list ->
            if (list.isEmpty()) {
                StatsUiState() // 데이터 없으면 기본값(0) 반환
            } else {
                // 1. 총 개수
                val total = list.size

                // 2. 평균 점수 (소수점 첫째 자리까지만 반올림은 UI에서 처리)
                val avg = list.map { it.score }.average()

                // 3. 최다 카테고리 찾기 (코틀린의 강력한 기능!)
                // 리스트 -> 카테고리별로 묶기 -> 개수 세기 -> 제일 큰 놈 찾기
                val topCat = list.groupingBy { it.category }
                    .eachCount()
                    .maxByOrNull { it.value }

                StatsUiState(
                    totalCount = total,
                    averageScore = avg,
                    topCategory = topCat?.key ?: "없음",
                    topCategoryCount = topCat?.value ?: 0
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = StatsUiState()
        )
}