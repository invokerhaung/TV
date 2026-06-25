# UI 迁移计划：Java XML → Kotlin + Jetpack Compose

## 一、现状概述

### 当前技术栈
- 语言：Java 21
- UI 框架：Android View 系统 + XML 布局
- 视图绑定：ViewBinding
- 架构：ViewModel + LiveData + EventBus
- TV 端：Leanback 库（Presenter/ArrayObjectAdapter 模式）
- 手机端：Material Design + RecyclerView + ViewPager2

### 迁移规模

| 大类 | 文件数 |
|------|--------|
| Leanback（TV）端 UI 文件 | 83 |
| Mobile（手机）端 UI 文件 | 82 |
| Main 共享 UI 文件 | 26 |
| 布局 XML 文件 | 178 |
| **合计** | **369** |

### 页面清单

**Mobile 端（9 个 Activity）：** HomeActivity, VideoActivity, SearchActivity, LiveActivity, HistoryActivity, KeepActivity, FileActivity, FolderActivity, ScanActivity

**Leanback 端（13 个 Activity）：** HomeActivity, VideoActivity, VodActivity, SearchActivity, LiveActivity, SettingActivity, SettingDanmakuActivity, SettingPlayerActivity, CastActivity, CollectActivity, FileActivity, KeepActivity, PushActivity

**共享页面（2 个 Activity）：** PlaybackActivity, CrashActivity

---

## 二、迁移策略

### 核心原则
1. **渐进式迁移**：不一次性重写，按模块逐步迁移，每个阶段可独立编译运行
2. **Compose 优先，View 兼容**：新页面用 Compose 写，旧页面通过 `AndroidView` / `ComposeView` 混合使用
3. **Mobile 先行，Leanback 跟进**：手机端 UI 更通用，先迁移积累经验；TV 端涉及焦点管理和 Leanback 库，难度更大
4. **功能不退化**：每个阶段迁移完成后功能与原版一致，作为合并标准
5. **保持 MVVM 架构**：保留 ViewModel + LiveData，Compose 通过 `observeAsState()` 订阅 LiveData

### 架构规范（MVVM）

```
┌─────────────────────────────────────────────────┐
│                   Compose UI                     │
│  (Screen / Component，纯声明式，无业务逻辑)        │
│  observeAsState() 订阅 LiveData                  │
│  调用 ViewModel 方法触发用户操作                   │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│                ViewModel                         │
│  (LiveData 暴露 UI State，处理业务逻辑)            │
│  不持有 View/Context 引用                         │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│              Repository / UseCase                 │
│  (数据层：网络、数据库、配置管理)                    │
└─────────────────────────────────────────────────┘
```

**关键规则：**
- ViewModel 保持 Java 或迁移为 Kotlin，但必须暴露 `LiveData<T>` 给 UI
- Compose Screen 通过 `viewModel.liveData.observeAsState()` 订阅状态
- 用户事件通过调用 ViewModel 方法传递（如 `viewModel.search(keyword)`）
- 不在 Compose 中直接访问 Repository 或数据层
- EventBus 逐步替换为 `MutableLiveData` / `StateFlow`，过渡期保留兼容
- 已有的 `SiteViewModel`、`LiveViewModel` 等保持不变，Compose 直接复用

### 关键技术决策

| 决策项 | 方案 |
|--------|------|
| Compose 版本 | Compose BOM 2025.x + Material 3 |
| 架构模式 | MVVM（ViewModel + LiveData），与现有一致 |
| TV 端焦点管理 | Compose TV 库（`androidx.tv:tv-foundation` / `tv-material`）替代 Leanback |
| 状态管理 | 保留 ViewModel + LiveData，Compose 通过 `observeAsState()` 桥接 |
| 事件总线 | EventBus → LiveData 回调 / SharedFlow（逐步替换） |
| 图片加载 | Glide → Coil（Compose 原生支持） |
| 导航 | Fragment → Compose Navigation |
| 弹幕播放器 | 保持 Media3 + 弹幕自定义 View，通过 `AndroidView` 嵌入 Compose |

---

## 三、分阶段计划

### 第一阶段：基础设施搭建

**目标：** 项目能编译 Kotlin + Compose，但不改变任何现有 UI

