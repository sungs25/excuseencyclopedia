package com.example.excuseencyclopedia

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.excuseencyclopedia.ui.home.HomeScreen
import com.example.excuseencyclopedia.ui.item.ItemEntryScreen
import com.example.excuseencyclopedia.ui.tabs.CalendarScreen
import com.example.excuseencyclopedia.ui.tabs.SettingsScreen
import com.example.excuseencyclopedia.ui.tabs.StatsScreen

// 1. 하단 탭 메뉴 정의
enum class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    Record("record", "기록", Icons.Default.List),
    Calendar("calendar", "캘린더", Icons.Default.DateRange),
    Stats("stats", "통계", Icons.Default.Home),
    Settings("settings", "설정", Icons.Default.Settings)
}

// 2. 탭이 아닌 다른 화면들 (글쓰기 등)
object Routes {
    const val Entry = "entry"
}

@Composable
fun ExcuseApp(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        // 3. 하단 내비게이션 바 구현
        bottomBar = {
            NavigationBar {
                // 현재 보고 있는 화면이 무엇인지 알아냄
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // 4개의 탭을 반복문으로 그림
                BottomNavItem.entries.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            if (screen == BottomNavItem.Record) {
                                // 스택에 쌓인 거(입력 화면 등) 다 치우고 메인으로 돌아갓!
                                navController.popBackStack(BottomNavItem.Record.route, inclusive = false)
                            }
                            // 2. 다른 탭(캘린더, 통계 등)을 눌렀다면?
                            else {
                                navController.navigate(screen.route) {
                                    // 기존 로직 유지 (상태 저장하며 이동)
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 4. 화면 갈아끼우는 곳 (NavHost)
        // innerPadding: 하단 바에 가려지지 않게 패딩을 줌
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Record.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // [탭 1] 기록 (메인)
            composable(BottomNavItem.Record.route) {
                HomeScreen(
                    navigateToItemEntry = { navController.navigate(Routes.Entry) }
                )
            }

            // [탭 2] 캘린더
            composable(BottomNavItem.Calendar.route) {
                CalendarScreen()
            }

            // [탭 3] 통계
            composable(BottomNavItem.Stats.route) {
                StatsScreen()
            }

            // [탭 4] 설정
            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }

            // [기타] 입력 화면 (Entry)
            // 참고: 입력 화면에서는 하단 바를 숨기고 싶을 수도 있지만, 일단은 간단하게 보이게 둡니다.
            composable(Routes.Entry) {
                ItemEntryScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}