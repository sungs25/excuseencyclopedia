package com.example.excuseencyclopedia.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // ★ 추가된 아이콘
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.excuseencyclopedia.data.PreferenceManager
import com.example.excuseencyclopedia.ui.Achievement
import com.example.excuseencyclopedia.ui.AchievementManager
import com.example.excuseencyclopedia.ui.AppViewModelProvider


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    navigateBack: () -> Unit,
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var isPremium by remember { mutableStateOf(prefs.isPremium) }

    // Repository 접근
    val excuses by viewModel.repository.getAllExcusesStream().collectAsState(initial = emptyList())

    val achievements = remember(excuses) {
        AchievementManager.calculateAchievements(excuses, prefs.editCount)
    }

    Scaffold(
        containerColor = Color(0xFFF6F7F9),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("업적 도감 🏆", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF6F7F9)),
                // ▼▼▼ [수정됨] 뒤로 가기 버튼 추가 ▼▼▼
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // 1. 업적 리스트
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (!isPremium) Modifier.blur(15.dp) else Modifier)
            ) {
                items(achievements) { achievement ->
                    AchievementCard(achievement)
                }
            }

            // 2. 잠금 화면
            if (!isPremium) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(30.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Lock, null, tint = PurpleMain, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("업적 시스템 잠금", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "숨겨진 재미와 도전과제를 확인하려면\n구독이 필요합니다.",
                                textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { prefs.isPremium = true; isPremium = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("월 3,000원에 구독하기") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    val isHiddenLocked = achievement.isHidden && !achievement.isUnlocked

    val cardColor = if (achievement.isUnlocked) Color.White else Color(0xFFEEEEEE)
    val contentColor = if (achievement.isUnlocked) Color.Black else Color.Gray
    val iconTint = if (achievement.isUnlocked) PurpleMain else Color.Gray

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(if (achievement.isUnlocked) 4.dp else 0.dp),
        modifier = Modifier.height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if(achievement.isUnlocked) PurpleMain.copy(alpha=0.1f) else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (isHiddenLocked) {
                    Text("?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                } else {
                    Icon(achievement.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isHiddenLocked) "???" else achievement.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isHiddenLocked) "조건을 달성하여\n잠금을 해제하세요." else achievement.description,
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                maxLines = 3,
                minLines = 2
            )
        }
    }
}