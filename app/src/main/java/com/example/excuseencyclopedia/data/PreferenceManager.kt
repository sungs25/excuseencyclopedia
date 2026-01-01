package com.example.excuseencyclopedia.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("excuse_prefs", Context.MODE_PRIVATE)

    // 1. 기록 횟수 저장 (0, 1, 2 반복)
    var saveCount: Int
        get() = prefs.getInt("save_count", 0)
        set(value) = prefs.edit().putInt("save_count", value).apply()

    // 2. 구독 여부 (true면 프리미엄)
    // 실제 앱에서는 결제 서버와 연동하지만, 지금은 테스트용으로 기기에 저장합니다.
    var isPremium: Boolean
        get() = prefs.getBoolean("is_premium", false)
        set(value) = prefs.edit().putBoolean("is_premium", value).apply()

    // 횟수 1 증가시키고, 3이 되면 true 반환 (광고 띄워라!)
    fun shouldShowAd(): Boolean {
        if (isPremium) return false // 프리미엄이면 광고 안 봄

        var current = saveCount
        current++

        if (current >= 3) {
            saveCount = 0 // 초기화
            return true // 광고 띄우세요!
        } else {
            saveCount = current
            return false // 아직 아님
        }
    }

    var editCount: Int
        get() = prefs.getInt("edit_count", 0)
        set(value) = prefs.edit().putInt("edit_count", value).apply()
}