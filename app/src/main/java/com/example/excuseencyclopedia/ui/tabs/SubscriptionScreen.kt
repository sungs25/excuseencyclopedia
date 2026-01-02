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

    // 현재 구독 상태
    var isPremium by remember { mutableStateOf(prefs.isPremium) }

    // 현재 선택된 플랜 (테스트용)
    var currentPlanId by remember { mutableStateOf(if (isPremium) "1_month" else "") }

    // 팝업 제어를 위한 상태 변수들
    var showPurchaseDialog by remember { mutableStateOf(false) } // 구독/변경 팝업
    var showCancelDialog by remember { mutableStateOf(false) }   // 해지 팝업
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) } // 선택한 플랜 저장용

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
            BenefitItem("무제한 변명 기록 저장 💾")

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
                        selectedPlan = plan
                        showPurchaseDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. 구독 해지 버튼
            if (isPremium) {
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(
                    onClick = { showCancelDialog = true }
                ) {
                    Text("구독 해지하기", color = Color.Gray, textDecoration = TextDecoration.Underline)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // [1] 구독/변경 확인 다이얼로그
    if (showPurchaseDialog && selectedPlan != null) {
        val plan = selectedPlan!!
        val isChange = isPremium

        AlertDialog(
            onDismissRequest = { showPurchaseDialog = false },
            title = { Text(if (isChange) "멤버십 변경" else "멤버십 구독") },
            text = {
                Text(
                    if (isChange) "'${plan.title}' 요금제로 변경하시겠습니까?\n결제 금액: ${plan.price}"
                    else "'${plan.title}' 구독을 시작하시겠습니까?\n결제 금액: ${plan.price}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isPremium = true
                        prefs.isPremium = true
                        currentPlanId = plan.id

                        val msg = if (isChange) "${plan.title}로 변경되었습니다." else "${plan.title} 구독 시작! 🎉"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                        showPurchaseDialog = false
                    }
                ) {
                    Text(if (isChange) "변경하기" else "구독하기", fontWeight = FontWeight.Bold, color = PurpleMain)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDialog = false }) { Text("취소") }
            },
            containerColor = Color.White
        )
    }

    // [2] 구독 해지 확인 다이얼로그
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("구독 해지") },
            text = { Text("정말로 구독을 해지하시겠습니까?\n모든 프리미엄 혜택이 사라집니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isPremium = false
                        prefs.isPremium = false
                        currentPlanId = ""

                        Toast.makeText(context, "구독이 해지되었습니다. 😢", Toast.LENGTH_SHORT).show()
                        showCancelDialog = false
                    }
                ) {
                    Text("해지하기", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("유지하기") }
            },
            containerColor = Color.White
        )
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
            .clickable(enabled = !isCurrentPlan) { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            // ★ 수정됨: 여기가 문제였습니다! (CenterVertically로 변경)
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
                            color = Color(0xFFFF5252),
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

            Button(
                onClick = onSelect,
                enabled = !isCurrentPlan,
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