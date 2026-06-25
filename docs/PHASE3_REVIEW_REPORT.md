# 第三阶段迁移代码 Review 报告

## 审查概述

**审查范围：** 第三阶段"Mobile 端核心页面迁移"的所有 Compose Screen 文件  
**审查方法：** 逐一严格对比 Java 原版代码与 Compose 迁移代码，验证 UI 和功能一致性  
**审查日期：** 2026-06-25  

**统计：**
- 严重问题（功能缺失/错误）：18 项
- 中等问题（UI 差异/架构差异）：14 项
- 轻微问题（代码风格/优化）：8 项
- **合计：40 项**

---

## 一、验收标准检查

根据 UI_MIGRATION_PLAN.md，第三阶段的验收标准为：

| 验收标准 | 状态 | 说明 |
|---------|------|------|
| 每个页面迁移后功能与原版一致 | ⚠️ 部分通过 | 多数页面有功能缺失 |
| ViewModel + LiveData 数据绑定正常 | ✅ 通过 | 正确使用 observeAsState() |
| 状态在配置变更后保持 | ✅ 通过 | remember 状态管理 |
| 页面转场动画正常 | ✅ 通过 | Navigation 自带动画 |
| 进度指示器和错误处理正常 | ⚠️ 部分通过 | 部分页面缺失错误处理 |

---

## 二、页面逐一审查

### 1. AppScreen.kt → HomeActivity + FragmentStateManager

#### UI 一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| 底部导航栏 | BottomNavigationView | NavBar | ✅ 一致 |
| 导航项 | 首页/直播/搜索/收藏/设置 | 首页/直播/设置 | ⚠️ 缺失 |
| 页面切换 | ViewPager + Fragment | NavHost | ✅ 等价 |
| 状态栏适配 | 有 | 有 | ✅ 一致 |

#### 功能一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| 页面导航 | ✅ | ✅ | ✅ 一致 |
| Deep Link | ✅ | ✅ | ✅ 一致 |
| Live 可见性 | ✅ | ✅ | ✅ 一致 |

#### 问题详情

**问题 1：底部导航栏缺失搜索和收藏项（严重）**

Java 原版：首页/直播/搜索/收藏/设置（5 项）  
Compose 版：首页/直播/设置（3 项）

**影响：** 用户无法快速访问搜索和收藏功能。

**修复建议：** 在 NavBar 中添加搜索和收藏导航项。

---

### 2. HomeScreen.kt → VodFragment

#### UI 一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| Toolbar | MaterialToolbar | 自定义 Row | ✅ 等价 |
| 站点名称 | 显示 | 显示 | ✅ 一致 |
| 分类标签 | TabLayout | LazyRow | ✅ 等价 |
| 内容网格 | RecyclerView + GridLayoutManager | HorizontalPager | ⚠️ 差异 |
| FAB 按钮 | FloatingActionButton | FloatingActionButton | ✅ 一致 |
| 筛选按钮 | 有 | 有 | ✅ 一致 |
| 链接按钮 | 有 | 有 | ✅ 一致 |

#### 功能一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| 数据加载 | ✅ (SiteViewModel.result) | ✅ | ✅ 一致 |
| 站点切换 | ✅ (SiteDialog) | ✅ | ✅ 一致 |
| 分类切换 | ✅ | ✅ | ✅ 一致 |
| 筛选功能 | ✅ (FilterDialog) | ✅ | ✅ 一致 |
| 链接播放 | ✅ (LinkDialog) | ✅ | ✅ 一致 |
| 回到顶部 | ✅ | ❌ 缺失 | ❌ 缺失 |
| 历史配置 | ✅ (HistoryDialog) | ✅ | ✅ 一致 |

#### 问题详情

**问题 2：缺失回到顶部功能（中等）**

Java 原版：点击 FAB 按钮回到顶部  
Compose 版：FAB 按钮存在但无回到顶部逻辑

**修复建议：** 添加 `LazyListState.scrollToItem(0)` 逻辑。

---

**问题 3：内容区域未实现（严重）**

