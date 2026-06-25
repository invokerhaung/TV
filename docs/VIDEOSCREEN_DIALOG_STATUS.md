# VideoScreen Dialog 实现状态

## 一、Dialog 列表及实现状态

| Dialog | 状态 | 空实现方法 | 说明 |
|--------|------|-----------|------|
| **ControlDialog** | ✅ 已实现 | 无 | 所有回调已连接 |
| **EpisodeGridDialog** | ✅ 已实现 | 无 | 剧集选择已连接 |
| **DanmakuDialog** | ⚠️ 部分实现 | `onSearchClick`、`onSettingClick`、`onChooseClick` | 弹幕数据为空 |
| **TitleDialog** | ⚠️ 部分实现 | `onTitleClick` | 标题数据为空 |
| **TrackDialog** | ⚠️ 部分实现 | `onOffsetClick`、`onChooseClick`、`onSubtitleClick` | 轨道数据为空 |
| **CastDialog** | ⚠️ 部分实现 | `onDeviceClick`、`onRefresh`、`onScan` | 设备列表为空 |
| **InfoDialog** | ✅ 已实现 | 无 | 信息显示完整 |
| **TimerDialog** | ⚠️ 部分实现 | `onTimerSet`、`onDelay`、`onReset` | 定时器逻辑为空 |
| **QualityDialog** | ⚠️ 部分实现 | `onQualityClick` | 画质切换逻辑为空 |
| **ParseDialog** | ⚠️ 部分实现 | `onParseClick` | 解析源切换逻辑为空 |

---

## 二、空实现方法详情

### 2.1 DanmakuDialog

```kotlin
DanmakuDialog(
    danmakus = emptyList(), // TODO: 获取弹幕列表
    showSearch = DanmakuApi.canSearch(),
    onDanmakuClick = { danmaku ->
        playerManager?.setDanmaku(if (danmaku.isSelected()) Danmaku.empty() else danmaku)
        showDanmakuDialog = false
    },
    onSearchClick = {
        // 空实现 - 需要打开弹幕搜索对话框
    },
    onSettingClick = {
        // 空实现 - 需要打开弹幕设置对话框
    },
    onChooseClick = {
        // 空实现 - 需要打开文件选择器
    },
    onDismiss = { showDanmakuDialog = false }
)
```

### 2.2 TitleDialog

```kotlin
TitleDialog(
    titles = emptyList(), // TODO: 获取标题列表
    onTitleClick = { index ->
        // 空实现 - 需要切换标题
        // playerManager?.setTitle(titles[index])
        showTitleDialog = false
    },
    onDismiss = { showTitleDialog = false }
)
```

### 2.3 TrackDialog

```kotlin
TrackDialog(
    title = when (trackType) {
        C.TRACK_TYPE_TEXT -> "字幕"
        C.TRACK_TYPE_AUDIO -> "音频"
        C.TRACK_TYPE_VIDEO -> "视频"
        else -> "轨道"
    },
    tracks = emptyList(), // TODO: 获取轨道列表
    showOffset = trackType == C.TRACK_TYPE_TEXT,
    showChoose = trackType == C.TRACK_TYPE_TEXT,
    showSubtitle = trackType == C.TRACK_TYPE_TEXT,
    onTrackClick = { track ->
        playerManager?.setTrack(listOf(track))
        showTrackDialog = false
    },
    onOffsetClick = {
        // 空实现 - 需要打开偏移对话框
    },
    onChooseClick = {
        // 空实现 - 需要打开文件选择器
    },
    onSubtitleClick = {
        // 空实现 - 需要打开字幕设置
    },
    onDismiss = { showTrackDialog = false }
)
```

### 2.4 CastDialog

```kotlin
CastDialog(
    devices = emptyList(), // TODO: 获取设备列表
    onDeviceClick = { device ->
        // 空实现 - 需要投屏到设备
        showCastDialog = false
    },
    onRefresh = { /* 空实现 - 刷新设备列表 */ },
    onScan = { /* 空实现 - 扫描设备 */ },
    onDismiss = { showCastDialog = false }
)
```

### 2.5 TimerDialog

```kotlin
TimerDialog(
    isRunning = false,
    tick = 0,
    onTimerSet = { minutes ->
        // 空实现 - 需要设置定时器
        showTimerDialog = false
    },
    onDelay = { /* 空实现 - 延迟定时器 */ },
    onReset = { /* 空实现 - 重置定时器 */ },
    onDismiss = { showTimerDialog = false }
)
```

### 2.6 QualityDialog

```kotlin
QualityDialog(
    result = result,
    onQualityClick = { position ->
        // 空实现 - 需要切换画质
        // result.getUrl().set(position)
        // 重新播放
    },
    onDismiss = { showQualityDialog = false }
)
```

### 2.7 ParseDialog

```kotlin
ParseDialog(
    parses = VodConfig.get().getParses(),
    onParseClick = { parse ->
        // 空实现 - 需要切换解析源
        // VodConfig.get().setParse(parse)
        // 重新播放
    },
    onDismiss = { showParseDialog = false }
)
```

---

## 三、其他空实现

### 3.1 线路切换逻辑

```kotlin
private fun onFlagSelected(flag: Flag, history: History?) {
    // TODO: 实现线路切换逻辑
}
```

### 3.2 剧集播放逻辑

```kotlin
private fun onEpisodeSelected(episode: Episode, history: History?) {
    // TODO: 实现剧集播放逻辑
}
```

### 3.3 反转剧集

```kotlin
Text(
    text = "反转",
    modifier = Modifier
        .clickable { /* 反转剧集 */ }
        .padding(horizontal = 8.dp)
)
```

---

## 四、需要连接的数据

| 数据 | 来源 | 说明 |
|------|------|------|
| 弹幕列表 | `playerManager?.getDanmakus()` | 需要连接 PlayerManager |
| 标题列表 | `playerManager?.getCurrentMediaTitles()` | 需要连接 PlayerManager |
| 轨道列表 | `playerManager?.getCurrentTracks()` | 需要连接 PlayerManager |
| 设备列表 | `DLNACastManager` | 需要连接 DLNA 模块 |
| 画质列表 | `result.getUrl().getValues()` | 需要连接 Result |
| 解析源列表 | `VodConfig.get().getParses()` | 需要连接 VodConfig |

---

## 五、总结

| 类别 | 数量 | 百分比 |
|------|------|--------|
| ✅ 完全实现 | 3 | 30% |
| ⚠️ 部分实现 | 7 | 70% |
| ❌ 未实现 | 0 | 0% |

**需要连接的数据：**
- 弹幕数据
- 标题数据
- 轨道数据
- 设备数据

**需要实现的逻辑：**
- 线路切换
- 剧集播放
- 画质切换
- 解析源切换
- 定时器控制
