package com.example.excuseencyclopedia.ui.tabs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.alarm.AlarmScheduler
import com.example.excuseencyclopedia.data.PreferenceManager
import com.example.excuseencyclopedia.ui.AppViewModelProvider

@Composable
fun SettingsScreen(
    onAchievementsClick: () -> Unit,
    // ★ 추가됨: 구독 관리 화면으로 이동하는 함수
    onManageSubscriptionClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // 알람 & 설정 관리자
    val alarmScheduler = remember { AlarmScheduler(context) }
    val prefs = remember { PreferenceManager(context) }

    // 상태 관리
    var isNotificationEnabled by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 구독 상태 (화면 갱신을 위해 State로 관리)
    // 실제 변경은 SubscriptionScreen에서 일어나므로 여기서는 읽기만 합니다.
    val isPremium = prefs.isPremium

    // 권한 요청 런처 (알림용)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isNotificationEnabled = true
                alarmScheduler.scheduleDailyAlarm()
                Toast.makeText(context, "매일 밤 9시에 알림이 울립니다.", Toast.LENGTH_SHORT).show()
            } else {
                isNotificationEnabled = false
                Toast.makeText(context, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        containerColor = Color(0xFFF6F7F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "설정",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // 1. 멤버십 설정 (구독 관리 화면 연결)
            SettingsGroupCard(title = "멤버십") {
                if (isPremium) {
                    // (1) 구독 중일 때
                    SettingsTextItem(
                        icon = Icons.Default.CheckCircle,
                        title = "프리미엄 이용 중 👑",
                        trailingText = "혜택 적용됨"
                    )

                    HorizontalDivider(color = Color(0xFFF6F7F9), thickness = 1.dp)

                    // 업적 도감 바로가기
                    SettingsClickableItem(
                        icon = Icons.Default.Star,
                        title = "나의 업적 도감 보기 🏆",
                        onClick = { onAchievementsClick() },
                        textColor = Color.Black,
                        iconColor = Color(0xFFFFD700)
                    )

                    HorizontalDivider(color = Color(0xFFF6F7F9), thickness = 1.dp)

                    // ★ 구독 변경 및 해지 (관리 화면으로 이동)
                    SettingsClickableItem(
                        icon = Icons.Default.Settings,
                        title = "멤버십 변경 및 해지",
                        onClick = { onManageSubscriptionClick() }, // 이동!
                        textColor = Color.Gray,
                        iconColor = Color.Gray
                    )

                } else {
                    // (2) 구독 안 했을 때 -> 관리 화면으로 이동 유도
                    SettingsClickableItem(
                        icon = Icons.Default.Star,
                        title = "프리미엄 구독하고 혜택받기",
                        subtitle = "광고 제거 + 업적 잠금 해제 + 리포트",
                        onClick = { onManageSubscriptionClick() }, // 이동!
                        textColor = PurpleMain,
                        iconColor = PurpleMain
                    )
                }
            }

            // 2. 일반 설정 (알림)
            SettingsGroupCard(title = "일반") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "매일 알림 받기 (밤 9시)",
                    checked = isNotificationEnabled,
                    onCheckedChange = { shouldEnable ->
                        if (shouldEnable) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    isNotificationEnabled = true
                                    alarmScheduler.scheduleDailyAlarm()
                                    Toast.makeText(context, "매일 밤 9시에 알림이 울립니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                isNotificationEnabled = true
                                alarmScheduler.scheduleDailyAlarm()
                                Toast.makeText(context, "매일 밤 9시에 알림이 울립니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            isNotificationEnabled = false
                            alarmScheduler.cancelDailyAlarm()
                            Toast.makeText(context, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // 3. 정보
            SettingsGroupCard(title = "정보") {
                SettingsTextItem(
                    icon = Icons.Default.Info,
                    title = "앱 버전",
                    trailingText = "1.0.0"
                )
                HorizontalDivider(color = Color(0xFFF6F7F9), thickness = 1.dp)
                SettingsTextItem(
                    icon = Icons.Default.Person,
                    title = "개발자",
                    trailingText = "핑계 장인"
                )
            }

            // 4. 데이터 관리
            SettingsGroupCard(title = "데이터 관리") {
                SettingsClickableItem(
                    icon = Icons.Default.Delete,
                    title = "모든 기록 초기화",
                    onClick = { showDeleteDialog = true },
                    textColor = Color.Red,
                    iconColor = Color.Red
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "모든 기록 삭제") },
            text = { Text(text = "정말로 모든 변명 기록을 지우시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        prefs.saveCount = 0
                        Toast.makeText(context, "모든 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            },
            containerColor = Color.White
        )
    }
}

// --- 하위 컴포넌트들 ---

@Composable
fun SettingsGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 10.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PurpleMain.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PurpleMain,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
fun SettingsTextItem(
    icon: ImageVector,
    title: String,
    trailingText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PurpleMain.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Text(text = trailingText, fontSize = 14.sp, color = Color.Gray)
    }
}

// Subtitle(보조 문구)을 지원하도록 개선된 버튼 아이템
@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    textColor: Color = Color.Black,
    iconColor: Color = PurpleMain
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))

            // 제목과 부제목을 세로로 배치
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textColor)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
    }
}