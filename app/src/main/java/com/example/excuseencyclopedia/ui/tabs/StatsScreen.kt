package com.example.excuseencyclopedia.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.ui.AppViewModelProvider

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "나의 변명 분석",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 1. 총 변명 횟수 카드
        StatCard(
            title = "총 변명 횟수",
            value = "${uiState.totalCount}회",
            description = "지금까지 이만큼 미뤘어요!",
            icon = Icons.Default.Info,
            color = MaterialTheme.colorScheme.primaryContainer
        )

        // 2. 평균 뻔뻔함 점수 카드
        StatCard(
            title = "평균 뻔뻔함",
            value = String.format("%.1f점", uiState.averageScore), // 소수점 1자리
            description = "5점 만점에 이 정도입니다.",
            icon = Icons.Default.Star,
            color = MaterialTheme.colorScheme.secondaryContainer
        )

        // 3. 최다 사용 핑계 카드
        StatCard(
            title = "가장 많이 쓴 핑계",
            value = uiState.topCategory,
            description = "총 ${uiState.topCategoryCount}번 사용했습니다.",
            icon = Icons.Default.Info, // 적절한 아이콘으로 변경 가능
            color = MaterialTheme.colorScheme.tertiaryContainer
        )

        // 여기에 나중에 그래프 같은 걸 추가할 수도 있겠죠?
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium, // 아주 큰 글씨
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}