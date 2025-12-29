package com.example.excuseencyclopedia.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.ui.AppViewModelProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEntryScreen(
    navigateBack: () -> Unit, // 뒤로 가기 함수
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope() // 비동기 작업(저장)을 위해 필요

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("변명 기록하기") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        ItemEntryBody(
            itemUiState = viewModel.itemUiState,
            onItemValueChange = viewModel::updateUiState,
            onSaveClick = {
                // 저장 버튼 누르면 코루틴 실행 -> 저장 -> 뒤로 가기
                coroutineScope.launch {
                    viewModel.saveItem()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
    }
}

@Composable
fun ItemEntryBody(
    itemUiState: ItemUiState,
    onItemValueChange: (ItemUiState) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. 무엇을 안 했나요?
        OutlinedTextField(
            value = itemUiState.task,
            onValueChange = { onItemValueChange(itemUiState.copy(task = it)) },
            label = { Text("안 한 일 (예: 운동)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 2. 왜 안 했나요? (여러 줄 입력 가능)
        OutlinedTextField(
            value = itemUiState.reason,
            onValueChange = { onItemValueChange(itemUiState.copy(reason = it)) },
            label = { Text("변명 내용 (예: 비가 와서)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
        )

        // 3. 뻔뻔함 점수 슬라이더
        Column {
            Text(text = "뻔뻔함 점수: ${itemUiState.score.toInt()}점")
            Slider(
                value = itemUiState.score,
                onValueChange = { onItemValueChange(itemUiState.copy(score = it)) },
                valueRange = 1f..5f,
                steps = 3 // 1점~5점 사이 3개의 칸 (1,2,3,4,5)
            )
        }

        // 4. 저장 버튼
        Button(
            onClick = onSaveClick,
            enabled = itemUiState.isEntryValid, // 내용이 있어야만 눌림
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("변명 도감에 등재하기")
        }
    }
}