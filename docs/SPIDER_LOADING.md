# Spider 加载机制文档

## 概述

本文档详细分析项目的 Spider（爬虫）加载机制、调用链和使用方式。

Spider 是内容源的核心抽象，支持三种语言实现：**Java（jar）**、**JavaScript（js）**、**Python（py）**。

## 多配置源支持

项目支持多个独立的 JSON 配置文件，用户可以通过弹窗选择使用哪个配置源。

### 功能特性

- 支持添加多个 vod 配置源
- 通过弹窗查看所有配置源
- 切换配置源后首页内容自动更新
- 支持删除不需要的配置源
- 配置源列表显示名称和 URL
- 当前选中的配置源有高亮标识

### 相关文件

| 文件 | 路径 | 用途 |
|------|------|------|
| ConfigSourceDialog (mobile) | `app/src/mobile/java/com/fongmi/android/tv/ui/dialog/ConfigSourceDialog.java` | 手机版配置源选择弹窗 |
| ConfigSourceDialog (leanback) | `app/src/leanback/java/com/fongmi/android/tv/ui/dialog/ConfigSourceDialog.java` | TV版配置源选择弹窗 |
| dialog_config_source.xml (mobile) | `app/src/mobile/res/layout/dialog_config_source.xml` | 手机版弹窗布局 |
| dialog_config_source.xml (leanback) | `app/src/leanback/res/layout/dialog_config_source.xml` | TV版弹窗布局 |
| item_config_source.xml (mobile) | `app/src/mobile/res/layout/item_config_source.xml` | 手机版列表项布局 |
| item_config_source.xml (leanback) | `app/src/leanback/res/layout/item_config_source.xml` | TV版列表项布局 |
| ic_setting_source.xml | `app/src/main/res/drawable/ic_setting_source.xml` | 配置源图标 |

### 使用方式

1. 在设置页面点击 vod 行右侧的"选择源"图标
2. 弹窗显示所有已添加的配置源
3. 点击配置源切换，长按显示删除按钮
4. 点击"添加配置源"按钮添加新的配置源

