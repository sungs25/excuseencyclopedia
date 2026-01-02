package com.example.excuseencyclopedia.ui

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

// 인앱 리뷰를 띄워주는 도우미 함수 (배포용 Clean 버전)
fun showInAppReview(context: Context) {
    val activity = context as? Activity ?: return
    val manager = ReviewManagerFactory.create(context)

    // 1. 리뷰 정보 요청
    val request = manager.requestReviewFlow()

    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // 2. 정보 수신 성공 시, 실제 리뷰 팝업 띄우기
            val reviewInfo = task.result
            val flow = manager.launchReviewFlow(activity, reviewInfo)

            flow.addOnCompleteListener { _ ->
                // 리뷰 작성 완료 또는 닫음 (별도 처리 불필요)
            }
        } else {
            // 오류 발생 시 사용자에게 알리지 않고 조용히 넘어감
        }
    }
}