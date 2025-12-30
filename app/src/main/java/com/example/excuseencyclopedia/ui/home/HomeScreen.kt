package com.example.excuseencyclopedia.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.data.Excuse
import com.example.excuseencyclopedia.ui.AppViewModelProvider
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToItemEntry: () -> Unit = {}
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    var excuseToDelete by remember { mutableStateOf<Excuse?>(null) }

    // 날짜 선택 팝업을 띄울지 여부
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("변명 기록장") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                // (선택 사항) 탑바 오른쪽이나 왼쪽에 설정 버튼 등을 둘 수 있음
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToItemEntry) {
                Icon(Icons.Default.Add, contentDescription = "추가")
            }
        }
    ) { innerPadding ->
        Column(modifier = modifier.padding(innerPadding)) {

            // ▼▼▼ 1. 연/월 헤더 (클릭 가능) ▼▼▼
            YearMonthHeader(
                currentDate = homeUiState.selectedDate,
                onHeaderClick = { showDatePicker = true }
            )

            // ▼▼▼ 2. 주간 달력 ▼▼▼
            WeekCalendarSection(
                selectedDate = homeUiState.selectedDate,
                onDateSelected = viewModel::updateDate
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider()

            // 3. 리스트
            HomeBody(
                excuseList = homeUiState.excuseList,
                onDeleteClick = { excuseToDelete = it },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // 삭제 확인 팝업
    if (excuseToDelete != null) {
        AlertDialog(
            onDismissRequest = { excuseToDelete = null },
            title = { Text("변명 삭제") },
            text = { Text("정말 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExcuse(excuseToDelete!!)
                    excuseToDelete = null
                }) { Text("삭제", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { excuseToDelete = null }) { Text("취소") }
            }
        )
    }

    // ▼▼▼ 날짜 선택 팝업 (DatePicker) ▼▼▼
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = homeUiState.selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val selectedDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.updateDate(selectedDate)
                    }
                    showDatePicker = false
                }) { Text("이동") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ▼▼▼ 연도와 월을 표시하는 헤더 컴포저블 ▼▼▼
@Composable
fun YearMonthHeader(
    currentDate: LocalDate,
    onHeaderClick: () -> Unit
) {
    // 예: 2024년 12월
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeaderClick() } // 여기를 누르면 달력이 뜸
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentDate.format(formatter),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "날짜 선택")
    }
}

@Composable
fun WeekCalendarSection(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }

    val state = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = selectedDate, // 처음 보여줄 날짜
    )

    // ▼▼▼ 핵심: 선택된 날짜가 바뀌면, 달력도 그 주(Week)로 점프해라! ▼▼▼
    LaunchedEffect(selectedDate) {
        state.scrollToWeek(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 10.dp)
    ) {
        WeekCalendar(
            state = state,
            dayContent = { day ->
                DayItem(
                    day = day,
                    isSelected = selectedDate == day.date,
                    onClick = { onDateSelected(it.date) }
                )
            }
        )
    }
}

@Composable
fun DayItem(
    day: WeekDay,
    isSelected: Boolean,
    onClick: (WeekDay) -> Unit
) {
    Column(
        modifier = Modifier
            .width(50.dp)
            .clickable { onClick(day) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color(0xFFFFC107)
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun HomeBody(
    excuseList: List<Excuse>,
    onDeleteClick: (Excuse) -> Unit,
    modifier: Modifier = Modifier
) {
    if (excuseList.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = "기록된 변명이 없습니다.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(excuseList, key = { it.id }) { excuse ->
                ExcuseItem(excuse = excuse, onDeleteClick = onDeleteClick)
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
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = excuse.task,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "\"${excuse.reason}\"",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "뻔뻔함 점수: ", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${excuse.score}점",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}