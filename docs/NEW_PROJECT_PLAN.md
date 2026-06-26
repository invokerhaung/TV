# 新建 Compose Multiplatform 项目详细计划

## 一、项目概述

### 1.1 目标

新建独立的 Compose Multiplatform 项目，支持 Android + Windows 双平台，复用原项目核心模块，重写 UI 和平台特定代码。

### 1.2 项目名称

**TV-Desktop**（包名：`com.fongmi.tv`）

### 1.3 技术栈

| 层级 | 技术选型 |
|------|----------|
| UI | Compose Multiplatform 1.7+ |
| 语言 | Kotlin 2.1+ |
| 网络 | OkHttp 5.x (复用) |
| 序列化 | kotlinx.serialization |
| 数据库 | SQLDelight |
| 依赖注入 | Koin |
| 图片 | Coil 3 (KMP) |
| 协程 | Kotlin Coroutines + Flow |
| 播放器 | ExoPlayer (Android) / VLC (Desktop) |
| 爬虫 | QuickJS (JS) / GraalPython (Python) / URLClassLoader (JAR) |

---

## 二、模块复用与重写分析

### 2.1 完整模块清单

| 模块 | 原路径 | 处理方式 | 说明 |
|------|--------|----------|------|
| **catvod** | `catvod/` | ✅ **直接引用** | Spider 接口、OkHttp 网络层、数据 Bean |
| **quickjs** | `quickjs/` | ⚠️ **引用 + 改造** | JS 引擎，需去除 Android Context 依赖 |
| **chaquo** | `chaquo/` | ❌ **不引用** | Android 专属，桌面端用 GraalPython 替代 |
| **bean/** | `app/.../bean/` | ⚠️ **部分复用** | 数据模型，需去除 Room 注解 |
| **config/** | `app/.../api/config/` | ⚠️ **部分复用** | 配置管理，需去除 Android 依赖 |
| **player/** | `app/.../player/` | ❌ **重写** | 定义抽象接口，平台各自实现 |
| **db/** | `app/.../db/` | ❌ **重写** | Room → SQLDelight |
| **ui/** | `app/.../ui/` | ❌ **重写** | Compose UI 重新设计 |
| **server/** | `app/.../server/` | ❌ **不引用** | PC 端暂不需要，Android 端如需可后续添加 |
| **dlna/** | `app/.../dlna/` | ⚠️ **部分复用** | jupnp 核心逻辑，平台特定部分重写 |
| **setting/** | `app/.../setting/` | ❌ **重写** | SharedPreferences → Multiplatform Settings |
| **event/** | `app/.../event/` | ❌ **不引用** | EventBus → Kotlin Flow |
| **AAR 库** | `app/libs/` | ❌ **不引用** | forcetech/hook/thunder/tvbus 均为 Android 专属 |

### 2.2 catvod 模块（直接引用）

**引用方式**：Git Submodule 或发布到 Maven Local

**可复用的包**：

```
catvod/src/main/java/com/github/catvod/
├── crawler/
│   ├── Spider.java              # 爬虫抽象接口
│   ├── SpiderNull.java          # 空实现
│   └── SpiderDebug.java         # 调试实现
├── bean/
│   ├── Doh.java                 # DNS over HTTPS 配置
│   ├── Header.java              # HTTP 头
│   └── Proxy.java               # 代理配置
├── net/
│   ├── OkHttp.java              # OkHttp 客户端（核心）
│   ├── OkDns.java               # 自定义 DNS
│   ├── OkProxySelector.java     # 代理选择器
│   ├── OkAuthenticator.java     # 认证器
│   └── interceptor/
│       ├── RequestInterceptor.java
│       ├── ResponseInterceptor.java
│       └── AuthInterceptor.java
└── utils/
    ├── Json.java                # JSON 工具
    ├── Trans.java               # 编码转换
    ├── UriUtil.java             # URI 工具
    └── Util.java                # 通用工具
```

**需要改造的部分**：

| 文件 | 改造内容 |
|------|----------|
| `Spider.java` | `init(Context)` → `init(Map<String, String>)` |
| `Init.java` | 去除 `WeakReference<Context>`，改用平台抽象 |
| `Path.java` | `Init.context().getCacheDir()` → `PlatformContext.cacheDir` |
| `Asset.java` | `Init.context().getAssets()` → `PlatformContext.loadAsset()` |
| `Doh.java` | 去除 Context 参数，改用配置注入 |

**改造后的 Spider 接口**：

```kotlin
// commonMain
abstract class Spider {
    open fun init(context: Map<String, String>) {}
    open suspend fun homeContent(filter: Boolean): String = ""
    open suspend fun homeVideoContent(): String = ""
    open suspend fun categoryContent(tid: String, pg: String, filter: Boolean, extend: Map<String, String>): String = ""
    open suspend fun detailContent(ids: List<String>): String = ""
    open suspend fun searchContent(key: String, quick: Boolean, pg: String): String = ""
    open suspend fun playerContent(flag: String, id: String, vipFlags: List<String>): String = ""
    open suspend fun liveContent(url: String): String = ""
    open fun destroy() {}
}

// 平台抽象
expect object PlatformContext {
    val cacheDir: String
    val filesDir: String
    fun loadAsset(path: String): ByteArray
}
```

### 2.3 quickjs 模块（引用 + 改造）

**引用方式**：Git Submodule

**可复用的包**：

```
quickjs/src/main/java/com/fongmi/quickjs/
├── crawler/
│   ├── Loader.java              # 加载器
│   ├── Spider.java              # JS Spider 封装
│   ├── Global.java              # 全局函数注入
│   ├── Connect.java             # 网络连接
│   └── Module.java              # 模块加载
└── utils/
    └── JSUtil.java              # JS 工具
```

**需要改造的部分**：

| 文件 | 改造内容 |
|------|----------|
| `Spider.java` | `init(Context)` → `init(Map)` |
| `Module.java` | `context.getAssets()` → `PlatformContext.loadAsset()` |
| `Global.java` | SharedPreferences → Multiplatform Settings |

### 2.4 bean 模块（部分复用）

**需要复用的数据模型**：

| 原文件 | 处理方式 | 改造内容 |
|--------|----------|----------|
| `Vod.java` | 复用 | 去除 `@SerializedName`，改用 `@Serializable` |
| `Result.java` | 复用 | 同上 |
| `Site.java` | 复用 | 去除 `@Entity`、`@PrimaryKey` |
| `Config.java` | 复用 | 去除 Room 注解 |
| `History.java` | 复用 | 去除 Room 注解 |
| `Keep.java` | 复用 | 去除 Room 注解 |
| `Live.java` | 复用 | 去除 Room 注解 |
| `Channel.java` | 复用 | 纯数据类，直接复用 |
| `Episode.java` | 复用 | 纯数据类，直接复用 |
| `Flag.java` | 复用 | 纯数据类，直接复用 |
| `Sub.java` | 复用 | 纯数据类，直接复用 |
| `Danmaku.java` | 复用 | 纯数据类，直接复用 |
| `Track.java` | 复用 | 去除 Room 注解 |
| `Device.java` | 复用 | 去除 Room 注解 |
| `Group.java` | 复用 | 纯数据类 |
| `Filter.java` | 复用 | 纯数据类 |
| `Class.java` | 复用 | 纯数据类 |

**改造示例**：

```kotlin
// 原版（Android）
@Entity(tableName = "history")
data class History(
    @PrimaryKey
    var key: String = "",
    @ColumnInfo(name = "vod_id")
    var vodId: String = "",
    // ...
)

// KMP 版
@Serializable
data class History(
    val key: String = "",
    val vodId: String = "",
    // ... (无 Room 注解，表结构在 SQLDelight 中定义)
)
```

### 2.5 server 模块（不引用）

**原因**：PC 端暂不需要 NanoHTTPD 服务器功能（远程控制、文件管理等）。

**说明**：
- 原项目中 NanoHTTPD 主要用于：远程遥控器、爬虫代理、文件管理、视频解析
- PC 端用户直接操作，不需要远程控制
- 爬虫代理功能可直接在进程内调用，无需 HTTP 中转
- 如后续需要，可单独添加

---

## 三、需要重写的模块

### 3.1 数据库层（Room → SQLDelight）

**原 Room 实体 → SQLDelight 表定义**：

```sql
-- sql/sqldelight/com/fongmi/tv/db/History.sq

-- 建表
CREATE TABLE history (
    key TEXT NOT NULL PRIMARY KEY,
    cid TEXT NOT NULL DEFAULT '',
    vod_id TEXT NOT NULL DEFAULT '',
    vod_name TEXT NOT NULL DEFAULT '',
    vod_pic TEXT NOT NULL DEFAULT '',
    episode_name TEXT NOT NULL DEFAULT '',
    create_time INTEGER NOT NULL DEFAULT 0,
    position INTEGER NOT NULL DEFAULT 0,
    duration INTEGER NOT NULL DEFAULT 0,
    opening INTEGER NOT NULL DEFAULT 0,
    ending INTEGER NOT NULL DEFAULT 0,
    speed REAL NOT NULL DEFAULT 1.0,
    scale INTEGER NOT NULL DEFAULT 0
);

-- 查询最近历史
findAll:
SELECT * FROM history
WHERE cid = ?
ORDER BY create_time DESC
LIMIT 60;

-- 按名称搜索
findByName:
SELECT * FROM history
WHERE vod_name LIKE '%' || ? || '%';

-- 插入或更新
insertOrUpdate:
INSERT OR REPLACE INTO history (key, cid, vod_id, vod_name, vod_pic, episode_name, create_time, position, duration, opening, ending, speed, scale)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

-- 删除
deleteByKey:
DELETE FROM history WHERE key = ?;
```

**DAO 接口**：

```kotlin
// commonMain
interface HistoryDao {
    suspend fun findAll(cid: String): List<History>
    suspend fun findByName(name: String): List<History>
    suspend fun findOne(key: String): History?
    suspend fun insertOrUpdate(history: History)
    suspend fun deleteByKey(key: String)
}
```

### 3.2 偏好设置（SharedPreferences → Multiplatform Settings）

```kotlin
// commonMain
interface SettingsStore {
    var configUrl: String?
    var liveUrl: String?
    var wallpaperUrl: String?
    var decode: Int
    var playerType: Int
    var danmakuEnabled: Boolean
    // ... 其他设置
}

// androidMain
class AndroidSettingsStore(private val prefs: SharedPreferences) : SettingsStore {
    override var configUrl: String?
        get() = prefs.getString("config_url", null)
        set(value) = prefs.edit().putString("config_url", value).apply()
    // ...
}

// desktopMain
class DesktopSettingsStore(private val file: File) : SettingsStore {
    private val properties = Properties().apply {
        if (file.exists()) file.inputStream().use { load(it) }
    }
    override var configUrl: String?
        get() = properties.getProperty("config_url")
        set(value) { properties.setProperty("config_url", value); save() }
    // ...
}
```

### 3.3 播放器模块（全新设计）

**抽象接口**：

```kotlin
// commonMain
interface PlayerEngine {
    val state: StateFlow<PlayerState>
    val position: StateFlow<Long>
    val duration: StateFlow<Long>
    val videoSize: StateFlow<VideoSize>

    suspend fun prepare(spec: PlaySpec)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun setVolume(volume: Float)
    fun setSpeed(speed: Float)
    fun setTrack(type: TrackType, index: Int)
    fun release()
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val error: String? = null
)

data class PlaySpec(
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val format: String? = null,
    val subtitles: List<Sub> = emptyList(),
    val startPosition: Long = 0
)

data class VideoSize(val width: Int, val height: Int)

enum class TrackType { VIDEO, AUDIO, SUBTITLE }

expect fun createPlayerEngine(): PlayerEngine
```

**Android 实现**：

```kotlin
// androidMain
class ExoPlayerEngine(private val context: Context) : PlayerEngine {
    private val player = ExoPlayer.Builder(context).build()
    // ... 使用 Media3 实现
}

actual fun createPlayerEngine(): PlayerEngine = ExoPlayerEngine(App.get())
```

**Desktop 实现**：

```kotlin
// desktopMain
class VlcPlayerEngine : PlayerEngine {
    private val factory = MediaPlayerFactory()
    private val player = factory.mediaPlayers().newMediaPlayer()
    // ... 使用 vlcj 实现
}

actual fun createPlayerEngine(): PlayerEngine = VlcPlayerEngine()
```

### 3.4 ViewModel 层（全新设计）

```kotlin
// commonMain
class SiteViewModel(
    private val configManager: ConfigManager
) : ViewModel() {
    private val _homeResult = MutableStateFlow<Result?>(null)
    val homeResult: StateFlow<Result?> = _homeResult.asStateFlow()

    private val _searchResult = MutableStateFlow<Result?>(null)
    val searchResult: StateFlow<Result?> = _searchResult.asStateFlow()

    private val _detailResult = MutableStateFlow<Result?>(null)
    val detailResult: StateFlow<Result?> = _detailResult.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun loadHome() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val spider = configManager.getSpider()
                val json = spider.homeContent(false)
                _homeResult.value = Gson().fromJson(json, Result::class.java)
            } catch (e: Exception) {
                // 错误处理
            } finally {
                _loading.value = false
            }
        }
    }

    fun search(keyword: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val spider = configManager.getSpider()
                val json = spider.searchContent(keyword, false, "1")
                _searchResult.value = Gson().fromJson(json, Result::class.java)
            } finally {
                _loading.value = false
            }
        }
    }
}
```

### 3.5 配置管理（重写）

```kotlin
// commonMain
class ConfigManager(
    private val httpClient: OkHttpClient,
    private val settings: SettingsStore,
    private val database: AppDatabase
) {
    private var vodConfig: VodConfig? = null
    private val spiders = ConcurrentHashMap<String, Spider>()

    suspend fun loadConfig(url: String): VodConfig {
        val json = withContext(Dispatchers.IO) {
            httpClient.newCall(Request.Builder().url(url).build())
                .execute().body?.string() ?: ""
        }
        vodConfig = Json.decodeFromString<VodConfig>(json)
        database.configDao().insertOrUpdate(Config(url = url, type = 0))
        return vodConfig!!
    }

    fun getSpider(key: String? = null): Spider {
        val siteKey = key ?: vodConfig?.sites?.firstOrNull()?.key ?: ""
        return spiders.getOrPut(siteKey) {
            val site = vodConfig?.sites?.find { it.key == siteKey }
            SpiderLoader.load(site?.api ?: "", site?.ext ?: "")
        }
    }
}
```

---

## 四、完整目录结构

```
TV-Desktop/
├── .git/
├── .gitignore
├── .idea/
├── build.gradle.kts                    # 根构建脚本
├── settings.gradle.kts                 # 模块声明
├── gradle.properties
├── gradle/
│   └── libs.versions.toml              # 版本目录
├── gradlew
├── gradlew.bat
│
├── docs/                               # 文档
│   ├── README.md
│   ├── ARCHITECTURE.md
│   └── CHANGELOG.md
│
├── catvod/                             # [引用] 爬虫抽象层（Git Submodule）
│   ├── build.gradle.kts                # 改造为 KMP 模块
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/
│       │       └── com/github/catvod/
│       │           ├── crawler/
│       │           │   ├── Spider.kt           # 改造：去除 Context
│       │           │   ├── SpiderNull.kt
│       │           │   └── PlatformContext.kt  # 新增：平台抽象
│       │           ├── bean/
│       │           │   ├── Doh.kt
│       │           │   ├── Header.kt
│       │           │   └── Proxy.kt
│       │           ├── net/
│       │           │   ├── OkHttp.kt           # 可直接复用
│       │           │   ├── OkDns.kt
│       │           │   └── interceptor/
│       │           └── utils/
│       │               ├── Json.kt
│       │               └── Trans.kt
│       ├── androidMain/
│       │   └── kotlin/
│       │       └── com/github/catvod/
│       │           └── PlatformContext.kt      # Android 实现
│       └── desktopMain/
│           └── kotlin/
│               └── com/github/catvod/
│                   └── PlatformContext.kt      # Desktop 实现
│
├── quickjs/                            # [引用] JS 引擎（Git Submodule）
│   ├── build.gradle.kts                # 改造为 KMP 模块
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/
│       │       └── com/fongmi/quickjs/
│       │           ├── JsEngine.kt            # JS 引擎接口
│       │           └── JsResult.kt
│       ├── androidMain/
│       │   └── kotlin/
│       │       └── com/fongmi/quickjs/
│       │           └── QuickJsEngine.kt       # Android 实现
│       └── desktopMain/
│           └── kotlin/
│               └── com/fongmi/quickjs/
│                   └── GraalJsEngine.kt       # Desktop 实现（GraalJS）
│
├── core/                               # [重写] 核心业务逻辑
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/
│       │   │   └── com/fongmi/tv/core/
│       │   │       ├── config/
│       │   │       │   ├── ConfigManager.kt       # 配置管理
│       │   │       │   ├── VodConfig.kt           # VOD 配置模型
│       │   │       │   └── LiveConfig.kt          # 直播配置模型
│       │   │       ├── spider/
│       │   │       │   ├── SpiderLoader.kt        # 爬虫加载器
│       │   │       │   ├── JarLoader.kt           # JAR 加载（抽象）
│       │   │       │   ├── JsLoader.kt            # JS 加载（抽象）
│       │   │       │   └── PyLoader.kt            # Python 加载（抽象）
│       │   │       ├── database/
│       │   │       │   ├── AppDatabase.kt         # 数据库接口
│       │   │       │   └── dao/
│       │   │       │       ├── HistoryDao.kt
│       │   │       │       ├── ConfigDao.kt
│       │   │       │       ├── KeepDao.kt
│       │   │       │       ├── SiteDao.kt
│       │   │       │       └── LiveDao.kt
│       │   │       ├── model/
│       │   │       │   ├── Vod.kt                 # 视频模型
│       │   │       │   ├── Result.kt              # 结果模型
│       │   │       │   ├── Site.kt                # 站点模型
│       │   │       │   ├── History.kt             # 历史模型
│       │   │       │   ├── Keep.kt                # 收藏模型
│       │   │       │   ├── Live.kt                # 直播模型
│       │   │       │   ├── Channel.kt             # 频道模型
│       │   │       │   ├── Episode.kt             # 剧集模型
│       │   │       │   ├── Flag.kt                # 线路模型
│       │   │       │   ├── Group.kt               # 分组模型
│       │   │       │   ├── Filter.kt              # 筛选模型
│       │   │       │   ├── Sub.kt                 # 字幕模型
│       │   │       │   └── Danmaku.kt             # 弹幕模型
│       │   │       └── util/
│       │   │           ├── Constants.kt
│       │   │           └── Extensions.kt
│       │   └── sqldelight/
│       │       └── com/fongmi/tv/db/
│       │           ├── History.sq
│       │           ├── Config.sq
│       │           ├── Keep.sq
│       │           ├── Site.sq
│       │           ├── Live.sq
│       │           ├── Track.sq
│       │           └── Device.sq
│       ├── androidMain/
│       │   └── kotlin/
│       │       └── com/fongmi/tv/core/
│       │           ├── database/
│       │           │   └── AndroidDatabase.kt     # Android SQLDelight 驱动
│       │           └── spider/
│       │               ├── AndroidJarLoader.kt    # DexClassLoader
│       │               └── AndroidPyLoader.kt    # Chaquopy
│       └── desktopMain/
│           └── kotlin/
│               └── com/fongmi/tv/core/
│                   ├── database/
│                   │   └── DesktopDatabase.kt     # Desktop SQLDelight 驱动
│                   └── spider/
│                       ├── DesktopJarLoader.kt    # URLClassLoader + dex-tools
│                       ├── DesktopJsLoader.kt     # GraalJS
│                       └── DesktopPyLoader.kt     # GraalPython
│
├── player/                             # [重写] 播放器模块
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/
│       │       └── com/fongmi/tv/player/
│       │           ├── PlayerEngine.kt            # 播放器接口
│       │           ├── PlayerManager.kt           # 播放器管理
│       │           ├── PlayerState.kt             # 状态模型
│       │           ├── PlaySpec.kt                # 播放规格
│       │           ├── TrackInfo.kt               # 轨道信息
│       │           └── SubtitleRenderer.kt        # 字幕渲染接口
│       ├── androidMain/
│       │   └── kotlin/
│       │       └── com/fongmi/tv/player/
│       │           ├── ExoPlayerEngine.kt         # ExoPlayer 实现
│       │           └── ExoSubtitleRenderer.kt
│       └── desktopMain/
│           └── kotlin/
│               └── com/fongmi/tv/player/
│                   ├── VlcPlayerEngine.kt         # VLC 实现
│                   └── VlcSubtitleRenderer.kt
│
├── composeApp/                         # [重写] Compose UI 模块
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/
│       │   │   └── com/fongmi/tv/ui/
│       │   │       ├── App.kt                    # 应用入口
│       │   │       ├── navigation/
│       │   │       │   ├── AppNavigation.kt      # 导航定义
│       │   │       │   └── AppRoutes.kt          # 路由定义
│       │   │       ├── screen/
│       │   │       │   ├── HomeScreen.kt         # 首页
│       │   │       │   ├── VideoScreen.kt        # 视频详情
│       │   │       │   ├── SearchScreen.kt       # 搜索
│       │   │       │   ├── LiveScreen.kt         # 直播
│       │   │       │   ├── FavoriteScreen.kt     # 收藏
│       │   │       │   ├── HistoryScreen.kt      # 历史
│       │   │       │   ├── SettingsScreen.kt     # 设置
│       │   │       │   └── PlayerScreen.kt       # 播放器
│       │   │       ├── component/
│       │   │       │   ├── VodCard.kt            # 视频卡片
│       │   │       │   ├── ImageAsync.kt         # 异步图片
│       │   │       │   ├── TopBar.kt             # 顶部栏
│       │   │       │   ├── NavBar.kt             # 导航栏
│       │   │       │   ├── FilterBar.kt          # 筛选栏
│       │   │       │   ├── EpisodeGrid.kt        # 剧集网格
│       │   │       │   ├── LoadingState.kt       # 加载状态
│       │   │       │   ├── DanmakuOverlay.kt     # 弹幕覆盖层
│       │   │       │   └── Dialog.kt             # 对话框
│       │   │       ├── theme/
│       │   │       │   ├── Color.kt              # 颜色定义
│       │   │       │   ├── Type.kt               # 字体定义
│       │   │       │   └── Theme.kt              # 主题定义
│       │   │       └── viewmodel/
│       │   │           ├── SiteViewModel.kt       # 站点 ViewModel
│       │   │           ├── LiveViewModel.kt       # 直播 ViewModel
│       │   │           ├── HistoryViewModel.kt    # 历史 ViewModel
│       │   │           ├── FavoriteViewModel.kt   # 收藏 ViewModel
│       │   │           └── PlayerViewModel.kt     # 播放器 ViewModel
│       │   └── composeResources/
│       │       ├── drawable/                      # 图标资源
│       │       │   ├── ic_home.xml
│       │       │   ├── ic_live.xml
│       │       │   ├── ic_search.xml
│       │       │   ├── ic_favorite.xml
│       │       │   ├── ic_settings.xml
│       │       │   ├── ic_back.xml
│       │       │   ├── ic_play.xml
│       │       │   ├── ic_pause.xml
│       │       │   └── ...
│       │       ├── values/
│       │       │   └── strings.xml                # 字符串资源
│       │       └── font/                          # 字体资源
│       ├── androidMain/
│       │   └── kotlin/
│       │       └── com/fongmi/tv/ui/
│       │           ├── theme/
│       │           │   └── PlatformTheme.kt       # Android 主题扩展
│       │           └── component/
│       │               └── PlayerView.kt          # Android 播放器视图
│       └── desktopMain/
│           └── kotlin/
│               └── com/fongmi/tv/ui/
│                   ├── theme/
│                   │   └── PlatformTheme.kt       # Desktop 主题扩展
│                   └── component/
│                       └── PlayerView.kt          # Desktop 播放器视图
│
├── app/                                # [重写] Android 应用壳
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── kotlin/
│           │   └── com/fongmi/tv/
│           │       ├── App.kt                    # Application
│           │       └── MainActivity.kt           # 主 Activity
│           └── res/
│               ├── values/
│               │   ├── strings.xml
│               │   ├── colors.xml
│               │   └── themes.xml
│               └── mipmap-*/
│                   └── ic_launcher.xml
│
├── desktop/                            # [重写] Windows 应用壳
│   ├── build.gradle.kts
│   └── src/
│       └── jvmMain/
│           ├── kotlin/
│           │   └── com/fongmi/tv/
│           │       └── Main.kt                   # 桌面入口
│           └── resources/
│               └── icon.ico                       # 应用图标
│
└── buildSrc/                           # [新增] 构建逻辑
    ├── build.gradle.kts
    └── src/
        └── main/
            └── kotlin/
                └── BuildConfig.kt                # 版本常量
