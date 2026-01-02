package com.example.excuseencyclopedia.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory

// 인앱 리뷰를 띄워주는 도우미 함수 (디버깅 버전)
fun showInAppReview(context: Context) {
    val activity = context as? Activity ?: return
    val manager = ReviewManagerFactory.create(context)

    // 1. 리뷰 정보 요청
    val request = manager.requestReviewFlow()

    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // 2. 정보 수신 성공
            Toast.makeText(context, "리뷰 요청 성공! (팝업 대기 중...)", Toast.LENGTH_SHORT).show()

            val reviewInfo = task.result
            val flow = manager.launchReviewFlow(activity, reviewInfo)

            flow.addOnCompleteListener { _ ->
                // 3. 리뷰 창 닫힘 (성공했든, 취소했든, 안 떴든 이리로 옴)
                Toast.makeText(context, "리뷰 흐름 종료됨", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 4. 오류 발생 (이유 출력)
            val errorMsg = task.exception?.message ?: "알 수 없는 오류"
            Log.e("ReviewHelper", "Review failed: $errorMsg") // 로그캣에도 출력
            Toast.makeText(context, "리뷰 요청 실패: $errorMsg", Toast.LENGTH_LONG).show()
        }
    }
}