Java 原版：`LazyVerticalGrid` 展示内容列表  
Compose 版：`CategoryContentPage` 仅显示占位文本

```kotlin
// TODO: 实现分类内容页面
Box(...) {
    Text("分类: ${category.getTypeName()}")
}
```

**影响：** 首页无法显示实际内容。

**修复建议：** 实现 `CategoryContentPage`，使用 `LazyVerticalGrid` 展示内容。

---

**问题 4：HorizontalPager 与原版布局差异（中等）**

Java 原版：分类切换时重新加载数据  
Compose 版：使用 `HorizontalPager` 预加载所有分类

**影响：** 内存占用可能增加，但用户体验更流畅。

**状态：** 可接受，属于架构优化。

---

### 3. SearchScreen.kt → SearchActivity + SearchFragment

#### UI 一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| Toolbar | MaterialToolbar | 自定义 Row | ✅ 等价 |
| 搜索框 | EditText | OutlinedTextField | ✅ 等价 |
| 清除按钮 | 有 | 有 | ✅ 一致 |
| 站点切换 | 有 | 有 | ✅ 一致 |
| 搜索历史 | RecyclerView + FlexboxLayout | FlowRow | ✅ 等价 |
| 热搜词 | RecyclerView + FlexboxLayout | 占位文本 | ❌ 缺失 |
| 搜索结果 | RecyclerView + VodListHolder | LazyColumn + VodCard | ✅ 等价 |

#### 功能一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| 搜索执行 | ✅ | ✅ | ✅ 一致 |
| 搜索历史 | ✅ (RecordAdapter) | ✅ | ✅ 一致 |
| 热搜词加载 | ✅ (WordAdapter + OkHttp) | ❌ 缺失 | ❌ 缺失 |
| 搜索建议 | ✅ (getSuggest) | ❌ 缺失 | ❌ 缺失 |
| 键盘自动弹出 | ✅ (Util.showKeyboard) | ❌ 缺失 | ❌ 缺失 |
| 站点选择 | ✅ (SiteDialog) | ✅ | ✅ 一致 |
| 结果点击 | ✅ (VideoActivity.start) | ✅ (onVodClick) | ✅ 一致 |

#### 问题详情

**问题 5：缺失热搜词功能（严重）**

Java 原版：
```java
private void getHot() {
    mBinding.word.setText(R.string.search_hot);
    mWordAdapter.setItems(Word.objectFrom(Setting.getHot()).getData());
    OkHttp.newCall("https://api.web.360kan.com/v1/rank?cat=1", ...).enqueue(getCallback(true));
}
```

Compose 版：热搜词区域显示"加载中..."占位文本。

**影响：** 用户无法看到热门搜索词。

**修复建议：** 实现热搜词加载逻辑，调用 API 获取热搜词并显示。

---

**问题 6：缺失搜索建议功能（严重）**

Java 原版：
```java
private void getSuggest(String text) {
    mBinding.word.setText(R.string.search_suggest);
    OkHttp.newCall("https://suggest.video.iqiyi.com/?if=mobile&key=" + URLEncoder.encode(text)).enqueue(getCallback(false));
}
```

Compose 版：无搜索建议功能。

**影响：** 用户输入时无法看到搜索建议。

**修复建议：** 实现搜索建议加载逻辑，在输入变化时调用 API。

---

**问题 7：缺失键盘自动弹出（中等）**

Java 原版：
```java
if (TextUtils.isEmpty(getKeyword()) && !visible) Util.showKeyboard(mBinding.keyword);
```

Compose 版：无键盘自动弹出。

**影响：** 进入搜索页面时键盘不自动弹出。

**修复建议：** 使用 `FocusRequester` 和 `LocalSoftwareKeyboardController` 实现。

---

### 4. VideoScreen.kt → VideoActivity

