package com.example.excuseencyclopedia.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.excuseencyclopedia.ExcuseApplication
import com.example.excuseencyclopedia.ui.home.HomeViewModel
import com.example.excuseencyclopedia.ui.item.ItemEntryViewModel
import com.example.excuseencyclopedia.ui.tabs.CalendarViewModel

// 뷰모델을 만들어주는 공장 객체.
object AppViewModelProvider {
    val Factory = viewModelFactory {

        // HomeViewModel을 만드는 법을 등록
        initializer {
            HomeViewModel(
                // application()을 통해 리포지토리를 꺼내와서 넣어
                excuseApplication().repository
            )
        }

        // 나중에 다른 뷰모델도 여기에 추가
        initializer {
            ItemEntryViewModel(excuseApplication().repository)
        }

        initializer {
            CalendarViewModel(excuseApplication().repository)
        }
    }
}

fun CreationExtras.excuseApplication(): ExcuseApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as ExcuseApplication)