package com.example.excuseencyclopedia

import android.app.Application
import com.example.excuseencyclopedia.data.AppDatabase
import com.example.excuseencyclopedia.data.OfflineExcuseRepository

class ExcuseApplication : Application() {
    // 1. 데이터베이스 인스턴스 생성 (lazy: 실제 필요할 때 만들어짐)
    private val database by lazy { AppDatabase.getDatabase(this) }

    // 2. 리포지토리 인스턴스 생성
    val repository by lazy { OfflineExcuseRepository(database.excuseDao()) }
}