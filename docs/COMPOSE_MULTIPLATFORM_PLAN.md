# Compose Multiplatform 迁移方案

## 一、项目现状分析

### 1.1 已有 Compose 代码资产

项目已启动 Compose 迁移，共有 24 个 Compose 文件：

```
app/src/main/java/com/fongmi/android/tv/ui/compose/
├── theme/          (3 files)  Color.kt, Theme.kt, Type.kt
├── component/      (11 files) ImageAsync, VodCard, NavBar, TopBar, LoadingState, 
│                              FilterBar, EpisodeGrid, EpisodeItem, Dialog, 
│                              DanmakuOverlay, Preview
├── screen/         (8 files)  AppScreen, HomeScreen, VideoScreen, SearchScreen, 
│                              LiveScreen, SettingsScreen, FavoriteScreen, 
│                              HistoryScreen, ComposeTestScreen
└── navigation/     (1 file)   AppNavigation (骨架/占位)
```

### 1.2 Android 依赖程度分类

| 依赖级别 | 文件数 | 说明 |
|----------|--------|------|
| **零依赖（可直接复用）** | 5 | Color.kt, Type.kt, LoadingState.kt, FilterBar.kt, EpisodeItem.kt, EpisodeGrid.kt, Dialog.kt |
| **轻度依赖（替换资源引用）** | 10 | NavBar, TopBar, AppScreen, HomeScreen, VideoScreen, SearchScreen, HistoryScreen, FavoriteScreen, LiveScreen, SettingsScreen |
| **中度依赖（替换图片加载+ViewModel）** | 2 | ImageAsync.kt (Coil 3 已支持 KMP), VodCard.kt |
| **重度依赖（需 expect/actual）** | 2 | Theme.kt (Android Window API), DanmakuOverlay.kt (AndroidView) |

### 1.3 核心模块依赖分析

| 模块 | Android 依赖深度 | KMP 可行性 |
|------|------------------|-----------|
| **catvod (爬虫抽象层)** | 中等 - Spider.init(Context) | ✅ 可迁移，需抽象 Context |
| **网络层 (OkHttp)** | 低 - OkHttp 已支持多平台 | ✅ 直接复用 |
| **数据层 (Room)** | 高 - Android Room | ⚠️ 需替换为 SQLDelight |
| **播放器 (Media3)** | 极高 - 完全 Android 专属 | ❌ 需平台特定实现 |
| **弹幕 (DanmakuController)** | 极高 - Media3 fork | ❌ 需平台特定实现 |
| **DLNA (jupnp)** | 高 - Android 专属 | ⚠️ 需使用 jupnp 桌面版 |
| **本地服务器 (NanoHTTPD)** | 低 - 纯 Java | ✅ 直接复用 |
| **爬虫运行时 (QuickJS)** | 中等 - JNI 绑定 | ⚠️ 需桌面版 QuickJS |
| **爬虫运行时 (Chaquopy)** | 极高 - Android 专属 | ❌ 需替换为 CPython |

---

## 二、目标架构设计

### 2.1 多平台模块结构

```
TV/
├── composeApp/                    # 新增：Compose Multiplatform 共享模块
│   ├── src/
│   │   ├── commonMain/           # 共享代码（UI + 业务逻辑）
│   │   │   ├── kotlin/
│   │   │   │   └── com/fongmi/tv/
│   │   │   │       ├── ui/       # Compose UI（迁移自现有代码）
│   │   │   │       ├── model/    # ViewModel（KMP 版）
│   │   │   │       ├── domain/   # 业务逻辑
│   │   │   │       └── data/     # 数据层接口
│   │   │   └── composeResources/ # 共享资源
│   │   ├── androidMain/          # Android 平台实现
│   │   ├── desktopMain/          # Windows/JVM 平台实现
│   │   └── ...
│   └── build.gradle.kts
│
├── catvod/                        # 改造：爬虫抽象层（KMP 化）
│   ├── src/
│   │   ├── commonMain/           # Spider 接口、数据模型
│   │   ├── commonTest/
│   │   ├── jvmMain/              # JVM/桌面实现
│   │   └── androidMain/          # Android 实现
│   └── build.gradle.kts
│
├── player/                        # 新增：播放器抽象层
│   ├── src/
│   │   ├── commonMain/           # PlayerEngine 接口
│   │   ├── androidMain/          # ExoPlayer 实现
│   │   └── desktopMain/          # VLC/mpv 实现
│   └── build.gradle.kts
│
├── app/                           # 保留：Android 壳工程
│   └── build.gradle.kts          # 依赖 composeApp
│
├── desktop/                       # 新增：Windows 桌面应用
│   ├── src/
│   │   └── jvmMain/
│   │       └── main.kt           # 桌面入口
│   └── build.gradle.kts
│
└── settings.gradle.kts            # 更新：包含新模块
```

