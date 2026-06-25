# UI 对齐计划：Compose vs XML 实现

## 一、差异总览

| 页面 | 差异类型 | 数量 | 优先级 |
|------|----------|------|--------|
| HomeScreen | 图标/颜色/功能 | 12 | 高 |
| VideoScreen | 功能严重缺失 | 25+ | 高 |
| SearchScreen | 功能缺失 | 8 | 中 |
| HistoryScreen | 布局/功能 | 6 | 中 |
| FavoriteScreen | 布局/功能 | 7 | 中 |
| LiveScreen | 功能严重缺失 | 17+ | 高 |
| SettingsScreen | 功能严重缺失 | 18+ | 高 |
| AppScreen (导航) | 结构差异 | 4 | 中 |

---

## 二、分步修复计划

### 第一步：图标对齐（1小时）

**目标：** 确保所有图标与原版一致

| 文件 | 当前图标 | 应改为 | 状态 |
|------|---------|--------|------|
| HomeScreen | `ic_action_history` | `ic_action_history` | ✅ 已修复 |
| AppScreen | `ic_nav_vod` | `ic_nav_vod` | ✅ 已修复 |
| HistoryScreen | `ic_action_sync` | `ic_action_sync` | ✅ 已修复 |
| HistoryScreen | `ic_action_delete` | `ic_action_delete` | ✅ 已修复 |
| FavoriteScreen | `ic_action_sync` | `ic_action_sync` | ✅ 已修复 |
| FavoriteScreen | `ic_action_delete` | `ic_action_delete` | ✅ 已修复 |
| SearchScreen | `ic_action_reset` | `ic_action_reset` | ✅ 已修复 |

**验收标准：** 所有图标与原版 XML 中使用的 drawable 一致

---

### 第二步：颜色对齐（2小时）

**目标：** 确保所有颜色与原版一致

| 位置 | 原版颜色 | 当前颜色 | 需要修改 |
|------|---------|---------|---------|
| HomeScreen TopAppBar 背景 | 透明 | 透明 | ✅ 已修复 |
| HomeScreen TopAppBar 文字 | 白色 | 白色 | ✅ 已修复 |
| HomeScreen TopAppBar 图标 | 白色 | 白色 | ✅ 已修复 |
| NavBar 背景 | 透明 | 透明 | ✅ 已修复 |
| NavBar 选中图标 | primary | primary | ✅ 已修复 |
| NavBar 未选中图标 | white | white | ✅ 已修复 |
| NavBar 指示器 | 透明 | 透明 | ✅ 已修复 |
| SettingsScreen 文字 | 白色 | 白色 | ✅ 已修复 |
| SettingsScreen 背景 | 深色 | 需要检查 | ⚠️ 待修复 |

**验收标准：** 所有颜色与原版一致

---

### 第三步：HomeScreen 功能对齐（4小时）

**目标：** 补充 HomeScreen 缺失的功能

| 功能 | 原版实现 | 当前状态 | 优先级 |
|------|---------|---------|--------|
| Logo 显示 | ImgUtil.logo，可点击弹出 HistoryDialog | ❌ 缺失 | 高 |
| 站点名称标题 | 显示 home site 名称，可点击弹出 SiteDialog | ❌ 缺失 | 高 |
| FAB - 筛选 | FloatingActionButton (ic_fab_filter) | ❌ 缺失 | 高 |
| FAB - 链接 | FloatingActionButton (ic_fab_link) | ❌ 缺失 | 高 |
| FAB - 回到顶部 | FloatingActionButton (ic_fab_top) | ❌ 缺失 | 中 |
| ViewPager + FolderFragment | 分类滑动 | ❌ 缺失 | 高 |
| 分类标签选择 | TypeAdapter，点击切换页面 | ⚠️ 功能不完整 | 高 |
| Config 加载 | VodConfig.init().load() + LiveConfig + WallConfig | ⚠️ 不完整 | 高 |

**验收标准：** 
- Logo 显示并可点击
- 站点名称显示并可切换
- FAB 按钮显示并可点击
- 分类标签可切换页面

---

### 第四步：VideoScreen 功能对齐（8小时）

**目标：** 补充 VideoScreen 缺失的功能

