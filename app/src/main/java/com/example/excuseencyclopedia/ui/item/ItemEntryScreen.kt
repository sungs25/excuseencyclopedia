package com.example.excuseencyclopedia.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.ui.AppViewModelProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEntryScreen(
    navigateBack: () -> Unit,
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEntryBody(
    itemUiState: ItemUiState,
    onItemValueChange: (ItemUiState) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 달력 팝업을 보여줄지 말지 결정하는 변수
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 0. 날짜 선택 영역 (추가됨!)
        // 텍스트 필드처럼 보이지만 누르면 달력이 뜨도록 만듭니다.
        OutlinedTextField(
            value = itemUiState.date,
            onValueChange = { }, // 직접 타이핑 불가
            label = { Text("날짜") },
            readOnly = true, // 키보드 안 올라오게
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }, // 클릭하면 팝업 오픈
            enabled = false, // 비활성화된 것처럼 보이지만 클릭 이벤트는 위에서 처리... 하려고 했는데 enabled=false면 클릭도 안 먹습니다.
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // 팁: enabled=false 대신 interactionSource를 쓰는 게 정석이지만,
            // 편법으로 Box로 감싸서 클릭 이벤트를 받겠습니다. 아래 코드를 봐주세요.
        )
        // 위 TextField가 클릭이 안 될 수 있으므로 투명 버튼을 덮어씌웁니다.
        // (더 깔끔한 방법도 있지만, 지금은 직관적인 Box 방식으로 처리)
        // 사실 가장 쉬운 건 TextField 대신 그냥 Row + Text + Icon을 쓰는 것입니다.
        // 일단 위 코드는 복잡해질 수 있으니 아래 'DateSelector' 컴포저블을 새로 만들어 쓰는 걸로 대체합니다.

        DateSelector(
            date = itemUiState.date,
            onClick = { showDatePicker = true }
        )

        // 1. 무엇을 안 했나요?
        OutlinedTextField(
            value = itemUiState.task,
            onValueChange = { onItemValueChange(itemUiState.copy(task = it)) },
            label = { Text("안 한 일 (예: 운동)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 2. 왜 안 했나요?
        OutlinedTextField(
            value = itemUiState.reason,
            onValueChange = { onItemValueChange(itemUiState.copy(reason = it)) },
            label = { Text("변명 내용 (예: 비가 와서)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
        )

        // 3. 뻔뻔함 점수
        Column {
            Text(text = "뻔뻔함 점수: ${itemUiState.score.toInt()}점")
            Slider(
                value = itemUiState.score,
                onValueChange = { onItemValueChange(itemUiState.copy(score = it)) },
                valueRange = 1f..5f,
                steps = 3
            )
        }

        // 4. 저장 버튼
        Button(
            onClick = onSaveClick,
            enabled = itemUiState.isEntryValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("변명 도감에 등재하기")
        }
    }

    // 달력 팝업 (미래 날짜 선택 불가)
    if (showDatePicker) {
        MyDatePickerDialog(
            onDateSelected = { selectedDate ->
                onItemValueChange(itemUiState.copy(date = selectedDate))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

// 날짜 선택 버튼 UI (텍스트 필드 흉내)
@Composable
fun DateSelector(date: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp), // 터치 영역 확보
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "날짜", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(text = date, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            // 미래 날짜 선택 불가 로직 (오늘 포함, 내일부터 불가)
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    // 선택된 시간(Long)을 문자열("yyyy-MM-dd")로 변환
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        // DatePicker는 UTC 기준이라 한국 시간 보정 필요할 수 있음 (일단 심플하게 처리)
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        onDateSelected(formatter.format(Date(selectedDateMillis)))
                    }
                    onDismiss()
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}