### 2.2 分层架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      Compose UI Layer                        │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  commonMain: Screen, Component, Theme, Navigation       ││
│  │  (迁移自 app/src/main/.../ui/compose/)                  ││
│  └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│                    ViewModel Layer                           │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  commonMain: SiteVM, LiveVM, HistoryVM (Kotlin Flow)    ││
│  └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│                    Domain Layer                              │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  commonMain: VodConfig, ConfigManager, HistoryManager   ││
│  └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│                    Data Layer                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  catvod      │  │  database    │  │  player          │  │
│  │  (Spider)    │  │  (SQLDelight)│  │  (expect/actual) │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                    Platform Layer                            │
│  ┌──────────────────────┐  ┌──────────────────────────────┐ │
│  │  Android             │  │  Desktop (Windows)           │ │
│  │  - ExoPlayer         │  │  - VLC (vlcj)               │ │
│  │  - Chaquopy          │  │  - CPython (GraalPython)    │ │
│  │  - Room              │  │  - SQLDelight               │ │
│  │  - DexClassLoader    │  │  - URLClassLoader            │ │
│  └──────────────────────┘  └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 三、最小化改造方案（进行中）

### 3.1 必须改造为 Kotlin 的类

#### 3.1.1 第一优先级：核心接口和数据模型

**catvod 模块（爬虫抽象层）- 必须改造**

| 文件 | 改造原因 | 改造方式 |
|------|----------|----------|
| `Spider.java` | 核心接口，需要放到 commonMain | 完全重写为 Kotlin |
| `Spider.java` 相关的接口 | 依赖 Spider | 改为 Kotlin |

**数据模型类 - 必须改造**

| 文件 | 改造原因 | 改造方式 |
|------|----------|----------|
| `Vod.java` | ViewModel 需要使用 | 改为 Kotlin data class |
| `Result.java` | ViewModel 返回类型 | 改为 Kotlin data class |
| `History.java` | 跨平台数据层 | 改为 Kotlin data class |
| `Keep.java` | 跨平台数据层 | 改为 Kotlin data class |
| `Site.java` | 配置模型 | 改为 Kotlin data class |
| `Config.java` | 配置模型 | 改为 Kotlin data class |
| `Episode.java` | UI 数据模型 | 改为 Kotlin data class |
| `Flag.java` | UI 数据模型 | 改为 Kotlin data class |
| `Class.java` | UI 数据模型 | 改为 Kotlin data class |
| `Channel.java` | 直播数据模型 | 改为 Kotlin data class |
| `Group.java` | 直播数据模型 | 改为 Kotlin data class |

**ViewModel 类 - 必须改造**

| 文件 | 改造原因 | 改造方式 |
|------|----------|----------|
| `SiteViewModel.java` | 需要使用 Kotlin Flow | 改为 Kotlin，LiveData → StateFlow |
| `LiveViewModel.java` | 需要使用 Kotlin Flow | 改为 Kotlin，LiveData → StateFlow |
| `HistoryViewModel.java` | 需要使用 Kotlin Flow | 改为 Kotlin，LiveData → StateFlow |

#### 3.1.2 第二优先级：业务逻辑类

| 文件 | 改造原因 | 改造方式 |
|------|----------|----------|
| `VodConfig.java` | 配置管理，需要跨平台 | 改为 Kotlin |
| `LiveConfig.java` | 配置管理 | 改为 Kotlin |
| `WallConfig.java` | 配置管理 | 改为 Kotlin |
| `SiteApi.java` | API 调用 | 改为 Kotlin |
| `AppDatabase.java` | 数据库抽象 | 改为 Kotlin 接口 |

### 3.2 可以保留 Java 的类

#### 3.2.1 平台特定实现（不需要改造）

| 类型 | 文件 | 原因 |
|------|------|------|
| **Android Activity** | HomeActivity, VideoActivity 等 | 已用 Compose Screen 替代 |
| **Android Fragment** | VodFragment, SearchFragment 等 | 已用 Compose Screen 替代 |
| **Room DAO** | HistoryDao, KeepDao 等 | 将用 SQLDelight 替代 |
| **Room Entity** | 已有 data class | 可通过互操作使用 |
| **ExoPlayer 相关** | PlayerManager 等 | 平台特定 |
| **View 相关** | ProgressLayout, DanmakuView 等 | 已用 Compose 替代 |

#### 3.2.2 通过 Java/Kotlin 互操作使用

现有的 Java 类可以通过以下方式在 KMP 中使用：
- 在 `jvmMain` 中直接引用 Java 类
- 在 `commonMain` 中定义接口，在 `jvmMain` 中实现

### 3.3 最小化改造路径

#### 阶段一：数据模型 Kotlin 化（1-2 天）

