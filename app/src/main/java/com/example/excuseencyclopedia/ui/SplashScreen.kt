package com.example.excuseencyclopedia.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape // ★ CircleShape import
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip // ★ clip import
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.excuseencyclopedia.R
import com.example.excuseencyclopedia.ui.tabs.PurpleMain
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // 애니메이션 상태 (크기 0 -> 1, 투명도 0 -> 1)
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // 1. 애니메이션 시작 (0.8초)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )

        // 2. 잠시 대기 (1.2초)
        delay(500)

        // 3. 다음 화면 이동
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleMain), // 전체 화면 배경색 (보라색)
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ★ [수정됨] 이미지를 감싸는 Box를 원형으로 자름
            Box(
                modifier = Modifier
                    .size(120.dp) // 로고 크기 설정
                    .scale(scale.value) // 커지는 애니메이션 적용
                    .clip(CircleShape) // ★ 이미지를 원형으로 자름
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize() // Box 크기에 맞춤
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 앱 이름
            Text(
                text = "변명 도감",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 슬로건
            Text(
                text = "뻔뻔한 당신을 위한 기록소",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.alpha(alpha.value)
            )
        }

        // 하단 저작권 표시
        Text(
            text = "© 2026 Excuse Encyclopedia",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
        )
    }
}