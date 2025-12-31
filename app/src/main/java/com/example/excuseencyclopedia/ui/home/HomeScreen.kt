package com.example.excuseencyclopedia.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

// 디자인에 쓸 포인트 컬러 정의 (보라색 계열)
val PurpleMain = Color(0xFF6C63FF)
val PurpleLight = Color(0xFFEBE9FF)
val GrayBackground = Color(0xFFF6F7F9) // 전체 배경색 (아주 연한 회색)

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
        // 배경색을 밝은 회색으로 설정
        containerColor = GrayBackground,
        /*floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToItemEntry,
                containerColor = PurpleMain, // FAB도 보라색으로
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp) // 버튼 크기 조금 키움
            ) {
                Icon(Icons.Default.Add, contentDescription = "추가", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center // 중앙 배치 (이미지처럼)*/
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp) // 전체적으로 양옆 여백 줌
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. 헤더 (2025년 12월 v)
            YearMonthHeader(
                currentDate = homeUiState.selectedDate,
                onHeaderClick = { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 주간 달력 (카드형 디자인)
            WeekCalendarSection(
                selectedDate = homeUiState.selectedDate,
                onDateSelected = viewModel::updateDate
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 리스트 (카드 리스트)
            HomeBody(
                excuseList = homeUiState.excuseList,
                onDeleteClick = { excuseToDelete = it },
                modifier = Modifier.weight(1f) // 남은 공간 다 차지
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

    // 날짜 선택 팝업 (기존 유지)
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
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = datePickerState) }
    }
}

// ▼▼▼ 헤더 디자인 변경 (왼쪽 정렬, 큰 글씨) ▼▼▼
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
            style = MaterialTheme.typography.headlineMedium, // 24~28sp 정도의 큰 글씨
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "날짜 선택", tint = Color.Gray)
    }
}

// ▼▼▼ 달력 디자인 변경 (카드형) ▼▼▼
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
        firstVisibleWeekDate = selectedDate,
    )

    LaunchedEffect(selectedDate) { state.scrollToWeek(selectedDate) }

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

@Composable
fun DayItem(
    day: WeekDay,
    isSelected: Boolean,
    onClick: (WeekDay) -> Unit
) {
    // 선택 여부에 따른 색상 결정
    val backgroundColor = if (isSelected) PurpleMain else Color.White
    val contentColor = if (isSelected) Color.White else Color.Black
    val elevation = if (isSelected) 8.dp else 2.dp

    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .width(48.dp) // 카드 너비 고정
            .height(70.dp) // 카드 높이 (이미지처럼 길게)
            .clickable { onClick(day) },
        shape = RoundedCornerShape(16.dp), // 둥근 모서리
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 날짜 (23, 24...)
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 요일 (Mon, Tue...)
            Text(
                text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
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
            Text(text = "기록된 내용이 없습니다.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp), // 카드 간 간격 넓게
            contentPadding = PaddingValues(bottom = 80.dp) // FAB에 가려지지 않게 여백
        ) {
            items(excuseList, key = { it.id }) { excuse ->
                ExcuseItemCard(excuse = excuse, onDeleteClick = onDeleteClick)
            }
        }
    }
}

// ▼▼▼ 리스트 아이템 디자인 변경 (하얀색 예쁜 카드) ▼▼▼
@Composable
fun ExcuseItemCard(
    excuse: Excuse,
    onDeleteClick: (Excuse) -> Unit
) {
    // 카테고리별 아이콘 및 색상 매핑
    val (icon, color) = when (excuse.category) {
        "건강&생활" -> Icons.Default.Favorite to Color(0xFFFF8A80) // 붉은 계열
        "일상&관리" -> Icons.Default.Face to Color(0xFF82B1FF)     // 파란 계열
        "자기계발&취미" -> Icons.Default.Star to Color(0xFFFFD180)  // 노란 계열
        else -> Icons.Default.List to Color(0xFFCFD8DC)            // 회색 계열
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp), // 더 둥글게
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // 1. 상단: 카테고리 아이콘 + 이름 + 삭제 버튼
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 아이콘 배경 (동그라미)
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

                Spacer(modifier = Modifier.weight(1f)) // 빈 공간 밀어내기

                // 삭제 아이콘 (작게)
                IconButton(onClick = { onDeleteClick(excuse) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. 메인: 안 한 일 (제목)
            Text(
                text = excuse.task,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), // 22sp로 확대
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. 내용: 변명
            Text(
                text = excuse.reason,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp), // 16sp로 확대
                color = Color.DarkGray // 가독성을 위해 조금 더 진한 회색
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. 하단: 점수 뱃지 (Pill shape)
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