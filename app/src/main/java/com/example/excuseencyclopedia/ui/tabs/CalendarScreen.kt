package com.example.excuseencyclopedia.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.excuseencyclopedia.ui.AppViewModelProvider
import com.example.excuseencyclopedia.ui.home.ExcuseItemCard
import com.example.excuseencyclopedia.ui.home.PurpleLight
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

    val excusesByDate = remember(uiState.excuseList) {
        uiState.excuseList.groupBy {
            try {
                LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                LocalDate.now()
            }
        }
    }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek() }

    // 기본 선택값: 오늘
    var selection by remember { mutableStateOf(LocalDate.now()) }

    // 날짜 선택 팝업 표시 여부
    var showDatePicker by remember { mutableStateOf(false) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    val visibleMonth = remember(state.firstVisibleMonth) { state.firstVisibleMonth.yearMonth }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        // 1. 헤더 (클릭 기능 추가됨)
        HeaderWithArrows(
            yearMonth = visibleMonth,
            // ▼ 제목 클릭 시 팝업 오픈
            onTitleClick = { showDatePicker = true },
            onPrevClick = {
                val prevMonth = state.firstVisibleMonth.yearMonth.minusMonths(1)
                coroutineScope.launch { state.animateScrollToMonth(prevMonth) }
                // ▼ 달 이동 시 그 달의 1일로 자동 선택
                selection = prevMonth.atDay(1)
            },
            onNextClick = {
                val nextMonth = state.firstVisibleMonth.yearMonth.plusMonths(1)
                coroutineScope.launch { state.animateScrollToMonth(nextMonth) }
                // ▼ 달 이동 시 그 달의 1일로 자동 선택
                selection = nextMonth.atDay(1)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        Spacer(modifier = Modifier.height(10.dp))

        // 2. 달력
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                DayCircle(
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
        Divider(color = Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(20.dp))

        // 3. 리스트
        Text(
            text = "${selection.monthValue}월 ${selection.dayOfMonth}일의 변명",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val selectedExcuses = excusesByDate[selection] ?: emptyList()

        if (selectedExcuses.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("이날은 핑계 댈 게 없었네요!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(selectedExcuses) { excuse ->
                    ExcuseItemCard(excuse = excuse, onDeleteClick = {})
                }
            }
        }
    }

    // ▼▼▼ 날짜 선택 팝업 (DatePicker) 추가 ▼▼▼
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selection
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

                        // 선택한 날짜로 이동 및 선택
                        coroutineScope.launch {
                            state.animateScrollToMonth(YearMonth.from(selectedDate))
                        }
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

// ▼▼▼ 헤더: 제목에 클릭 이벤트 추가 ▼▼▼
@Composable
fun HeaderWithArrows(
    yearMonth: YearMonth,
    onTitleClick: () -> Unit, // 클릭 콜백 추가
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 날짜 텍스트 (클릭 가능하도록 변경)
        Text(
            text = yearMonth.format(formatter),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.clickable { onTitleClick() } // ★ 클릭 시 팝업
        )

        Row {
            IconButton(onClick = onPrevClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "이전 달",
                    tint = PurpleMain,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "다음 달",
                    tint = PurpleMain,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ... DaysOfWeekTitle, DayCircle 등 아래 코드는 그대로 두시면 됩니다 ...
@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun DayCircle(
    day: CalendarDay,
    isSelected: Boolean,
    hasExcuse: Boolean,
    onClick: (CalendarDay) -> Unit
) {
    val backgroundColor = when {
        isSelected -> PurpleMain
        hasExcuse -> PurpleLight
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        hasExcuse -> PurpleMain
        day.position != DayPosition.MonthDate -> Color.LightGray
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontWeight = if (isSelected || hasExcuse) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}