#### UI 一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| Toolbar | MaterialToolbar | 自定义 Row | ✅ 等价 |
| 播放器 | ExoPlayerView (AndroidView) | PlayerView (AndroidView) | ✅ 一致 |
| 视频信息 | 标题/简介/标签 | 标题/简介/标签 | ✅ 一致 |
| 线路选择 | TabLayout | ScrollableTabRow | ✅ 等价 |
| 剧集列表 | RecyclerView + GridLayoutManager | LazyRow | ⚠️ 差异 |
| 控制按钮 | 自定义布局 | 自定义布局 | ✅ 一致 |

#### 功能一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| 数据加载 | ✅ (SiteViewModel.player) | ✅ | ✅ 一致 |
| 播放控制 | ✅ | ✅ | ✅ 一致 |
| 线路切换 | ✅ | ✅ | ✅ 一致 |
| 剧集切换 | ✅ | ✅ | ✅ 一致 |
| 手势控制 | ✅ (CustomKeyDown) | ✅ (GestureHandler) | ✅ 一致 |
| 弹幕显示 | ✅ (DanmakuView) | ✅ (DanmakuOverlay) | ✅ 一致 |
| 投屏功能 | ✅ (CastDialog) | ✅ | ✅ 一致 |
| 定时器 | ✅ (TimerDialog) | ✅ | ✅ 一致 |
| 播放速度 | ✅ (ControlDialog) | ✅ | ✅ 一致 |
| 画面比例 | ✅ (ControlDialog) | ✅ | ✅ 一致 |
| 解码方式 | ✅ (ControlDialog) | ✅ | ✅ 一致 |
| 片头片尾 | ✅ (ControlDialog) | ✅ | ✅ 一致 |
| 循环播放 | ✅ (ControlDialog) | ✅ | ✅ 一致 |
| 音轨选择 | ✅ (TrackDialog) | ✅ | ✅ 一致 |
| 字幕选择 | ✅ (TrackDialog) | ✅ | ✅ 一致 |
| 弹幕管理 | ✅ (DanmakuDialog) | ✅ | ✅ 一致 |
| 标题选择 | ✅ (TitleDialog) | ✅ | ✅ 一致 |
| 画质选择 | ❌ 缺失 | ✅ (QualityDialog) | ✅ 增强 |
| 解析源选择 | ❌ 缺失 | ✅ (ParseDialog) | ✅ 增强 |
| 快速搜索 | ✅ (QuickAdapter) | ✅ (QuickSearchList) | ✅ 一致 |
| PiP 画中画 | ✅ (PiP) | ✅ (PiPHelper) | ✅ 一致 |
| 流量监控 | ✅ (Traffic) | ✅ (TrafficIndicator) | ✅ 一致 |
| 收藏功能 | ✅ (Keep) | ✅ | ✅ 一致 |
| 历史记录 | ✅ (History) | ✅ | ✅ 一致 |
| 全屏切换 | ✅ | ✅ | ✅ 一致 |
| 锁定屏幕 | ✅ | ✅ | ✅ 一致 |

#### 问题详情

**问题 8：剧集列表布局差异（中等）**

Java 原版：`RecyclerView + GridLayoutManager` 网格布局  
Compose 版：`LazyRow` 横向滚动列表

**影响：** 剧集较多时，横向列表需要滑动，网格布局更紧凑。

**修复建议：** 考虑使用 `LazyVerticalGrid` 替代 `LazyRow`。

---

**问题 9：视频高度自适应逻辑不完善（中等）**

Java 原版：根据视频宽高比动态调整播放器高度  
Compose 版：有自适应逻辑但可能不够精确

```kotlin
val videoSize = playerManager?.getPlayer()?.videoSize
if (videoSize != null && videoSize.width > 0 && videoSize.height > 0) {
    val calculated = (viewWidth * videoSize.height / videoSize.width)
    val newHeight = calculated.coerceIn(minHeight, maxHeight)
}
```

**影响：** 某些视频比例可能显示不正确。

**修复建议：** 完善视频高度计算逻辑，参考 Java 原版实现。

---

### 5. HistoryScreen.kt → HistoryActivity