```kotlin
// 改造前：Vod.java
public class Vod implements Parcelable, Diffable<Vod> {
    private String vodId;
    private String vodName;
    // ... getter/setter
}

// 改造后：Vod.kt
data class Vod(
    @SerializedName("vod_id") val vodId: String = "",
    @SerializedName("vod_name") val vodName: String = "",
    // ...
) : Parcelable, Diffable<Vod> {
    // 保持兼容的 getter
    val id: String get() = vodId.ifEmpty { "" }
    val name: String get() = vodName.ifEmpty { "" }
}
```

#### 阶段二：ViewModel Kotlin 化（1-2 天）

```kotlin
// 改造前：SiteViewModel.java
public class SiteViewModel extends ViewModel {
    private final MutableLiveData<Result> result;
    public LiveData<Result> getResult() { return result; }
}

// 改造后：SiteViewModel.kt
class SiteViewModel : ViewModel() {
    private val _result = MutableStateFlow<Result?>(null)
    val result: StateFlow<Result?> = _result.asStateFlow()
}
```

#### 阶段三：核心接口 Kotlin 化（1-2 天）

```kotlin
// 改造前：Spider.java
public abstract class Spider {
    public void init(Context context, String extend) {}
    public abstract String homeContent(boolean filter);
}

// 改造后：Spider.kt
abstract class Spider {
    open fun init(extend: String) {}
    open suspend fun homeContent(filter: Boolean): String = ""
}
```

---

## 四、详细迁移方案

### 4.1 阶段一：基础架构搭建（2-3 周）

#### 4.1.1 创建 Compose Multiplatform 模块

**目标**：建立 `composeApp` 模块，配置多平台构建。

**步骤**：
1. 创建 `composeApp/build.gradle.kts`，配置 Kotlin Multiplatform + Compose Multiplatform 插件
2. 配置目标平台：`androidTarget()` + `jvm("desktop")`
3. 迁移共享资源到 `composeResources/`
4. 配置依赖：Compose BOM, Navigation Compose, Coil 3 (KMP)

**验收标准**：
- [ ] `./gradlew :composeApp:desktopRun` 能启动空白窗口
- [ ] `./gradlew :composeApp:assembleDebug` 能生成 Android APK
- [ ] 共享资源（图标、字符串）能在两个平台正确加载

#### 4.1.2 迁移 catvod 模块为 KMP

**目标**：将爬虫抽象层改造为 KMP 模块。

**关键改造点**：

```kotlin
// commonMain - Spider 接口（去除 Context 依赖）
abstract class Spider {
    open fun init(extend: String) {}
    open suspend fun homeContent(filter: Boolean): String = ""
    open suspend fun homeVideoContent(): String = ""
    open suspend fun categoryContent(tid: String, pg: String, filter: Boolean, extend: Map<String, String>): String = ""
    open suspend fun detailContent(ids: List<String>): String = ""
    open suspend fun searchContent(key: String, quick: Boolean, pg: String): String = ""
    open suspend fun playerContent(flag: String, id: String, vipFlags: List<String>): String = ""
    open suspend fun liveContent(url: String): String = ""
    open fun destroy() {}
}

// commonMain - 平台抽象
expect object PlatformContext {
    val cacheDir: String
    val filesDir: String
    fun loadAsset(path: String): ByteArray
}

// androidMain - Android 实现
actual object PlatformContext {
    actual val cacheDir: String get() = App.get().cacheDir.absolutePath
    actual val filesDir: String get() = App.get().filesDir.absolutePath
    actual fun loadAsset(path: String): ByteArray = App.get().assets.open(path).readBytes()
}

// desktopMain - JVM 实现
actual object PlatformContext {
    actual val cacheDir: String = System.getProperty("java.io.tmpdir") + "/tv/cache"
    actual val filesDir: String = System.getProperty("user.home") + "/.tv"
    actual fun loadAsset(path: String): ByteArray = 
        object {}.javaClass.getResourceAsStream("/assets/$path")?.readBytes() ?: byteArrayOf()
}
```

**验收标准**：
- [ ] Spider 接口在 commonMain 编译通过
- [ ] OkHttp 网络层在两个平台正常工作
- [ ] catvod 模块的单元测试在两个平台通过

#### 4.1.3 建立数据层抽象

**目标**：用 SQLDelight 替代 Room，实现跨平台数据库。

**数据库迁移映射**：

| Room Entity | SQLDelight Table | 说明 |
|-------------|------------------|------|
| Config | config | 配置 URL |
| Site | site | 站点定义 |
| History | history | 播放历史 |
| Keep | keep | 收藏 |
| Live | live | 直播源 |
| Track | track | 音视频轨偏好 |
| Device | device | DLNA 设备 |

**验收标准**：
- [ ] SQLDelight schema 定义完成，7 张表与 Room 版本一致
- [ ] 数据库迁移脚本（v35）编写完成
- [ ] DAO 接口在 commonMain 定义，两个平台实现
- [ ] 备份/恢复功能正常工作

---

