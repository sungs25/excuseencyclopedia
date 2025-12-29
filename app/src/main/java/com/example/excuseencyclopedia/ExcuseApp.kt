package com.example.excuseencyclopedia

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.excuseencyclopedia.ui.home.HomeScreen
import com.example.excuseencyclopedia.ui.item.ItemEntryScreen

// 화면들의 주소(Route)를 미리 이름 지어둡니다. (오타 방지)
enum class ExcuseScreen {
    Start, // 메인 화면
    Entry  // 입력 화면
}

@Composable
fun ExcuseApp(
    navController: NavHostController = rememberNavController()
) {
    // NavHost: 여기서 화면을 갈아입혀줍니다.
    NavHost(
        navController = navController,
        startDestination = ExcuseScreen.Start.name // 앱 켜면 처음 보여줄 화면
    ) {

        // 1. 메인 화면 (Start)
        composable(route = ExcuseScreen.Start.name) {
            HomeScreen(
                // (+) 버튼 누르면 Entry 화면으로 이동해라!
                navigateToItemEntry = { navController.navigate(ExcuseScreen.Entry.name) }
            )
        }

        // 2. 입력 화면 (Entry)
        composable(route = ExcuseScreen.Entry.name) {
            ItemEntryScreen(
                // 저장하거나 뒤로가기 누르면 이전 화면으로 돌아가라!
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}