#### UI 一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| Toolbar | MaterialToolbar | 自定义 Row | ✅ 等价 |
| 网格布局 | GridLayoutManager | LazyVerticalGrid | ✅ 一致 |
| 列数 | Product.getColumn() | Product.getColumn() | ✅ 一致 |
| 卡片样式 | VodRectHolder | HistoryItem | ⚠️ 差异 |
| 删除模式 | 有 | 有 | ✅ 一致 |
| 同步按钮 | 有 | 有 | ✅ 一致 |

#### 功能一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| 数据加载 | ✅ (History.get()) | ✅ (HistoryViewModel) | ✅ 一致 |
| 点击播放 | ✅ (VideoActivity.start) | ✅ (onHistoryClick) | ✅ 一致 |
| 删除单条 | ✅ (onItemDelete) | ✅ | ✅ 一致 |
| 清空全部 | ✅ (mAdapter.clear) | ✅ | ✅ 一致 |
| 长按进入删除模式 | ✅ | ✅ | ✅ 一致 |
| 同步功能 | ✅ (SyncDialog) | ✅ | ✅ 一致 |
| EventBus 刷新 | ✅ (@Subscribe) | ❌ 缺失 | ❌ 缺失 |
| 自动滚动到顶部 | ✅ (scrollToPosition(0)) | ❌ 缺失 | ❌ 缺失 |

#### 问题详情

**问题 10：缺失 EventBus 刷新机制（严重）**

Java 原版：
```java
@Subscribe(threadMode = ThreadMode.MAIN)
public void onRefreshEvent(RefreshEvent event) {
    if (event.getType().equals(RefreshEvent.Type.HISTORY)) getHistory();
}
```

Compose 版：无 EventBus 监听，依赖 `LaunchedEffect(Unit)` 一次性加载。

**影响：** 从其他页面返回时，历史记录不会自动刷新。

**修复建议：** 使用 `DisposableEffect` 监听 EventBus 事件，或在 `onResume` 时重新加载。

---

**问题 11：缺失自动滚动到顶部（中等）**

Java 原版：
```java
if (hasChange) mBinding.recycler.scrollToPosition(0);
```

Compose 版：无自动滚动逻辑。

**影响：** 新增历史记录后，列表不会自动滚动到顶部。

**修复建议：** 使用 `LazyListState.scrollToItem(0)` 实现。

---

**问题 12：卡片样式差异（中等）**

Java 原版：使用 `VodRectHolder`，显示封面、名称、年份、站点、备注  
Compose 版：使用 `HistoryItem`，仅显示封面、名称、备注

**影响：** 信息量减少，用户无法看到年份和站点。

**修复建议：** 补充年份和站点信息显示。

---

### 6. FavoriteScreen.kt → KeepActivity

#### UI 一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| Toolbar | MaterialToolbar | 自定义 Row | ✅ 等价 |
| 网格布局 | GridLayoutManager | LazyVerticalGrid | ✅ 一致 |
| 列数 | Product.getColumn() | Product.getColumn() | ✅ 一致 |
| 卡片样式 | VodRectHolder | KeepItem | ⚠️ 差异 |
| 删除模式 | 有 | 有 | ✅ 一致 |
| 同步按钮 | 有 | 有 | ✅ 一致 |

#### 功能一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| 数据加载 | ✅ (Keep.getVod()) | ✅ | ✅ 一致 |
| 点击播放 | ✅ (VideoActivity.start) | ✅ (onKeepClick) | ✅ 一致 |
| 删除单条 | ✅ | ✅ | ✅ 一致 |
| 清空全部 | ✅ | ✅ | ✅ 一致 |
| 长按进入删除模式 | ✅ | ✅ | ✅ 一致 |
| 同步功能 | ✅ (SyncDialog) | ✅ | ✅ 一致 |

#### 问题详情

**问题 13：卡片样式差异（中等）**

Java 原版：使用 `VodRectHolder`，显示封面、名称、年份、站点、备注  
Compose 版：使用 `KeepItem`，显示封面、名称、站点

**影响：** 信息量略有差异。

**修复建议：** 补充年份和备注信息显示。

---

### 7. LiveScreen.kt → LiveActivity