**改动内容：**
- `build.gradle` 添加 Kotlin 插件（`org.jetbrains.kotlin.android`）
- `build.gradle` 添加 Compose 编译器配置（`buildFeatures { compose = true }`）
- 添加 Compose BOM 及核心依赖：
  - `androidx.compose:compose-bom`
  - `androidx.compose.ui:ui`
  - `androidx.compose.material3:material3`
  - `androidx.compose.ui:ui-tooling-preview`
  - `androidx.activity:activity-compose`
  - `androidx.lifecycle:lifecycle-viewmodel-compose`
  - `androidx.navigation:navigation-compose`
  - `androidx.compose.runtime:runtime-livedata`（LiveData → Compose State 桥接）
  - `androidx.tv:tv-foundation` / `androidx.tv:tv-material`（TV 端）
- 添加 Kotlin 依赖：`kotlin-stdlib`、`kotlinx-coroutines-android`
- 将 `compileOptions` 升级或并行支持 Kotlin
- 创建 Compose 主题文件（`Theme.kt`、`Color.kt`、`Type.kt`），从现有 `colors.xml` / `styles.xml` 迁移主题变量

**验收标准：**
- `./gradlew assembleLeanbackArm64_v8aDebug` 和 `./gradlew assembleMobileArm64_v8aDebug` 编译通过
- 现有功能无任何变化
- 可以在一个空的 Compose Screen 中通过 `observeAsState()` 读取一个 ViewModel 的 LiveData 并显示

---

### 第二阶段：基础组件层

**目标：** 建立 Compose 可复用组件库，对应现有 ViewHolder / Adapter / 自定义 View

**改动内容：**

| 组件 | 对应现有实现 | 说明 |
|------|-------------|------|
| `VodCard`（Rect/Oval/List） | VodRectHolder / VodOvalHolder / VodListHolder | 内容卡片，三种样式 |
| `LoadingState` | ProgressLayout | 加载中/空状态/错误状态 |
| `Dialog` 系列 | BaseAlertDialog / BaseBottomSheetDialog | AlertDialog / ModalBottomSheet |
| `Image` | Glide → Coil | AsyncImage |
| `TopBar` / `NavBar` | MaterialToolbar + BottomNavigationView | TopAppBar + NavigationBar |
| `FilterChip` / `Tag` | FlexboxLayout + ValueAdapter | FlowRow + FilterChip |
| `EpisodeGrid` / `EpisodeList` | EpisodeAdapter | LazyVerticalGrid / LazyRow |
| `DanmakuOverlay` | DanmakuView | AndroidView 包裹现有 DanmakuView |

**目录结构：**
```
app/src/main/java/com/fongmi/android/tv/ui/compose/
├── theme/
│   ├── Theme.kt
│   ├── Color.kt
│   └── Type.kt
├── component/
│   ├── VodCard.kt
│   ├── LoadingState.kt
│   ├── ImageAsync.kt
│   ├── FilterBar.kt
│   └── DanmakuOverlay.kt
└── navigation/
    └── AppNavigation.kt
```

**验收标准：**
- 组件可在 Preview 中独立预览
- 组件覆盖主要 UI 元素的 80% 以上样式变体
- 编译通过，现有功能不退化

---

### 第三阶段：Mobile 端核心页面迁移

**目标：** 将手机端的主流程页面迁移到 Compose，每个页面严格遵循 MVVM

**页面 → ViewModel 映射：**

| 页面 | ViewModel | 主要 LiveData |
|------|-----------|---------------|
| HomeActivity / VodFragment | SiteViewModel | `result: LiveData<Result>` |
| SearchActivity | SiteViewModel | `search: LiveData<Result>` |
| VideoActivity | SiteViewModel | `player: LiveData<Result>` |
| LiveActivity | LiveViewModel | 频道/EPG 数据 |
| HistoryActivity | 需新建 HistoryViewModel | 历史列表 |
| SettingFragment | 需新建 SettingViewModel | 配置项 |

**迁移顺序（按依赖关系和复杂度排列）：**

#### 3.1 导航框架 + HomeActivity
- 建立 Compose Navigation 图
- BottomNavigationBar（首页/直播/搜索/我的/设置）
- 用 `NavHost` 替代 `FragmentStateManager` + `ViewPager`

#### 3.2 VodFragment（首页内容）
- `LazyVerticalGrid` 展示内容分类
- 订阅 `SiteViewModel.result.observeAsState()`
- 站点切换（`SiteDialog`）
- 筛选栏（`FilterBar`）

#### 3.3 SearchActivity + SearchFragment
- 搜索框 + 键盘
- 订阅 `SiteViewModel.search.observeAsState()`
- 搜索结果列表（`LazyColumn`）

