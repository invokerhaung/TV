package com.fongmi.android.tv.ui.compose.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import com.fongmi.android.tv.R
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.bean.Channel
import com.fongmi.android.tv.bean.Group
import com.fongmi.android.tv.model.LiveViewModel
import com.fongmi.android.tv.player.PlayerManager
import com.fongmi.android.tv.ui.compose.component.LoadingState
import com.fongmi.android.tv.ui.compose.component.LoadingStateType

/**
 * 直播屏幕
 * 对应 LiveActivity
 */
@Composable
fun LiveScreen(
    onBackClick: () -> Unit = {},
    liveViewModel: LiveViewModel = viewModel()
) {
    val context = LocalContext.current
    val live by liveViewModel.live().observeAsState()
    val url by liveViewModel.url().observeAsState()
    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    var selectedChannelIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerManager by remember { mutableStateOf<PlayerManager?>(null) }

    // 频道组和频道列表状态
    val groupListState = rememberLazyListState()
    val channelListState = rememberLazyListState()

    // 加载直播源
    LaunchedEffect(Unit) {
        if (LiveConfig.hasUrl()) {
            liveViewModel.parse(LiveConfig.get().getHome())
        }
    }

    val groups = live?.getGroups() ?: emptyList()
    val currentGroup = groups.getOrNull(selectedGroupIndex)
    val channels = currentGroup?.getChannel() ?: emptyList()
    val currentChannel = channels.getOrNull(selectedChannelIndex)

    // 播放频道
    LaunchedEffect(selectedGroupIndex, selectedChannelIndex) {
        currentChannel?.let { channel ->
            if (channel.getUrls().isNotEmpty()) {
                // 请求播放地址（对应 Java 原版 LiveViewModel.getUrl）
                liveViewModel.getUrl(channel)
            }
        }
    }

    // 监听播放地址变化
    LaunchedEffect(url) {
        url?.let { result ->
            // 播放地址加载完成
            isPlaying = true
        }
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
            IconButton(onClick = onBackClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_control_back),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            Text(
                text = "直播",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            // 频道信息
            if (currentChannel != null) {
                Text(
                    text = currentChannel.getName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // 播放器区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            // 使用 AndroidView 包裹 ExoPlayerView
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = true
                        player = playerManager?.getPlayer()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 播放控制覆盖层
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // 播放/暂停按钮
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            playerManager?.pause()
                        } else {
                            playerManager?.play()
                        }
                        isPlaying = !isPlaying
                    }
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isPlaying) {
                                androidx.media3.ui.R.drawable.exo_icon_pause
                            } else {
                                androidx.media3.ui.R.drawable.exo_icon_play
                            }
                        ),
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(48.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }

                // 上一个/下一个按钮
                Row {
                    IconButton(
                        onClick = {
                            if (selectedChannelIndex > 0) {
                                selectedChannelIndex--
                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_widget_rewind),
                            contentDescription = "上一个",
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (selectedChannelIndex < channels.size - 1) {
                                selectedChannelIndex++
                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_widget_forward),
                            contentDescription = "下一个",
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }
            }
        }

        // 频道组和频道列表
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // 频道组列表
            LazyColumn(
                state = groupListState,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxSize()
            ) {
                itemsIndexed(groups) { index, group ->
                    GroupItem(
                        group = group,
                        isSelected = index == selectedGroupIndex,
                        onClick = {
                            selectedGroupIndex = index
                            selectedChannelIndex = 0
                        }
                    )
                }
            }

            // 频道列表
            LazyColumn(
                state = channelListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                itemsIndexed(channels) { index, channel ->
                    ChannelItem(
                        channel = channel,
                        isSelected = index == selectedChannelIndex,
                        onClick = {
                            selectedChannelIndex = index
                        }
                    )
                }
            }
        }
    }
}

/**
 * 频道组项
 */
@Composable
private fun GroupItem(
    group: Group,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Text(
            text = group.getName(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(12.dp)
        )
    }
}

/**
 * 频道项
 */
@Composable
private fun ChannelItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${channel.getNumber()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp)
            )
            Text(
                text = channel.getName(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
