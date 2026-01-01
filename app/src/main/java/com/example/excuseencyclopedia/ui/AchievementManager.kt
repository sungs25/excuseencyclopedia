package com.example.excuseencyclopedia.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.excuseencyclopedia.data.Excuse // ★ 여기를 Excuse로 수정!
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 1. 업적 데이터 모델 (그대로 유지)
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isHidden: Boolean = false,
    val isUnlocked: Boolean = false
)

// 2. 업적 판독기
object AchievementManager {

    // ★ 여기 입력 타입을 List<Excuse>로 변경했습니다!
    fun calculateAchievements(excuses: List<Excuse>, editCount: Int): List<Achievement> {
        val achievements = mutableListOf<Achievement>()

        // --- (1) 시간 & 요일 관련 ---

        // 월요병 (월요일)
        val hasMonday = excuses.any {
            parseDate(it.date).dayOfWeek == DayOfWeek.MONDAY
        }
        achievements.add(Achievement(
            "monday", "월요병 말기 환자", "일주일의 시작은 역시 핑계와 함께!", Icons.Default.DateRange, isUnlocked = hasMonday
        ))

        // 불금 (금요일)
        val hasFriday = excuses.any {
            parseDate(it.date).dayOfWeek == DayOfWeek.FRIDAY
        }
        achievements.add(Achievement(
            "friday", "불금엔 칼퇴각", "이 시간엔 누구도 나를 막을 수 없다.", Icons.Default.ExitToApp, isUnlocked = hasFriday
        ))

        // 작심삼일 (연속 3일)
        val sortedDates = excuses.map { parseDate(it.date) }.distinct().sorted()
        var consecutiveDays = 1
        var maxConsecutive = 1
        if (sortedDates.isNotEmpty()) {
            for (i in 0 until sortedDates.size - 1) {
                if (sortedDates[i].plusDays(1) == sortedDates[i+1]) {
                    consecutiveDays++
                    if (consecutiveDays > maxConsecutive) maxConsecutive = consecutiveDays
                } else {
                    consecutiveDays = 1
                }
            }
        }
        achievements.add(Achievement(
            "3days", "작심삼일 마스터", "3일 연속 핑계라니, 끈기가 대단하군요!", Icons.Default.Refresh, isUnlocked = maxConsecutive >= 3
        ))


        // --- (2) 스타일 분석형 ---

        // 구구절절 (100자 이상)
        val hasLong = excuses.any { it.reason.length >= 100 }
        achievements.add(Achievement(
            "long_text", "구구절절 사연가", "핑계에 기승전결이 완벽합니다. 소설가세요?", Icons.Default.Edit, isUnlocked = hasLong
        ))

        // 귀차니즘 (5글자 이하)
        val hasShort = excuses.any { it.reason.length <= 5 && it.reason.isNotBlank() }
        achievements.add(Achievement(
            "short_text", "귀차니즘의 정수", "긴말 필요 없죠. 인정합니다.", Icons.Default.ThumbUp, isUnlocked = hasShort
        ))


        // --- (3) 도감 수집형 ---

        // 외길 인생 (한 카테고리 10회 이상)
        val categoryCounts = excuses.groupingBy { it.category }.eachCount()
        val oneWay = categoryCounts.any { it.value >= 10 }
        achievements.add(Achievement(
            "one_way", "외길 인생", "한 우물만 파는 당신, 핑계 장인입니다.", Icons.Default.LocationOn, isUnlocked = oneWay
        ))

        // 첫 번째 흑역사
        achievements.add(Achievement(
            "first_step", "첫 번째 흑역사", "핑계 도감의 첫 페이지가 작성되었습니다.", Icons.Default.Star, isUnlocked = excuses.isNotEmpty()
        ))


        // --- (4) 히든 / 이스터에그 ---

        // 비가 와서 그랬어
        val rainCount = excuses.count {
            it.reason.contains("비") || it.reason.contains("날씨") || it.reason.contains("우산")
        }

        achievements.add(Achievement(
            "rain",
            "비가 와서 그랬어",
            "날씨 탓은 국룰이죠. 하늘도 당신 편입니다.",
            // ★ [수정됨] WaterDrop -> Info (기본 아이콘으로 변경)
            Icons.Default.Info,
            isHidden = true,
            isUnlocked = rainCount >= 1
        ))

        // 내일부터 진짜 함
        val tmrCount = excuses.count { it.reason.contains("내일") }
        achievements.add(Achievement(
            "tomorrow", "내일부터 진짜 함", "원래 다이어트와 공부는 내일부터 하는 겁니다.", Icons.Default.ArrowForward,
            isHidden = true, isUnlocked = tmrCount >= 1
        ))

        // 777 잭팟 (테스트용 10회)
        achievements.add(Achievement(
            "jackpot", "777 잭팟", "핑계도 대다 보면 행운이 옵니다.", Icons.Default.Favorite,
            isHidden = true, isUnlocked = excuses.size >= 777
        ))

        return achievements
    }

    private fun parseDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
}