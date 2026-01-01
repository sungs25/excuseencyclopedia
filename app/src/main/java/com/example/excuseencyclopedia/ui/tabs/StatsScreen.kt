package com.example.excuseencyclopedia.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.data.PreferenceManager
import com.example.excuseencyclopedia.ui.AppViewModelProvider
import java.time.format.DateTimeFormatter

val PurpleMain = Color(0xFF6C63FF)
val GrayBackground = Color(0xFFF6F7F9)

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var isPremium by remember { mutableStateOf(prefs.isPremium) }

    Box(
        modifier = Modifier.fillMaxSize().background(GrayBackground)
    ) {
        // --- 1. 통계 내용 ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
                .then(if (!isPremium) Modifier.blur(15.dp) else Modifier),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 날짜 이동 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.updateDate(uiState.selectedDate.minusMonths(1)) },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "이전", tint = Color.Gray) }

                Text(
                    text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월")),
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black
                )

                IconButton(
                    onClick = { viewModel.updateDate(uiState.selectedDate.plusMonths(1)) },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                ) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "다음", tint = Color.Gray) }
            }

            // ★ 1. [이달의 리포트 카드] - 게이미피케이션 (누적 제외, 카테고리 추가)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleMain),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("이달의 핑계 레벨", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.userTitle,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // 핵심 요약 3종 (누적을 빼고 -> 최다 카테고리를 넣음)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween // 간격 균등 분배
                    ) {
                        // 1. 횟수
                        SummaryItem(
                            label = "이번 달",
                            value = "${uiState.monthlyCount}회",
                            modifier = Modifier.weight(1f)
                        )
                        // 2. 점수
                        SummaryItem(
                            label = "평균 뻔뻔함",
                            value = String.format("%.1f", uiState.monthlyAverage),
                            modifier = Modifier.weight(1f)
                        )
                        // 3. [NEW] 주력 핑계 (카테고리)
                        SummaryItem(
                            label = "주력 핑계",
                            value = uiState.monthlyTopCategory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ★ 2. [핑계 성향 분석] - 그래프
            Text("핑계 성향 분석", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (uiState.categoryStats.isEmpty()) {
                        Text("아직 데이터가 없습니다.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        uiState.categoryStats.forEach { stat ->
                            CategoryProgressRow(name = stat.name, count = stat.count, percentage = stat.percentage)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // ★ 3. [전체 누적 기록] - 하단으로 이동됨
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White), // 흰색 배경
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("전설의 시작부터", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("총 누적", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${uiState.totalCount}회",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurpleMain
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // --- 2. 잠금 화면 ---
        if (!isPremium) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(30.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, null, tint = PurpleMain, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("프리미엄 리포트 잠금", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "당신의 변명 패턴을 분석하고\n'이달의 칭호'를 확인하세요!",
                            textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { prefs.isPremium = true; isPremium = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("월 3,000원에 구독하기") }
                    }
                }
            }
        }
    }
}

// --- 하위 컴포넌트들 ---

@Composable
fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 카테고리 이름이 길어질 수 있으므로 maxLines와 overflow 처리
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun CategoryProgressRow(name: String, count: Int, percentage: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("${count}회 (${(percentage * 100).toInt()}%)", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = PurpleMain,
            trackColor = Color(0xFFF0F0F0),
        )
    }
}