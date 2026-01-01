package com.example.excuseencyclopedia.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// 디자인 컬러 정의
val PurpleMain = Color(0xFF6C63FF)
val PurpleLight = Color(0xFFEBE9FF)
val GrayBackground = Color(0xFFF6F7F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToItemEntry: () -> Unit = {}
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    var excuseToDelete by remember { mutableStateOf<Excuse?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = GrayBackground,
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. 헤더
            YearMonthHeader(
                currentDate = homeUiState.selectedDate,
                onHeaderClick = { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 주간 달력
            WeekCalendarSection(
                selectedDate = homeUiState.selectedDate,
                onDateSelected = viewModel::updateDate
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 리스트
            HomeBody(
                excuseList = homeUiState.excuseList,
                onDeleteClick = { excuseToDelete = it },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // 삭제 팝업
    if (excuseToDelete != null) {
        AlertDialog(
            onDismissRequest = { excuseToDelete = null },
            title = { Text("변명 삭제") },
            text = { Text("이 기록을 지우시겠습니까?") },
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

    // 날짜 선택 팝업
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = homeUiState.selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            // ★ 핵심 1: 미래 날짜 선택 불가 설정 (SelectableDates)
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // 오늘까지만 선택 가능 (내일부턴 false)
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
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
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun YearMonthHeader(
    currentDate: LocalDate,
    onHeaderClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREA)

    Row(
        modifier = Modifier
            .clickable { onHeaderClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentDate.format(formatter),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "날짜 선택", tint = Color.Gray)
    }
}

@Composable
fun WeekCalendarSection(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentDate = remember { LocalDate.now() } // 오늘 날짜
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }

    val state = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = selectedDate,
    )

    LaunchedEffect(selectedDate) { state.scrollToWeek(selectedDate) }

    WeekCalendar(
        state = state,
        dayContent = { day ->
            // ★ 핵심 2: 미래 날짜인지 확인
            val isFuture = day.date.isAfter(currentDate)

            DayItem(
                day = day,
                isSelected = selectedDate == day.date,
                isFuture = isFuture, // 미래 여부 전달
                onClick = {
                    // ★ 핵심 3: 미래가 아닐 때만 클릭 허용
                    if (!isFuture) {
                        onDateSelected(it.date)
                    }
                }
            )
        }
    )
}

@Composable
fun DayItem(
    day: WeekDay,
    isSelected: Boolean,
    isFuture: Boolean, // 추가됨
    onClick: (WeekDay) -> Unit
) {
    val backgroundColor = if (isSelected) PurpleMain else Color.White
    // 미래 날짜면 연한 회색 글씨, 아니면 검정/흰색
    val contentColor = if (isSelected) Color.White else if (isFuture) Color.LightGray else Color.Black
    val elevation = if (isSelected) 8.dp else 2.dp

    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .width(48.dp)
            .height(70.dp)
            .clickable(enabled = !isFuture) { onClick(day) }, // 미래 날짜 클릭 방지
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else if (isFuture) Color.LightGray else Color.Gray
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
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(text = "오늘 안 한 일과 변명을 기록해 보세요.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(excuseList, key = { it.id }) { excuse ->
                ExcuseItemCard(excuse = excuse, onDeleteClick = onDeleteClick)
            }
        }
    }
}

@Composable
fun ExcuseItemCard(
    excuse: Excuse,
    onDeleteClick: (Excuse) -> Unit
) {
    val (icon, color) = when (excuse.category) {
        "건강&생활" -> Icons.Default.Favorite to Color(0xFFFF8A80)
        "일상&관리" -> Icons.Default.Face to Color(0xFF82B1FF)
        "자기계발&취미" -> Icons.Default.Star to Color(0xFFFFD180)
        else -> Icons.Default.List to Color(0xFFCFD8DC)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = excuse.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { onDeleteClick(excuse) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = excuse.task,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = excuse.reason,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = PurpleLight,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "${excuse.score}점",
                            style = MaterialTheme.typography.labelMedium,
                            color = PurpleMain,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}