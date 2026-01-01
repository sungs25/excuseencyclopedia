package com.example.excuseencyclopedia

import android.app.Activity // ★ 추가
import android.widget.Toast // ★ 추가
import androidx.activity.compose.BackHandler // ★ 추가
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
import androidx.compose.runtime.mutableLongStateOf // ★ 추가
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue // ★ 추가
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // ★ 추가
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

    // ★ [기능 추가] 뒤로가기 2번 눌러 종료하기 로직
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    // 현재 화면이 메인 탭(기록, 캘린더, 통계, 설정) 중 하나인지 확인
    val rootRoutes = BottomNavItem.entries.map { it.route }
    val isRootScreen = currentRoute in rootRoutes

    // 메인 화면일 때만 뒤로가기 동작을 가로챕니다.
    BackHandler(enabled = isRootScreen) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            // 2초 안에 두 번 눌렀으면 -> 앱 종료
            (context as? Activity)?.finish()
        } else {
            // 첫 번째 클릭 -> 토스트 메시지 & 시간 기록
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
                        FloatingActionButton(
                            onClick = { navController.navigate(Routes.Entry) },
                            containerColor = PurpleMain,
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
            // 기본 애니메이션
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(BottomNavItem.Record.route) { HomeScreen() }
            composable(BottomNavItem.Calendar.route) { CalendarScreen() }
            composable(BottomNavItem.Stats.route) { StatsScreen() }

            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    onAchievementsClick = { navController.navigate(Routes.Achievements) }
                )
            }

            // 기록 화면 애니메이션 (위로 올라오기)
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

            composable(Routes.Achievements) {
                AchievementsScreen(navigateBack = { navController.popBackStack() })
            }
        }
    }
}