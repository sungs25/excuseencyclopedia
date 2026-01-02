package com.example.excuseencyclopedia.ui.tabs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.excuseencyclopedia.data.PreferenceManager


// 구독 상품 데이터 클래스
data class SubscriptionPlan(
    val id: String,
    val title: String,
    val price: String,
    val duration: String,
    val discount: String? = null,
    val isBest: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    // 현재 구독 상태 (테스트를 위해 화면 내부 상태로 관리하거나 Prefs 연동)
    var isPremium by remember { mutableStateOf(prefs.isPremium) }

    // 현재 선택된 플랜 (테스트용: 기본값은 없음, 구독 중이면 '1개월'로 가정)
    // 실제 앱에서는 구글 결제 라이브러리에서 가져온 skuId를 써야 함
    var currentPlanId by remember { mutableStateOf(if (isPremium) "1_month" else "") }

    val plans = listOf(
        SubscriptionPlan("1_month", "가볍게 시작", "2,900원", "/월"),
        SubscriptionPlan("6_month", "실속 패키지", "13,900원", "/6개월", "20% 할인"),
        SubscriptionPlan("1_year", "최고의 가성비", "23,900원", "/년", "31% 할인", isBest = true)
    )

    Scaffold(
        containerColor = Color(0xFFF6F7F9),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("멤버십 관리", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF6F7F9))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. 혜택 안내 섹션
            Text(
                text = "프리미엄 혜택",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            BenefitItem("모든 광고 완벽 제거 🚫")
            BenefitItem("나만의 업적 도감 잠금 해제 🏆")
            BenefitItem("월간/연간 프리미엄 리포트 제공 📊")

            Spacer(modifier = Modifier.height(30.dp))

            // 2. 구독 플랜 선택 섹션
            Text(
                text = "요금제 선택",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            plans.forEach { plan ->
                PlanCard(
                    plan = plan,
                    isCurrentPlan = (isPremium && currentPlanId == plan.id),
                    onSelect = {
                        if (!isPremium) {
                            // 신규 구독
                            isPremium = true
                            prefs.isPremium = true
                            currentPlanId = plan.id
                            Toast.makeText(context, "${plan.title} 구독 시작! 🎉", Toast.LENGTH_SHORT).show()
                        } else if (currentPlanId != plan.id) {
                            // 구독 변경 (업그레이드/다운그레이드)
                            currentPlanId = plan.id
                            Toast.makeText(context, "${plan.title}로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. 구독 해지 버튼 (구독 중일 때만 표시)
            if (isPremium) {
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(
                    onClick = {
                        isPremium = false
                        prefs.isPremium = false
                        currentPlanId = ""
                        Toast.makeText(context, "구독이 해지되었습니다. 😢", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("구독 해지하기", color = Color.Gray, textDecoration = TextDecoration.Underline)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 15.sp, color = Color.DarkGray)
    }
}

@Composable
fun PlanCard(
    plan: SubscriptionPlan,
    isCurrentPlan: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isCurrentPlan) PurpleMain else Color.Transparent
    val borderWidth = if (isCurrentPlan) 2.dp else 0.dp

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = !isCurrentPlan) { onSelect() } // 현재 플랜이면 클릭 불가
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 배지 (할인율 or Best)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (plan.isBest) {
                        Surface(
                            color = PurpleMain,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                "BEST", color = Color.White,
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (plan.discount != null) {
                        Surface(
                            color = Color(0xFFFF5252), // 빨간색
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                plan.discount, color = Color.White,
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (plan.isBest || plan.discount != null) Spacer(modifier = Modifier.height(8.dp))

                Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(plan.price, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PurpleMain)
                    Text(plan.duration, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                }
            }

            // 오른쪽 버튼
            Button(
                onClick = onSelect,
                enabled = !isCurrentPlan, // 현재 플랜이면 비활성화
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentPlan) Color.Gray else PurpleMain,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isCurrentPlan) "이용 중" else "선택",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}