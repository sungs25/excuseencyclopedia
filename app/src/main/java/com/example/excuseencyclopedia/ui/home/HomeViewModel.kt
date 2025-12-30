package com.example.excuseencyclopedia.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.excuseencyclopedia.data.Excuse
import com.example.excuseencyclopedia.data.ExcuseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 1. 화면에 보여줄 데이터 상태 (State) 정의
// 리스트가 비어있는지, 로딩 중인지 등을 표현하는 데이터 클래스입니다.
data class HomeUiState(
    val excuseList: List<Excuse> = listOf()
)

// 2. 뷰모델 클래스
class HomeViewModel(private val repository: ExcuseRepository) : ViewModel() {

    // 3. UI 상태 (StateFlow)
    // 리포지토리에서 데이터를 계속 관찰(Flow)하다가,
    // 데이터가 바뀌면 HomeUiState로 변환해서 화면에 뿌려줍니다.
    val homeUiState: StateFlow<HomeUiState> = repository.getAllExcusesStream()
        .map { HomeUiState(it) } // 가져온 리스트를 UI 상태로 포장
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L), // 화면이 꺼져도 5초간은 데이터 유지
            initialValue = HomeUiState() // 초기값 (빈 리스트)
        )
    fun deleteExcuse(excuse: Excuse) {
        viewModelScope.launch {
            repository.deleteExcuse(excuse)
        }
    }
}