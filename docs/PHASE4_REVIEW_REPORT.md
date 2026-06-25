# 第四阶段迁移代码 Review 报告

## 审查概述

**审查范围：** 第四阶段"Mobile 端 Dialog 和剩余组件迁移"的所有 Compose 代码  
**审查方法：** 逐一严格对比 Java 原版代码与 Compose 迁移代码  
**审查日期：** 2026-06-25  

**统计：**
- 严重问题（功能缺失/错误）：22 项
- 中等问题（架构差异）：4 项
- 轻微问题（UI 差异）：4 项
- **合计：30 项**

---

## 一、严重问题（功能缺失/错误）

### 1. ConfigDialog - 缺失 URL 自动补全和文件选择器

**Java 原版功能：**
- `detect()` 方法：输入 `h` 自动补全 `http://`，输入 `f` 自动补全 `file://`，输入 `a` 自动补全 `assets://`
- `FileChooser` 文件选择器：点击末尾图标可选择本地配置文件
- `IME_ACTION_DONE` 键盘完成监听
- `ConfigListener` 回调接口

**Compose 版现状：**
- 仅有基础的输入框和确认/取消按钮
- 无 URL 自动补全逻辑
- 无文件选择器集成
- 无键盘事件处理

**修复建议：** 补充 `detect()` 逻辑到 `onValueChange` 回调中；集成 `ActivityResultContracts` 实现文件选择；添加 `KeyboardActions` 处理完成事件。

---

### 2. FilterDialog - Dialog 类型不一致

**Java 原版：** `BaseBottomSheetDialog`（底部弹出面板）  
**Compose 版：** `AlertDialog`（居中弹出对话框）

**影响：** 用户交互体验完全不同，底部弹出更适合筛选操作。

**修复建议：** 改用 `ModalBottomSheet` 替代 `AlertDialog`。

---

### 3. ControlDialog - 缺失解析源列表和长按事件

**Java 原版功能：**
- `ParseAdapter` 解析源列表（可切换解析源）
- `onLongClick` 长按事件（player/ending/opening 按钮支持长按重置）
- 与父布局 `ActivityVideoBinding` 的直接交互（同步 decode/ending/opening/repeat 状态）

**Compose 版现状：**
- 缺失解析源列表区域
- 所有按钮只有 `onClick`，无 `onLongClick`
- 状态通过参数传入，无双向同步

**修复建议：** 添加解析源列表组件；为 player/ending/opening 按钮添加长按支持（`combinedClickable`）。

---

### 4. DanmakuDialog - 缺失文件选择器功能

**Java 原版功能：**
- `FileChooser.from(launcher).show(new String[]{"text/*"})` 选择本地弹幕文件
- 选择前暂停播放器（`player.pause()`）
- `ActivityResultLauncher` 处理文件选择结果

**Compose 版现状：**
- `onChooseClick` 回调存在但无实际实现
- 无文件类型过滤

**修复建议：** 在 VideoScreen 中为 `onChooseClick` 实现完整的文件选择逻辑。

---

### 5. DanmakuSearchDialog - 缺失键盘自动弹出

**Java 原版功能：**
- `Util.showKeyboard(binding.keyword)` 打开时自动弹出键盘
- 键盘焦点管理（搜索结果列表可获取焦点）

**Compose 牨现状：**
- 无键盘自动弹出
- 无焦点管理

**修复建议：** 使用 `FocusRequester` 在 Dialog 显示时自动请求焦点；配合 `LocalSoftwareKeyboardController` 显示键盘。

---

### 6. DanmakuSettingDialog - 缺失自适应形态和开关

**Java 原版功能：**
- `BottomSheet` / `SideSheet` 两种形态根据屏幕方向自适应
- `DanmakuSettingPanel` 完整绑定逻辑（包含 show/hide 弹幕开关）
- 全屏横屏时使用 SideSheet，竖屏时使用 BottomSheet

**Compose 版现状：**
- 只有单一 `AlertDialog` 形态
- 缺失 show/hide 弹幕开关（`onShowToggle` 参数存在但 UI 中未渲染开关组件）
- 透明度/字号/速度的范围与 Java 版可能不一致

**修复建议：** 添加 `Switch` 组件控制弹幕显示/隐藏；考虑使用 `ModalBottomSheet` 替代 `AlertDialog`。

---

### 7. OffsetDialog - 缺失自适应形态

**Java 原版功能：**
- `BottomSheet` / `SideSheet` 两种形态根据屏幕方向自适应
- `OffsetPanel` 完整绑定逻辑

