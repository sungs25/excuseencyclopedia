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
import com.example.excuseencyclopedia.data.PreferenceManager
import com.example.excuseencyclopedia.ui.OnboardingScreen
import com.example.excuseencyclopedia.ui.SplashScreen // ★ Import 확인
import com.example.excuseencyclopedia.ui.home.HomeScreen
import com.example.excuseencyclopedia.ui.item.ItemEntryScreen
import com.example.excuseencyclopedia.ui.tabs.AchievementsScreen
import com.example.excuseencyclopedia.ui.tabs.CalendarScreen
import com.example.excuseencyclopedia.ui.tabs.SettingsScreen
import com.example.excuseencyclopedia.ui.tabs.StatsScreen
import com.example.excuseencyclopedia.ui.tabs.SubscriptionScreen

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
    const val Splash = "splash" // ★ 스플래시 경로 추가
    const val Onboarding = "onboarding"
    const val Entry = "entry"
    const val Achievements = "achievements"
    const val Subscription = "subscription"
}

val PurpleMain = Color(0xFF6C63FF)

@Composable
fun ExcuseApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    // ★ 시작 화면은 무조건 Splash
    val startDestination = Routes.Splash

    // 하단 바 숨김 조건 (기록 입력, 온보딩, 스플래시 화면일 때 숨김)
    val showBottomBar = currentRoute != Routes.Entry &&
            currentRoute != Routes.Onboarding &&
            currentRoute != Routes.Splash

    var backPressedTime by remember { mutableLongStateOf(0L) }

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

                    // [가운데: + 버튼]
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val isEntryScreen = currentDestination?.route == Routes.Entry
                        FloatingActionButton(
                            onClick = { if (!isEntryScreen) navController.navigate(Routes.Entry) },
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            // ★ [NEW] 0. 스플래시 화면
            composable(Routes.Splash) {
                SplashScreen(
                    onTimeout = {
                        // 스플래시가 끝나면 첫 실행 여부에 따라 이동
                        val nextScreen = if (prefs.isFirstRun) Routes.Onboarding else BottomNavItem.Record.route

                        navController.navigate(nextScreen) {
                            // 뒤로가기 눌러서 스플래시로 돌아오지 못하게 제거
                            popUpTo(Routes.Splash) { inclusive = true }
                        }
                    }
                )
            }

            // 1. 온보딩
            composable(Routes.Onboarding) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(BottomNavItem.Record.route) {
                            popUpTo(Routes.Onboarding) { inclusive = true }
                        }
                    }
                )
            }

            // 2. 홈 (기록)
            composable(BottomNavItem.Record.route) { HomeScreen() }

            // 3. 캘린더
            composable(BottomNavItem.Calendar.route) { CalendarScreen() }

            // 4. 통계
            composable(BottomNavItem.Stats.route) { StatsScreen() }

            // 5. 설정
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    onAchievementsClick = { navController.navigate(Routes.Achievements) },
                    onManageSubscriptionClick = { navController.navigate(Routes.Subscription) }
                )
            }

            // 6. 기록 입력
            composable(
                route = Routes.Entry,
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it }, animationSpec = tween(400)
                    ) + fadeIn()
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it }, animationSpec = tween(400)
                    ) + fadeOut()
                }
            ) {
                ItemEntryScreen(navigateBack = { navController.popBackStack() })
            }

            // 7. 업적 도감
            composable(Routes.Achievements) {
                AchievementsScreen(navigateBack = { navController.popBackStack() })
            }

            // 8. 구독 관리
            composable(Routes.Subscription) {
                SubscriptionScreen(navigateBack = { navController.popBackStack() })
            }
        }
    }
}