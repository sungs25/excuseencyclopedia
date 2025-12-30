package com.example.excuseencyclopedia.ui.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.excuseencyclopedia.data.ExcuseRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: ExcuseRepository) : ViewModel() {

    // 모든 데이터 삭제 함수
    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllExcuses()
        }
    }
}