## 架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                      配置文件 (config.json)                   │
│  "spider": "assets://custom_spider.jar"  ← 全局 jar          │
│  "sites": [{ "api": "csp_Bili", ... }]  ← 站点定义           │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    VodConfig (配置解析)                       │
│  parseConfig() → initSite() → 解析 sites 数组                │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│              BaseLoader (Spider 调度中心 - 单例)              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  JarLoader   │  │  JsLoader    │  │  PyLoader    │       │
│  │ (DexClass)   │  │ (QuickJS)    │  │ (Chaquopy)   │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
└─────────┼─────────────────┼─────────────────┼───────────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spider 抽象基类                           │
│  init() / homeContent() / categoryContent() / ...           │
└─────────────────────────────────────────────────────────────┘
```

## 一、Spider 接口定义

### 文件位置

`catvod/src/main/java/com/github/catvod/crawler/Spider.java`

### 生命周期方法

| 方法 | 参数 | 返回值 | 用途 |
|------|------|--------|------|
| `init(Context, String)` | context, extend配置 | void | 初始化爬虫，加载配置 |
| `homeContent(boolean)` | filter是否显示筛选 | JSON String | 获取首页分类和筛选条件 |
| `homeVideoContent()` | 无 | JSON String | 获取首页推荐视频列表 |
| `categoryContent(...)` | tid, pg, filter, extend | JSON String | 获取分类下的视频列表（支持翻页） |
| `detailContent(List<String>)` | ids视频ID列表 | JSON String | 获取视频详情（播放源、剧集等） |
| `searchContent(String, boolean)` | key关键词, quick快速 | JSON String | 搜索视频 |
| `searchContent(String, boolean, String)` | key, quick, pg页码 | JSON String | 搜索视频（带分页） |
| `playerContent(String, String, List)` | flag播放源, id视频ID, vipFlags | JSON String | 获取播放地址 |
| `liveContent(String)` | url直播源地址 | JSON String | 获取直播源内容 |
| `manualVideoCheck()` | 无 | boolean | 是否使用自定义视频检测 |
| `isVideoFormat(String)` | url | boolean | 判断URL是否为视频格式 |
| `proxy(Map<String,String>)` | params参数 | Object[] | 本地代理处理 |
| `action(String)` | action动作 | String | 执行自定义动作 |
| `destroy()` | 无 | void | 销毁爬虫，释放资源 |

### 辅助类

| 类 | 文件位置 | 用途 |
|----|----------|------|
| `SpiderNull` | `catvod/src/main/java/com/github/catvod/crawler/SpiderNull.java` | 空实现，加载失败时的占位符 |
| `SpiderDebug` | `catvod/src/main/java/com/github/catvod/crawler/SpiderDebug.java` | 调试日志工具 |

## 二、BaseLoader 调度中心

### 文件位置

`app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java`

### 路由逻辑

BaseLoader 是单例模式，内部持有三个子 Loader，根据 `api` 字段的格式路由到对应的 Loader：

```java
// api 包含 ".py"  → PyLoader（Python 爬虫）
// api 包含 ".js"  → JsLoader（JavaScript 爬虫）
// api 以 "csp_" 开头 → JarLoader（Java 爬虫）
// 其他情况 → SpiderNull（空实现）
```

### 核心方法

| 方法 | 用途 |
|------|------|
| `getSpider(key, api, ext, jar)` | 根据 api 路由到对应 Loader 获取 Spider 实例 |
| `getSpider(key)` | 通过 siteKey 获取已缓存的 Spider |
| `parseJar(jar, recent)` | 预加载全局 jar 文件 |
| `proxy(params)` | 代理请求路由 |
| `clear()` | 清除所有已加载的 Spider |

## 三、三种爬虫加载方式

### 3.1 JarLoader（Java 爬虫）

**文件**: `app/src/main/java/com/fongmi/android/tv/api/loader/JarLoader.java`

**适用场景**: api 以 `csp_` 开头的站点

**加载流程**:

```
1. parseJar(key, jar)
   ├── 下载或验证 jar 文件（支持 HTTP/file/assets 协议）
   ├── 可选 MD5 校验
   └── 调用 load() 加载

2. load(key, file)
   ├── 创建 DexClassLoader
   ├── invokeInit() → 反射调用 com.github.catvod.spider.Init.init(Context)
   ├── invokeProxy() → 反射获取 com.github.catvod.spider.Proxy.proxy(Map)
   └── 缓存 ClassLoader

3. getSpider(key, api, ext, jar)
   ├── computeIfAbsent 懒加载
   ├── 从 api 提取类名：csp_Bili → com.github.catvod.spider.Bili
   ├── loader.loadClass().newInstance() 实例化
   ├── 设置 siteKey
   ├── 调用 init(context, ext)
   └── 缓存到 spiders Map
```

**核心数据结构**:

```java
ConcurrentHashMap<String, DexClassLoader> loaders;  // jar key → ClassLoader
ConcurrentHashMap<String, Method> methods;          // proxy 方法缓存
ConcurrentHashMap<String, Spider> spiders;          // spider key → 实例
```

### 3.2 JsLoader（JavaScript 爬虫）

**文件**: `app/src/main/java/com/fongmi/android/tv/api/loader/JsLoader.java`

**适用场景**: api 包含 `.js` 的站点

**加载流程**:

```
1. getSpider(key, api, ext, jar)
   ├── computeIfAbsent 懒加载
   ├── loader.spider(api, dex) 创建 QuickJS Spider
   │   └── 委托给 quickjs 模块的 Loader
   ├── 设置 siteKey
   ├── 调用 init(context, ext)
   └── 缓存到 spiders Map