| 功能 | 原版实现 | 当前状态 | 优先级 |
|------|---------|---------|--------|
| ExoPlayer 播放器 | PlayerView 集成 | ❌ 占位文本 | P0 |
| 播放/暂停控制 | play 按钮 | ❌ 缺失 | P0 |
| 上一集/下一集 | prev/next 按钮 | ❌ 缺失 | P0 |
| 全屏模式 | enterFullscreen/exitFullscreen | ❌ 缺失 | P0 |
| 手势控制 | CustomKeyDown | ❌ 缺失 | P1 |
| 线路选择 | FlagAdapter + RecyclerView | ⚠️ 静态 | P0 |
| 剧集列表 | EpisodeAdapter + RecyclerView | ⚠️ 静态 | P0 |
| 画质选择 | QualityAdapter | ❌ 缺失 | P1 |
| 弹幕功能 | DanmakuController | ❌ 缺失 | P1 |
| 投屏功能 | CastDialog | ❌ 缺失 | P1 |
| 倍速播放 | speed 控制 | ❌ 缺失 | P1 |
| 画面缩放 | scale 循环 | ❌ 缺失 | P2 |
| 收藏功能 | keep 按钮 | ❌ 缺失 | P1 |
| 历史记录同步 | 自动保存位置 | ❌ 缺失 | P0 |

**验收标准：**
- 播放器可播放视频
- 播放控制正常
- 线路/剧集可选择
- 历史记录自动保存

---

### 第五步：SearchScreen 功能对齐（3小时）

**目标：** 补充 SearchScreen 缺失的功能

| 功能 | 原版实现 | 当前状态 | 优先级 |
|------|---------|---------|--------|
| 热搜词显示 | 360kan API | ❌ 缺失 | 中 |
| 搜索建议 | iqiyi suggest API | ❌ 缺失 | 中 |
| 搜索历史记录 | RecordAdapter + FlexboxLayout | ❌ 缺失 | 高 |
| 站点切换 | SiteDialog | ❌ 缺失 | 高 |
| 搜索范围 | 搜索所有可搜索站点 | ⚠️ 仅 home site | 高 |
| 键盘自动弹出 | 无关键词时自动弹出 | ❌ 缺失 | 中 |

**验收标准：**
- 搜索历史显示
- 可切换搜索站点
- 搜索范围正确

---

### 第六步：HistoryScreen 功能对齐（2小时）

**目标：** 补充 HistoryScreen 缺失的功能

| 功能 | 原版实现 | 当前状态 | 优先级 |
|------|---------|---------|--------|
| 网格布局 | GridLayoutManager + Product.getColumn() | ❌ 列表布局 | 高 |
| 删除确认对话框 | MaterialAlertDialogBuilder | ❌ 缺失 | 高 |
| 同步功能 | SyncDialog.create().history() | ❌ 空实现 | 中 |
| 长按切换删除模式 | onLongClick | ❌ 缺失 | 中 |
| 单个删除 | onItemDelete | ⚠️ 行为不同 | 中 |

**验收标准：**
- 网格布局显示
- 删除需二次确认
- 同步功能可用

---

### 第七步：FavoriteScreen 功能对齐（2小时）

**目标：** 补充 FavoriteScreen 缺失的功能

| 功能 | 原版实现 | 当前状态 | 优先级 |
|------|---------|---------|--------|
| 数据加载 | Keep.getVod() | ❌ 空列表 | P0 |
| 网格布局 | GridLayoutManager | ❌ 列表布局 | 高 |
| 删除确认 | MaterialAlertDialogBuilder | ❌ 缺失 | 高 |
| 同步功能 | SyncDialog.create().keep() | ❌ 空实现 | 中 |
| 点击逻辑 | 检查 Config 是否匹配 | ⚠️ 简化 | 高 |

**验收标准：**
- 收藏数据正确加载
- 网格布局显示
- 删除需二次确认

---

### 第八步：LiveScreen 功能对齐（6小时）

**目标：** 补充 LiveScreen 缺失的功能

| 功能 | 原版实现 | 当前状态 | 优先级 |
|------|---------|---------|--------|
| 视频播放器 | ExoPlayer + 全屏 | ❌ 占位文本 | P0 |
| 频道组列表 | GroupAdapter | ❌ 缺失 | P0 |
| 频道列表 | ChannelAdapter | ❌ 空数据 | P0 |
| EPG 节目表 | EpgDataAdapter | ❌ 缺失 | P1 |
| 手势控制 | CustomKeyDown | ❌ 缺失 | P1 |
| 投屏 | CastDialog | ❌ 缺失 | P1 |
| 直播源切换 | LiveDialog | ❌ 缺失 | P0 |
| 线路切换 | nextLine | ❌ 缺失 | P1 |