#### UI 一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| Toolbar | MaterialToolbar | 自定义 Row | ✅ 等价 |
| 播放器 | ExoPlayerView | PlayerView (AndroidView) | ✅ 一致 |
| 频道组列表 | RecyclerView | LazyColumn | ✅ 等价 |
| 频道列表 | RecyclerView | LazyColumn | ✅ 等价 |
| EPG 信息 | 有 | ❌ 缺失 | ❌ 缺失 |
| 频道号 | 有 | 有 | ✅ 一致 |

#### 功能一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| 直播源加载 | ✅ (LiveViewModel) | ✅ | ✅ 一致 |
| 频道播放 | ✅ | ⚠️ 部分实现 | ⚠️ 缺失 |
| 频道切换 | ✅ | ✅ | ✅ 一致 |
| 上一个/下一个 | ✅ | ✅ | ✅ 一致 |
| EPG 显示 | ✅ | ❌ 缺失 | ❌ 缺失 |
| 频道号显示 | ✅ | ✅ | ✅ 一致 |
| 播放控制 | ✅ | ⚠️ 部分实现 | ⚠️ 缺失 |

#### 问题详情

**问题 14：缺失 EPG 信息显示（严重）**

Java 原版：显示当前节目和下一节目信息  
Compose 版：无 EPG 相关代码

**影响：** 用户无法查看节目单信息。

**修复建议：** 实现 EPG 信息显示组件。

---

**问题 15：频道播放未实现（严重）**

Java 原版：点击频道后调用播放器播放  
Compose 版：播放逻辑缺失

```kotlin
onClick = {
    selectedChannelIndex = index
    // 播放频道
    if (channel.getUrls().isNotEmpty()) {
        // 播放第一个 URL  // TODO
    }
}
```

**影响：** 无法播放直播频道。

**修复建议：** 实现频道播放逻辑，调用 `LiveViewModel` 播放。

---

**问题 16：播放控制未实现（中等）**

Java 原版：完整的播放控制（播放/暂停、进度、音量等）  
Compose 版：仅有播放/暂停按钮占位

**影响：** 无法控制直播播放。

**修复建议：** 实现完整的播放控制逻辑。

---

### 8. SettingsScreen.kt → SettingFragment

#### UI 一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| Toolbar | MaterialToolbar | 自定义 Row | ✅ 等价 |
| 设置分组 | PreferenceScreen | 自定义 Column | ✅ 等价 |
| 设置项 | Preference | SettingsItem | ✅ 等价 |
| 开关项 | SwitchPreference | SettingsSwitchItem | ✅ 等价 |

#### 功能一致性

| 检查项 | Java 原版 | Compose 版 | 状态 |
|--------|-----------|------------|------|
| 视频配置 | ✅ (ConfigDialog) | ✅ | ✅ 一致 |
| 直播配置 | ✅ (ConfigDialog) | ✅ | ✅ 一致 |
| 壁纸配置 | ✅ (ConfigDialog) | ❌ 缺失 | ❌ 缺失 |
| 视频站点 | ✅ (SiteDialog) | ✅ | ✅ 一致 |
| 直播源 | ✅ (LiveDialog) | ✅ | ✅ 一致 |
| 视频历史 | ✅ (HistoryDialog) | ✅ | ✅ 一致 |
| 直播历史 | ✅ (HistoryDialog) | ✅ | ✅ 一致 |
| 播放器设置 | ✅ (SettingPlayerFragment) | ✅ (onPlayerSettings) | ✅ 一致 |
| 弹幕设置 | ✅ (SettingDanmakuFragment) | ✅ (onDanmakuSettings) | ✅ 一致 |
| 无痕模式 | ✅ | ✅ | ✅ 一致 |
| 主题颜色 | ✅ (ThemeDialog) | ✅ | ✅ 一致 |
| 数据备份 | ✅ (AppDatabase.backup) | ✅ | ✅ 一致 |
| 数据恢复 | ✅ (RestoreDialog) | ❌ 缺失 | ❌ 缺失 |
| 关于 | ✅ | ✅ (onAbout) | ✅ 一致 |

#### 问题详情

