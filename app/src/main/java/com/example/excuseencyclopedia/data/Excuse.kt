package com.example.excuseencyclopedia.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity: 이 클래스가 데이터베이스의 테이블이 된다는 뜻입니다.
@Entity(tableName = "excuse_table")
data class Excuse(
    // @PrimaryKey: 주민등록번호처럼 각 변명을 구분하는 고유 ID입니다.
    // autoGenerate = true: 우리가 신경 안 써도 앱이 알아서 1, 2, 3... 번호를 매겨줍니다.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,    // 날짜 (예: "2025-12-28")
    val task: String,    // 안 한 일 (예: "영어 공부")
    val reason: String,  // 변명 내용 (예: "넷플릭스가 나를 유혹했다")
    val category: String, // 카테고리 (예: "유혹")
    val score: Int       // 뻔뻔함 점수 (1~5)
)