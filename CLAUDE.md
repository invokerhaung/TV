# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于 CatVod 的 Android 影音应用，同时支持 **Android TV（leanback）** 和 **手机（mobile）** 两种形态。通过外部 JSON 配置扩展内容，支持 Java/JavaScript/Python 三种语言编写爬虫插件。

- 包名：`com.fongmi.android.tv`
- minSdk：24，语言：Java 21，无测试、无 CI/CD

## 构建命令

```bash
# Debug 构建（四选一组合 flavor × abi）
./gradlew assembleLeanbackArm64_v8aDebug
./gradlew assembleMobileArm64_v8aDebug

# Release 构建（需在 local.properties 配置签名）
./gradlew assembleLeanbackArm64_v8aRelease
./gradlew assembleMobileArm64_v8aRelease

# APK 输出位置
# Debug:  app/build/outputs/apk/{flavor}/{abi}/debug/
# Release: Release/apk/（根目录，构建脚本自动复制）
```

Product flavors 两个维度：**mode**（leanback / mobile）× **abi**（arm64_v8a / armeabi_v7a）。

## 模块结构

| 模块 | 说明 |
|------|------|
| `app` | 主应用，`src/main/` 共享逻辑，`src/leanback/` TV UI，`src/mobile/` 手机 UI |
| `catvod` | 爬虫抽象层，Spider 接口 + OkHttp 网络栈 |
| `quickjs` | QuickJS JS 引擎包装，运行 JavaScript 爬虫 |
| `chaquo` | Chaquopy Python 引擎，运行 Python 爬虫 |

`app/libs/` 下有预编译 AAR：forcetech、hook、jianpian、thunder、tvbus（均为直播/下载引擎）。

## 关键包（app/src/main/java/com/fongmi/android/tv/）

| 包 | 职责 |
|---|------|
| `api/` | Spider 管理、爬虫加载（DexClassLoader / QuickJS / Chaquopy） |
| `bean/` | 数据模型（Site、Vod、Episode、Live、Channel 等） |
| `db/` | Room 数据库（历史、收藏等） |
| `player/` | ExoPlayer/Media3 播放器封装 |
| `server/` | NanoHTTPD 本地 HTTP 服务器（端口 9978-9998） |
| `dlna/` | DLNA 投屏（DMC 手机端投放 / DMR 电视端接收） |
| `model/` | ViewModel 层 |
| `ui/` | UI 组件 |
| `setting/` | 偏好设置 |
| `event/` | EventBus 事件定义 |

## 核心依赖

- **播放**：Media3 ExoPlayer 1.8.0 + FFmpeg 软解、DanmakuFlameMaster 弹幕
- **网络**：OkHttp 5.3.2（DoH、代理、Brotli）、NanoHTTPD 2.3.1
- **数据库**：Room 2.8.4
- **图片**：Glide 5.0.7（OkHttp3 集成、AVIF）
- **序列化**：Gson 2.14.0、SimpleXML 2.7.1
- **事件总线**：EventBus 3.3.1
- **DLNA**：JUPnP 3.0.4
- **YouTube 提取**：NewPipeExtractor v0.26.1

## 项目文档

详细文档均在 `docs/` 目录（繁体中文）：

| 文件 | 内容 |
|------|------|
| `docs/CONFIG.md` | Vod/Live JSON 配置完整字段说明 |
| `docs/SPIDER.md` | Spider API 所有方法规格与返回格式 |
| `docs/LOCAL.md` | 本地 HTTP 服务器所有端点说明 |
| `docs/LIVE.md` | 直播源格式（TXT / M3U / JSON）完整说明 |