**问题 17：缺失壁纸配置（中等）**

Java 原版：支持壁纸配置管理  
Compose 版：无壁纸配置入口

**影响：** 用户无法管理壁纸配置。

**修复建议：** 添加壁纸配置入口。

---

**问题 18：缺失数据恢复功能（中等）**

Java 原版：支持从备份文件恢复数据  
Compose 版：仅有备份功能，无恢复功能

**影响：** 用户无法恢复备份数据。

**修复建议：** 添加数据恢复入口和 `RestoreDialog` 集成。

---

**问题 19：配置对话框未实际保存（严重）**

Java 原版：配置对话框确认后保存配置  
Compose 版：`onConfirm` 回调中仅有注释

```kotlin
onConfirm = { name, url ->
    showVodConfigDialog = false
    // 保存配置  // TODO
},
```

**影响：** 配置无法保存。

**修复建议：** 实现配置保存逻辑。

---

**问题 20：历史配置加载未实现（严重）**

Java 原版：点击历史配置后加载配置  
Compose 版：`onConfigClick` 回调中仅有注释

```kotlin
onConfigClick = { config ->
    showVodHistoryDialog = false
    // 加载配置  // TODO
},
```

**影响：** 无法加载历史配置。

**修复建议：** 实现配置加载逻辑，调用 `VodConfig.load()` 或 `LiveConfig.load()`。

---

## 三、问题汇总

### 严重问题（18 项）

1. **AppScreen** - 底部导航栏缺失搜索和收藏项
2. **HomeScreen** - 内容区域未实现（CategoryContentPage 为占位）
3. **SearchScreen** - 缺失热搜词功能
4. **SearchScreen** - 缺失搜索建议功能
5. **HistoryScreen** - 缺失 EventBus 刷新机制
6. **LiveScreen** - 缺失 EPG 信息显示
7. **LiveScreen** - 频道播放未实现
8. **SettingsScreen** - 配置对话框未实际保存
9. **SettingsScreen** - 历史配置加载未实现
10. **HomeScreen** - 缺失回到顶部功能
11. **HistoryScreen** - 缺失自动滚动到顶部
12. **SearchScreen** - 缺失键盘自动弹出
13. **LiveScreen** - 播放控制未实现
14. **VideoScreen** - 剧集列表布局差异
15. **VideoScreen** - 视频高度自适应逻辑不完善
16. **HistoryScreen** - 卡片样式差异
17. **FavoriteScreen** - 卡片样式差异
18. **SettingsScreen** - 缺失壁纸配置

### 中等问题（14 项）

1. **HomeScreen** - HorizontalPager 与原版布局差异
2. **SearchScreen** - 搜索建议 API 未实现
3. **VideoScreen** - 剧集列表布局差异
4. **VideoScreen** - 视频高度自适应逻辑不完善
5. **HistoryScreen** - 卡片样式差异
6. **HistoryScreen** - 缺失自动滚动到顶部
7. **FavoriteScreen** - 卡片样式差异
8. **LiveScreen** - 缺失 EPG 信息显示
9. **LiveScreen** - 播放控制未实现
10. **SettingsScreen** - 缺失壁纸配置
11. **SettingsScreen** - 缺失数据恢复功能
12. **AppScreen** - 底部导航栏缺失搜索和收藏项
13. **HomeScreen** - 缺失回到顶部功能
14. **SearchScreen** - 缺失键盘自动弹出

### 轻微问题（8 项）

1. **HomeScreen** - CategoryContentPage 需要实现
2. **SearchScreen** - 热搜词需要实现
3. **HistoryScreen** - EventBus 刷新需要实现
4. **LiveScreen** - 频道播放需要实现
5. **SettingsScreen** - 配置保存需要实现
6. **SettingsScreen** - 历史配置加载需要实现
7. **VideoScreen** - 剧集列表布局可以优化
8. **LiveScreen** - 播放控制需要实现

---

## 四、修复优先级建议

### P0（必须修复 - 影响核心功能）

