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

    // 광고 미리 로딩하기
    fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        // 구글 제공 테스트 전면광고 ID
        InterstitialAd.load(context, "ca-app-pub-3940256099942544/1033173712", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
            })
    }

    // 광고 보여주기
    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // 광고 닫으면 할 일 (화면 이동)
                    interstitialAd = null
                    loadAd() // 다음을 위해 미리 로드
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    onAdDismissed() // 실패해도 이동은 시켜줘야 함
                }
            }
            interstitialAd?.show(activity)
        } else {
            // 광고가 아직 안 불러와졌으면 그냥 이동
            onAdDismissed()
            loadAd()
        }
    }
}