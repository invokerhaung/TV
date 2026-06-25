package com.fongmi.android.tv.ui.compose.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fongmi.android.tv.R
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.ui.compose.component.NavItem
import com.fongmi.android.tv.ui.compose.component.NavBar
import com.fongmi.android.tv.ui.compose.theme.TVTheme

/**
 * 导航路由
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
    fun searchRoute(keyword: String = "") = if (keyword.isNotEmpty()) "search?keyword=$keyword" else "search"
}

/**
 * 需要显示底部导航栏的路由
 */
private val bottomNavRoutes = setOf(
    AppRoutes.HOME,
    AppRoutes.SEARCH,
    AppRoutes.FAVORITE,
    AppRoutes.SETTINGS
)

/**
 * 应用主屏幕
 */
@Composable
fun AppScreen() {
    TVTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // 判断是否显示底部导航栏
            val showBottomBar = currentRoute in bottomNavRoutes

            // 判断 Live 是否可见
            val liveVisible = LiveConfig.hasUrl()

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavBar(
                            currentRoute = currentRoute ?: AppRoutes.HOME,
                            liveVisible = liveVisible,
                            onNavigate = { route ->
                                if (route == AppRoutes.LIVE) {
                                    // Live 是独立 Activity，需要启动 Activity
                                    // 这里暂时跳过，后续需要通过 Context 启动
                                } else {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
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
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AppNavigation(
                        navController = navController
                    )
                }
            }
        }
    }
}

/**
 * 底部导航栏
 * 对应 Java 原版 BottomNavigationView，包含首页/直播/搜索/收藏/设置
 */
@Composable
private fun NavBar(
    currentRoute: String,
    liveVisible: Boolean,
    onNavigate: (String) -> Unit
) {
    val items = buildList {
        add(NavItem(AppRoutes.HOME, "首页", R.drawable.ic_nav_vod))
        if (liveVisible) {
            add(NavItem(AppRoutes.LIVE, "直播", R.drawable.ic_nav_live))
        }
        add(NavItem(AppRoutes.SEARCH, "搜索", R.drawable.ic_action_search))
        add(NavItem(AppRoutes.FAVORITE, "收藏", R.drawable.ic_action_keep))
        add(NavItem(AppRoutes.SETTINGS, "设置", R.drawable.ic_nav_setting))
    }

    NavBar(
        items = items,
        currentRoute = currentRoute,
        onNavigate = onNavigate
    )
}

/**
 * 应用导航
 */
@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME
    ) {
        // 首页
        composable(AppRoutes.HOME) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(AppRoutes.searchRoute()) },
                onNavigateToHistory = { navController.navigate(AppRoutes.HISTORY) },
                onNavigateToFavorite = { navController.navigate(AppRoutes.FAVORITE) },
                onNavigateToVideo = { vod ->
                    navController.navigate(AppRoutes.videoRoute(vod.getId()))
                }
            )
        }

        // 搜索
        composable(
            route = "search?keyword={keyword}",
            arguments = listOf(
                navArgument("keyword") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val keyword = backStackEntry.arguments?.getString("keyword") ?: ""
            SearchScreen(
                initialKeyword = keyword,
                onBackClick = { navController.popBackStack() },
                onVodClick = { vod ->
                    navController.navigate(AppRoutes.videoRoute(vod.getId()))
                }
            )
        }

        // 搜索（无参数）
        composable(AppRoutes.SEARCH) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onVodClick = { vod ->
                    navController.navigate(AppRoutes.videoRoute(vod.getId()))
                }
            )
        }

        // 收藏
        composable(AppRoutes.FAVORITE) {
            FavoriteScreen(
                onBackClick = { navController.popBackStack() },
                onKeepClick = { keep ->
                    navController.navigate(AppRoutes.videoRoute(keep.getVodId()))
                }
            )
        }

        // 设置
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onPlayerSettings = { /* 播放器设置 */ },
                onDanmakuSettings = { /* 弹幕设置 */ },
                onAbout = { /* 关于 */ }
            )
        }

        // 视频详情
        composable(
            route = AppRoutes.VIDEO,
            arguments = listOf(
                navArgument("vodId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val vodId = backStackEntry.arguments?.getString("vodId") ?: ""
            VideoScreen(
                vodId = vodId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 历史记录
        composable(AppRoutes.HISTORY) {
            HistoryScreen(
                onBackClick = { navController.popBackStack() },
                onHistoryClick = { history ->
                    navController.navigate(AppRoutes.videoRoute(history.getVodId()))
                }
            )
        }
    }
}
