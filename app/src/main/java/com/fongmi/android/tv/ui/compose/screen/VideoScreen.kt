package com.fongmi.android.tv.ui.compose.screen

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.media3.common.C
import androidx.media3.ui.PlayerView
import com.fongmi.android.tv.R
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Danmaku
import com.fongmi.android.tv.bean.Episode
import com.fongmi.android.tv.ui.activity.VideoActivity
import com.fongmi.android.tv.ui.compose.dialog.ReceiveDialog
import com.fongmi.android.tv.bean.Flag
import com.fongmi.android.tv.bean.History
import com.fongmi.android.tv.bean.Keep
import com.fongmi.android.tv.bean.Parse
import com.fongmi.android.tv.bean.Sub
import com.fongmi.android.tv.bean.Track
import com.fongmi.android.tv.bean.Vod
import com.fongmi.android.tv.db.AppDatabase
import com.fongmi.android.tv.model.SiteViewModel
import com.fongmi.android.tv.player.PlayerHelper
import com.fongmi.android.tv.player.PlayerManager
import com.fongmi.android.tv.setting.DanmakuSetting
import com.fongmi.android.tv.setting.PlayerSetting
import com.fongmi.android.tv.setting.Setting
import com.fongmi.android.tv.ui.compose.component.EpisodeGrid
import com.fongmi.android.tv.ui.compose.component.GestureListener
import com.fongmi.android.tv.ui.compose.component.OverlayIndicator
import com.fongmi.android.tv.ui.compose.component.PiPHelper
import com.fongmi.android.tv.ui.compose.component.QuickSearchList
import com.fongmi.android.tv.ui.compose.component.TrafficIndicator
import com.fongmi.android.tv.ui.compose.component.ImageAsync
import com.fongmi.android.tv.ui.compose.component.LoadingState
import com.fongmi.android.tv.ui.compose.component.LoadingStateType
import com.fongmi.android.tv.ui.compose.component.gestureControl
import com.fongmi.android.tv.ui.compose.dialog.CastDialog
import com.fongmi.android.tv.ui.compose.dialog.ControlDialog
import com.fongmi.android.tv.ui.compose.dialog.DanmakuDialog
import com.fongmi.android.tv.ui.compose.dialog.DanmakuSearchDialog
import com.fongmi.android.tv.ui.compose.dialog.DanmakuSettingDialog
import com.fongmi.android.tv.ui.compose.dialog.EpisodeGridDialog
import com.fongmi.android.tv.ui.compose.dialog.InfoDialog
import com.fongmi.android.tv.ui.compose.dialog.OffsetDialog
import com.fongmi.android.tv.ui.compose.dialog.ParseDialog
import com.fongmi.android.tv.ui.compose.dialog.QualityDialog
import com.fongmi.android.tv.ui.compose.dialog.SubtitleDialog
import com.fongmi.android.tv.ui.compose.dialog.TimerDialog
import com.fongmi.android.tv.ui.compose.dialog.TitleDialog
import com.fongmi.android.tv.ui.compose.dialog.TrackDialog
import com.fongmi.android.tv.bean.CastVideo
import com.fongmi.android.tv.bean.Config
import com.fongmi.android.tv.bean.Device
import com.fongmi.android.tv.dlna.DLNACast
import com.fongmi.android.tv.dlna.DLNACastManager
import com.fongmi.android.tv.utils.FileChooser
import com.fongmi.android.tv.utils.ResUtil
import com.fongmi.android.tv.utils.Util
import com.github.catvod.net.OkHttp

/**
 * 视频详情屏幕
 * 对应 VideoActivity
 */
