package com.fongmi.android.tv.ui.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fongmi.android.tv.Product
import com.fongmi.android.tv.R
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Class
import com.fongmi.android.tv.bean.Config
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.model.SiteViewModel
import com.fongmi.android.tv.ui.compose.component.LoadingState
import com.fongmi.android.tv.ui.compose.component.LoadingStateType
import com.fongmi.android.tv.ui.compose.component.VodCard
import com.fongmi.android.tv.ui.compose.component.VodCardStyle
import com.fongmi.android.tv.ui.compose.dialog.FilterDialog
import com.fongmi.android.tv.ui.compose.dialog.HistoryDialog
import com.fongmi.android.tv.ui.compose.dialog.LinkDialog
import com.fongmi.android.tv.ui.compose.dialog.SiteDialog
import kotlinx.coroutines.launch

/**
 * 首页屏幕
 * 对应 VodFragment
 */
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToFavorite: () -> Unit = {},
    onNavigateToVideo: (Vod) -> Unit = {},
    siteViewModel: SiteViewModel = viewModel()
) {
    val context = LocalContext.current
    val result by siteViewModel.result.observeAsState()
    val scope = rememberCoroutineScope()

    // 状态
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var showSiteDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }

    // 获取分类列表
    val categories = result?.getTypes() ?: emptyList()

    // 加载首页内容
    LaunchedEffect(Unit) {
        siteViewModel.homeContent()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo
                IconButton(
                    onClick = { showHistoryDialog = true }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_compose),
                        contentDescription = "Logo",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 站点名称标题
                val siteName = VodConfig.get().getHome()?.getName()?.ifEmpty {
                    VodConfig.get().getConfig()?.getName()?.ifEmpty { "首页" }
                } ?: "首页"
                Text(
                    text = siteName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showSiteDialog = true }
                        .padding(horizontal = 8.dp)
                )

                // 搜索按钮
                IconButton(onClick = onNavigateToSearch) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_action_search),
                        contentDescription = "搜索",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }

                // 历史按钮
                IconButton(onClick = onNavigateToHistory) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_action_history),
                        contentDescription = "历史",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }

                // 收藏按钮
                IconButton(onClick = onNavigateToFavorite) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_action_keep),
                        contentDescription = "收藏",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }

            // 分类标签
            if (categories.isNotEmpty()) {
                CategoryTabs(
                    categories = categories,
                    selectedIndex = selectedCategoryIndex,
                    onCategoryClick = { index ->
                        selectedCategoryIndex = index
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 内容区域
            val state = when {
                result == null -> LoadingStateType.LOADING
                result?.getList()?.isEmpty() == true -> LoadingStateType.EMPTY
                else -> LoadingStateType.CONTENT
            }

            LoadingState(
                state = state,
                emptyMessage = "暂无内容",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (categories.isNotEmpty()) {
                    // 使用 HorizontalPager 实现滑动切换
                    val pagerState = rememberPagerState(
                        initialPage = selectedCategoryIndex,
                        pageCount = { categories.size }
                    )

                    // 同步 pager 状态到分类选择
                    LaunchedEffect(pagerState.currentPage) {
                        selectedCategoryIndex = pagerState.currentPage
                    }

                    // 同步分类选择到 pager
                    LaunchedEffect(selectedCategoryIndex) {
                        if (pagerState.currentPage != selectedCategoryIndex) {
                            pagerState.animateScrollToPage(selectedCategoryIndex)
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val category = categories[page]
                        // 每个分类的内容页面
                        CategoryContentPage(
                            siteKey = VodConfig.get().getHome().getKey(),
                            category = category,
                            result = result,
                            onVodClick = onNavigateToVideo
                        )
                    }
                } else {
                    // 无分类时显示首页内容
                    val homeVods = result?.getList() ?: emptyList()
                    if (homeVods.isNotEmpty()) {
                        HomeContentGrid(
                            vods = homeVods,
                            onVodClick = onNavigateToVideo
                        )
                    }
                }
            }
        }

        // FAB 按钮
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 回到顶部 FAB
            SmallFloatingActionButton(
                onClick = {
                    // 回到顶部由 CategoryContentPage 内部处理
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_fab_top),
                    contentDescription = "回到顶部",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                )
            }

            // 筛选 FAB
            if (categories.isNotEmpty() && categories.getOrNull(selectedCategoryIndex)?.getFilters()?.isNotEmpty() == true) {
                FloatingActionButton(
                    onClick = { showFilterDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_fab_filter),
                        contentDescription = "筛选",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                    )
                }
            }

            // 链接 FAB
            FloatingActionButton(
                onClick = { showLinkDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_fab_link),
                    contentDescription = "链接",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                )
            }
        }
    }

    // 对话框
    if (showSiteDialog) {
        SiteDialog(
            sites = VodConfig.get().getSites(),
            change = true,
            onSiteClick = { site ->
                VodConfig.get().setHome(site)
                showSiteDialog = false
                // 重新加载内容
                siteViewModel.homeContent()
            },
            onSearchToggle = { _, _ -> },
            onChangeToggle = { _, _ -> },
            onDismiss = { showSiteDialog = false }
        )
    }

    if (showHistoryDialog) {
        HistoryDialog(
            configs = Config.getAll(0),
            readOnly = true,
            onConfigClick = { config ->
                showHistoryDialog = false
                // 加载配置
                VodConfig.load(config, object : com.fongmi.android.tv.impl.Callback() {
                    override fun success() {
                        siteViewModel.homeContent()
                    }
                })
            },
            onDeleteClick = { },
            onDismiss = { showHistoryDialog = false }
        )
    }

    if (showFilterDialog && categories.isNotEmpty()) {
        val currentCategory = categories.getOrNull(selectedCategoryIndex)
        if (currentCategory != null) {
            FilterDialog(
                filters = currentCategory.getFilters(),
                onFilterSelected = { key, value ->
                    // 应用筛选
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }
    }

    if (showLinkDialog) {
        LinkDialog(
            onConfirm = { url ->
                showLinkDialog = false
                // 播放链接
            },
            onDismiss = { showLinkDialog = false }
        )
    }
}

/**
 * 分类标签栏
 */
@Composable
private fun CategoryTabs(
    categories: List<Class>,
    selectedIndex: Int,
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = index == selectedIndex
            Text(
                text = category.getTypeName(),
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                modifier = Modifier
                    .clickable { onCategoryClick(index) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * 分类内容页面
 * 对应 Java 原版 TypeFragment
 */
@Composable
private fun CategoryContentPage(
    siteKey: String,
    category: Class,
    result: com.fongmi.android.tv.bean.Result?,
    onVodClick: (Vod) -> Unit
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    // 获取当前分类的内容
    val vods = result?.getList() ?: emptyList()

    // 判断是否显示回到顶部按钮
    val showScrollToTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (vods.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无内容",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // 内容网格
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(Product.getColumn(context)),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vods) { vod ->
                    VodCard(
                        vod = vod,
                        style = VodCardStyle.RECT,
                        onClick = { onVodClick(vod) }
                    )
                }
            }
        }

        // 回到顶部按钮
        if (showScrollToTop) {
            SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        gridState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_fab_top),
                    contentDescription = "回到顶部",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                )
            }
        }
    }
}

/**
 * 首页内容网格
 * 当没有分类时显示首页内容
 */
@Composable
private fun HomeContentGrid(
    vods: List<Vod>,
    onVodClick: (Vod) -> Unit
) {
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(Product.getColumn(context)),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(vods) { vod ->
                VodCard(
                    vod = vod,
                    style = VodCardStyle.RECT,
                    onClick = { onVodClick(vod) }
                )
            }
        }
    }
}
