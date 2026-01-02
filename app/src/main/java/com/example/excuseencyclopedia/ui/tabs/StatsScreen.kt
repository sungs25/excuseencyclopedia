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

// 색상 정의
val PurpleMain = Color(0xFF6C63FF)
val PurpleLight = Color(0xFFEBE9FF)
val GrayBackground = Color(0xFFF6F7F9)
val BarChartColor = Color(0xFF9FA8DA)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsScreen(
    onNavigateToSubscription: () -> Unit, // ★ [추가] 구독 화면으로 이동하는 콜백
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    // 현재 프리미엄 상태 확인
    val isPremium = prefs.isPremium

    Box(
        modifier = Modifier.fillMaxSize().background(GrayBackground)
    ) {
        // --- 1. 통계 내용 (프리미엄 아니면 블러 처리) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
                .then(if (!isPremium) Modifier.blur(15.dp) else Modifier),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // [헤더] 날짜 이동
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

            // 1. [이달의 리포트 카드]
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryItem("이번 달", "${uiState.monthlyCount}회", Modifier.weight(1f))
                        SummaryItem("평균 뻔뻔함", String.format("%.1f", uiState.monthlyAverage), Modifier.weight(1f))
                        SummaryItem("주력 핑계", uiState.monthlyTopCategory, Modifier.weight(1f))
                    }
                }
            }

            // 2. [월별 추이]
            Column {
                Text("월별 변명 추이", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                BarChartSection(data = uiState.monthlyTrend)
            }

            // 3. [자주 쓰는 단어]
            Column {
                Text("나의 단골 멘트", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                WordCloudSection(words = uiState.frequentWords)
            }

            // 4. [핑계 성향 분석]
            Column {
                Text("핑계 카테고리 분석", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
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
            }

            // 5. [전체 누적 기록]
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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

            Spacer(modifier = Modifier.height(80.dp))
        }

        // --- 2. 잠금 화면 (프리미엄 아닐 때) ---
        if (!isPremium) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(30.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(PurpleLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, null, tint = PurpleMain, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("프리미엄 리포트 잠금", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "당신의 변명 패턴을 분석하고\n'이달의 칭호'를 확인하세요!",
                            textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // ★ [수정] 구독 화면으로 이동하도록 변경 & 텍스트 수정
                        Button(
                            onClick = { onNavigateToSubscription() },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("구독하러 가기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ★ 1. 막대 차트 컴포넌트
// ==========================================
@Composable
fun BarChartSection(data: List<MonthlyTrend>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (data.isEmpty()) {
            Box(Modifier.padding(30.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("데이터가 부족합니다.", color = Color.Gray)
            }
        } else {
            val maxCount = data.maxOfOrNull { it.count } ?: 1

            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        if (item.count > 0) {
                            Text(
                                text = "${item.count}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        val barHeight = 120.dp * (item.count.toFloat() / maxCount)

                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(if (item.count > 0) barHeight else 1.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (item.count == maxCount) PurpleMain else BarChartColor)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = item.month,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// ★ 2. 워드 클라우드 컴포넌트
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordCloudSection(words: List<WordFrequency>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (words.isEmpty()) {
            Box(Modifier.padding(30.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("데이터가 부족합니다.", color = Color.Gray)
            }
        } else {
            FlowRow(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                words.forEachIndexed { index, word ->
                    val fontSize = when {
                        index == 0 -> 20.sp
                        index < 3 -> 16.sp
                        else -> 13.sp
                    }
                    val backgroundColor = when {
                        index == 0 -> PurpleMain
                        index < 3 -> Color(0xFFFFD54F)
                        else -> GrayBackground
                    }
                    val textColor = if (index == 0) Color.White else Color.Black

                    Surface(
                        color = backgroundColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "#${word.word} ${if (word.count > 1) "${word.count}" else ""}",
                            fontSize = fontSize,
                            color = textColor,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- 기타 컴포넌트 ---
@Composable
fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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