package com.example.excuseencyclopedia.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.data.Excuse
import com.example.excuseencyclopedia.ui.AppViewModelProvider
import com.example.excuseencyclopedia.ui.home.ExcuseItem
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// 요일 리스트 생성 함수
fun daysOfWeek(firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): List<DayOfWeek> {
    return (0..6).map { firstDayOfWeek.plus(it.toLong()) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // DB 데이터를 날짜별로 그룹화
    val excusesByDate = remember(uiState.excuseList) {
        uiState.excuseList.groupBy {
            try {
                LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                LocalDate.now()
            }
        }
    }

    // 캘린더 설정
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // 과거 100개월
    val endMonth = remember { currentMonth.plusMonths(100) }   // 미래 100개월
    val daysOfWeek = remember { daysOfWeek() }

    // 사용자가 선택한 날짜 (기본값: 오늘)
    var selection by remember { mutableStateOf(LocalDate.now()) }

    // 날짜 선택 팝업 표시 여부
    var showDatePicker by remember { mutableStateOf(false) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    // 현재 스크롤된 달(Month)을 감지
    val visibleMonth = remember(state.firstVisibleMonth) { state.firstVisibleMonth.yearMonth }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // ▼▼▼ 1. 연/월 헤더 (클릭 시 팝업) ▼▼▼
        CalendarHeader(
            yearMonth = visibleMonth,
            onHeaderClick = { showDatePicker = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. 요일 헤더
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        Spacer(modifier = Modifier.height(10.dp))

        // 3. 달력 본체
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(
                    day = day,
                    isSelected = selection == day.date,
                    hasExcuse = excusesByDate.containsKey(day.date),
                    onClick = { clicked ->
                        if (clicked.position == DayPosition.MonthDate) {
                            selection = clicked.date
                        }
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
        Divider()
        Spacer(modifier = Modifier.height(10.dp))

        // 4. 선택된 날짜의 변명 리스트
        Text(
            text = "${selection.monthValue}월 ${selection.dayOfMonth}일의 변명",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val selectedExcuses = excusesByDate[selection] ?: emptyList()

        if (selectedExcuses.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("이날은 핑계 댈 게 없었네요!", color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(selectedExcuses) { excuse ->
                    ExcuseItem(excuse = excuse, onDeleteClick = {})
                }
            }
        }
    }

    // ▼▼▼ 5. 날짜 선택 팝업 (DatePicker) ▼▼▼
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = visibleMonth.atDay(1)
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

                        // 선택한 날짜가 있는 달로 이동
                        coroutineScope.launch {
                            state.scrollToMonth(YearMonth.from(selectedDate))
                        }
                        // (옵션) 선택값도 그 날짜로 바꿀지 여부:
                        selection = selectedDate
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

// 헤더 컴포저블
@Composable
fun CalendarHeader(
    yearMonth: YearMonth,
    onHeaderClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeaderClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center // 가운데 정렬
    ) {
        Text(
            text = yearMonth.format(formatter),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "날짜 선택")
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    hasExcuse: Boolean,
    onClick: (CalendarDay) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            .border(
                width = if (day.date == LocalDate.now() && !isSelected) 1.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (isSelected) Color.White
                else if (day.position == DayPosition.MonthDate) MaterialTheme.colorScheme.onSurface
                else Color.LightGray
            )

            if (hasExcuse && day.position == DayPosition.MonthDate) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color.Red)
                )
            }
        }
    }
}