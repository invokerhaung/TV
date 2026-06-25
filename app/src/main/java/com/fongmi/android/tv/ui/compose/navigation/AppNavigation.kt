package com.fongmi.android.tv.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * 应用导航路由定义
 */
object AppRoutes {
    const val HOME = "home"
    const val LIVE = "live"
    const val SEARCH = "search"
    const val FAVORITE = "favorite"
    const val SETTINGS = "settings"
    const val VIDEO = "video/{vodId}"
    const val HISTORY = "history"

    fun videoRoute(vodId: String) = "video/$vodId"
}

/**
 * 应用导航组件
 * 使用 Compose Navigation 替代 Fragment + ViewPager
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = AppRoutes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppRoutes.HOME) {
            // HomeScreen - 将在阶段三实现
        }

        composable(AppRoutes.LIVE) {
            // LiveScreen - 将在阶段三实现
        }

        composable(AppRoutes.SEARCH) {
            // SearchScreen - 将在阶段三实现
        }

        composable(AppRoutes.FAVORITE) {
            // FavoriteScreen - 将在阶段三实现
        }

        composable(AppRoutes.SETTINGS) {
            // SettingsScreen - 将在阶段三实现
        }

        composable(AppRoutes.VIDEO) { backStackEntry ->
            val vodId = backStackEntry.arguments?.getString("vodId") ?: ""
            // VideoScreen - 将在阶段三实现
        }

        composable(AppRoutes.HISTORY) {
            // HistoryScreen - 将在阶段三实现
        }
    }
}
