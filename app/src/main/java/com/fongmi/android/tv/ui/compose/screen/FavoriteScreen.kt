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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.fongmi.android.tv.Product
import com.fongmi.android.tv.R
import com.fongmi.android.tv.bean.Keep
import com.fongmi.android.tv.ui.compose.component.ImageAsync
import com.fongmi.android.tv.ui.compose.component.LoadingState
import com.fongmi.android.tv.ui.compose.component.LoadingStateType
import com.fongmi.android.tv.ui.compose.dialog.SyncDialog
import kotlinx.coroutines.launch

/**
 * 收藏屏幕
 * 对应 KeepActivity
 */
@Composable
fun FavoriteScreen(
    onBackClick: () -> Unit = {},
    onKeepClick: (Keep) -> Unit = {}
) {
    val context = LocalContext.current
    var keeps by remember { mutableStateOf(loadKeeps()) }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var isDeleteMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var keepToDelete by remember { mutableStateOf<Keep?>(null) }

    // 判断是否显示回到顶部按钮
    val showScrollToTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
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
                text = "收藏",
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
            keeps.isEmpty() -> LoadingStateType.EMPTY
            else -> LoadingStateType.CONTENT
        }

        LoadingState(
            state = state,
            emptyMessage = "暂无收藏",
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
                items(keeps) { keep ->
                    KeepItem(
                        keep = keep,
                        isDeleteMode = isDeleteMode,
                        onClick = {
                            if (isDeleteMode) {
                                keepToDelete = keep
                                showDeleteConfirm = true
                            } else {
                                onKeepClick(keep)
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
                    text = if (keepToDelete != null) "删除收藏" else "清空收藏",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = if (keepToDelete != null) {
                        "确定要删除 ${keepToDelete?.getVodName()} 的收藏吗？"
                    } else {
                        "确定要清空所有收藏吗？此操作不可撤销。"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (keepToDelete != null) {
                            keepToDelete!!.delete()
                            keeps = loadKeeps()
                            keepToDelete = null
                        } else {
                            Keep.deleteAll()
                            keeps = loadKeeps()
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
                        keepToDelete = null
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
            type = "keep",
            onDeviceClick = { device, mode -> /* 同步到设备 */ },
            onRefresh = { /* 刷新设备列表 */ },
            onScan = { /* 扫描设备 */ },
            onDismiss = { showSyncDialog = false }
        )
    }
}

/**
 * 加载收藏数据
 */
private fun loadKeeps(): List<Keep> {
    return Keep.getVod()
}

/**
 * 收藏项
 * 完善卡片信息显示（对应 Java 原版 VodRectHolder）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeepItem(
    keep: Keep,
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
                    url = keep.getVodPic(),
                    contentDescription = keep.getVodName(),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 信息（对应 Java 原版 VodRectHolder 的 name、site、year、remark）
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // 名称
                Text(
                    text = keep.getVodName(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 站点名称
                if (keep.getSiteName().isNotEmpty()) {
                    Text(
                        text = keep.getSiteName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