#### 3.4 VideoActivity（视频详情 + 播放）
- 播放器区域：`AndroidView` 包裹 ExoPlayerView（保持不变）
- 信息区：标题、简介、标签（Compose）
- 订阅 `SiteViewModel.player.observeAsState()` 获取播放信息
- 线路/剧集选择（Compose Tab + LazyVerticalGrid）
- 弹幕控制面板
- 这是最复杂的页面，需要 Compose 与原生 View 深度混合

#### 3.5 LiveActivity（直播）
- 频道列表 + EPG
- 播放器区域（`AndroidView`）
- 订阅 `LiveViewModel` 的频道数据

#### 3.6 次要页面
- HistoryActivity / KeepActivity / CollectFragment
- SettingFragment / SettingPlayerFragment / SettingDanmakuFragment
- FileActivity / FolderActivity / ScanActivity

**验收标准：**
- 每个页面迁移后功能与原版一致
- ViewModel + LiveData 数据绑定正常，状态在配置变更后保持
- 页面转场动画正常
- 进度指示器和错误处理正常

---

### 第四阶段：Mobile 端 Dialog 和剩余组件迁移 ✅ 已完成

**目标：** 完成所有 Dialog 和辅助 UI 的迁移

**已完成的 Dialog（30 个）：**

| 优先级 | Dialog | 状态 | 说明 |
|--------|--------|------|------|
| 高 | ConfigDialog | ✅ | 配置管理 |
| 高 | FilterDialog | ✅ | 筛选（ModalBottomSheet） |
| 高 | SiteDialog | ✅ | 站点切换 |
| 高 | HistoryDialog | ✅ | 历史记录 |
| 高 | ControlDialog | ✅ | 播放控制（ModalBottomSheet） |
| 中 | EpisodeGridDialog | ✅ | 剧集选择（网格） |
| 中 | EpisodeListDialog | ✅ | 剧集选择（列表） |
| 中 | SpeedDialog | ✅ | 播放速度 |
| 中 | BufferDialog | ✅ | 缓冲设置 |
| 中 | UaDialog | ✅ | UA 设置 |
| 中 | CastDialog | ✅ | 投屏 |
| 中 | SyncDialog | ✅ | 同步 |
| 中 | ReceiveDialog | ✅ | 接收投屏 |
| 中 | LinkDialog | ✅ | 链接 |
| 中 | PassDialog | ✅ | 密码 |
| 低 | ThemeDialog | ✅ | 主题颜色 |
| 低 | TimerDialog | ✅ | 定时器 |
| 低 | InfoDialog | ✅ | 信息 |
| 低 | RestoreDialog | ✅ | 恢复备份 |
| 低 | UpdateDialog | ✅ | 更新 |
| 低 | DanmakuApiDialog | ✅ | 弹幕 API |
| 低 | LiveDialog | ✅ | 直播源 |
| 低 | WebDialog | ✅ | 网页 |
| 共享 | DanmakuDialog | ✅ | 弹幕管理 |
| 共享 | DanmakuSearchDialog | ✅ | 弹幕搜索 |
| 共享 | DanmakuSettingDialog | ✅ | 弹幕设置 |
| 共享 | OffsetDialog | ✅ | 偏移设置 |
| 共享 | SubtitleDialog | ✅ | 字幕设置 |
| 共享 | TitleDialog | ✅ | 标题选择 |
| 共享 | TrackDialog | ✅ | 音轨选择 |

**验收标准：**
- ✅ 所有 Dialog 功能与原版一致
- ✅ Dialog 的显示/隐藏动画正常（使用 Material 3 ModalBottomSheet）
- ✅ 手势操作（下滑关闭等）正常
- ✅ 图标和颜色与原版保持一致

---

### 第五阶段：Leanback（TV）端迁移

**目标：** 将 TV 端 UI 迁移到 Compose TV，保持 MVVM 架构

**关键技术变化：**
- Leanback Presenter/ArrayObjectAdapter → Compose TV `LazyVerticalGrid` / `TvLazyRow`
- `HorizontalGridView` / `VerticalGridView` → `TvLazyHorizontalGrid` / `TvLazyVerticalGrid`
- `FocusHighlight` → Compose TV `FocusableItem` + 自定义焦点样式
- `CustomKeyDownVod` → `onKeyEvent` / `onPreviewKeyEvent`
- `CustomRowPresenter` → Compose TV 内置焦点滚动行为
- ViewModel + LiveData 保持不变，与 Mobile 端共享同一套 ViewModel

**迁移顺序：**

