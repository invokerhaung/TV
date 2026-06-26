package com.fongmi.android.tv.ui.compose.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.fongmi.android.tv.bean.History
import com.fongmi.android.tv.event.RefreshEvent
import com.fongmi.android.tv.model.HistoryViewModel
import com.fongmi.android.tv.ui.compose.component.ImageAsync
import com.fongmi.android.tv.ui.compose.component.LoadingState
import com.fongmi.android.tv.ui.compose.component.LoadingStateType
import com.fongmi.android.tv.ui.compose.dialog.SyncDialog
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 历史记录屏幕
 * 对应 HistoryActivity
 */
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {},
    onHistoryClick: (History) -> Unit = {},
    historyViewModel: HistoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val histories by historyViewModel.histories.observeAsState(initial = emptyList())
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var isDeleteMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var historyToDelete by remember { mutableStateOf<History?>(null) }

    // 判断是否显示回到顶部按钮
    val showScrollToTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    // 加载历史记录
    LaunchedEffect(Unit) {
        historyViewModel.loadHistories()
    }

    // EventBus 刷新机制（对应 Java 原版 @Subscribe RefreshEvent）
    DisposableEffect(Unit) {
        val listener = object {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onRefreshEvent(event: RefreshEvent) {
                if (event.getType() == RefreshEvent.Type.HISTORY) {
                    historyViewModel.loadHistories()
                    // 自动滚动到顶部
                    scope.launch {
                        gridState.animateScrollToItem(0)
                    }
                }
            }
        }
        EventBus.getDefault().register(listener)
        onDispose {
            EventBus.getDefault().unregister(listener)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isDeleteMode) {
                        isDeleteMode = false
                    } else {
                        onBackClick()
                    }
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_control_back),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            Text(
                text = "历史记录",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
            // 同步按钮
            IconButton(onClick = { showSyncDialog = true }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_action_sync),
                    contentDescription = "同步",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            // 删除按钮
            IconButton(
                onClick = {
                    if (isDeleteMode) {
                        showDeleteConfirm = true
                    } else {
                        isDeleteMode = true
                    }
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_action_delete),
                    contentDescription = if (isDeleteMode) "清空" else "删除",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        val state = when {
            histories.isEmpty() -> LoadingStateType.EMPTY
            else -> LoadingStateType.CONTENT
        }

        LoadingState(
            state = state,
            emptyMessage = "暂无历史记录",
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // 网格布局
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(Product.getColumn(context)),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(histories) { history ->
                    HistoryItem(
                        history = history,
                        isDeleteMode = isDeleteMode,
                        onClick = {
                            if (isDeleteMode) {
                                historyToDelete = history
                                showDeleteConfirm = true
                            } else {
                                onHistoryClick(history)
                            }
                        },
                        onLongClick = {
                            isDeleteMode = !isDeleteMode
                        }
                    )
                }
            }
        }

        // 回到顶部按钮
        if (showScrollToTop) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            gridState.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_fab_top),
                        contentDescription = "回到顶部",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = if (historyToDelete != null) "删除记录" else "清空历史",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = if (historyToDelete != null) {
                        "确定要删除 ${historyToDelete?.getVodName()} 的播放记录吗？"
                    } else {
                        "确定要清空所有历史记录吗？此操作不可撤销。"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (historyToDelete != null) {
                            historyViewModel.deleteHistory(historyToDelete!!)
                            historyToDelete = null
                        } else {
                            historyViewModel.clearHistories()
                            isDeleteMode = false
                        }
                        showDeleteConfirm = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        historyToDelete = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 同步对话框
    if (showSyncDialog) {
        SyncDialog(
            devices = emptyList(),
            type = "history",
            onDeviceClick = { device, mode -> /* 同步到设备 */ },
            onRefresh = { /* 刷新设备列表 */ },
            onScan = { /* 扫描设备 */ },
            onDismiss = { showSyncDialog = false }
        )
    }
}

/**
 * 历史记录项
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryItem(
    history: History,
    isDeleteMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeleteMode) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column {
            // 封面图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                ImageAsync(
                    url = history.getVodPic(),
                    contentDescription = history.getVodName(),
                    modifier = Modifier.fillMaxSize()
                )
                // 备注标签
                if (history.getVodRemarks().isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = history.getVodRemarks(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 信息
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = history.getVodName(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
