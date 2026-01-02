package com.example.excuseencyclopedia.ui.tabs

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.excuseencyclopedia.data.Excuse
import com.example.excuseencyclopedia.data.ExcuseRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsViewModel(private val repository: ExcuseRepository) : ViewModel() {

    // 1. 데이터 내보내기 (백업)
    fun backupData(context: Context, uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                // DB에서 모든 데이터 가져오기 (Flow를 1회성 리스트로 받음)
                val excuseList = repository.getAllExcusesStream().first()

                // 데이터를 JSON 문자열로 변환
                val gson = Gson()
                val jsonString = gson.toJson(excuseList)

                // 파일에 쓰기
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
    }

    // 2. 데이터 가져오기 (복원)
    fun restoreData(context: Context, uri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                // 파일 읽기
                val stringBuilder = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line)
                            line = reader.readLine()
                        }
                    }
                }

                // JSON을 데이터 리스트로 변환
                val jsonString = stringBuilder.toString()
                val gson = Gson()
                val listType = object : TypeToken<List<Excuse>>() {}.type
                val excuseList: List<Excuse> = gson.fromJson(jsonString, listType)

                // DB에 넣기 (기존 데이터 유지하면서 추가 or 덮어쓰기 전략)
                // 여기서는 간단하게 반복문으로 insert (ID가 같으면 무시됨)
                excuseList.forEach { excuse ->
                    repository.insertExcuse(excuse)
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
    }

    // 데이터 전체 삭제 (초기화용)
    fun clearAllData() {
        viewModelScope.launch {
            // Repository에 deleteAll 함수가 없다면 DAO에 추가 필요 (지금은 반복문으로 삭제)
            val list = repository.getAllExcusesStream().first()
            list.forEach { repository.deleteExcuse(it) }
        }
    }
}