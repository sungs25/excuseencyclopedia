package com.example.excuseencyclopedia.data

import kotlinx.coroutines.flow.Flow

interface ExcuseRepository {
    // 1. 모든 변명 가져오기 (Flow: 데이터가 변하면 자동 갱신)
    fun getAllExcusesStream(): Flow<List<Excuse>>

    // 2. 특정 날짜 변명 가져오기
    fun getExcusesByDateStream(date: String): Flow<List<Excuse>>

    // 3. 변명 추가하기 (suspend: 오래 걸리니 코루틴으로 실행)
    suspend fun insertExcuse(excuse: Excuse)

    // 4. 변명 삭제하기
    suspend fun deleteExcuse(excuse: Excuse)

    // 5. 변명 수정하기
    suspend fun updateExcuse(excuse: Excuse)

    //6. 모든 변명 삭제하기
    suspend fun deleteAllExcuses()
}