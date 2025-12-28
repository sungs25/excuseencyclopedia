package com.example.excuseencyclopedia.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. entities: 우리가 만든 변명(Excuse) 설계도를 여기에 등록합니다.
// version: 나중에 앱 업데이트해서 DB 구조를 바꿀 때 숫자를 올립니다.
@Database(entities = [Excuse::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 2. 아까 만든 도구(DAO)를 꺼내 쓸 수 있게 해줍니다.
    abstract fun excuseDao(): ExcuseDao

    companion object {
        // 3. 싱글톤 패턴 (Singleton)
        // 앱 전체에서 DB 문은 딱 하나만 열어두겠다는 뜻입니다.
        // 여러 군데서 동시에 문을 열면 데이터가 꼬일 수 있거든요.
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // 만약 이미 만들어진 DB가 있다면 그걸 반환하고,
            // 없다면 새로 만듭니다. (synchronized: 여러 명이 동시에 못 들어오게 잠금)
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "excuse_database" // 폰에 저장될 실제 파일 이름
                )
                    .build()
                    .also { Instance = it }
            }
        }
    }
}