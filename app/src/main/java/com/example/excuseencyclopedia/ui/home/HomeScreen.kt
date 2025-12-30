package com.example.excuseencyclopedia.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.data.Excuse
import com.example.excuseencyclopedia.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToItemEntry: () -> Unit = {} // 나중에 글쓰기 화면으로 이동할 때 쓸 함수
) {
    // 뷰모델의 데이터를 실시간으로 관찰합니다. (데이터가 바뀌면 화면도 다시 그려짐)
    val homeUiState by viewModel.homeUiState.collectAsState()
    var excuseToDelete by remember { mutableStateOf<Excuse?>(null) }

    Scaffold(
        // 1. 상단 바 (Top Bar)
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("변명 도감", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        // 2. 우측 하단 플로팅 버튼 (FAB)
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToItemEntry,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "변명 추가", tint = Color.White)
            }
        }
    ) { innerPadding ->
        // 3. 실제 내용물 (리스트)
        HomeBody(
            excuseList = homeUiState.excuseList,
            onDeleteClick = { excuse -> excuseToDelete = excuse },
            modifier = modifier.padding(innerPadding)
        )
    }

    if (excuseToDelete != null) {
        AlertDialog(
            onDismissRequest = { excuseToDelete = null }, // 팝업 바깥 누르면 닫기
            title = { Text("변명 삭제") },
            text = { Text("정말 이 변명을 삭제하시겠습니까?\n(삭제하면 복구할 수 없습니다.)") },
            confirmButton = {
                TextButton(onClick = {
                    // 확인 누르면 실제 삭제 수행
                    viewModel.deleteExcuse(excuseToDelete!!)
                    excuseToDelete = null // 팝업 닫기
                }) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { excuseToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun HomeBody(
    excuseList: List<Excuse>,
    onDeleteClick: (Excuse) -> Unit,
    modifier: Modifier = Modifier
) {
    if (excuseList.isEmpty()) {
        // 데이터가 없을 때: 텅 빈 화면 보여주기
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "오늘은 할 일 다 하셨나요?",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    } else {
        // 데이터가 있을 때: 리스트 보여주기
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp) // 아이템 간 간격
        ) {
            items(excuseList, key = { it.id }) { excuse ->
                ExcuseItem(
                    excuse = excuse,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
fun ExcuseItem(
    excuse: Excuse,
    onDeleteClick: (Excuse) -> Unit,
    modifier: Modifier = Modifier
) {
    // 각각의 변명 카드 디자인
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. 날짜와 카테고리
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = excuse.date,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "#${excuse.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                IconButton(onClick = { onDeleteClick(excuse) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = Color.Gray // 너무 튀지 않게 회색으로
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 2. 안 한 일 (제목)
            Text(
                text = "안 함: ${excuse.task}",
                style = MaterialTheme.typography.titleMedium
            )

            // 3. 변명 (내용) - 약간 기울여서 감성 있게
            Text(
                text = "\"${excuse.reason}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 4. 뻔뻔함 점수 (우측 하단)
            Text(
                text = "뻔뻔함: ${excuse.score}/5",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
            )
        }
    }
}