**验收标准：**
- 直播可播放
- 频道列表显示
- 频道可切换

---

### 第九步：SettingsScreen 功能对齐（4小时）

**目标：** 补充 SettingsScreen 缺失的功能

| 功能 | 原版实现 | 当前状态 | 优先级 |
|------|---------|---------|--------|
| VOD 配置管理 | ConfigDialog.vod() | ❌ 缺失 | P0 |
| 直播配置管理 | ConfigDialog.live() | ❌ 缺失 | P0 |
| VOD 站点选择 | SiteDialog | ❌ 缺失 | P0 |
| 直播源选择 | LiveDialog | ❌ 缺失 | P0 |
| 播放器设置 | SettingPlayerFragment | ❌ 空实现 | P1 |
| 弹幕设置 | SettingDanmakuFragment | ❌ 空实现 | P1 |
| 无痕模式 | incognito 开关 | ❌ 缺失 | P2 |
| 主题颜色 | ThemeDialog | ❌ 缺失 | P2 |
| 缓存管理 | 显示缓存大小 + 清除 | ❌ 缺失 | P2 |
| 数据备份/恢复 | AppDatabase.backup/restore | ❌ 缺失 | P2 |

**验收标准：**
- 配置可管理
- 站点可选择
- 基础设置可用

---

### 第十步：AppScreen 导航对齐（2小时）

**目标：** 修复导航结构差异

| 功能 | 原版实现 | 当前状态 | 优先级 |
|------|---------|---------|--------|
| 底部导航项数 | 3 项 (vod, live, setting) | 5 项 | 高 |
| Live 可见性 | 仅在 LiveConfig.hasUrl() 时可见 | 始终可见 | 高 |
| 导航方式 | FragmentStateManager | NavController | 架构不同 |

**验收标准：**
- 导航项与原版一致
- Live 可见性逻辑正确

---

## 三、执行顺序

```
第一阶段：基础对齐（3小时）
├── 第一步：图标对齐 ✅
├── 第二步：颜色对齐 ✅
└── 第十步：AppScreen 导航对齐

第二阶段：核心页面（15小时）
├── 第三步：HomeScreen 功能对齐
├── 第四步：VideoScreen 功能对齐
└── 第八步：LiveScreen 功能对齐

第三阶段：次要页面（9小时）
├── 第五步：SearchScreen 功能对齐
├── 第六步：HistoryScreen 功能对齐
├── 第七步：FavoriteScreen 功能对齐
└── 第九步：SettingsScreen 功能对齐
```

---

## 四、验收标准

### 整体验收标准：
1. ✅ 所有图标与原版一致
2. ✅ 所有颜色与原版一致
3. ✅ 所有功能可正常使用
4. ✅ 编译通过，无报错
5. ✅ 运行时无崩溃

### 逐页验收标准：
- 每个页面的功能与原版一致
- UI 布局与原版相似
- 交互逻辑与原版一致

---

## 五、风险评估

| 风险 | 影响 | 应对措施 |
|------|------|---------|
| VideoScreen 复杂度高 | 可能超期 | 分阶段实现，先基础后高级 |
| LiveScreen 依赖播放器 | 需要先实现播放器 | 使用 AndroidView 包裹现有播放器 |
| 部分功能需要原生代码 | 无法纯 Compose 实现 | 使用 AndroidView 桥接 |
| EventBus 替换 | 需要重构事件系统 | 逐步替换为 LiveData/Flow |

---

## 六、时间估算

| 阶段 | 预估时间 | 说明 |
|------|---------|------|
| 第一阶段：基础对齐 | 3 小时 | 图标/颜色/导航 |
| 第二阶段：核心页面 | 15 小时 | Home/Video/Live |
| 第三阶段：次要页面 | 9 小时 | Search/History/Favorite/Settings |
| **合计** | **27 小时** | 约 3-4 个工作日 |

---

## 七、下一步行动

1. **立即执行：** 第一步图标对齐 ✅ 已完成
2. **立即执行：** 第二步颜色对齐 ✅ 已完成
3. **立即执行：** 第十步 AppScreen 导航对齐
4. **下一步：** 第三步 HomeScreen 功能对齐
5. **后续：** 按优先级逐步完成其他页面
