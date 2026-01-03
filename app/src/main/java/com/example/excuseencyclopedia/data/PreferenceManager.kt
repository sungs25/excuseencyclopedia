package com.example.excuseencyclopedia.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("excuse_prefs", Context.MODE_PRIVATE)

    // 1. 기록 횟수 저장 (광고용 카운트)
    var saveCount: Int
        get() = prefs.getInt("save_count", 0)
        set(value) = prefs.edit().putInt("save_count", value).apply()

    // 리뷰 유도용 누적 카운트
    var totalSaveCount: Int
        get() = prefs.getInt("total_save_count", 0)
        set(value) = prefs.edit().putInt("total_save_count", value).apply()

    // 리뷰 요청 여부 (true면 이미 요청함)
    var isReviewRequested: Boolean
        get() = prefs.getBoolean("is_review_requested", false)
        set(value) = prefs.edit().putBoolean("is_review_requested", value).apply()

    // 2. 구독 여부 (true면 프리미엄)
    var isPremium: Boolean
        get() = prefs.getBoolean("is_premium", false)
        set(value) = prefs.edit().putBoolean("is_premium", value).apply()

    // ▼▼▼ [수정된 부분] 기존 shouldShowAd를 지우고 아래 2개 함수로 교체 ▼▼▼

    // (1) 카운트를 1 올리고, 3회 이상인지 '확인'만 하는 함수
    // ★ 주의: 여기서 0으로 초기화하지 않습니다! (기회를 보존하기 위해)
    fun checkAdCount(): Boolean {
        if (isPremium) return false // 프리미엄이면 광고 대상 아님

        var current = saveCount
        current++

        // 증가된 값을 저장 (0 -> 1 -> 2 -> 3 -> 4...)
        saveCount = current

        // 3회 이상이면 true 반환
        return current >= 3
    }

    // (2) 광고가 '진짜로 떴을 때' 호출해서 카운트를 0으로 초기화하는 함수
    fun resetAdCount() {
        saveCount = 0
    }

    // ▲▲▲ [수정 끝] ▲▲▲

    var editCount: Int
        get() = prefs.getInt("edit_count", 0)
        set(value) = prefs.edit().putInt("edit_count", value).apply()

    var isFirstRun: Boolean
        get() = prefs.getBoolean("is_first_run", true)
        set(value) = prefs.edit().putBoolean("is_first_run", value).apply()

    var isAlarmEnabled: Boolean
        get() = prefs.getBoolean("is_alarm_enabled", false)
        set(value) = prefs.edit().putBoolean("is_alarm_enabled", value).apply()
}