**Compose 版现状：**
- 只有单一 `AlertDialog` 形态

**修复建议：** 考虑使用 `ModalBottomSheet` 替代 `AlertDialog`。

---

### 8. SubtitleDialog - 缺失全屏模式适配

**Java 原版功能：**
- 全屏模式下图标变白（`MDColor.WHITE`）
- 透明背景模式（`transparent()` 方法）
- 窗口宽度根据全屏/非全屏调整（`ResUtil.dp2px(isFull() ? 232 : 216)`）

**Compose 版现状：**
- 无全屏模式检测
- 无图标颜色自适应
- 无透明背景
- 使用 `AlertDialog` 而非 `BottomSheetDialog`

**修复建议：** 传入 `isFullscreen` 参数；根据模式调整图标颜色和背景透明度。

---

### 9. TrackDialog - 缺失文件选择器和轨道名称格式化

**Java 原版功能：**
- `FileChooser` 文件选择器（支持多种字幕格式：SRT、SSA、VTT、TTML）
- `DefaultTrackNameProvider` 轨道名称格式化
- `PlayerHelper.describeFormat(format)` 格式描述

**Compose 版现状：**
- 有 `onChooseClick` 回调但无文件选择实现
- 轨道名称直接使用 `track.getName()`，未格式化

**修复建议：** 在 VideoScreen 中实现文件选择逻辑；确保轨道名称格式化正确。

---

### 10. EpisodeGridDialog - 布局形式差异

**Java 原版功能：**
- `ViewPager2` + `TabLayout` + `Fragment` 实现分页
- 根据横竖屏调整 `itemCount`（`ResUtil.isLand` 横屏 5 行，竖屏 10 行）
- `EpisodeFragment` 独立管理每页内容

**Compose 版现状：**
- 使用 `FlowRow` + `ScrollableTabRow` 实现分页
- `itemCount` 固定为 `spanCount * 10`，未区分横竖屏
- 无独立 Fragment，所有逻辑在单个 Composable 中

**修复建议：** 添加横竖屏判断逻辑；考虑使用 `LazyVerticalGrid` 替代 `FlowRow` 以获得更好的滚动性能。

---

### 11. EpisodeListDialog - Dialog 类型完全不同

**Java 原版：** `BaseSideSheetDialog`（侧边弹出面板）  
**Compose 版：** `AlertDialog`（居中弹出对话框）

**影响：** 侧边弹出更适合剧集列表选择，居中弹出会遮挡视频内容。

**修复建议：** 改用 `ModalDrawerSheet` 或自定义侧边弹出组件。

---

### 12. UpdateDialog - 缺失进度更新和不可取消性

**Java 原版功能：**
- `setCancelable(false)` 阻止点击外部和返回键关闭
- `setProgress(int progress)` 动态更新按钮文案为下载进度百分比
- 按钮点击事件在 `onStart()` 中设置（非构造时）

**Compose 版现状：**
- `onDismissRequest = { }` 仅阻止点击外部，返回键仍可关闭
- 有 `progress` 参数但未实现动态更新机制
- 按钮事件直接绑定

**修复建议：** 使用 `BackHandler` 阻止返回键关闭；实现进度动态更新机制。

---

### 13. SiteDialog - 缺失长按全选/全取消功能

**Java 原版功能：**
- `onSearchLongClick` 长按搜索图标：全选/全取消所有站点的搜索属性
- `onChangeLongClick` 长按切换图标：全选/全取消所有站点的切换属性
- `SpaceItemDecoration` 间距装饰器

**Compose 版现状：**
- 仅有短按切换单个站点属性
- 无长按全选功能
- 使用默认间距

**修复建议：** 为 Checkbox 添加 `combinedClickable` 支持长按；实现全选/全取消逻辑。

---

### 14. TimerDialog - 回调管理方式变更

**Java 原版功能：**
- 实现 `Timer.Callback` 接口
- `onTick` 实时更新倒计时显示
- `onFinish` 自动关闭对话框
- `dismiss()` 时清理回调（`Timer.get().setCallback(null)`）

**Compose 版现状：**
- 回调逻辑移至 VideoScreen 的 `DisposableEffect` 中
- Compose 版本身是纯 UI 组件

**影响：** 功能基本等价，但解耦方式不同。VideoScreen 中的实现是正确的。

**状态：** 可接受，无需修复。

---

### 15. CastDialog - 架构重构（纯 UI 化）

