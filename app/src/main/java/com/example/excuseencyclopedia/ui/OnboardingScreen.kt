package com.example.excuseencyclopedia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.excuseencyclopedia.data.PreferenceManager
import com.example.excuseencyclopedia.ui.tabs.PurpleMain
import kotlinx.coroutines.launch

// 온보딩 페이지 데이터 모델
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    // 페이지 데이터 정의
    val pages = listOf(
        OnboardingPage(
            "할 일을 미루셨나요?",
            "괜찮습니다. 죄책감은 넣어두세요.\n우리에겐 '그럴듯한 변명'이 필요할 뿐입니다.",
            Icons.Default.Face
        ),
        OnboardingPage(
            "변명을 기록하고 수집하세요",
            "오늘 안 한 일과 이유를 적어보세요.",
            Icons.Default.Create
        ),
        OnboardingPage(
            "당신의 뻔뻔함을 증명하세요",
            "통계를 통해 나의 핑계 패턴을 분석하고\n'전설의 핑계왕' 칭호를 획득하세요!",
            Icons.Default.Star
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // [상단] 건너뛰기 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (pagerState.currentPage < pages.size - 1) {
                TextButton(onClick = {
                    // 건너뛰면 바로 완료 처리
                    prefs.isFirstRun = false
                    onFinished()
                }) {
                    Text("건너뛰기", color = Color.Gray)
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp)) // 자리 차지용
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // [중앙] 슬라이드 내용 (Pager)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }

        Spacer(modifier = Modifier.weight(1f))

        // [하단] 인디케이터 (점 3개)
        Row(
            modifier = Modifier.height(50.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) PurpleMain else Color.LightGray
                val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(8.dp)
                        .width(width) // 선택된 건 길게
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // [하단] 다음 / 시작하기 버튼
        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    // 다음 페이지로 슬라이드
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    // 마지막 페이지면 완료 처리
                    prefs.isFirstRun = false
                    onFinished()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain)
        ) {
            Text(
                text = if (pagerState.currentPage == pages.size - 1) "핑계 기록 시작하기" else "다음",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 아이콘을 감싸는 원형 배경
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(PurpleMain.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = PurpleMain,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}