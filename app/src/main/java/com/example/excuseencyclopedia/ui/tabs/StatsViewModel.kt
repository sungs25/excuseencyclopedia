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

// 1. 데이터 클래스 정의 (차트 & 워드클라우드용)
data class CategoryStat(
    val name: String,
    val count: Int,
    val percentage: Float
)

data class MonthlyTrend(
    val month: String,
    val count: Int
)

data class WordFrequency(
    val word: String,
    val count: Int
)

data class StatsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val monthlyCount: Int = 0,
    val monthlyAverage: Double = 0.0,
    val monthlyTopCategory: String = "-",
    val totalCount: Int = 0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val userTitle: String = "🥚 핑계 신생아",
    // ★ 추가된 데이터 필드
    val monthlyTrend: List<MonthlyTrend> = emptyList(),
    val frequentWords: List<WordFrequency> = emptyList()
)

class StatsViewModel(val repository: ExcuseRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAllExcusesStream(),
        _selectedDate
    ) { excuseList, selectedDate ->

        // 전체 누적 수
        val total = excuseList.size

        // 이번 달 데이터 필터링
        val currentMonthStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val monthlyList = excuseList.filter { it.date.startsWith(currentMonthStr) }

        // --- 데이터가 없을 때 기본값 처리 ---
        if (monthlyList.isEmpty() && total == 0) {
            StatsUiState(selectedDate = selectedDate)
        } else {
            // ==========================================
            // 1. 기본 월간 통계 (횟수, 평균 점수)
            // ==========================================
            val count = monthlyList.size
            val avg = if (count > 0) monthlyList.map { it.score }.average() else 0.0

            // ==========================================
            // 2. 카테고리 분석
            // ==========================================
            val categoryMap = monthlyList.groupingBy { it.category }.eachCount()
            val catStats = categoryMap.map { (name, cnt) ->
                CategoryStat(name, cnt, if (count > 0) cnt.toFloat() / count else 0f)
            }.sortedByDescending { it.count }

            val topCat = catStats.firstOrNull()?.name ?: "-"

            // ==========================================
            // 3. 진화하는 핑계러 칭호 (누적 횟수 기준이 아닌 월간 활동량 기준 등 변경 가능)
            // ==========================================
            // (여기서는 '월간 횟수'를 기준으로 할지, '누적 횟수'를 기준으로 할지 결정해야 함.
            //  보통 칭호는 누적으로 주는 게 좋지만, 코드 상 monthlyCount를 쓰고 계셨음.
            //  일단 monthlyCount 기준으로 유지하되, 필요시 total로 변경하세요.)
            val title = when {
                count >= 30 -> "👴 전설의 핑계 깎는 노인" // 하루 1번 꼴
                count >= 20 -> "🤖 핑계 자판기"
                count >= 15 -> "💨 숨 쉬듯 핑계"
                count >= 10 -> "🧠 논리 창조가"
                count >= 5 -> "✨ 임기응변 유망주"
                count >= 2 -> "🌱 귀여운 핑계 새싹"
                else -> "🥚 핑계 신생아"
            }

            // ==========================================
            // 4. [NEW] 월별 추이 (막대 차트)
            // ==========================================
            // 선택된 날짜 기준으로 과거 6개월치 데이터 생성
            val trendList = (0..5).map { i ->
                val targetMonth = selectedDate.minusMonths(5L - i)
                val targetMonthStr = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val targetCount = excuseList.count { it.date.startsWith(targetMonthStr) }

                MonthlyTrend(
                    month = targetMonth.format(DateTimeFormatter.ofPattern("M월")),
                    count = targetCount
                )
            }

            // ==========================================
            // 5. [NEW] 워드 클라우드 (단어 빈도 분석)
            // ==========================================
            // 변명 내용(Reason)과 할 일(Task)을 모두 합쳐서 분석
            val allText = monthlyList.joinToString(" ") { "${it.task} ${it.reason}" }

            // 공백 및 특수문자로 분리 -> 2글자 이상만 필터링 -> 카운팅 -> 정렬 -> 상위 15개
            val wordList = allText.split(Regex("[\\s.,!?\"'()]+")) // 특수문자 제거하며 쪼개기
                .filter { it.length >= 2 } // 1글자짜리(은,는,이,가 등) 제외
                .filter { it !in listOf("너무", "진짜", "그냥", "하고", "해서") } // 불용어(Stopwords) 필터링
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(15) // 상위 15개만
                .map { WordFrequency(it.key, it.value) }

            // 최종 UI State 반환
            StatsUiState(
                selectedDate = selectedDate,
                monthlyCount = count,
                monthlyAverage = avg,
                monthlyTopCategory = topCat,
                totalCount = total,
                categoryStats = catStats,
                userTitle = title,
                monthlyTrend = trendList,     // 추가됨
                frequentWords = wordList      // 추가됨
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