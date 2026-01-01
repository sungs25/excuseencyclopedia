package com.example.excuseencyclopedia.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.data.PreferenceManager
import com.example.excuseencyclopedia.ui.AppViewModelProvider
import java.time.format.DateTimeFormatter
import java.util.Locale

// 색상 정의
val PurpleMain = Color(0xFF6C63FF)
val GrayBackground = Color(0xFFF6F7F9)

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // 구독 여부 관리
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var isPremium by remember { mutableStateOf(prefs.isPremium) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBackground)
    ) {

        // --- 1. 통계 내용 (구독 안 하면 흐리게) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
                .then(if (!isPremium) Modifier.blur(15.dp) else Modifier),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "변명 분석",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // ▼▼▼ [복구됨] 날짜 이동 화살표 (< 202X년 X월 >) ▼▼▼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이전 달 버튼
                IconButton(
                    onClick = {
                        viewModel.updateDate(uiState.selectedDate.minusMonths(1))
                    },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "이전 달", tint = Color.Gray)
                }

                // 현재 날짜 표시 (예: 2026년 01월)
                Text(
                    text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월")),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // 다음 달 버튼
                IconButton(
                    onClick = {
                        viewModel.updateDate(uiState.selectedDate.plusMonths(1))
                    },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "다음 달", tint = Color.Gray)
                }
            }
            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

            // (1) 이번 달 총 횟수
            StatCard(
                title = "이번 달 기록",
                value = "${uiState.monthlyCount}회",
                color = PurpleMain
            )

            // (2) 이번 달 최다 카테고리
            StatCard(
                title = "주로 댄 핑계",
                value = uiState.monthlyTopCategory,
                color = Color(0xFF5C6BC0)
            )

            // (3) 이번 달 평균 점수
            StatCard(
                title = "평균 뻔뻔함",
                value = String.format(Locale.getDefault(), "%.1f점", uiState.monthlyAverage),
                color = Color(0xFFEF5350)
            )

            // (4) 전체 누적
            StatCard(
                title = "지금까지 총 누적",
                value = "${uiState.totalCount}회",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // --- 2. 잠금 화면 (구독 유도) ---
        if (!isPremium) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(PurpleMain.copy(alpha = 0.1f), RoundedCornerShape(30.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(32.dp))
                        }

                        Text("통계 기능 잠금", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        Text(
                            "나의 변명 패턴을 분석하려면\n구독이 필요합니다.",
                            fontSize = 15.sp, color = Color.Gray, textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                prefs.isPremium = true
                                isPremium = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("월 3,000원에 구독하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Text("광고 제거 + 통계 무제한", fontSize = 12.sp, color = PurpleMain, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
    }
}