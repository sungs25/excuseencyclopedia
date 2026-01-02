package com.example.excuseencyclopedia

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.excuseencyclopedia.ui.home.HomeScreen
import com.example.excuseencyclopedia.ui.item.ItemEntryScreen
import com.example.excuseencyclopedia.ui.tabs.AchievementsScreen
import com.example.excuseencyclopedia.ui.tabs.CalendarScreen
import com.example.excuseencyclopedia.ui.tabs.SettingsScreen
import com.example.excuseencyclopedia.ui.tabs.StatsScreen
import com.example.excuseencyclopedia.ui.tabs.SubscriptionScreen // ★ import 확인

// 1. 하단 탭 메뉴 정의
enum class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    Record("record", "기록", Icons.AutoMirrored.Filled.List),
    Calendar("calendar", "캘린더", Icons.Default.DateRange),
    Stats("stats", "통계", Icons.Default.Info),
    Settings("settings", "설정", Icons.Default.Settings)
}

object Routes {
    const val Entry = "entry"
    const val Achievements = "achievements"
    const val Subscription = "subscription" // ★ 구독 화면 경로 추가
}

// 디자인 컬러 정의
val PurpleMain = Color(0xFF6C63FF)

@Composable
fun ExcuseApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // 기록 화면일 때는 하단 바 숨김
    val showBottomBar = currentRoute != Routes.Entry

    // 뒤로가기 2번 눌러 종료하기 로직
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    // 메인 탭 화면들 정의
    val rootRoutes = BottomNavItem.entries.map { it.route }
    val isRootScreen = currentRoute in rootRoutes

    BackHandler(enabled = isRootScreen) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            backPressedTime = currentTime
            Toast.makeText(context, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomAppBar(
                    containerColor = Color.White,
                    tonalElevation = 10.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    // [왼쪽 2개 아이콘]
                    BottomNavItem.entries.take(2).forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (screen == BottomNavItem.Record) {
                                    navController.popBackStack(BottomNavItem.Record.route, inclusive = false)
                                } else {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = null, modifier = Modifier.size(28.dp)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PurpleMain,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // [가운데: 안 한 일 기록 버튼]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val isEntryScreen = currentDestination?.route == Routes.Entry

                        FloatingActionButton(
                            onClick = {
                                if (!isEntryScreen) {
                                    navController.navigate(Routes.Entry)
                                }
                            },
                            containerColor = if (isEntryScreen) Color.Gray else PurpleMain,
                            contentColor = Color.White,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(4.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "추가", modifier = Modifier.size(32.dp))
                        }
                    }

                    // [오른쪽 2개 아이콘]
                    BottomNavItem.entries.takeLast(2).forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = null, modifier = Modifier.size(28.dp)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PurpleMain,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Record.route,
            modifier = Modifier.padding(innerPadding),
            // 기본 애니메이션 (탭 전환)
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            // 1. 기록 (홈)
            composable(BottomNavItem.Record.route) { HomeScreen() }

            // 2. 캘린더
            composable(BottomNavItem.Calendar.route) { CalendarScreen() }

            // 3. 통계
            composable(BottomNavItem.Stats.route) { StatsScreen() }

            // 4. 설정 (★ 업데이트됨)
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    onAchievementsClick = { navController.navigate(Routes.Achievements) },
                    // ★ 구독 관리 화면 연결
                    onManageSubscriptionClick = { navController.navigate(Routes.Subscription) }
                )
            }

            // 5. 기록 입력 (슬라이드 애니메이션)
            composable(
                route = Routes.Entry,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(400)
                    ) + fadeIn()
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(400)
                    ) + fadeOut()
                }
            ) {
                ItemEntryScreen(navigateBack = { navController.popBackStack() })
            }

            // 6. 업적 도감
            composable(Routes.Achievements) {
                AchievementsScreen(navigateBack = { navController.popBackStack() })
            }

            // 7. [NEW] 구독 관리 화면 (★ 새로 추가됨)
            composable(Routes.Subscription) {
                SubscriptionScreen(navigateBack = { navController.popBackStack() })
            }
        }
    }
}