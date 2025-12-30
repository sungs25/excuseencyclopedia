package com.example.excuseencyclopedia.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ▼▼▼ [수정 1] daysOfWeek 함수가 없어서 에러 났을 겁니다. 직접 추가해줍니다. ▼▼▼
fun daysOfWeek(firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): List<DayOfWeek> {
    return (0..6).map { firstDayOfWeek.plus(it.toLong()) }
}

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // DB 날짜 파싱
    val excusesByDate = remember(uiState.excuseList) {
        uiState.excuseList.groupBy {
            try {
                LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                LocalDate.now()
            }
        }
    }

    CalendarContent(excusesByDate = excusesByDate)
}



@Composable
fun CalendarContent(
    excusesByDate: Map<LocalDate, List<Excuse>>
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(12) }

    // 여기서 아까 만든 함수를 사용합니다.
    val daysOfWeek = remember { daysOfWeek() }

    var selection by remember { mutableStateOf(LocalDate.now()) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        Spacer(modifier = Modifier.height(10.dp))

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

        // ▼▼▼ [수정 2] Material3에서는 Divider 대신 HorizontalDivider를 씁니다. ▼▼▼
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "${selection.monthValue}월 ${selection.dayOfMonth}일의 변명",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val selectedExcuses = excusesByDate[selection] ?: emptyList()

        if (selectedExcuses.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("이날은 핑계 댈 게 없었네요!\n(아니면 기록을 안 했거나...)", color = Color.Gray, textAlign = TextAlign.Center)
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