**Java 原版功能：**
- 完整的 DLNA 投屏逻辑（`DLNACastManager` 初始化、设备发现、设备监听）
- 局域网设备扫描（`ScanTask`）
- OkHttp 请求发送投屏指令
- `ActivityResultLauncher` 处理扫描结果

**Compose 版现状：**
- 纯 UI 组件，仅负责显示设备列表和触发回调
- 所有业务逻辑移至 VideoScreen

**影响：** 架构更清晰，但 VideoScreen 代码量增加。功能等价。

**状态：** 可接受，无需修复。

---

### 16. SyncDialog - 架构重构（纯 UI 化）

**Java 原版功能：**
- 完整的同步逻辑（FormBody 构建、OkHttp 请求、设备扫描）
- `ScanTask` 局域网扫描
- 同步模式切换（mode 0/1/2）

**Compose 版现状：**
- 纯 UI 组件
- 缺失同步模式切换功能
- 缺失长按强制同步功能

**修复建议：** 在调用层补充同步模式切换和长按强制同步逻辑。

---

### 17. ReceiveDialog - 架构重构（纯 UI 化）

**Java 原版功能：**
- `VodConfig.load` 加载配置
- 进度显示（`showProgress`/`hideProgress`）
- 错误处理（`Notify.show`）

**Compose 版现状：**
- 纯 UI 组件
- 有 `isLoading` 参数支持进度显示
- 无配置加载逻辑

**影响：** 功能需在调用层实现。VideoScreen 中暂未集成此 Dialog。

**修复建议：** 在 VideoScreen 中补充 ReceiveDialog 的集成逻辑。

---

### 18. LiveDialog - 缺失 setAction 逻辑

**Java 原版功能：**
- `adapter.setAction(!isFull())` 根据是否为全屏 Activity 设置不同行为
- 全屏时显示 boot/pass 复选框，非全屏时隐藏

**Compose 版现状：**
- `action` 参数存在但需调用者正确传入
- 功能等价，但需确认调用者逻辑

**状态：** 可接受，需确认调用者。

---

### 19. PassDialog - 缺失窗口宽度限制

**Java 原版功能：**
- `getDialog().getWindow().setLayout(ResUtil.dp2px(250), -1)` 限制窗口宽度为 250dp

**Compose 版现状：**
- 使用默认 AlertDialog 宽度
- 无宽度限制

**修复建议：** 使用 `Modifier.widthIn(max = 250.dp)` 或 Dialog 的 `properties` 参数限制宽度。

---

### 20. UaDialog - 缺失 UA 自动补全功能

**Java 原版功能：**
- `detect()` 方法：输入 `c` 自动补全为 Chrome UA，输入 `o` 自动补全为 OkHttp UA
- 使用 `com.github.catvod.utils.Util.CHROME` / `Util.OKHTTP` 预设值

**Compose 版现状：**
- 仅有基础输入框
- 无 UA 自动补全逻辑

**修复建议：** 在 `onValueChange` 中添加 UA 快捷补全逻辑。

---

### 21. WebDialog - 灵活性降低

**Java 原版功能：**
- 通用 WebView 容器，接受 View 参数
- 实现 `DialogInterface.OnDismissListener` 接口

**Compose 版现状：**
- 硬编码 URL 加载逻辑
- 使用 `AndroidView` 包裹 WebView

**影响：** 灵活性降低，但当前使用场景（URL 加载）功能等价。

**状态：** 可接受。

---

### 22. InfoDialog - 缺失复制功能

**Java 原版功能：**
- `Util.copy(text)` 复制到剪贴板
- URL 点击触发分享（`Listener.onShare`）
- URL 长按复制
- Header 长按复制
- `buildHeader` 方法格式化 Headers Map

**Compose 版现状：**
- 有分享功能（`onShare` 回调）
- 缺失复制到剪贴板功能
- 缺失长按交互

**修复建议：** 使用 `ClipboardManager` 实现复制功能；为 URL 和 Header 添加长按复制支持。

---

## 二、中等问题（架构差异）

### 1. 回调模式变更

**变更内容：** Java 版使用 Listener 接口回调，Compose 版使用 Lambda 回调。

