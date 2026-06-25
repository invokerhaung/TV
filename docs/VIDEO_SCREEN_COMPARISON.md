# VideoScreen 功能对比：Java vs Compose

## 一、功能对比表

| 功能 | Java 版本 (VideoActivity) | Compose 版本 (VideoScreen) | 状态 |
|------|---------------------------|---------------------------|------|
| **播放器** | ExoPlayerView (AndroidView) | AndroidView 包裹 PlayerView | ✅ 一致 |
| **播放/暂停** | checkPlay() → player().isPlaying() | isPlaying 状态切换 | ⚠️ 需要连接 PlayerManager |
| **上一集/下一集** | checkNext() / checkPrev() | 未实现 | ❌ 缺失 |
| **全屏模式** | enterFullscreen() / exitFullscreen() | 未实现 | ❌ 缺失 |
| **手势控制** | CustomKeyDown | 未实现 | ❌ 缺失 |
| **线路选择** | FlagAdapter + onItemClick | ScrollableTabRow + selectedFlagIndex | ⚠️ 需要连接播放逻辑 |
| **剧集列表** | EpisodeAdapter + onItemClick | LazyRow + EpisodeGridDialog | ⚠️ 需要连接播放逻辑 |
| **画质选择** | QualityAdapter | 未实现 | ❌ 缺失 |
| **解析源选择** | ParseAdapter | 未实现 | ❌ 缺失 |
| **弹幕功能** | DanmakuController + DanmakuDialog | DanmakuDialog (空数据) | ⚠️ 需要连接数据 |
| **投屏功能** | CastDialog | 未实现 | ❌ 缺失 |
| **字幕选择** | TrackDialog (type=TEXT) | TrackDialog (空数据) | ⚠️ 需要连接数据 |
| **音频选择** | TrackDialog (type=AUDIO) | TrackDialog (空数据) | ⚠️ 需要连接数据 |
| **视频选择** | TrackDialog (type=VIDEO) | TrackDialog (空数据) | ⚠️ 需要连接数据 |
| **倍速播放** | player().addSpeed() / setSpeed() | speed 状态 + PlayerSetting | ⚠️ 需要连接 PlayerManager |
| **画面缩放** | setScale() + mHistory.setScale() | scale 状态 + PlayerSetting | ⚠️ 需要连接 PlayerManager |
| **解码切换** | player().toggleDecode() | decode 状态切换 | ⚠️ 需要连接 PlayerManager |
| **循环播放** | player().setRepeatOne() | isRepeat 状态 | ⚠️ 需要连接 PlayerManager |
| **片头设置** | setOpening() + mHistory.setOpening() | opening 状态 | ⚠️ 需要连接 History |
| **片尾设置** | setEnding() + mHistory.setEnding() | ending 状态 | ⚠️ 需要连接 History |
| **收藏功能** | Keep.find() + delete/createKeep() | 未实现 | ❌ 缺失 |
| **历史记录同步** | saveHistory() + mHistory | history 状态 (部分) | ⚠️ 需要完善 |
| **屏幕锁定** | setLock() + mKeyDown.setLock() | 未实现 | ❌ 缺失 |
| **屏幕旋转** | setRotate() + setRequestedOrientation() | 未实现 | ❌ 缺失 |
| **PiP 画中画** | PiP 类 | 未实现 | ❌ 缺失 |
| **视频高度自适应** | changeHeight() + mAnimator | 固定 220.dp | ❌ 缺失 |
| **快速搜索切换站点** | QuickAdapter | 未实现 | ❌ 缺失 |
| **流量监控** | Traffic 显示 | 未实现 | ❌ 缺失 |
| **详情信息** | site/director/actor/content/remark | 简化显示 | ⚠️ 部分实现 |
| **下拉刷新** | SwipeRefreshLayout | 未实现 | ❌ 缺失 |
| **信息对话框** | InfoDialog | 未实现 | ❌ 缺失 |
| **标题选择** | TitleDialog + player().getCurrentMediaTitles() | TitleDialog (空数据) | ⚠️ 需要连接数据 |

---

## 二、可共用的部分

### 2.1 已经共用的组件

| 组件 | 说明 |
|------|------|
| `EpisodeGrid` | 剧集网格组件 |
| `EpisodeItem` | 剧集项组件 |
| `ImageAsync` | 异步图片加载 |
| `LoadingState` | 加载状态组件 |
| `FilterBar` | 筛选栏组件 |
| `VodCard` | 内容卡片组件 |

### 2.2 可以共用的 Dialog