#### 5.1 基础组件
- TV 端主题（焦点高亮色、圆角、阴影）
- `TvVodCard`（支持焦点放大效果）
- `TvNavigationBar` / `TvSidebar`
- `TvDialog`

#### 5.2 HomeActivity（TV 主页）
- 功能行（FuncPresenter → TvLazyRow）
- 历史记录行（HistoryPresenter）
- 内容推荐行（VodPresenter + CustomSelector）
- 标题栏 + 工具栏焦点切换
- 订阅 `SiteViewModel.result.observeAsState()`

#### 5.3 VideoActivity（TV 视频详情 + 播放）
- 播放器区域：`AndroidView` 包裹 ExoPlayerView
- 线路/剧集选择：焦点导航的横向列表
- D-pad 按键处理：`onKeyEvent` 替代 `CustomKeyDownVod`
- 订阅 `SiteViewModel.player.observeAsState()`

#### 5.4 VodActivity / SearchActivity / LiveActivity

#### 5.5 Setting 系列（3 个 Activity）

#### 5.6 次要页面（CastActivity, CollectActivity, FileActivity, KeepActivity, PushActivity）

**验收标准：**
- D-pad 导航在所有页面正常工作
- 焦点高亮效果与原版一致
- 焦点切换逻辑（如标题栏→内容区）正常
- 长按/短按区分正常
- ViewModel + LiveData 数据绑定正常

---

### 第六阶段：清理和优化

**目标：** 移除旧代码，优化性能

**改动内容：**
- 删除所有已迁移的 Java UI 文件
- 删除已废弃的 XML 布局文件
- 移除 ViewBinding 配置（`buildFeatures { viewBinding = false }`）
- 移除 Leanback 依赖（`leanback` 库）
- 移除 `viewpager2`、`swiperefreshlayout` 等不再需要的依赖
- 替换 EventBus 为 LiveData / SharedFlow（如果尚未完成）
- 替换 Glide 为 Coil（如果尚未完成）
- ProGuard / R8 规则更新
- 性能优化：减少重组（Recomposition）、优化列表滚动性能

**验收标准：**
- APK 体积不显著增大（目标：增长 < 15%）
- 冷启动时间不退化
- 列表滚动帧率 ≥ 55fps
- 所有自动化测试通过（如有）

---

## 四、风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|---------|
| TV 端焦点管理复杂度高 | Leanback 迁移可能超期 | 充分利用 Compose TV 库，必要时保留部分 AndroidView 包裹 |
| ExoPlayer/弹幕与 Compose 混合 | 播放器区域可能有渲染问题 | 播放器始终用 AndroidView 包裹，不强求 Compose 化 |
| 第三方库不支持 Compose | Glide、Lottie 等可能需要替换 | Glide → Coil，Lottie 使用 Compose 版本 |
| EventBus 事件丢失 | 混合架构下事件可能未正确传递 | 逐步替换为 LiveData 回调，过渡期保留 EventBus |
| Leanback 库移除后功能缺失 | 部分 Leanback 特有功能无 Compose 等价 | 逐一评估，必要时自行实现 |
| ProGuard 规则不兼容 | Release 构建可能崩溃 | 每个阶段都测试 Release 构建 |

---

## 五、时间估算

| 阶段 | 预估工作量 | 说明 |
|------|-----------|------|
| 第一阶段：基础设施 | 1-2 天 | 主要是 Gradle 配置 |
| 第二阶段：基础组件 | 3-5 天 | 可复用组件库 |
| 第三阶段：Mobile 核心页面 | 7-10 天 | VideoActivity 是难点 |
| 第四阶段：Mobile Dialog | 3-5 天 | 数量多但复杂度低 |
| 第五阶段：Leanback 端 | 10-15 天 | 焦点管理是最大挑战 |
| 第六阶段：清理优化 | 3-5 天 | 删除旧代码 + 性能调优 |
| **合计** | **27-42 天** | 按单人全职估算 |

---

## 六、迁移期间的并行开发策略

迁移期间项目仍需正常迭代。采用以下策略避免冲突：

1. **Feature Branch 开发**：每个阶段在独立分支开发，合并前需完整回归测试
2. **Bridge 模式**：Compose 页面通过 `ComposeView` 嵌入现有 Activity，或通过 `AndroidView` 在 Compose 中包裹旧 View
3. **逐步替换**：同一功能有 Java 和 Kotlin 两版时，通过 Feature Flag 切换，默认使用稳定版
4. **代码共存期**：允许 Java 和 Kotlin 文件共存于同一模块，不强制一次性转换