| Dialog | Java 接口 | Compose Lambda |
|--------|-----------|----------------|
| ConfigDialog | `ConfigListener` | `onConfirm: (String, String) -> Unit` |
| FilterDialog | `FilterListener` | `onFilterSelected: (String, Value) -> Unit` |
| SiteDialog | `SiteListener` | `onSiteClick: (Site) -> Unit` |
| SpeedDialog | `SpeedListener` | `onConfirm: (Float) -> Unit` |
| BufferDialog | `BufferListener` | `onConfirm: (Int) -> Unit` |
| UaDialog | `UaListener` | `onConfirm: (String) -> Unit` |
| DanmakuApiDialog | `DanmakuListener` | `onConfirm: (String) -> Unit` |
| PassDialog | `PassListener` | `onConfirm: (String) -> Unit` |
| ThemeDialog | `Listener` | `onColorSelected: (Int) -> Unit` |
| TrackDialog | `Listener` | `onSubtitleClick: () -> Unit` |
| CastDialog | `Listener` | `onDismiss: () -> Unit` |

**影响：** 这是 Compose 架构的合理变更，Lambda 回调更符合 Compose 的声明式编程范式。但需确认所有调用点已正确适配。

**状态：** 可接受，需确认调用点。

---

### 2. Dialog 类型变更

以下 Dialog 从 BottomSheet/SideSheet 改为 AlertDialog：

| Dialog | Java 类型 | Compose 类型 | 影响程度 |
|--------|-----------|--------------|----------|
| FilterDialog | BottomSheet | AlertDialog | 高 - 交互体验不同 |
| EpisodeListDialog | SideSheet | AlertDialog | 高 - 交互体验不同 |
| DanmakuSettingDialog | BottomSheet/SideSheet | AlertDialog | 中 - 功能可用 |
| OffsetDialog | BottomSheet/SideSheet | AlertDialog | 中 - 功能可用 |
| SubtitleDialog | BottomSheet | AlertDialog | 中 - 功能可用 |
| ControlDialog | BottomSheet | AlertDialog | 高 - 交互体验不同 |

**影响：** AlertDialog 是居中弹出，会遮挡背景内容；BottomSheet 是底部弹出，更适合移动端操作。

**修复建议：** 优先将 FilterDialog、EpisodeListDialog、ControlDialog 改为 ModalBottomSheet。

---

### 3. 生命周期管理变更

**Java 版：** 通过 Fragment 生命周期管理（`onDestroyView`、`onDestroy`、`dismiss`）  
**Compose 版：** 通过 `DisposableEffect` 管理

**需验证的资源释放：**
- `DanmakuApi.cancel()` - 弹幕搜索取消
- `DLNACastManager.release()` - DLNA 释放
- `ScanTask.stop()` - 扫描任务停止
- `Timer.setCallback(null)` - 定时器回调清理

**状态：** VideoScreen 中的 DisposableEffect 实现基本正确，需逐一验证。

---

### 4. VideoScreen 代码量过大

**现状：** VideoScreen.kt 约 1387 行，包含：
- 30+ 个状态变量
- 15+ 个 Dialog 的调用逻辑
- 播放器管理逻辑
- 手势处理逻辑
- 投屏逻辑
- 弹幕搜索逻辑

**影响：** 可读性和维护性较差，违反单一职责原则。

**修复建议：** 考虑将 Dialog 调用逻辑抽取为独立的 Composable 函数或使用 DialogState 管理类。

---

## 三、轻微问题（UI 差异）

### 1. 字符串硬编码

**Compose 版问题：** 所有文案硬编码在代码中（如"配置管理"、"筛选"、"站点选择"），Java 版使用 `R.string` 资源引用。

**影响：** 无法支持国际化（i18n）。

**修复建议：** 将所有硬编码字符串替换为 `stringResource(R.string.xxx)`。

---

### 2. Dialog 标题文案不一致

| Dialog | Java 标题 | Compose 标题 |
|--------|-----------|--------------|
| ConfigDialog | `R.string.setting_vod` / `setting_live` / `setting_wall` | "视频配置" / "直播配置" / "壁纸配置" |
| ControlDialog | 无标题 | "播放控制" |
| DanmakuDialog | 无标题 | "弹幕" |
| EpisodeGridDialog | 无标题 | "剧集选择" |
| EpisodeListDialog | 无标题 | "剧集列表" |
| DanmakuSearchDialog | 无标题 | "搜索弹幕" |
| DanmakuSettingDialog | 无标题 | "弹幕设置" |
| OffsetDialog | 无标题 | "字幕偏移" / "音频偏移" |
| SubtitleDialog | 无标题 | "字幕设置" |
| TitleDialog | 无标题 | "标题选择" |
| TimerDialog | 无标题 | "定时器" |
| BufferDialog | `R.string.player_buffer` | "缓冲大小" |
| UaDialog | `R.string.player_ua` | "User-Agent" |
| SpeedDialog | `R.string.player_speed` | "播放速度" |
| DanmakuApiDialog | `R.string.danmaku_api` | "弹幕 API" |
| ThemeDialog | `R.string.setting_theme_color` | "主题颜色" |
| RestoreDialog | 无标题 | "恢复备份" |
| ReceiveDialog | 无标题 | "接收投屏" |