| Dialog | Java 版本 | Compose 版本 | 是否共用 |
|--------|-----------|--------------|---------|
| ControlDialog | ControlDialog.java | ControlDialog.kt | ⚠️ 功能不同 |
| EpisodeGridDialog | EpisodeGridDialog.java | EpisodeGridDialog.kt | ✅ 可共用 |
| EpisodeListDialog | EpisodeListDialog.java | EpisodeListDialog.kt | ✅ 可共用 |
| DanmakuDialog | DanmakuDialog.java | DanmakuDialog.kt | ✅ 可共用 |
| DanmakuSearchDialog | DanmakuSearchDialog.java | DanmakuSearchDialog.kt | ✅ 可共用 |
| DanmakuSettingDialog | DanmakuSettingDialog.java | DanmakuSettingDialog.kt | ✅ 可共用 |
| TitleDialog | TitleDialog.java | TitleDialog.kt | ✅ 可共用 |
| TrackDialog | TrackDialog.java | TrackDialog.kt | ✅ 可共用 |
| CastDialog | CastDialog.java | CastDialog.kt | ✅ 可共用 |
| InfoDialog | InfoDialog.java | InfoDialog.kt | ✅ 可共用 |
| SpeedDialog | SpeedDialog.java | SpeedDialog.kt | ✅ 可共用 |
| TimerDialog | TimerDialog.java | TimerDialog.kt | ✅ 可共用 |

### 2.3 可以共用的工具类

| 工具类 | 说明 |
|--------|------|
| `PlayerManager` | 播放器管理 |
| `PlayerSetting` | 播放器设置 |
| `DanmakuSetting` | 弹幕设置 |
| `ResUtil` | 资源工具类 |
| `Util` | 通用工具类 |

---

## 三、实现差异

### 3.1 架构差异

**Java 版本：**
- 使用 ViewBinding 直接操作 UI 控件
- 通过 `parent.control.action.xxx` 直接访问控件
- 使用 EventBus 传递事件
- 使用 LiveData 观察数据

**Compose 版本：**
- 使用声明式 UI
- 通过状态变量管理 UI
- 使用回调函数传递事件
- 使用 `observeAsState()` 订阅 LiveData

### 3.2 数据流差异

**Java 版本：**
```
User Action → View.performClick() → Activity Method → PlayerManager
```

**Compose 版本：**
```
User Action → Callback → State Update → Recomposition → PlayerManager
```

---

## 四、需要补充的功能

### 4.1 高优先级（P0）

1. **播放/暂停** - 连接 PlayerManager
2. **上一集/下一集** - 连接 EpisodeAdapter 逻辑
3. **线路选择** - 连接 FlagAdapter 逻辑
4. **剧集选择** - 连接 EpisodeAdapter 逻辑
5. **历史记录同步** - 连接 History 数据库

### 4.2 中优先级（P1）

1. **全屏模式** - 实现 enterFullscreen/exitFullscreen
2. **手势控制** - 实现 CustomKeyDown
3. **弹幕功能** - 连接 DanmakuController
4. **投屏功能** - 连接 CastDialog
5. **字幕/音频/视频选择** - 连接 TrackDialog 数据
6. **倍速播放** - 连接 PlayerManager
7. **画面缩放** - 连接 PlayerManager
8. **解码切换** - 连接 PlayerManager
9. **循环播放** - 连接 PlayerManager
10. **片头/片尾设置** - 连接 History

### 4.3 低优先级（P2）

1. **屏幕锁定** - 实现 Lock 功能
2. **屏幕旋转** - 实现 Rotate 功能
3. **PiP 画中画** - 实现 PiP 功能
4. **视频高度自适应** - 实现动画
5. **流量监控** - 实现 Traffic 显示
6. **信息对话框** - 实现 InfoDialog
7. **标题选择** - 连接 TitleDialog 数据
8. **画质选择** - 实现 QualityAdapter
9. **解析源选择** - 实现 ParseAdapter
10. **下拉刷新** - 实现 SwipeRefreshLayout

---

## 五、建议

### 5.1 短期目标（1-2天）

1. 连接 PlayerManager，实现基本播放控制
2. 连接 History，实现历史记录同步
3. 连接 Flag/Episode，实现线路和剧集选择

### 5.2 中期目标（3-5天）

1. 实现全屏模式
2. 实现手势控制
3. 连接弹幕功能
4. 连接投屏功能

### 5.3 长期目标（1-2周）

1. 实现所有缺失功能
2. 优化性能和用户体验
3. 测试和修复 Bug
