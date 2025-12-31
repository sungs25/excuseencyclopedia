package com.example.excuseencyclopedia.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.ui.AppViewModelProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// 디자인 통일성을 위한 색상
val GrayBackground = Color(0xFFF6F7F9)
val PurpleMain = Color(0xFF6C63FF)

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = GrayBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. 상단 날짜 헤더 ( < 2025년 12월 > )
            StatsHeader(
                currentDate = uiState.selectedDate,
                onPrevClick = { viewModel.updateDate(uiState.selectedDate.minusMonths(1)) },
                onNextClick = { viewModel.updateDate(uiState.selectedDate.plusMonths(1)) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. 섹션 제목
            Text(
                text = "이번 달 분석",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // 3. 통계 카드들
            // (1) 변명 횟수
            StatCard(
                title = "변명 횟수",
                value = "${uiState.monthlyCount}회",
                valueColor = PurpleMain
            )

            // (2) 평균 뻔뻔함
            StatCard(
                title = "평균 뻔뻔함",
                value = String.format("%.1f점", uiState.monthlyAverage),
                valueColor = Color.Black
            )

            // (3) 가장 많이 쓴 변명 카테고리
            StatCard(
                title = "가장 많이 쓴 변명 카테고리",
                value = uiState.monthlyTopCategory,
                valueColor = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            // (4) 전체 누적 변명 횟수
            StatCard(
                title = "전체 누적 변명 횟수",
                value = "${uiState.totalCount}회",
                valueColor = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ▼▼▼ 수정됨: 아래 화살표 아이콘 삭제 ▼▼▼
@Composable
fun StatsHeader(
    currentDate: LocalDate,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // 이전 달 버튼
        IconButton(onClick = onPrevClick) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "이전", tint = Color.Gray)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 날짜 텍스트 (이제 옆에 화살표 없음)
        Text(
            text = currentDate.format(formatter),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 다음 달 버튼
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "다음", tint = Color.Gray)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    valueColor: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}