### 4.2 阶段二：UI 层迁移（3-4 周）

#### 4.2.1 资源迁移

**目标**：将 `R.drawable.*` 迁移到 Compose Resources。

**迁移清单**：

```
res/drawable/ → composeResources/drawable/
├── ic_setting_home.svg
├── ic_nav_live.svg
├── ic_action_search.svg
├── ic_action_keep.svg
├── ic_nav_setting.svg
├── ic_control_back.svg
└── ... (其他图标)
```

**代码变更示例**：

```kotlin
// 迁移前
painterResource(id = R.drawable.ic_action_search)

// 迁移后
painterResource(Res.drawable.ic_action_search)
```

**验收标准**：
- [ ] 所有图标资源迁移到 `composeResources/`
- [ ] 所有 `painterResource(R.drawable.*)` 替换为 `painterResource(Res.drawable.*)`
- [ ] 字符串资源迁移到 `Res.string.*`

#### 4.2.2 ViewModel 迁移

**目标**：将 LiveData 替换为 Kotlin Flow。

**迁移示例**：

```kotlin
// 迁移前（Android）
class SiteViewModel : ViewModel() {
    val result = MutableLiveData<Result>()
    
    fun homeContent() {
        viewModelScope.launch {
            val data = repository.homeContent()
            result.value = data
        }
    }
}

// 观察
val result by siteViewModel.result.observeAsState()

// 迁移后（KMP）
class SiteViewModel : ViewModel() {
    private val _result = MutableStateFlow<Result?>(null)
    val result: StateFlow<Result?> = _result.asStateFlow()
    
    fun homeContent() {
        viewModelScope.launch {
            val data = repository.homeContent()
            _result.value = data
        }
    }
}

// 观察
val result by siteViewModel.result.collectAsState()
```

**验收标准**：
- [ ] 所有 ViewModel 的 LiveData 替换为 StateFlow
- [ ] 所有 `observeAsState()` 替换为 `collectAsState()`
- [ ] ViewModel 在 commonMain 编译通过

#### 4.2.3 Theme 系统迁移

**目标**：用 expect/actual 隔离 Android Window 操作。

```kotlin
// commonMain
expect fun applySystemBarsColors(isDark: Boolean)

@Composable
fun TVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    
    applySystemBarsColors(darkTheme)
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// androidMain
actual fun applySystemBarsColors(isDark: Boolean) {
    // Android 特有的状态栏/导航栏颜色设置
    val window = (LocalView.current.context as Activity).window
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDark
}

// desktopMain
actual fun applySystemBarsColors(isDark: Boolean) {
    // 桌面端无需操作
}
```

**验收标准**：
- [ ] Theme.kt 的 Android 依赖通过 expect/actual 隔离
- [ ] 桌面端使用标准 Material3 配色
- [ ] Android 端保留动态颜色（Android 12+）

#### 4.2.4 图片加载迁移

**目标**：使用 Coil 3 KMP 版本。

```kotlin
// commonMain - ImageAsync.kt
@Composable
fun ImageAsync(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
}
```

**验收标准**：
- [ ] Coil 3 KMP 依赖配置完成
- [ ] 图片加载在两个平台正常工作
- [ ] 图片缓存机制正常

#### 4.2.5 弹幕组件迁移

**目标**：用 expect/actual 实现跨平台弹幕。

```kotlin
// commonMain
expect class DanmakuRenderer() {
    fun setDanmakuData(data: String)
    fun sendDanmaku(text: String)
    fun setEnabled(enabled: Boolean)
    fun release()
}

@Composable
expect fun DanmakuOverlay(
    renderer: DanmakuRenderer,
    modifier: Modifier = Modifier
)

// androidMain - 使用 Media3 DanmakuController
actual class DanmakuRenderer {
    private val controller = DanmakuController()
    // ... 实现
}

// desktopMain - 自定义 Canvas 实现
actual class DanmakuRenderer {
    private val danmakuList = mutableStateListOf<DanmakuItem>()
    // ... 实现
}

@Composable
actual fun DanmakuOverlay(renderer: DanmakuRenderer, modifier: Modifier) {
    Canvas(modifier = modifier) {
        // 绘制弹幕
    }
}
```

**验收标准**：
- [ ] Android 端弹幕功能与原版一致
- [ ] 桌面端弹幕基本功能可用
- [ ] 弹幕配置（透明度、速度等）正常工作

---

### 4.3 阶段三：播放器集成（2-3 周）

#### 4.3.1 播放器抽象层

**目标**：定义跨平台播放器接口。

