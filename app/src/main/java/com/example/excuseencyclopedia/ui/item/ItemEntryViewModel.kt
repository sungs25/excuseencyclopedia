package com.example.excuseencyclopedia.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.excuseencyclopedia.data.Excuse
import com.example.excuseencyclopedia.data.ExcuseRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1. 화면에 입력된 데이터를 담는 그릇 (State)
data class ItemUiState(
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()), // <-- 추가됨!
    val task: String = "",
    val reason: String = "",
    val category: String = "기타",
    val score: Float = 3f,
    val isEntryValid: Boolean = false
)

// 2. 입력 데이터를 DB용 데이터(Excuse)로 바꾸는 변환기 (확장 함수)
fun ItemUiState.toExcuse(): Excuse {
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    return Excuse(
        date = date,
        task = task,
        reason = reason,
        category = category,
        score = score.toInt()
    )
}

class ItemEntryViewModel(private val repository: ExcuseRepository) : ViewModel() {

    // 화면 상태 관리 (사용자가 글자를 칠 때마다 여기가 업데이트됨)
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    // 사용자 입력 업데이트 함수
    fun updateUiState(newItemUiState: ItemUiState) {
        itemUiState = newItemUiState.copy(
            // 할 일과 변명 내용이 비어있지 않아야 저장 버튼 활성화
            isEntryValid = newItemUiState.task.isNotBlank() && newItemUiState.reason.isNotBlank()
        )
    }

    // 저장하기 함수
    suspend fun saveItem() {
        if (itemUiState.isEntryValid) {
            repository.insertExcuse(itemUiState.toExcuse())
        }
    }
}