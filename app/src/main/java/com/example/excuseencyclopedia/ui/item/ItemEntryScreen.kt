package com.example.excuseencyclopedia.ui.item

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.ui.tabs.PurpleMain
import com.example.excuseencyclopedia.data.PreferenceManager
import com.example.excuseencyclopedia.ui.AdMobHelper
import com.example.excuseencyclopedia.ui.AppViewModelProvider
import com.example.excuseencyclopedia.ui.showInAppReview
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// 배경색
val GrayBackground = Color(0xFFF6F7F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEntryScreen(
    navigateBack: () -> Unit,
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // --- 광고 및 설정 관리자 ---
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    val adHelper = remember { AdMobHelper(context) }

    // ★ 리뷰 팝업 표시 여부를 제어하는 상태 변수
    var showReviewDialog by remember { mutableStateOf(false) }

    // 화면 진입 시 광고 미리 로드
    LaunchedEffect(Unit) {
        adHelper.loadAd()
    }

    Scaffold(
        containerColor = GrayBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("변명 기록하기", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = GrayBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. 카테고리 선택
            CategorySelector(
                selectedCategory = viewModel.itemUiState.category,
                onCategorySelected = { viewModel.updateUiState(viewModel.itemUiState.copy(category = it)) }
            )

            // 2. 안 한 일 입력
            StyledTextField(
                value = viewModel.itemUiState.task,
                onValueChange = { viewModel.updateUiState(viewModel.itemUiState.copy(task = it)) },
                placeholder = "안 한 일",
                singleLine = true
            )

            // 3. 변명 입력
            StyledTextField(
                value = viewModel.itemUiState.reason,
                onValueChange = { viewModel.updateUiState(viewModel.itemUiState.copy(reason = it)) },
                placeholder = "변명",
                minLines = 3,
                singleLine = false
            )

            // 4. 날짜 선택
            DateSelectorBox(
                date = viewModel.itemUiState.date,
                onDateSelected = { newDate ->
                    viewModel.updateUiState(viewModel.itemUiState.copy(date = newDate))
                }
            )

            // 5. 뻔뻔함 점수 슬라이더
            ScoreSliderBox(
                score = viewModel.itemUiState.score,
                onScoreChanged = { newScore ->
                    viewModel.updateUiState(viewModel.itemUiState.copy(score = newScore))
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 6. 저장 버튼
            Button(
                onClick = {
                    coroutineScope.launch {
                        // (1) 데이터 저장
                        viewModel.saveItem()

                        // (2) 누적 저장 횟수 증가
                        val newTotalCount = prefs.totalSaveCount + 1
                        prefs.totalSaveCount = newTotalCount

                        // (3) 리뷰 요청 조건 체크
                        // 조건: 정확히 10번째 저장이고 && 아직 리뷰 요청(도장)을 안 받았다면
                        if (newTotalCount == 10 && !prefs.isReviewRequested) {
                            // ★ 바로 API를 부르지 않고, 우리가 만든 팝업(Dialog)을 먼저 띄움
                            showReviewDialog = true
                        } else {
                            // (4) 리뷰 대상이 아니면 -> 광고 로직 실행
                            if (prefs.shouldShowAd()) {
                                val activity = context as? Activity
                                if (activity != null) {
                                    // 광고 보여주고 -> 닫히면 뒤로가기
                                    adHelper.showAd(activity) {
                                        navigateBack()
                                    }
                                } else {
                                    navigateBack()
                                }
                            } else {
                                // 광고 대상도 아니면 그냥 뒤로가기
                                navigateBack()
                            }
                        }
                    }
                },
                enabled = viewModel.itemUiState.isEntryValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleMain,
                    disabledContainerColor = PurpleMain.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "변명 등록하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // ★★★ [리뷰 요청 팝업] 10번째 저장 시에만 나타남 ★★★
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = {
                // 바깥 부분 터치 시: 창 닫고 그냥 홈으로 이동
                showReviewDialog = false
                navigateBack()
            },
            title = {
                Text(
                    text = "🎉 축하합니다!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = "벌써 10번째 변명을 기록하셨네요!\n꾸준한 기록에 박수를 보냅니다. 👏\n\n잠시 시간을 내어 앱을 평가해 주실 수 있나요?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 1. "좋아요" 선택 -> 도장 찍고(true) 구글 리뷰 호출
                        prefs.isReviewRequested = true
                        showInAppReview(context)

                        showReviewDialog = false
                        navigateBack() // 홈으로 이동
                    }
                ) {
                    Text("좋아요", color = PurpleMain, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // 2. "나중에" 선택 -> 도장 찍고(true) 그냥 종료
                        // (여기서 true로 하면 다시는 안 물어봄. 계속 물어보려면 이 줄 삭제)
                        prefs.isReviewRequested = true

                        showReviewDialog = false
                        navigateBack() // 홈으로 이동
                    }
                ) {
                    Text("나중에", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}


// --- 아래는 UI 컴포넌트들 (기존과 동일) ---

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.padding(20.dp)
        ) {
            if (value.isEmpty()) {
                Text(text = placeholder, color = Color.Gray)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                singleLine = singleLine,
                minLines = minLines,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun getCategoryIconAndColor(category: String): Pair<ImageVector, Color> {
    return when (category) {
        "건강&생활" -> Icons.Default.Favorite to Color(0xFFFF8A80)
        "일상&관리" -> Icons.Default.Face to Color(0xFF82B1FF)
        "자기계발&취미" -> Icons.Default.Star to Color(0xFFFFD180)
        else -> Icons.AutoMirrored.Filled.List to Color(0xFFCFD8DC)
    }
}

@Composable
fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("건강&생활", "일상&관리", "자기계발&취미", "기타")
    val (icon, color) = getCategoryIconAndColor(selectedCategory)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if(selectedCategory == "기타" && selectedCategory !in categories) "카테고리" else selectedCategory,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold
                )
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            categories.forEach { category ->
                val (itemIcon, itemColor) = getCategoryIconAndColor(category)
                DropdownMenuItem(
                    text = { Text(category) },
                    leadingIcon = { Icon(itemIcon, contentDescription = null, tint = itemColor) },
                    onClick = { onCategorySelected(category); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectorBox(
    date: String,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEBE9FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = date, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val checkDate = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()
                    val today = LocalDate.now()
                    return !checkDate.isAfter(today)
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        onDateSelected(formatter.format(Date(selectedMillis)))
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun ScoreSliderBox(
    score: Float,
    onScoreChanged: (Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "뻔뻔함 점수",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${score.toInt()}점",
                    fontSize = 18.sp,
                    color = PurpleMain,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = score,
                onValueChange = { onScoreChanged(it) },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = PurpleMain,
                    activeTrackColor = PurpleMain,
                    inactiveTrackColor = PurpleMain.copy(alpha = 0.2f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("소심함(1)", fontSize = 12.sp, color = Color.Gray)
                Text("철면피(5)", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}