```kotlin
// commonMain - PlayerEngine.kt
interface PlayerEngine {
    val state: StateFlow<PlayerState>
    val position: StateFlow<Long>
    val duration: StateFlow<Long>
    
    suspend fun prepare(url: String, headers: Map<String, String>)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun setVolume(volume: Float)
    fun release()
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val error: String? = null
)

// commonMain - PlayerManager.kt
class PlayerManager {
    private val engine: PlayerEngine = createPlatformPlayer()
    
    val state = engine.state
    val position = engine.position
    val duration = engine.duration
    
    suspend fun play(url: String, headers: Map<String, String> = emptyMap()) {
        engine.prepare(url, headers)
        engine.play()
    }
    
    fun pause() = engine.pause()
    fun seekTo(ms: Long) = engine.seekTo(ms)
}

expect fun createPlatformPlayer(): PlayerEngine
```

#### 4.3.2 Windows 播放器实现

**方案**：使用 vlcj（VLC 的 Java 绑定）。

```kotlin
// desktopMain - VlcPlayerEngine.kt
class VlcPlayerEngine : PlayerEngine {
    private val mediaPlayerFactory = MediaPlayerFactory()
    private val mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer()
    
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()
    
    private val _position = MutableStateFlow(0L)
    override val position: StateFlow<Long> = _position.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()
    
    override suspend fun prepare(url: String, headers: Map<String, String>) {
        val media = mediaPlayerFactory.media().newMedia(url)
        headers.forEach { (key, value) ->
            media.addOption(":http-header=$key: $value")
        }
        mediaPlayer.media().prepare(media)
    }
    
    override fun play() = mediaPlayer.controls().play()
    override fun pause() = mediaPlayer.controls().pause()
    override fun seekTo(positionMs: Long) = mediaPlayer.controls().setTime(positionMs)
    override fun setVolume(volume: Float) = mediaPlayer.audio().setVolume((volume * 100).toInt())
    override fun release() = mediaPlayer.release()
}
```

**验收标准**：
- [ ] VLC 播放器在 Windows 上能播放 HLS/DASH/HTTP 流
- [ ] 播放控制（播放/暂停/seek）正常
- [ ] 字幕加载正常
- [ ] 硬件解码自动检测

---

### 4.4 阶段四：爬虫引擎集成（3-4 周）

#### 4.4.1 JavaScript 爬虫（QuickJS）

**方案**：使用 GraalJS 或桌面版 QuickJS。

```kotlin
// desktopMain - DesktopJsEngine.kt
class DesktopJsEngine {
    private val context: Context = Context.create("js")
    
    fun loadScript(script: String) {
        context.eval("js", script)
    }
    
    fun callFunction(name: String, vararg args: Any): Any? {
        val jsArgs = args.map { toJsValue(it) }
        return context.getBindings("js").getMember(name).invokeMember("call", *jsArgs.toTypedArray())
    }
}
```

**验收标准**：
- [ ] JS 爬虫能在桌面端加载和执行
- [ ] 内置库（http.js, cat.js, cheerio.js）正常工作
- [ ] HTTP 请求通过 OkHttp 正常发送

#### 4.4.2 Python 爬虫

**方案**：使用 GraalPython 或嵌入式 CPython。

```kotlin
// desktopMain - DesktopPythonEngine.kt
class DesktopPythonEngine {
    private val engine = Engine.create("python")
    private val context = engine.createContext()
    
    fun loadScript(scriptPath: String) {
        val script = File(scriptPath).readText()
        context.eval("python", script)
    }
    
    fun callFunction(name: String, vararg args: Any): Any? {
        return context.getBindings("python").getMember(name).invokeMember("call", *args)
    }
}
```

**验收标准**：
- [ ] Python 爬虫能在桌面端加载和执行
- [ ] requests 库正常工作
- [ ] 依赖模块自动下载正常

#### 4.4.3 JAR 爬虫

**方案**：使用 URLClassLoader + dex-tools 自动转换。

Android 的 JAR 包内含 `.dex` 文件（非标准 `.class`），桌面端无法直接加载。通过 dexlib2 在运行时自动检测并转换。

```kotlin
// desktopMain - JarUtils.kt
enum class JarType {
    STANDARD,   // 标准 JVM jar (含 .class)
    ANDROID,    // Android jar (含 .dex)
    UNKNOWN
}

object JarUtils {
    /**
     * 判断 JAR 类型：检查内部是否包含 .dex 文件
     */
    fun detectJarType(jarPath: String): JarType {
        ZipFile(jarPath).use { zip ->
            val entries = zip.entries().asSequence().toList()
            val hasDex = entries.any { it.name.endsWith(".dex") }
            val hasClass = entries.any {
                it.name.endsWith(".class") && !it.name.startsWith("META-INF")
            }
            return when {
                hasDex && !hasClass -> JarType.ANDROID
                hasClass && !hasDex -> JarType.STANDARD
                hasDex && hasClass  -> JarType.ANDROID
                else -> JarType.UNKNOWN
            }
        }
    }
}
```