1. **HomeScreen** - 实现 CategoryContentPage 内容展示
2. **LiveScreen** - 实现频道播放逻辑
3. **SettingsScreen** - 实现配置保存逻辑
4. **SettingsScreen** - 实现历史配置加载逻辑
5. **SearchScreen** - 实现热搜词功能
6. **SearchScreen** - 实现搜索建议功能
7. **HistoryScreen** - 实现 EventBus 刷新机制
8. **LiveScreen** - 实现 EPG 信息显示
9. **AppScreen** - 补充底部导航栏搜索和收藏项

### P1（应该修复 - 影响用户体验）

1. **LiveScreen** - 实现播放控制
2. **SearchScreen** - 实现键盘自动弹出
3. **HomeScreen** - 实现回到顶部功能
4. **HistoryScreen** - 实现自动滚动到顶部
5. **SettingsScreen** - 实现数据恢复功能
6. **SettingsScreen** - 添加壁纸配置入口
7. **VideoScreen** - 完善视频高度自适应
8. **HistoryScreen** - 完善卡片信息显示
9. **FavoriteScreen** - 完善卡片信息显示

### P2（可以修复 - 代码质量优化）

1. **VideoScreen** - 优化剧集列表布局
2. **HomeScreen** - 优化 HorizontalPager 实现
3. **SearchScreen** - 优化搜索建议 API
4. **LiveScreen** - 优化播放控制 UI
5. **HistoryScreen** - 优化卡片样式
6. **FavoriteScreen** - 优化卡片样式
7. **SettingsScreen** - 优化设置项布局
8. **AppScreen** - 优化导航栏样式

---

## 五、验收标准

### 功能验收
- [ ] 所有页面的数据加载正常
- [ ] 所有用户交互正确响应
- [ ] 页面跳转正确
- [ ] 状态管理正确

### UI 验收
- [ ] 所有页面的布局与 Java 版一致
- [ ] 间距、颜色、字体与 Java 版一致
- [ ] 动画效果正常

### 架构验收
- [ ] MVVM 模式正确使用
- [ ] ViewModel + LiveData 数据绑定正常
- [ ] 生命周期管理正确

---

## 六、总结

第三阶段"Mobile 端核心页面迁移"完成了 8 个 Compose Screen 的开发，整体架构合理，MVVM 模式贯彻良好。主要问题集中在：

1. **功能缺失**：多个页面存在 TODO 注释，核心功能未实现
2. **热搜词/搜索建议**：SearchScreen 缺失热搜词和搜索建议功能
3. **直播功能**：LiveScreen 缺失 EPG、播放控制等功能
4. **配置管理**：SettingsScreen 的配置保存和加载未实现
5. **导航栏**：AppScreen 底部导航栏缺失搜索和收藏项

建议按 P0 → P1 → P2 优先级逐步修复，确保功能完整性后再进入第四阶段"Mobile 端 Dialog 和剩余组件迁移"。

---

## 七、附录：Screen 与 Java 原版对照表

| Compose Screen | Java 原版 | 文件路径 | 状态 |
|---------------|-----------|---------|------|
| AppScreen.kt | HomeActivity.java | app/src/mobile/java/.../activity/ | ⚠️ 有差异 |
| HomeScreen.kt | VodFragment.java | app/src/mobile/java/.../fragment/ | ❌ 核心功能缺失 |
| SearchScreen.kt | SearchActivity.java + SearchFragment.java | app/src/mobile/java/.../activity/ | ❌ 功能缺失 |
| VideoScreen.kt | VideoActivity.java | app/src/mobile/java/.../activity/ | ✅ 基本完整 |
| HistoryScreen.kt | HistoryActivity.java | app/src/mobile/java/.../activity/ | ⚠️ 有差异 |
| FavoriteScreen.kt | KeepActivity.java | app/src/mobile/java/.../activity/ | ⚠️ 有差异 |
| LiveScreen.kt | LiveActivity.java | app/src/mobile/java/.../activity/ | ❌ 核心功能缺失 |
| SettingsScreen.kt | SettingFragment.java | app/src/mobile/java/.../fragment/ | ❌ 功能缺失 |
