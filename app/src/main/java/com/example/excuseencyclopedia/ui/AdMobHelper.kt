package com.example.excuseencyclopedia.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdMobHelper(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null

    // 광고 로딩하기
    fun loadAd() {
        val adRequest = AdRequest.Builder().build()

        // 실제 배포 시에는 아래 "ca-app-pub..." 부분을 본인의 '실제 전면광고 ID'로 교체해야 합니다!
        // (지금은 테스트 ID 그대로 두셔도 됩니다)
        InterstitialAd.load(context, "ca-app-pub-3940256099942544/1033173712", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    Log.d("AdMobHelper", "광고 로드 실패: ${adError.message}")
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d("AdMobHelper", "광고 로드 성공")
                }
            })
    }

    // 광고가 준비되었는지 확인 (ItemEntryScreen 로직용)
    fun isAdLoaded(): Boolean {
        return interstitialAd != null
    }

    // 광고 보여주기
    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // 광고를 닫았을 때
                    interstitialAd = null
                    loadAd() // 다음 광고 미리 로드
                    onAdDismissed() // 화면 이동
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // 광고 띄우기 실패했을 때
                    interstitialAd = null
                    onAdDismissed() // 그래도 화면 이동은 시켜줌
                }
            }
            interstitialAd?.show(activity)
        } else {
            // 광고가 로드 안 된 상태라면 그냥 통과
            onAdDismissed()
            loadAd() // 다시 로드 시도
        }
    }
}