```kotlin
// desktopMain - DexConverter.kt
object DexConverter {
    /**
     * 从 JAR 中提取 .dex 并转换为标准 JVM .jar
     */
    fun jarToStdJar(jarPath: String): String {
        val outputDir = File(PlatformContext.cacheDir, "converted")
        outputDir.mkdirs()
        val stdJar = File(outputDir, File(jarPath).nameWithoutExtension + "_std.jar")

        // 缓存命中直接返回
        if (stdJar.exists()) return stdJar.absolutePath

        // 1. 从 JAR 中提取所有 .dex 文件
        val dexFiles = mutableListOf<File>()
        ZipFile(jarPath).use { zip ->
            zip.entries().asSequence()
                .filter { it.name.endsWith(".dex") }
                .forEach { entry ->
                    val dexFile = File(outputDir, entry.name)
                    zip.getInputStream(entry).use { input ->
                        dexFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    dexFiles.add(dexFile)
                }
        }

        // 2. 转换每个 dex 为 class 并打包成标准 jar
        JarOutputStream(FileOutputStream(stdJar)).use { jarOut ->
            dexFiles.forEach { dexFile ->
                val dex = DexFileFactory.loadDex(dexFile, 25)
                dex.classes.forEach { classDef ->
                    val classBytes = transformToClass(classDef)
                    val entryName = classDef.type
                        .removePrefix("L").removeSuffix(";")
                        .replace('/', '.') + ".class"
                    jarOut.putNextEntry(JarEntry(entryName))
                    jarOut.write(classBytes)
                    jarOut.closeEntry()
                }
            }
        }

        return stdJar.absolutePath
    }
}
```

```kotlin
// desktopMain - DesktopJarLoader.kt
class DesktopJarLoader {
    private val loaders = ConcurrentHashMap<String, URLClassLoader>()
    private val cache = ConcurrentHashMap<String, String>()

    fun loadSpider(jarPath: String, className: String): Spider {
        // 自动检测并转换
        val stdJarPath = cache.getOrPut(jarPath) {
            when (JarUtils.detectJarType(jarPath)) {
                JarType.STANDARD -> jarPath
                JarType.ANDROID  -> DexConverter.jarToStdJar(jarPath)
                JarType.UNKNOWN  -> throw IllegalArgumentException("Unknown jar format: $jarPath")
            }
        }

        val loader = loaders.computeIfAbsent(stdJarPath) {
            URLClassLoader(arrayOf(File(stdJarPath).toURI().toURL()), javaClass.classLoader)
        }
        val clazz = loader.loadClass("com.github.catvod.spider.$className")
        val spider = clazz.getDeclaredConstructor().newInstance() as Spider
        spider.init(PlatformContext.cacheDir)
        return spider
    }
}
```

**依赖配置**：

```kotlin
// desktop/build.gradle.kts
dependencies {
    implementation("org.smali:dexlib2:2.5.2")  // dex 转换库
}
```

**验收标准**：
- [ ] 标准 JAR 爬虫能直接加载
- [ ] Android dex JAR 爬虫能自动转换并加载
- [ ] 转换后的 JAR 有缓存，不重复转换
- [ ] 反射调用正常工作
- [ ] 多个 JAR 之间不冲突

---

### 4.5 阶段五：Windows 应用打包（1-2 周）

#### 4.5.1 应用入口

```kotlin
// desktopMain - main.kt
fun main() = application {
    val windowState = rememberWindowState(
        width = 1280.dp,
        height = 720.dp
    )
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "TV",
        state = windowState
    ) {
        App()  // 共享的 Compose 入口
    }
}
```

#### 4.5.2 打包配置

使用 `compose.desktop.application` 插件：

```kotlin
// desktop/build.gradle.kts
compose.desktop {
    application {
        mainClass = "com.fongmi.tv.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            
            packageName = "TV"
            packageVersion = "1.0.0"
            
            windows {
                menu = true
                iconFile.set(project.file("src/main/resources/icon.ico"))
                upgradeUuid = "your-uuid-here"
            }
        }
    }
}
```

**验收标准**：
- [ ] `./gradlew :desktop:packageMsi` 能生成 Windows 安装包
- [ ] 安装后能正常启动
- [ ] 应用图标和名称正确

---

## 五、功能对齐矩阵

### 5.1 核心功能

| 功能 | Android 实现 | Windows 实现 | 复用率 | 优先级 |
|------|-------------|-------------|--------|--------|
| **配置加载** | OkHttp | OkHttp (共享) | 100% | P0 |
| **首页内容** | SiteViewModel | SiteViewModel (共享) | 95% | P0 |
| **分类浏览** | SiteViewModel | SiteViewModel (共享) | 95% | P0 |
| **搜索** | SiteViewModel | SiteViewModel (共享) | 95% | P0 |
| **视频详情** | VideoScreen | VideoScreen (共享) | 90% | P0 |
| **播放器** | ExoPlayer | VLC | 30% | P0 |
| **弹幕** | DanmakuController | 自定义实现 | 20% | P1 |
| **历史记录** | Room + HistoryVM | SQLDelight + HistoryVM | 80% | P0 |
| **收藏** | Room + KeepVM | SQLDelight + KeepVM | 80% | P0 |
| **直播** | LiveVM | LiveVM (共享) | 85% | P1 |
| **DLNA 投屏** | jupnp (Android) | jupnp (桌面版) | 60% | P2 |
| **本地服务器** | NanoHTTPD | NanoHTTPD (共享) | 100% | P1 |