```

---

## 五、Git Submodule 配置

### 5.1 引用原项目模块

```bash
# 在 TV-Desktop 目录下
git submodule add <原项目仓库URL>/catvod.git catvod
git submodule add <原项目仓库URL>/quickjs.git quickjs
```

### 5.2 改造策略

对于引用的模块，有两种策略：

**策略 A：Fork 后修改（推荐）**

```bash
# 1. Fork 原项目
# 2. 在 Fork 中创建 kmp 分支
# 3. 改造 catvod 和 quickjs 为 KMP 模块
# 4. 在 TV-Desktop 中引用 Fork
git submodule add <fork仓库URL>/catvod.git catvod
```

**策略 B：直接复制源码**

```bash
# 1. 复制 catvod 源码到 TV-Desktop/catvod/
# 2. 手动改造为 KMP 模块
# 3. 后续与原项目手动同步更新
```

**推荐策略 A**：便于同步原项目更新。

---

## 六、依赖配置

### 6.1 根 build.gradle.kts

```kotlin
plugins {
    kotlin("multiplatform") version "2.1.0" apply false
    kotlin("plugin.serialization") version "2.1.0" apply false
    id("org.jetbrains.compose") version "1.7.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("app.cash.sqldelight") version "2.0.0" apply false
}
```

### 6.2 gradle/libs.versions.toml

```toml
[versions]
kotlin = "2.1.0"
compose = "1.7.0"
coroutines = "1.9.0"
serialization = "1.7.3"
okhttp = "5.0.0-alpha.14"
sqldelight = "2.0.0"
coil = "3.0.4"
koin = "3.5.6"
navigation = "2.8.0-alpha10"
vlcj = "4.10.1"

