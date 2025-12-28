package com.example.excuseencyclopedia.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExcuseDao {

    // 1. 변명 추가하기 (Create)
    // OnConflictStrategy.IGNORE: 만약 똑같은 ID가 들어오면 무시해라 (충돌 방지)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExcuse(excuse: Excuse)

    // 2. 변명 수정하기 (Update)
    @Update
    suspend fun updateExcuse(excuse: Excuse)

    // 3. 변명 삭제하기 (Delete)
    @Delete
    suspend fun deleteExcuse(excuse: Excuse)

    // 4. 전체 목록 가져오기 (Read - All)
    // 날짜 최신순(DESC)으로 정렬해서 가져옵니다.
    // Flow: 데이터가 바뀌면 화면도 자동으로 바뀌게 해주는 '파이프'입니다.
    @Query("SELECT * from excuse_table ORDER BY date DESC")
    fun getAllExcuses(): Flow<List<Excuse>>

    // 5. 특정 날짜의 변명만 가져오기 (Read - Date)
    @Query("SELECT * from excuse_table WHERE date = :date")
    fun getExcusesByDate(date: String): Flow<List<Excuse>>
}