### 5.2 爬虫支持

| 爬虫类型 | Android 加载方式 | Windows 加载方式 | 兼容性 |
|----------|-----------------|-----------------|--------|
| **JS 爬虫** | QuickJS (JNI) | GraalJS / QuickJS-JNI | 95% |
| **Python 爬虫** | Chaquopy | GraalPython / CPython | 85% |
| **JAR 爬虫** | DexClassLoader | URLClassLoader | 90% |

### 5.3 不支持的功能（Windows 版）

| 功能 | 原因 | 替代方案 |
|------|------|---------|
| **Thunder/迅雷下载** | AAR 依赖，Android 专属 | 调用系统默认下载器 |
| **TVBus P2P** | AAR 依赖 | 不支持 |
| **ForceTV** | AAR 依赖 | 不支持 |
| **JianPian P2P** | AAR 依赖 | 不支持 |

---

## 六、依赖库对照表

### 6.1 可直接复用（已支持 KMP）

| 功能 | 库 | 版本 | 说明 |
|------|-----|------|------|
| 网络 | OkHttp | 5.3.2 | ✅ 已支持 KMP |
| JSON | Gson | 2.14.0 | ⚠️ 推荐替换为 kotlinx.serialization |
| 图片 | Coil 3 | 3.0+ | ✅ 已支持 KMP |
| 协程 | Kotlin Coroutines | 1.9+ | ✅ KMP 核心 |
| 序列化 | kotlinx.serialization | 1.7+ | ✅ 替代 Gson |

### 6.2 需要替换的库

| 功能 | 原库 | 替换库 | 说明 |
|------|------|--------|------|
| 数据库 | Room | SQLDelight | KMP 数据库方案 |
| 偏好设置 | SharedPreferences | Multiplatform Settings | KMP 偏好设置 |
| 事件总线 | EventBus | Kotlin Flow | 响应式事件 |
| 图片加载 (旧) | Glide | Coil 3 | 统一到 Coil |

### 6.3 平台特定库

| 功能 | Android | Windows/JVM |
|------|---------|-------------|
| 播放器 | Media3 ExoPlayer | vlcj / mpv |
| Python 运行时 | Chaquopy | GraalPython / Jython |
| JS 运行时 | QuickJS (JNI) | GraalJS |
| 动态加载 | DexClassLoader | URLClassLoader |
| 系统托盘 | - | Java AWT |
| 文件对话框 | SAF | JFileChooser / Compose |

---

## 七、风险评估与应对

### 7.1 高风险项

| 风险 | 影响 | 应对策略 |
|------|------|---------|
| **VLC 播放器兼容性** | 部分视频格式可能不支持 | 1. 测试主流格式<br>2. 预留 mpv 备选方案 |
| **JAR 爬虫兼容性** | 部分爬虫可能依赖 Android API | 1. 建立兼容层<br>2. 提供迁移指南 |
| **弹幕性能** | Canvas 渲染可能卡顿 | 1. 使用硬件加速<br>2. 限制同屏数量 |

### 7.2 中风险项

| 风险 | 影响 | 应对策略 |
|------|------|---------|
| **QuickJS 兼容性** | 部分 JS API 可能不一致 | 1. 使用 GraalJS 兼容模式<br>2. 补充 polyfill |
| **SQLDelight 迁移** | 数据库结构可能不兼容 | 1. 编写详细迁移脚本<br>2. 支持数据导出/导入 |

### 7.3 低风险项

| 风险 | 影响 | 应对策略 |
|------|------|---------|
| **UI 差异** | 桌面端布局可能不完美 | 1. 响应式设计<br>2. 平台特定调整 |

---

## 八、开发计划

### 8.1 里程碑

| 阶段 | 时间 | 交付物 | 验收标准 |
|------|------|--------|---------|
| **M1: 基础架构** | 第 1-3 周 | KMP 模块结构、catvod 迁移、SQLDelight | 桌面端能启动空白窗口，数据层可读写 |
| **M2: UI 迁移** | 第 4-7 周 | 所有 Screen 迁移完成 | 桌面端能显示首页、搜索、详情（无播放） |
| **M3: 播放器** | 第 8-10 周 | VLC 集成、弹幕 | 桌面端能播放视频，弹幕正常显示 |
| **M4: 爬虫引擎** | 第 11-14 周 | JS/Python/JAR 爬虫 | 桌面端能加载和执行三种爬虫 |
| **M5: 打包发布** | 第 15-16 周 | Windows 安装包 | MSI 安装包可正常安装和运行 |