**修复建议：** 使用 `stringResource()` 引用字符串资源，保持与 Java 版一致。

---

### 3. 按钮文案变更

| Dialog | Java 按钮 | Compose 按钮 |
|--------|-----------|--------------|
| ConfigDialog | `R.string.dialog_positive` / `dialog_negative` | "确定" / "取消" |
| SpeedDialog | `R.string.dialog_positive` / `dialog_negative` | "确定" / "取消" |
| BufferDialog | `R.string.dialog_positive` / `dialog_negative` | "确定" / "取消" |
| UaDialog | `R.string.dialog_positive` / `dialog_negative` | "确定" / "取消" |
| LinkDialog | `R.string.dialog_positive` / `dialog_negative` | "确定" / "取消" |
| HistoryDialog | 无确认按钮 | "关闭" |
| SiteDialog | 无确认按钮 | "关闭" |

**修复建议：** 使用 `stringResource()` 引用字符串资源。

---

### 4. 间距/尺寸细微差异

**Compose 版使用 Material 3 默认间距：**
- `AlertDialog` 内边距：24dp
- 按钮内边距：12dp
- 文本间距：8dp / 16dp

**Java 版使用自定义间距：**
- `SpaceItemDecoration(1, 8)` - 8dp 间距
- `SpaceItemDecoration(1, 16)` - 16dp 间距
- `SpaceItemDecoration(8)` - 8dp 间距

**影响：** 视觉效果可能有细微差异，但不影响功能。

**状态：** 可接受。

---

## 四、修复优先级建议

### P0（必须修复 - 影响核心功能）
1. ConfigDialog - URL 自动补全和文件选择器
2. ControlDialog - 解析源列表和长按事件
3. DanmakuDialog - 文件选择器功能
4. EpisodeListDialog - Dialog 类型（SideSheet → ModalDrawerSheet）
5. FilterDialog - Dialog 类型（BottomSheet → ModalBottomSheet）
6. UpdateDialog - 不可取消性和进度更新
7. SiteDialog - 长按全选功能
8. UaDialog - UA 自动补全功能
9. InfoDialog - 复制功能

### P1（应该修复 - 影响用户体验）
1. DanmakuSettingDialog - 自适应形态和开关
2. SubtitleDialog - 全屏模式适配
3. ControlDialog - Dialog 类型（BottomSheet → ModalBottomSheet）
4. DanmakuSearchDialog - 键盘自动弹出
5. PassDialog - 窗口宽度限制
6. TrackDialog - 文件选择器
7. EpisodeGridDialog - 横竖屏适配
8. SyncDialog - 同步模式切换和长按强制同步
9. ReceiveDialog - 集成到 VideoScreen

### P2（可以修复 - 影响代码质量）
1. 字符串硬编码 → 资源引用
2. Dialog 标题文案一致性
3. VideoScreen 代码量优化
4. 间距/尺寸细微差异

---

## 五、验收标准

### 功能验收
- [ ] 所有 30 个 Dialog 的功能与 Java 版一致
- [ ] 所有回调事件正确触发
- [ ] 边界条件处理正确（空列表、网络错误等）

### UI 验收
- [ ] Dialog 类型与 Java 版一致（BottomSheet/SideSheet/AlertDialog）
- [ ] 标题和按钮文案与 Java 版一致
- [ ] 间距和布局与 Java 版视觉效果一致

### 架构验收
- [ ] 资源释放正确（DisposableEffect）
- [ ] 状态管理正确（状态提升）
- [ ] 回调链路完整（Screen → ViewModel → Repository）

---

## 六、总结

第四阶段迁移完成了 30 个 Dialog 从 Java XML 到 Kotlin Compose 的转换，整体架构合理，MVVM 模式贯彻良好。主要问题集中在：

1. **功能缺失**：部分 Dialog 缺失了 Java 版的辅助功能（自动补全、文件选择器、长按事件等）
2. **Dialog 类型变更**：多个 BottomSheet/SideSheet 改为 AlertDialog，影响交互体验
3. **字符串硬编码**：未使用资源引用，影响国际化

建议按 P0 → P1 → P2 优先级逐步修复，确保功能完整性后再进入第五阶段（Leanback TV 端迁移）。