@Composable
fun VideoScreen(
    vodId: String,
    onBackClick: () -> Unit = {},
    siteViewModel: SiteViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val playerResult by siteViewModel.player.observeAsState()

    // 播放器状态
    var selectedFlagIndex by remember { mutableIntStateOf(0) }
    var selectedEpisodeIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }

    // 对话框状态
    var showControlDialog by remember { mutableStateOf(false) }
    var showEpisodeDialog by remember { mutableStateOf(false) }
    var showDanmakuDialog by remember { mutableStateOf(false) }
    var showTitleDialog by remember { mutableStateOf(false) }
    var showTrackDialog by remember { mutableStateOf(false) }
    var showCastDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showParseDialog by remember { mutableStateOf(false) }
    var showReceiveDialog by remember { mutableStateOf(false) }
    var trackType by remember { mutableIntStateOf(0) }

    // 子对话框状态
    var showDanmakuSearchDialog by remember { mutableStateOf(false) }
    var showDanmakuSettingDialog by remember { mutableStateOf(false) }
    var showOffsetDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }

    // 接收投屏事件（对应 Java 原版 onCastEvent）
    var castEvent by remember { mutableStateOf<com.fongmi.android.tv.event.CastEvent?>(null) }

    // 设备列表
    var castDevices by remember { mutableStateOf<List<Device>>(emptyList()) }

    // PlayerView 引用（用于字幕设置）
    var playerViewRef by remember { mutableStateOf<androidx.media3.ui.PlayerView?>(null) }

    // 播放器设置
    var speed by remember { mutableStateOf(PlayerSetting.getSpeed()) }
    var scale by remember { mutableIntStateOf(PlayerSetting.getScale()) }
    var isRepeat by remember { mutableStateOf(false) }
    var decode by remember { mutableStateOf("硬解") }
    var opening by remember { mutableLongStateOf(0L) }
    var ending by remember { mutableLongStateOf(0L) }

    // 历史记录
    var history by remember { mutableStateOf<History?>(null) }

    // 播放器管理器
    var playerManager by remember { mutableStateOf<PlayerManager?>(null) }

    // 文件选择器（需要在 playerManager 定义之后）
    val danmakuFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            val path = FileChooser.getPathFromUri(result.data!!.data!!)
            if (path != null) {
                playerManager?.setDanmaku(Danmaku.from(path))
            }
        }
    }

    val subtitleFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            val path = FileChooser.getPathFromUri(result.data!!.data!!)
            if (path != null) {
                playerManager?.setSub(Sub.from(path))
            }
        }
    }

    // 手势状态
    var showBrightOverlay by remember { mutableStateOf(false) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var showSeekOverlay by remember { mutableStateOf(false) }
    var brightProgress by remember { mutableIntStateOf(0) }
    var volumeProgress by remember { mutableIntStateOf(0) }
    var seekTime by remember { mutableLongStateOf(0L) }

    // 视频高度自适应（对应 Java 原版 changeHeight 方法）
    var videoHeight by remember { mutableIntStateOf(220) }
    val density = context.resources.displayMetrics.density
    val minHeight = (150 * density).toInt()
    val maxHeight = (context.resources.displayMetrics.heightPixels / 2)

    LaunchedEffect(vodId) {
        siteViewModel.detailContent(
            VodConfig.get().getHome()?.getKey() ?: "",
            vodId
        )
    }

    // 监听投屏事件（对应 Java 原版 @Subscribe CastEvent）
    DisposableEffect(Unit) {
        val listener = object {
            @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
            fun onCastEvent(event: com.fongmi.android.tv.event.CastEvent) {
                castEvent = event
                showReceiveDialog = true
            }
        }
        org.greenrobot.eventbus.EventBus.getDefault().register(listener)
        onDispose {
            org.greenrobot.eventbus.EventBus.getDefault().unregister(listener)
        }
    }

    // 提前获取数据，确保在整个函数中可访问
    val currentVod = playerResult?.getVod()
    val currentFlags = currentVod?.getFlags() ?: emptyList()

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
            IconButton(onClick = {
                if (isFullscreen) {
                    isFullscreen = false
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                } else {
                    onBackClick()
                }
            }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_control_back),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            Text(
                text = currentVod?.getName() ?: "视频详情",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
            // 收藏按钮
            IconButton(onClick = {
                val historyKey = getHistoryKey(vodId)
                val keep = Keep.find(historyKey)
                if (keep != null) keep.delete()
                else createKeep(currentVod, vodId)
            }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_control_keep_off),
                    contentDescription = "收藏",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            // 投屏按钮
            IconButton(onClick = { showCastDialog = true }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_control_cast),
                    contentDescription = "投屏",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            // 信息按钮
            IconButton(onClick = { showInfoDialog = true }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_control_info),
                    contentDescription = "信息",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        if (currentVod == null) {
            LoadingState(
                state = LoadingStateType.LOADING,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {}
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // 播放器区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isFullscreen) 0.dp else (videoHeight / density).dp)
                        .weight(if (isFullscreen) 1f else 0f)
                        .then(
                            if (activity != null) {
                                Modifier.gestureControl(
                                    activity = activity,
                                    isLocked = isLocked,
                                    listener = object : GestureListener {
                                        override fun onSingleTap() {
                                            // 切换控制栏显示
                                        }
                                        override fun onDoubleTap() {
                                            // 播放/暂停
                                            if (isPlaying) playerManager?.pause()
                                            else playerManager?.play()
                                            isPlaying = !isPlaying
                                        }
                                        override fun onSpeedUp() {
                                            // 加速播放
                                        }
                                        override fun onSpeedEnd() {
                                            // 恢复速度
                                        }
                                        override fun onBright(progress: Int) {
                                            brightProgress = progress
                                            showBrightOverlay = true
                                        }
                                        override fun onVolume(progress: Int) {
                                            volumeProgress = progress
                                            showVolumeOverlay = true
                                        }
                                        override fun onSeeking(time: Long) {
                                            seekTime = time
                                            showSeekOverlay = true
                                        }
                                        override fun onSeekEnd(time: Long) {
                                            // 跳转到指定时间
                                            playerManager?.seekTo(time)
                                            showSeekOverlay = false
                                        }
                                        override fun onFlingUp() {
                                            // 上滑 - 下一集
                                            checkNext(currentFlags, selectedFlagIndex, selectedEpisodeIndex) { selectedEpisodeIndex = it }
                                        }
                                        override fun onFlingDown() {
                                            // 下滑 - 上一集
                                            checkPrev(currentFlags, selectedFlagIndex, selectedEpisodeIndex) { selectedEpisodeIndex = it }
                                        }
                                        override fun onTouchEnd() {
                                            showBrightOverlay = false
                                            showVolumeOverlay = false
                                            showSeekOverlay = false
                                        }
                                    }
                                )
                            } else {
                                Modifier
                            }
                        )
                ) {
                    // 使用 AndroidView 包裹 ExoPlayerView
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = true
                                player = playerManager?.getPlayer()
                                playerViewRef = this
                            }
                        },
                        update = { view ->
                            // 监听视频尺寸变化（对应 Java 原版 changeHeight 方法）
                            val videoSize = playerManager?.getPlayer()?.videoSize
                            if (videoSize != null && videoSize.width > 0 && videoSize.height > 0) {
                                val viewWidth = view.width
                                val calculated = (viewWidth * videoSize.height / videoSize.width)
                                val newHeight = calculated.coerceIn(minHeight, maxHeight)
                                if (newHeight != videoHeight) {
                                    videoHeight = newHeight
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // 亮度/音量/进度覆盖层
                    if (showBrightOverlay) {
                        OverlayIndicator(
                            icon = R.drawable.ic_widget_bright_high,
                            progress = brightProgress,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    if (showVolumeOverlay) {
                        OverlayIndicator(
                            icon = R.drawable.ic_widget_volume_high,
                            progress = volumeProgress,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    if (showSeekOverlay) {
                        OverlayIndicator(
                            icon = R.drawable.ic_widget_forward,
                            progress = ((seekTime / 1000) % 100).toInt(),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // 流量监控
                    if (isPlaying) {
                        TrafficIndicator(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        )
                    }

                    // 播放控制覆盖层
                    if (!isLocked) {
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
                                    if (isPlaying) playerManager?.pause()
                                    else playerManager?.play()
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

                            // 控制按钮
                            Row {
                                // 上一集
                                IconButton(onClick = { checkPrev(currentFlags, selectedFlagIndex, selectedEpisodeIndex) { selectedEpisodeIndex = it } }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_widget_rewind),
                                        contentDescription = "上一集",
                                        modifier = Modifier.size(32.dp),
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                }
                                // 下一集
                                IconButton(onClick = { checkNext(currentFlags, selectedFlagIndex, selectedEpisodeIndex) { selectedEpisodeIndex = it } }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_widget_forward),
                                        contentDescription = "下一集",
                                        modifier = Modifier.size(32.dp),
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                }
                                // 设置
                                IconButton(onClick = { showControlDialog = true }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_control_setting),
                                        contentDescription = "设置",
                                        modifier = Modifier.size(32.dp),
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                }
                                // 锁定
                                IconButton(onClick = { isLocked = !isLocked }) {
                                    Image(
                                        painter = painterResource(
                                            id = if (isLocked) R.drawable.ic_control_lock_on else R.drawable.ic_control_lock_off
                                        ),
                                        contentDescription = if (isLocked) "解锁" else "锁定",
                                        modifier = Modifier.size(32.dp),
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                }
                                // 旋转
                                IconButton(onClick = {
                                    isFullscreen = !isFullscreen
                                    activity?.requestedOrientation = if (isFullscreen) {
                                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                    } else {
                                        ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                                    }
                                }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_control_rotate),
                                        contentDescription = "旋转",
                                        modifier = Modifier.size(32.dp),
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                }
                                // PiP 画中画
                                IconButton(onClick = {
                                    activity?.let { PiPHelper.enter(it) }
                                }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_control_cast),
                                        contentDescription = "画中画",
                                        modifier = Modifier.size(32.dp),
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isFullscreen) {
                    // 快速搜索状态
                    var showQuickSearch by remember { mutableStateOf(false) }
                    val quickSearchResults by siteViewModel.search.observeAsState()

                    // 视频信息
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // 标题（点击触发快速搜索）
                        Text(
                            text = currentVod.getName(),
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable {
                                // 触发快速搜索
                                siteViewModel.searchContent(
                                    VodConfig.get().getHome(),
                                    currentVod.getName(),
                                    true,
                                    "1"
                                )
                                showQuickSearch = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (currentVod.getRemarks().isNotEmpty()) {
                            Text(
                                text = currentVod.getRemarks(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 标签信息
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentVod.getYear().isNotEmpty()) {
                                InfoChip(text = currentVod.getYear())
                            }
                            if (currentVod.getArea().isNotEmpty()) {
                                InfoChip(text = currentVod.getArea())
                            }
                            if (currentVod.getTypeName().isNotEmpty()) {
                                InfoChip(text = currentVod.getTypeName())
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 简介
                        if (currentVod.getContent().isNotEmpty()) {
                            Text(
                                text = "简介",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = currentVod.getContent(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (showQuickSearch && quickSearchResults?.getList()?.isNotEmpty() == true) {
                        Text(
                            text = "快速搜索",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        QuickSearchList(
                            vods = quickSearchResults?.getList() ?: emptyList(),
                            onVodClick = { vod ->
                                // 切换到新视频
                                siteViewModel.detailContent(vod.getSiteKey(), vod.getId())
                                showQuickSearch = false
                            },
                            modifier = Modifier.height(200.dp)
                        )
                    }

                    // 线路选择
                    if (currentFlags.isNotEmpty()) {
                        Text(
                            text = "线路",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        ScrollableTabRow(
                            selectedTabIndex = selectedFlagIndex,
                            modifier = Modifier.fillMaxWidth(),
                            edgePadding = 16.dp
                        ) {
                            currentFlags.forEachIndexed { index, flag ->
                                Tab(
                                    selected = selectedFlagIndex == index,
                                    onClick = {
                                        if (selectedFlagIndex != index) {
                                            selectedFlagIndex = index
                                            selectedEpisodeIndex = 0
                                            // 切换线路
                                            onFlagSelected(flag, history, siteViewModel) { selectedEpisodeIndex = it }
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = flag.getShow(),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                )
                            }
                        }

                        // 剧集列表
                        val selectedFlag = currentFlags.getOrNull(selectedFlagIndex)
                        val episodes = selectedFlag?.getEpisodes() ?: emptyList()

                        if (episodes.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "剧集",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row {
                                    Text(
                                        text = "反转",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clickable {
                                                history?.setRevSort(!(history?.isRevSort ?: false))
                                                currentFlags.forEach { flag ->
                                                    java.util.Collections.reverse(flag.getEpisodes())
                                                }
                                            }
                                            .padding(horizontal = 8.dp)
                                    )
                                    Text(
                                        text = "展开",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { showEpisodeDialog = true }
                                    )
                                }
                            }

                            // 剧集网格列表（对应 Java 原版 GridLayoutManager）
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(episodes) { index, episode ->
                                    EpisodeChip(
                                        episode = episode,
                                        isSelected = index == selectedEpisodeIndex,
                                        onClick = {
                                            selectedEpisodeIndex = index
                                            val currentFlag = currentFlags.getOrNull(selectedFlagIndex)
                                            onEpisodeSelected(episode, currentFlag, history, siteViewModel)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // 控制对话框
    if (showControlDialog) {
        ControlDialog(
            speed = speed,
            decode = decode,
            opening = if (opening > 0) Util.timeMs(opening) else "跳过",
            ending = if (ending > 0) Util.timeMs(ending) else "跳过",
            repeat = isRepeat,
            scales = ResUtil.getStringArray(R.array.select_scale).toList(),
            currentScale = ResUtil.getStringArray(R.array.select_scale)[scale],
            showTrack = playerManager?.haveTrack(C.TRACK_TYPE_TEXT) == true ||
                    playerManager?.haveTrack(C.TRACK_TYPE_AUDIO) == true ||
                    playerManager?.haveTrack(C.TRACK_TYPE_VIDEO) == true,
            showTitle = playerManager?.haveTitle() == true,
            showDanmaku = DanmakuSetting.isLoad(),
            showParse = true,
            onSpeedChange = { newSpeed ->
                speed = newSpeed
                playerManager?.setSpeed(newSpeed)
                history?.setSpeed(newSpeed)
            },
            onDecodeClick = {
                playerManager?.toggleDecode()
                decode = playerManager?.getDecodeText() ?: "硬解"
            },
            onOpeningClick = {
                val position = playerManager?.getPosition() ?: 0L
                val duration = playerManager?.getDuration() ?: 0L
                if (playerManager?.canSetOpening(position, duration) == true) {
                    opening = position
                    history?.setOpening(position)
                }
            },
            onEndingClick = {
                val position = playerManager?.getPosition() ?: 0L
                val duration = playerManager?.getDuration() ?: 0L
                if (playerManager?.canSetEnding(position, duration) == true) {
                    ending = duration - position
                    history?.setEnding(ending)
                }
            },
            onRepeatClick = {
                isRepeat = !isRepeat
                playerManager?.setRepeatOne(isRepeat)
            },
            onScaleClick = { scaleText ->
                val scaleArray = ResUtil.getStringArray(R.array.select_scale)
                val index = scaleArray.indexOf(scaleText)
                if (index >= 0) {
                    scale = index
                    history?.setScale(scale)
                }
            },
            onTimerClick = {
                showTimerDialog = true
                showControlDialog = false
            },
            onTextClick = {
                trackType = C.TRACK_TYPE_TEXT
                showTrackDialog = true
                showControlDialog = false
            },
            onAudioClick = {
                trackType = C.TRACK_TYPE_AUDIO
                showTrackDialog = true
                showControlDialog = false
            },
            onVideoClick = {
                trackType = C.TRACK_TYPE_VIDEO
                showTrackDialog = true
                showControlDialog = false
            },
            onTitleClick = {
                showTitleDialog = true
                showControlDialog = false
            },
            onPlayerClick = {
                // 选择播放器
                if (activity != null) {
                    playerManager?.getUrl()?.let { url ->
                        PlayerHelper.choose(activity, url, playerManager?.getHeaders() ?: emptyMap(), playerManager?.isVod() == true, playerManager?.getPosition() ?: 0L, currentVod?.getName() ?: "")
                    }
                }
            },
            onDanmakuClick = {
                showDanmakuDialog = true
                showControlDialog = false
            },
            onDismiss = { showControlDialog = false }
        )
    }

    // 剧集对话框
    if (showEpisodeDialog) {
        val dialogFlags = currentVod?.getFlags() ?: emptyList()
        val dialogFlag = dialogFlags.getOrNull(selectedFlagIndex)
        val episodes = dialogFlag?.getEpisodes() ?: emptyList()
        EpisodeGridDialog(
            episodes = episodes,
            reverse = history?.isRevSort() == true,
            onEpisodeClick = { episode ->
                selectedEpisodeIndex = episodes.indexOf(episode)
                showEpisodeDialog = false
                onEpisodeSelected(episode, dialogFlag, history, siteViewModel)
            },
            onDismiss = { showEpisodeDialog = false }
        )
    }

    // 弹幕对话框
    if (showDanmakuDialog) {
        DanmakuDialog(
            danmakus = playerManager?.getDanmakus() ?: emptyList(),
            showSearch = playerManager?.getMetadata() != null && DanmakuSetting.getEffectiveApiUrl().isNotEmpty(),
            onDanmakuClick = { danmaku ->
                playerManager?.setDanmaku(if (danmaku.isSelected()) Danmaku.empty() else danmaku)
                showDanmakuDialog = false
            },
            onSearchClick = {
                showDanmakuDialog = false
                showDanmakuSearchDialog = true
            },
            onSettingClick = {
                showDanmakuDialog = false
                showDanmakuSettingDialog = true
            },
            onChooseClick = {
                showDanmakuDialog = false
                playerManager?.pause()
                FileChooser.from(danmakuFileLauncher).show(arrayOf("text/*"))
            },
            onDismiss = { showDanmakuDialog = false }
        )
    }

    // 标题对话框
    if (showTitleDialog) {
        val titles = playerManager?.getCurrentMediaTitles() ?: emptyList()
        TitleDialog(
            titles = titles.map { it.label.toString() },
            onTitleClick = { index ->
                playerManager?.setTitle(titles[index])
                showTitleDialog = false
            },
            onDismiss = { showTitleDialog = false }
        )
    }

    // 轨道对话框
    if (showTrackDialog) {
        TrackDialog(
            title = when (trackType) {
                C.TRACK_TYPE_TEXT -> "字幕"
                C.TRACK_TYPE_AUDIO -> "音频"
                C.TRACK_TYPE_VIDEO -> "视频"
                else -> "轨道"
            },
            tracks = getTracks(playerManager, trackType),
            showOffset = (trackType == C.TRACK_TYPE_TEXT || trackType == C.TRACK_TYPE_AUDIO) &&
                    (playerManager?.haveTrack(C.TRACK_TYPE_TEXT) == true || playerManager?.haveTrack(C.TRACK_TYPE_AUDIO) == true),
            showChoose = trackType == C.TRACK_TYPE_TEXT && playerManager?.isVod() == true,
            showSubtitle = trackType == C.TRACK_TYPE_TEXT && playerManager?.haveTrack(C.TRACK_TYPE_TEXT) == true,
            onTrackClick = { track ->
                playerManager?.setTrack(listOf(track.key(playerManager?.getKey()).save()))
                showTrackDialog = false
            },
            onOffsetClick = {
                showTrackDialog = false
                showOffsetDialog = true
            },
            onChooseClick = {
                showTrackDialog = false
                playerManager?.pause()
                FileChooser.from(subtitleFileLauncher).show(
                    arrayOf(
                        "application/x-subrip",
                        "text/x-ssa",
                        "text/vtt",
                        "application/ttml+xml",
                        "audio/*",
                        "text/*",
                        "application/octet-stream"
                    )
                )
            },
            onSubtitleClick = {
                showTrackDialog = false
                showSubtitleDialog = true
            },
            onDismiss = { showTrackDialog = false }
        )
    }

    // 投屏对话框
    if (showCastDialog) {
        // DLNACastManager 生命周期管理
        DisposableEffect(Unit) {
            DLNACastManager.get().init(context)
            DLNACastManager.get().setDeviceListener(object : DLNACastManager.DeviceListener {
                override fun onDeviceAdded(device: Device) {
                    castDevices = (castDevices + device).sorted()
                }
                override fun onDeviceRemoved(device: Device) {
                    castDevices = castDevices.filter { it.uuid != device.uuid }
                }
            })
            onDispose {
                DLNACastManager.get().setDeviceListener(null)
                DLNACastManager.get().release(context)
            }
        }

        // 加载设备列表
        LaunchedEffect(showCastDialog) {
            val devices = mutableListOf<Device>()
            devices.addAll(Device.getAll())
            val dlnaDevices = DLNACastManager.get().getRegistered()
            for (dlna in dlnaDevices) {
                if (devices.none { it.uuid == dlna.uuid }) devices.add(dlna)
            }
            castDevices = devices.sorted()
            if (castDevices.isEmpty()) {
                DLNACastManager.get().search()
            }
        }

        CastDialog(
            devices = castDevices,
            showScan = true,
            onDeviceClick = { device ->
                val name = currentVod?.getName() ?: ""
                val url = playerManager?.getUrl() ?: ""
                val position = playerManager?.getPosition() ?: 0L
                val headers = playerManager?.getHeaders() ?: emptyMap()
                val video = CastVideo(name, url, position, headers)
                if (device.isDLNA) {
                    DLNACast(video) { showCastDialog = false }.cast(device)
                } else {
                    val body = okhttp3.FormBody.Builder()
                        .add("device", Device.get().toString())
                        .add("config", Config.vod().toString())
                        .build()
                    val client = OkHttp.client(15000)
                    OkHttp.newCall(client, device.ip + "/action?do=cast", body).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                            com.fongmi.android.tv.App.post { com.fongmi.android.tv.utils.Notify.show(e.message) }
                        }
                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            response.use { res ->
                                if (res.body?.string() == "OK") {
                                    com.fongmi.android.tv.App.post { showCastDialog = false }
                                } else {
                                    com.fongmi.android.tv.App.post { com.fongmi.android.tv.utils.Notify.show(com.fongmi.android.tv.R.string.device_offline) }
                                }
                            }
                        }
                    })
                }
            },
            onRefresh = {
                Device.delete()
                castDevices = emptyList()
                DLNACastManager.get().search()
            },
            onScan = {
                // 扫描需要跳转到 ScanActivity，通过 Intent 启动
                try {
                    val intent = Intent(context, Class.forName("com.fongmi.android.tv.ui.activity.ScanActivity"))
                    activity?.startActivity(intent)
                } catch (_: Exception) {
                }
            },
            onDismiss = { showCastDialog = false }
        )
    }

    // 信息对话框
    if (showInfoDialog) {
        InfoDialog(
            title = currentVod?.getName() ?: "",
            url = playerManager?.getUrl() ?: "",
            header = playerManager?.getHeaders()?.entries?.joinToString("\n") { "${it.key}: ${it.value}" } ?: "",
            onShare = { title ->
                if (activity != null) {
                    PlayerHelper.share(activity, playerManager?.getUrl(), playerManager?.getHeaders() ?: emptyMap(), title)
                }
            },
            onDismiss = { showInfoDialog = false }
        )
    }

    // 定时器对话框
    if (showTimerDialog) {
        val timer = com.fongmi.android.tv.utils.Timer.get()
        var timerTick by remember { mutableLongStateOf(timer.getTick()) }
        var timerRunning by remember { mutableStateOf(timer.isRunning) }

        // 设置 Timer 回调
        DisposableEffect(Unit) {
            timer.setCallback(object : com.fongmi.android.tv.utils.Timer.Callback {
                override fun onTick(tick: Long) {
                    timerTick = tick
                    timerRunning = true
                }
                override fun onFinish() {
                    timerRunning = false
                    timerTick = 0
                    showTimerDialog = false
                }
            })
            onDispose { timer.setCallback(null) }
        }

        TimerDialog(
            isRunning = timerRunning,
            tick = timerTick,
            onTimerSet = { minutes ->
                timer.set(java.util.concurrent.TimeUnit.MINUTES.toMillis(minutes.toLong()))
                showTimerDialog = false
            },
            onDelay = { timer.delay() },
            onReset = {
                timer.reset()
                showTimerDialog = false
            },
            onDismiss = { showTimerDialog = false }
        )
    }

    // 画质选择对话框
    if (showQualityDialog) {
        playerResult?.let { result ->
            QualityDialog(
                result = result,
                onQualityClick = { position ->
                    result.getUrl().set(position)
                    // 重新播放：使用当前 flag 和 episode 重新请求播放
                    val currentFlag = currentFlags.getOrNull(selectedFlagIndex)
                    val currentEpisode = currentFlag?.getEpisodes()?.getOrNull(selectedEpisodeIndex)
                    if (currentFlag != null && currentEpisode != null) {
                        val key = VodConfig.get().getHome()?.getKey() ?: ""
                        siteViewModel.playerContent(key, currentFlag.getFlag(), currentEpisode.getUrl())
                    }
                    showQualityDialog = false
                },
                onDismiss = { showQualityDialog = false }
            )
        }
    }

    // 解析源选择对话框
    if (showParseDialog) {
        val parses = VodConfig.get().getParses()
        ParseDialog(
            parses = parses,
            onParseClick = { parse ->
                VodConfig.get().setParse(parse)
                // 重新播放：使用当前 flag 和 episode 重新请求播放
                val currentFlag = currentFlags.getOrNull(selectedFlagIndex)
                val currentEpisode = currentFlag?.getEpisodes()?.getOrNull(selectedEpisodeIndex)
                if (currentFlag != null && currentEpisode != null) {
                    playerManager?.stop()
                    playerManager?.clearMediaItems()
                    val key = VodConfig.get().getHome()?.getKey() ?: ""
                    siteViewModel.playerContent(key, currentFlag.getFlag(), currentEpisode.getUrl())
                }
                showParseDialog = false
            },
            onDismiss = { showParseDialog = false }
        )
    }

    // 弹幕搜索对话框
    if (showDanmakuSearchDialog) {
        var searchResults by remember { mutableStateOf<List<Danmaku>>(emptyList()) }
        var isSearching by remember { mutableStateOf(false) }

        DanmakuSearchDialog(
            keyword = currentVod?.getName() ?: "",
            isLoading = isSearching,
            danmakus = searchResults,
            onSearch = { keyword ->
                isSearching = true
                searchResults = emptyList()
                val metadata = playerManager?.getMetadata()
                val artist = metadata?.artist?.toString() ?: ""
                com.fongmi.android.tv.api.DanmakuApi.newCall(keyword, artist).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                        com.fongmi.android.tv.App.post { isSearching = false }
                    }
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        response.use { res ->
                            val body = res.body?.string() ?: return@use
                            val items = Danmaku.arrayFrom(body)
                            com.fongmi.android.tv.App.post {
                                searchResults = items
                                isSearching = false
                            }
                        }
                    }
                })
            },
            onDanmakuClick = { danmaku ->
                playerManager?.setDanmaku(danmaku)
                showDanmakuSearchDialog = false
            },
            onDismiss = {
                com.fongmi.android.tv.api.DanmakuApi.cancel()
                showDanmakuSearchDialog = false
            }
        )
    }

    // 弹幕设置对话框
    if (showDanmakuSettingDialog) {
        DanmakuSettingDialog(
            alpha = DanmakuSetting.getTransparency(),
            fontSize = DanmakuSetting.getTextScale(),
            speed = DanmakuSetting.getDurationMs() / 8000f, // 归一化：8000ms = 1.0f
            show = DanmakuSetting.isShow(),
            onAlphaChange = {
                DanmakuSetting.putTransparency(it)
                playerManager?.setDanmakuConfig(DanmakuSetting.getConfig())
            },
            onFontSizeChange = {
                DanmakuSetting.putTextScale(it)
                playerManager?.setDanmakuConfig(DanmakuSetting.getConfig())
            },
            onSpeedChange = { normalizedSpeed ->
                // 将归一化的速度转换为 duration（毫秒）：speed 越大 = duration 越小 = 弹幕越快
                val durationMs = (8000f / normalizedSpeed).toLong().coerceIn(2000L, 20000L)
                DanmakuSetting.putDurationMs(durationMs)
                playerManager?.setDanmakuConfig(DanmakuSetting.getConfig())
            },
            onShowToggle = {
                DanmakuSetting.putShow(it)
                playerManager?.setDanmakuEnabled(it)
            },
            onDismiss = { showDanmakuSettingDialog = false }
        )
    }

    // 偏移对话框
    if (showOffsetDialog) {
        val currentOffset = if (trackType == C.TRACK_TYPE_TEXT) {
            (playerManager?.getTextOffsetMs() ?: 0L) / 1000f
        } else {
            (playerManager?.getAudioOffsetMs() ?: 0L) / 1000f
        }
        OffsetDialog(
            type = if (trackType == C.TRACK_TYPE_TEXT) 0 else 1,
            offset = currentOffset,
            onOffsetChange = { offset ->
                if (trackType == C.TRACK_TYPE_TEXT) {
                    playerManager?.setTextOffsetMs((offset * 1000).toLong())
                } else {
                    playerManager?.setAudioOffsetMs((offset * 1000).toLong())
                }
            },
            onReset = {
                if (trackType == C.TRACK_TYPE_TEXT) {
                    playerManager?.setTextOffsetMs(0L)
                } else {
                    playerManager?.setAudioOffsetMs(0L)
                }
            },
            onDismiss = { showOffsetDialog = false }
        )
    }

    // 字幕设置对话框
    if (showSubtitleDialog) {
        SubtitleDialog(
            onUp = {
                playerViewRef?.subtitleView?.let { sv ->
                    sv.addPosition(0.005f)
                    PlayerSetting.putSubtitlePosition(sv.position)
                }
            },
            onDown = {
                playerViewRef?.subtitleView?.let { sv ->
                    sv.subPosition(0.005f)
                    PlayerSetting.putSubtitlePosition(sv.position)
                }
            },
            onLarge = {
                playerViewRef?.subtitleView?.let { sv ->
                    sv.addTextSize(0.002f)
                    PlayerSetting.putSubtitleTextSize(sv.textSize)
                }
            },
            onSmall = {
                playerViewRef?.subtitleView?.let { sv ->
                    sv.subTextSize(0.002f)
                    PlayerSetting.putSubtitleTextSize(sv.textSize)
                }
            },
            onReset = {
                playerViewRef?.subtitleView?.let { sv ->
                    PlayerSetting.putSubtitleTextSize(0.0f)
                    PlayerSetting.putSubtitlePosition(0.0f)
                    sv.reset()
                }
            },
            onDismiss = { showSubtitleDialog = false }
        )
    }

    // 接收投屏对话框（对应 Java 原版 ReceiveDialog）
    if (showReceiveDialog && castEvent != null) {
        val event = castEvent!!
        val history = event.history()
        var isLoading by remember { mutableStateOf(false) }

        ReceiveDialog(
            deviceName = event.device().getName(),
            vodName = history.getVodName(),
            vodPic = history.getVodPic(),
            isLoading = isLoading,
            onReceive = {
                // 接收投屏（对应 Java 原版 onReceiveCast）
                if (VodConfig.get().getConfig()?.equals(event.config()) == true) {
                    // 配置相同，直接播放
                    VideoActivity.cast(context as Activity, history.save(VodConfig.getCid()))
                    showReceiveDialog = false
                } else {
                    // 配置不同，先加载配置
                    isLoading = true
                    VodConfig.load(event.config(), object : com.fongmi.android.tv.impl.Callback() {
                        override fun success() {
                            VideoActivity.cast(context as Activity, history.save(VodConfig.getCid()))
                            isLoading = false
                            showReceiveDialog = false
                        }

                        override fun error(msg: String) {
                            com.fongmi.android.tv.utils.Notify.show(msg)
                            isLoading = false
                        }
                    })
                }
            },
            onDismiss = { showReceiveDialog = false }
        )
    }
}

/**
 * 获取历史记录 Key
 */
private fun getHistoryKey(vodId: String): String {
    val cid = VodConfig.getCid()
    return "${VodConfig.get().getHome()?.getKey() ?: ""}$$vodId$$cid"
}

/**
 * 创建收藏
 */
private fun createKeep(vod: Vod?, vodId: String) {
    if (vod == null) return
    val keep = Keep()
    keep.setKey(getHistoryKey(vodId))
    keep.setSiteName(VodConfig.get().getHome()?.getName() ?: "")
    keep.setVodName(vod.getName())
    keep.setVodPic(vod.getPic())
    keep.save()
}

/**
 * 线路选择 - 切换线路后无缝衔接到对应集
 */
private fun onFlagSelected(
    flag: Flag,
    history: History?,
    siteViewModel: SiteViewModel,
    onEpisodeIndexChange: (Int) -> Unit
) {
    if (flag.isSelected) return
    // 在新线路中查找与当前集名匹配的集（无缝衔接）
    val remarks = history?.getVodRemarks() ?: ""
    val episode = flag.find(remarks, false)
    if (episode != null) {
        val episodeIndex = flag.getEpisodes().indexOf(episode)
        if (episodeIndex >= 0) onEpisodeIndexChange(episodeIndex)
        // 更新历史记录
        history?.setVodFlag(flag.getFlag())
        history?.setVodRemarks(episode.getName())
        // 发起播放请求
        val key = VodConfig.get().getHome()?.getKey() ?: ""
        siteViewModel.playerContent(key, flag.getFlag(), episode.getUrl())
    }
}

/**
 * 剧集选择 - 播放指定剧集
 */
private fun onEpisodeSelected(
    episode: Episode,
    flag: Flag?,
    history: History?,
    siteViewModel: SiteViewModel
) {
    // 更新历史记录
    history?.setVodRemarks(episode.getName())
    history?.setEpisodeUrl(episode.getUrl())
    // 发起播放请求
    val key = VodConfig.get().getHome()?.getKey() ?: ""
    val flagName = flag?.getFlag() ?: ""
    siteViewModel.playerContent(key, flagName, episode.getUrl())
}

/**
 * 上一集
 */
private fun checkPrev(
    flags: List<Flag>,
    selectedFlagIndex: Int,
    selectedEpisodeIndex: Int,
    onIndexChange: (Int) -> Unit
) {
    if (selectedEpisodeIndex > 0) {
        onIndexChange(selectedEpisodeIndex - 1)
    }
}

/**
 * 下一集
 */
private fun checkNext(
    flags: List<Flag>,
    selectedFlagIndex: Int,
    selectedEpisodeIndex: Int,
    onIndexChange: (Int) -> Unit
) {
    val currentFlag = flags.getOrNull(selectedFlagIndex)
    val episodes = currentFlag?.getEpisodes() ?: emptyList()
    if (selectedEpisodeIndex < episodes.size - 1) {
        onIndexChange(selectedEpisodeIndex + 1)
    }
}

/**
 * 获取轨道列表
 */
private fun getTracks(playerManager: PlayerManager?, type: Int): List<Track> {
    if (playerManager == null) return emptyList()
    val tracks = mutableListOf<Track>()
    val groups = playerManager.getCurrentTracks()?.groups ?: return emptyList()
    for (i in groups.indices) {
        val group = groups[i]
        if (group.type != type) continue
        for (j in 0 until group.length) {
            val format = group.getTrackFormat(j)
            val name = PlayerHelper.describeFormat(format)
            val track = Track(type, name, format.toString())
            track.setSelected(group.isTrackSelected(j))
            tracks.add(track)
        }
    }
    return tracks
}

/**
 * 剧集标签
 */
@Composable
private fun EpisodeChip(
    episode: Episode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = episode.getName(),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/**
 * 信息标签
 */
@Composable
private fun InfoChip(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