### 8.2 团队配置建议

| 角色 | 人数 | 职责 |
|------|------|------|
| **KMP 架构师** | 1 | 模块设计、平台抽象 |
| **UI 开发** | 2 | Compose UI 迁移、响应式设计 |
| **播放器开发** | 1 | VLC 集成、弹幕实现 |
| **爬虫开发** | 1 | QuickJS/CPython 集成 |
| **测试** | 1 | 多平台测试、兼容性验证 |

---

## 九、附录

### 9.1 技术栈总结

```
┌─────────────────────────────────────────────────────────┐
│                    技术栈全景图                          │
├─────────────────────────────────────────────────────────┤
│  UI 层                                                  │
│  ├── Compose Multiplatform 1.7+                        │
│  ├── Material 3                                         │
│  ├── Navigation Compose                                 │
│  └── Coil 3 (KMP)                                      │
├─────────────────────────────────────────────────────────┤
│  业务层                                                  │
│  ├── Kotlin Coroutines + Flow                          │
│  ├── kotlinx.serialization                             │
│  └── Koin (DI)                                         │
├─────────────────────────────────────────────────────────┤
│  数据层                                                  │
│  ├── SQLDelight (数据库)                                │
│  ├── Multiplatform Settings (偏好设置)                  │
│  └── DataStore KMP (可选)                               │
├─────────────────────────────────────────────────────────┤
│  网络层                                                  │
│  ├── OkHttp 5.x                                        │
│  └── Ktor Client (可选)                                 │
├─────────────────────────────────────────────────────────┤
│  平台层                                                  │
│  ├── Android: ExoPlayer, Chaquopy, Room                │
│  └── Desktop: VLC, GraalPython, SQLDelight             │
└─────────────────────────────────────────────────────────┘
```

### 9.2 参考资源

- [Compose Multiplatform 官方文档](https://www.jetbrains.com/lp/compose-multiplatform/)
- [KMP 指南](https://kotlinlang.org/docs/multiplatform.html)
- [vlcj 文档](https://capricasoftware.co.uk/projects/vlcj-6)
- [SQLDelight 文档](https://cashapp.github.io/sqldelight/)

### 9.3 当前进度

#### 已完成（UI 迁移阶段）

- [x] 阶段一：基础设施搭建（Kotlin + Compose 环境）
- [x] 阶段二：基础组件层（VodCard, NavBar, TopBar 等）
- [x] 阶段三：Mobile 端核心页面迁移（HomeScreen, VideoScreen, SearchScreen 等）
- [x] 导航框架 + BottomNavigationBar

#### 进行中

- [ ] 阶段四：Mobile 端 Dialog 迁移（23 个 Dialog）

#### 待开始

- [ ] 阶段五：Leanback（TV）端迁移
- [ ] 阶段六：清理和优化

#### 桌面端开发前的最小化 Kotlin 改造

**核心原则：改造过程中 mobile 和 TV 端功能保持不变**

**兼容性策略：**
1. **Java/Kotlin 互操作**：改造后的 Kotlin 类必须保持与现有 Java 代码 100% 兼容
2. **渐进式改造**：每次只改造一个文件，确保编译通过后再继续
3. **保持 API 不变**：getter/setter 方法签名保持一致，不破坏现有调用
4. **双轨运行**：改造期间 Java 和 Kotlin 文件共存，逐步替换

**必须改造（15 个文件）：**
1. 数据模型：Vod, Result, History, Keep, Site, Config, Episode, Flag, Class, Channel, Group
2. ViewModel：SiteViewModel, LiveViewModel, HistoryViewModel
3. 核心接口：Spider

**改造方式（保持兼容）：**
```kotlin
// 改造前：Vod.java
public class Vod implements Parcelable {
    private String vodId;
    public String getVodId() { return vodId; }
    public void setVodId(String vodId) { this.vodId = vodId; }
}

// 改造后：Vod.kt（保持完全兼容）
@Parcelize
data class Vod(
    @SerializedName("vod_id") private var vodId: String = ""
) : Parcelable {
    // 保持 Java 兼容的 getter/setter
    fun getVodId(): String = vodId
    fun setVodId(value: String) { vodId = value }
    
    // Kotlin 属性访问
    val id: String get() = vodId
}
```

**可保留 Java（通过互操作）：**
- 所有 Activity/Fragment（已用 Compose 替代）
- Room DAO（将用 SQLDelight 替代）
- 平台特定实现

**验收标准：**
- [ ] 每次改造后 `./gradlew assembleMobileArm64_v8aDebug` 编译通过
- [ ] 每次改造后 `./gradlew assembleLeanbackArm64_v8aDebug` 编译通过
- [ ] 现有功能无任何回归