[libraries]
# Kotlin
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }

# Compose
compose-navigation = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation" }

# Network
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-brotli = { module = "com.squareup.okhttp3:okhttp-brotli", version.ref = "okhttp" }
okhttp-doh = { module = "com.squareup.okhttp3:okhttp-dnsoverhttps", version.ref = "okhttp" }

# Database
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-sqlite = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }

# Image
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-okhttp = { module = "io.coil-kt.coil3:coil-network-okhttp", version.ref = "coil" }

# DI
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }

# Desktop
vlcj = { module = "uk.co.caprica:vlcj", version.ref = "vlcj" }
vlcj-javafx = { module = "uk.co.caprica:vlcj-javafx", version.ref = "vlcj" }

# Dex tools (for JAR conversion)
dexlib2 = { module = "org.smali:dexlib2", version = "2.5.2" }

[bundles]
okhttp = ["okhttp", "okhttp-brotli", "okhttp-doh"]
sqldelight = ["sqldelight-runtime", "sqldelight-coroutines"]
```

### 6.3 composeApp/build.gradle.kts

```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.animation)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.navigation)
            implementation(libs.coil.compose)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Project modules
            implementation(project(":catvod"))
            implementation(project(":core"))
            implementation(project(":player"))
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.sqldelight.android)
            implementation(libs.coil.network.okhttp)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.sqldelight.sqlite)
            implementation(libs.vlcj)
            implementation(libs.vlcj.javafx)
            implementation(libs.dexlib2)
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.fongmi.tv.db")
        }
    }
}
```

---

## 七、开发阶段与验收标准

### 7.1 M0: 项目搭建（第 1 周）

**任务**：
- [ ] 创建 TV-Desktop 项目结构
- [ ] 配置 Git Submodule（catvod, quickjs）
- [ ] 配置 Gradle 构建脚本
- [ ] 配置 Compose Multiplatform 插件
- [ ] 创建空的模块目录结构

**验收标准**：
- [ ] `./gradlew :desktop:run` 能启动空白窗口
- [ ] `./gradlew :app:assembleDebug` 能生成 Android APK
- [ ] Git Submodule 能正常拉取

### 7.2 M1: 核心层（第 2-3 周）

**任务**：
- [ ] 改造 catvod 为 KMP 模块
- [ ] 定义 PlatformContext expect/actual
- [ ] 创建 SQLDelight 数据库定义
- [ ] 创建数据模型（去除 Room 注解）
- [ ] 实现 ConfigManager
- [ ] 实现 SpiderLoader 抽象

**验收标准**：
- [ ] catvod 模块在两个平台编译通过
- [ ] SQLDelight 能在两个平台创建数据库
- [ ] ConfigManager 能加载远程配置
- [ ] 数据模型序列化/反序列化正常

### 7.3 M2: 基础 UI（第 4-6 周）

**任务**：
- [ ] 迁移 Compose Resources（图标、字符串）
- [ ] 实现 Theme 系统（expect/actual）
- [ ] 实现 ImageAsync（Coil 3 KMP）
- [ ] 实现 HomeScreen
- [ ] 实现 SearchScreen
- [ ] 实现 VideoScreen
- [ ] 实现 HistoryScreen
- [ ] 实现 FavoriteScreen
- [ ] 实现 SettingsScreen
- [ ] 实现 Navigation

**验收标准**：
- [ ] 首页能显示内容列表
- [ ] 搜索功能正常
- [ ] 视频详情页能显示信息
- [ ] 历史和收藏列表正常显示
- [ ] 设置页能修改配置

### 7.4 M3: 播放器（第 7-8 周）

**任务**：
- [ ] 定义 PlayerEngine 接口
- [ ] 实现 ExoPlayerEngine（Android）
- [ ] 实现 VlcPlayerEngine（Desktop）
- [ ] 实现 PlayerScreen
- [ ] 实现播放控制（播放/暂停/seek）
- [ ] 实现字幕加载

**验收标准**：
- [ ] Android 端能播放 HLS/HTTP 流
- [ ] Desktop 端能播放 HLS/HTTP 流
- [ ] 播放控制正常
- [ ] 字幕能正常显示

### 7.5 M4: 爬虫引擎（第 9-11 周）

**任务**：
- [ ] 实现 JsLoader（GraalJS）
- [ ] 实现 PyLoader（GraalPython）
- [ ] 实现 JarLoader（URLClassLoader + dex-tools）
- [ ] 集成 quickjs 模块
- [ ] 测试 JS 爬虫
- [ ] 测试 Python 爬虫
- [ ] 测试 JAR 爬虫

**验收标准**：
- [ ] JS 爬虫能在两个平台加载和执行
- [ ] Python 爬虫能在两个平台加载和执行
- [ ] JAR 爬虫（标准和 dex 格式）能正常加载
- [ ] 爬虫的 HTTP 请求正常

### 7.6 M5: 完善与打包（第 12-13 周）

**任务**：
- [ ] 实现弹幕系统
- [ ] 实现本地服务器集成
- [ ] 实现 DLNA 基础功能
- [ ] 性能优化
- [ ] Bug 修复
- [ ] Windows MSI 打包
- [ ] Android APK 签名打包

**验收标准**：
- [ ] 弹幕能正常显示
- [ ] 本地服务器能正常访问
- [ ] Windows 安装包能正常安装和运行
- [ ] Android APK 能正常安装和运行
- [ ] 核心功能与原项目基本一致

---

## 八、风险与应对

| 风险 | 影响 | 应对策略 |
|------|------|----------|
| VLC 播放器兼容性 | 部分格式不支持 | 预留 mpv 备选方案 |
| GraalJS 与 QuickJS 差异 | 部分 JS API 不兼容 | 补充 polyfill |
| JAR 爬虫转换失败 | 部分爬虫无法加载 | 提供详细错误日志 |
| SQLDelight 迁移 | 数据结构不兼容 | 提供数据导入工具 |
| 原项目更新 | 需同步 catvod/quickjs | 使用 Git Submodule 定期同步 |

---

## 九、总结

| 分类 | 模块 | 处理方式 | 工作量 |
|------|------|----------|--------|
| **引用** | catvod | Git Submodule + 改造 | 1 周 |
| **引用** | quickjs | Git Submodule + 改造 | 0.5 周 |
| **不引用** | server | PC 端暂不需要 | 0 |
| **重写** | core (配置/爬虫/数据库) | 全新编写 | 2 周 |
| **重写** | player | 全新编写 | 2 周 |
| **重写** | composeApp (UI) | 参考原项目重写 | 3 周 |
| **重写** | app (Android 壳) | 全新编写 | 0.5 周 |
| **重写** | desktop (Windows 壳) | 全新编写 | 0.5 周 |
| **总计** | | | **~9.5 周** |

核心复用率约 25%（catvod + quickjs），其余为全新编写。相比原项目迁移，新项目更干净、更易维护。