```

**QuickJS Spider 内部初始化** (`quickjs/src/main/java/com/fongmi/quickjs/crawler/Spider.java`):

```
initializeJS()
├── createCtx()
│   ├── 创建 QuickJSContext
│   ├── 注入 Console
│   ├── 加载 http.js（提供 req/http 网络函数）
│   ├── 注入 Local 存储
│   └── 设置模块加载器
├── createFun()
│   ├── 注入 Global 函数（s2t/t2s/getProxy/js2Proxy/setTimeout/加密等）
│   └── 尝试加载 jar 中的 Function 扩展
└── createObj()
    ├── 从网络/assets 读取 JS 文件
    ├── 判断是否 catvod 格式（__jsEvalReturn）
    ├── 执行 JS 模块
    ├── 执行 spider.js 桥接脚本
    └── 获取 __JS_SPIDER__ 对象
```

**spider.js 桥接脚本** (`quickjs/src/main/assets/js/lib/spider.js`):

```javascript
import * as spider from '%s'  // %s 被替换为 api URL

if (!globalThis.__JS_SPIDER__) {
    if (spider.__jsEvalReturn) {
        globalThis.req = http
        globalThis.__JS_SPIDER__ = spider.__jsEvalReturn()
    } else if (spider.default) {
        globalThis.__JS_SPIDER__ = typeof spider.default === 'function' 
            ? spider.default() 
            : spider.default
    }
}
```

### 3.3 PyLoader（Python 爬虫）

**文件**: `app/src/main/java/com/fongmi/android/tv/api/loader/PyLoader.java`

**适用场景**: api 包含 `.py` 的站点

**加载流程**:

```
1. getSpider(key, api, ext)
   ├── computeIfAbsent 懒加载
   ├── loader.spider(api) 创建 Chaquopy Spider
   │   ├── 调用 Python app.py 的 spider() 函数
   │   │   ├── 下载 .py 文件到本地
   │   │   └── SourceFileLoader 动态加载模块
   │   └── 实例化 Spider
   ├── 设置 siteKey
   ├── 调用 init(context, ext)
   └── 缓存到 spiders Map
```

**Python 桥接层**:

- Java 桥接: `chaquo/src/main/java/com/fongmi/chaquo/Spider.java`
- Python 桥接: `chaquo/src/main/python/app.py`
- Python 基类: `chaquo/src/main/python/base/spider.py`

## 四、方法名称映射

三种语言的方法名不完全一致，映射关系如下：

| 功能 | Java 方法名 | QuickJS 方法名 | Python 方法名 |
|------|------------|---------------|--------------|
| 初始化 | `init(ctx, ext)` | `init(ext)` | `init(extend)` |
| 首页分类 | `homeContent(filter)` | `home(filter)` | `homeContent(filter)` |
| 首页推荐 | `homeVideoContent()` | `homeVod()` | `homeVideoContent()` |
| 分类列表 | `categoryContent(tid,pg,filter,ext)` | `category(tid,pg,filter,obj)` | `categoryContent(tid,pg,filter,ext)` |
| 详情 | `detailContent(ids)` | `detail(ids[0])` | `detailContent(ids)` |
| 搜索 | `searchContent(key,quick)` | `search(key,quick)` | `searchContent(key,quick,pg)` |
| 搜索分页 | `searchContent(key,quick,pg)` | `search(key,quick,pg)` | 同上 |
| 播放地址 | `playerContent(flag,id,vipFlags)` | `play(flag,id,array)` | `playerContent(flag,id,vipFlags)` |
| 直播源 | `liveContent(url)` | `live(url)` | `liveContent(url)` |
| 视频嗅探 | `manualVideoCheck()` | `sniffer()` | `manualVideoCheck()` |
| 视频格式 | `isVideoFormat(url)` | `isVideo(url)` | `isVideoFormat(url)` |
| 代理 | `proxy(params)` | `proxy(obj)` | `localProxy(param)` |
| 动作 | `action(action)` | `action(action)` | `action(action)` |
| 销毁 | `destroy()` | `destroy()` | `destroy()` |

## 五、调用链分析

### 5.1 配置加载阶段

```
VodConfig.load(config, callback)
  └→ VodConfig.load(Config)
      └→ VodConfig.parseConfig()
          └→ VodConfig.initSite()
              ├→ BaseLoader.get().parseJar(spider, true)  // 预加载全局 jar
              └→ Site.objectFrom(element, spider)          // 每个 site 继承全局 jar
```

**配置文件示例** (`config.json`):

```json
{
  "spider": "assets://custom_spider.jar",  // 全局 jar 路径
  "sites": [
    {
      "key": "bili",
      "name": "哔哩哔哩",
      "type": 3,                           // type=3 表示 Spider 模式
      "api": "csp_BiliBili",               // 以 csp_ 开头 → JarLoader
      "ext": "https://example.com/ext.json"
    },
    {
      "key": "js_demo",
      "name": "JS示例",
      "type": 3,
      "api": "https://example.com/spider.js",  // 包含 .js → JsLoader
      "ext": ""
    }
  ]
}
```

### 5.2 首页加载调用链

```
UI 层
  └→ SiteViewModel.homeContent()
      └→ SiteApi.homeContent(site)
          ├→ site.recent().spider()                    // 获取 Spider 实例
          │   └→ Site.spider()
          │       └→ BaseLoader.getSpider(key, api, ext, jar)
          │           ├→ isPy(api)?  → PyLoader.getSpider()
          │           ├→ isJs(api)?  → JsLoader.getSpider()
          │           ├→ isCsp(api)? → JarLoader.getSpider()
          │           └→ else        → new SpiderNull()
          ├→ spider.homeContent(true)                  // 获取分类+筛选
          └→ spider.homeVideoContent()                 // 获取推荐视频
```

### 5.3 分类列表调用链

```
UI 层
  └→ SiteViewModel.categoryContent(key, tid, page, filter, extend)
      └→ SiteApi.categoryContent(key, tid, page, filter, extend)
          └→ site.recent().spider().categoryContent(tid, page, filter, extend)
```

### 5.4 视频详情调用链

```
UI 层
  └→ SiteViewModel.detailContent(key, id)
      └→ SiteApi.detailContent(key, id)
          └→ site.recent().spider().detailContent(Arrays.asList(id))
```

### 5.5 搜索调用链

```
UI 层
  └→ SiteViewModel.searchContent(sites, keyword, quick)
      └→ SearchTask(site, keyword, quick)              // 并行搜索任务
          └→ SiteApi.searchContent(site, keyword, quick, page)
              ├→ site.spider().searchContent(keyword, quick)       // 第一页
              └→ site.spider().searchContent(keyword, quick, page) // 后续页
```

### 5.6 播放调用链

```
UI 层
  └→ SiteViewModel.playerContent(key, flag, id)
      └→ SiteApi.playerContent(key, flag, id)
          └→ site.recent().spider().playerContent(flag, id, flags)
```

### 5.7 直播调用链

```
UI 层
  └→ LiveParser.start(live)
      └→ live.spider().liveContent(live.getUrl())      // 仅当 live.getApi() 非空时
```

### 5.8 视频嗅探调用链

```
WebView
  └→ CustomWebView.isVideoFormat(url)
      └→ VodConfig.get().getSite(key).spider()
          ├→ spider.manualVideoCheck()                 // 判断是否自定义
          └→ spider.isVideoFormat(url)                 // 自定义检测
```

### 5.9 代理调用链

```
HTTP 请求
  └→ BaseLoader.proxy(params)
      ├→ params.containsKey("siteKey")? → getSpider(siteKey).proxy(params)
      ├→ "js".equals(params.get("do"))? → jsLoader.proxy(params)
      ├→ "py".equals(params.get("do"))? → pyLoader.proxy(params)
      └→ else                           → jarLoader.proxy(params)
```

## 六、关键文件路径汇总

### 核心接口与基类

| 文件 | 路径 | 用途 |
|------|------|------|
| Spider 抽象基类 | `catvod/src/main/java/com/github/catvod/crawler/Spider.java` | 定义爬虫生命周期方法 |
| SpiderNull | `catvod/src/main/java/com/github/catvod/crawler/SpiderNull.java` | 空实现占位符 |
| SpiderDebug | `catvod/src/main/java/com/github/catvod/crawler/SpiderDebug.java` | 调试日志 |

### 加载器

| 文件 | 路径 | 用途 |
|------|------|------|
| BaseLoader | `app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java` | 调度中心，路由到对应 Loader |
| JarLoader | `app/src/main/java/com/fongmi/android/tv/api/loader/JarLoader.java` | DexClassLoader 加载 Java 爬虫 |
| JsLoader | `app/src/main/java/com/fongmi/android/tv/api/loader/JsLoader.java` | QuickJS 加载 JavaScript 爬虫 |
| PyLoader | `app/src/main/java/com/fongmi/android/tv/api/loader/PyLoader.java` | Chaquopy 加载 Python 爬虫 |

### QuickJS 模块

| 文件 | 路径 | 用途 |
|------|------|------|
| Spider 桥接 | `quickjs/src/main/java/com/fongmi/quickjs/crawler/Spider.java` | QuickJS Spider 实现 |
| Loader | `quickjs/src/main/java/com/fongmi/quickjs/crawler/Loader.java` | QuickJS Spider 工厂 |
| Async | `quickjs/src/main/java/com/fongmi/quickjs/utils/Async.java` | Promise 异步处理 |
| Module | `quickjs/src/main/java/com/fongmi/quickjs/utils/Module.java` | JS 模块加载 |
| Global | `quickjs/src/main/java/com/fongmi/quickjs/method/Global.java` | 全局函数注入 |
| Local | `quickjs/src/main/java/com/fongmi/quickjs/method/Local.java` | 本地存储 |
| http.js | `quickjs/src/main/assets/js/lib/http.js` | HTTP 请求函数 |
| spider.js | `quickjs/src/main/assets/js/lib/spider.js` | Spider 桥接脚本 |

### Chaquopy 模块

| 文件 | 路径 | 用途 |
|------|------|------|
| Spider 桥接 | `chaquo/src/main/java/com/fongmi/chaquo/Spider.java` | Python Spider Java 桥接 |
| Loader | `chaquo/src/main/java/com/fongmi/chaquo/Loader.java` | Python Spider 工厂 |
| app.py | `chaquo/src/main/python/app.py` | Python 桥接函数 |
| spider.py | `chaquo/src/main/python/base/spider.py` | Python Spider 基类 |

### 业务调用层

| 文件 | 路径 | 用途 |
|------|------|------|
| VodConfig | `app/src/main/java/com/fongmi/android/tv/api/config/VodConfig.java` | 配置解析 |
| SiteApi | `app/src/main/java/com/fongmi/android/tv/api/SiteApi.java` | Spider 调用封装 |
| SiteViewModel | `app/src/main/java/com/fongmi/android/tv/model/SiteViewModel.java` | ViewModel 层 |
| SearchTask | `app/src/main/java/com/fongmi/android/tv/model/SearchTask.java` | 搜索任务 |
| Site | `app/src/main/java/com/fongmi/android/tv/bean/Site.java` | 站点数据模型 |
| Live | `app/src/main/java/com/fongmi/android/tv/bean/Live.java` | 直播数据模型 |
| LiveParser | `app/src/main/java/com/fongmi/android/tv/api/parser/LiveParser.java` | 直播解析 |
| CustomWebView | `app/src/main/java/com/fongmi/android/tv/ui/custom/CustomWebView.java` | 视频嗅探 |

## 七、完整流程总结

```
[配置阶段]
config.json 解析
  ├── "spider" 字段 → BaseLoader.parseJar() 预加载全局 jar
  └── "sites" 数组 → 每个 Site 记录 key/api/ext/jar/type
       └── type=3 表示 Spider 模式

[运行阶段 - 懒加载]
Site.spider() / Live.spider()
  └→ BaseLoader.getSpider(key, api, ext, jar)
      └→ 根据 api 路由到对应 Loader
          └→ Loader.getSpider() (computeIfAbsent 懒加载)
              ├── 下载/加载 jar/py/js
              ├── 实例化 Spider 子类
              ├── 设置 siteKey
              ├── 调用 init(context, ext)
              └── 缓存到 ConcurrentHashMap

[业务调用]
UI → ViewModel → SiteApi → Spider 方法调用
```
