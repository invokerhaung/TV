package com.fongmi.android.tv.api.loader;

import android.content.Context;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.utils.Download;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.Util;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

/**
 * JarLoader - Java 爬虫加载器
 *
 * 使用 DexClassLoader 动态加载 jar 文件中的 Spider 类。
 * 适用于 api 以 "csp_" 开头的站点（如 "csp_BiliBili" → 加载 com.github.catvod.spider.BiliBili）
 *
 * 加载流程：
 * 1. parseJar() - 下载或验证 jar 文件
 * 2. load() - 用 DexClassLoader 加载 jar，反射调用 Init.init() 和获取 Proxy.proxy()
 * 3. getSpider() - 懒加载 Spider 实例，缓存到 spiders Map
 */
public class JarLoader {

    private final ConcurrentHashMap<String, DexClassLoader> loaders;  // jar key → ClassLoader
    private final ConcurrentHashMap<String, Method> methods;          // jar key → Proxy.proxy() 方法
    private final ConcurrentHashMap<String, Spider> spiders;          // spider key → Spider 实例
    private final ConcurrentHashMap<String, Object> locks;            // jar key → 同步锁
    private volatile String recent;  // 最近使用的 jar key

    public JarLoader() {
        loaders = new ConcurrentHashMap<>();
        methods = new ConcurrentHashMap<>();
        spiders = new ConcurrentHashMap<>();
        locks = new ConcurrentHashMap<>();
    }

    public void clear() {
        spiders.values().forEach(Spider::destroy);
        loaders.clear();
        methods.clear();
        spiders.clear();
        locks.clear();
        recent = null;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    /**
     * 加载 jar 文件到 DexClassLoader
     *
     * @param key jar 的唯一标识（MD5）
     * @param file jar 文件
     */
    private void load(String key, File file) {
        if (Thread.interrupted()) return;
        if (!Path.exists(file) || !file.setReadOnly()) return;
        String cachePath = Path.jar().getAbsolutePath();
        // 创建 DexClassLoader 加载 jar
        DexClassLoader loader = new DexClassLoader(file.getAbsolutePath(), cachePath, cachePath, App.get().getClassLoader());
        invokeInit(loader);      // 反射调用 jar 中的初始化方法
        invokeProxy(key, loader); // 反射获取代理方法
        loaders.put(key, loader);
    }

    /**
     * 反射调用 jar 中的 Init.init(Context) 方法
     * jar 中必须包含 com.github.catvod.spider.Init 类
     */
    private void invokeInit(DexClassLoader loader) {
        try {
            Class<?> clz = loader.loadClass("com.github.catvod.spider.Init");
            Method method = clz.getMethod("init", Context.class);
            method.invoke(clz, App.get());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射获取 jar 中的 Proxy.proxy(Map) 方法
     * jar 中必须包含 com.github.catvod.spider.Proxy 类
     */
    private void invokeProxy(String key, DexClassLoader loader) {
        try {
            Class<?> clz = loader.loadClass("com.github.catvod.spider.Proxy");
            Method method = clz.getMethod("proxy", Map.class);
            methods.put(key, method);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析并加载 jar 文件
     *
     * 支持的协议：
     * - assets:// - 从 assets 目录读取
     * - http:// 或 https:// - 从网络下载
     * - file:// - 从本地文件读取
     *
     * 支持 MD5 校验：jar 路径可附加 ";md5;hash值" 或 ";md5;http://md5url"
     *
     * @param key jar 的唯一标识（MD5）
     * @param jar jar 文件路径
     */
    public void parseJar(String key, String jar) {
        if (loaders.containsKey(key)) return;  // 已加载，跳过
        if (jar.startsWith("assets")) jar = UrlUtil.convert(jar);  // 转换 assets 路径
        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {  // 双重检查锁，防止并发加载
            if (loaders.containsKey(key)) return;
            // 解析 MD5 校验
            String[] texts = jar.split(";md5;");
            String md5 = texts.length > 1 ? texts[1].trim() : "";
            if (md5.startsWith("http")) md5 = OkHttp.string(md5).trim();
            jar = texts[0];
            // 根据协议加载
            if (!md5.isEmpty() && Util.equals(jar, md5)) {
                load(key, Path.jar(jar));  // MD5 匹配，使用缓存
            } else if (jar.startsWith("http")) {
                load(key, Download.create(jar, Path.jar(jar)).get());  // 下载并加载
            } else if (jar.startsWith("file")) {
                load(key, Path.local(jar));  // 本地文件
            }
        }
    }

    public DexClassLoader dex(String jar) {
        try {
            String jaKey = Util.md5(jar);
            parseJar(jaKey, jar);
            return loaders.get(jaKey);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取 Spider 实例（懒加载 + 缓存）
     *
     * 从 api 提取类名：如 "csp_BiliBili" → "BiliBili" → 加载 com.github.catvod.spider.BiliBili
     *
     * @param key 站点标识
     * @param api 爬虫标识（如 "csp_BiliBili"）
     * @param ext 扩展配置
     * @param jar jar 文件路径
     * @return Spider 实例
     */
    public Spider getSpider(String key, String api, String ext, String jar) {
        String jaKey = Util.md5(jar);
        String spKey = jaKey + key;  // 组合唯一标识
        return spiders.computeIfAbsent(spKey, k -> {
            try {
                parseJar(jaKey, jar);  // 确保 jar 已加载
                DexClassLoader loader = loaders.get(jaKey);
                if (loader == null) return new SpiderNull();
                // 从 api 提取类名并实例化：csp_BiliBili → com.github.catvod.spider.BiliBili
                Spider spider = (Spider) loader.loadClass("com.github.catvod.spider." + api.split("csp_")[1]).newInstance();
                spider.siteKey = key;
                spider.init(App.get(), ext);  // 初始化爬虫
                return spider;
            } catch (Throwable e) {
                e.printStackTrace();
                return new SpiderNull();  // 加载失败返回空实现
            }
        });
    }

    private DexClassLoader requireRecentLoader() {
        DexClassLoader loader = loaders.get(recent);
        if (loader == null) throw new IllegalStateException("No jar loaded for recent key: " + recent);
        return loader;
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) throws Throwable {
        Class<?> clz = requireRecentLoader().loadClass("com.github.catvod.parser.Json" + key);
        Method method = clz.getMethod("parse", LinkedHashMap.class, String.class);
        return (JSONObject) method.invoke(null, jxs, url);
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) throws Throwable {
        Class<?> clz = requireRecentLoader().loadClass("com.github.catvod.parser.Mix" + key);
        Method method = clz.getMethod("parse", LinkedHashMap.class, String.class, String.class, String.class);
        return (JSONObject) method.invoke(null, jxs, name, flag, url);
    }

    public Object[] proxy(Map<String, String> params) throws Exception {
        Method method = recent != null ? methods.get(recent) : null;
        Object[] result = proxyInvoke(method, params);
        if (result != null) return result;
        return tryOthers(params);
    }

    private Object[] tryOthers(Map<String, String> p) {
        return methods.entrySet().stream().filter(e -> !e.getKey().equals(recent)).map(e -> proxyInvoke(e.getValue(), p)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private Object[] proxyInvoke(Method method, Map<String, String> params) {
        try {
            return method == null ? null : (Object[]) method.invoke(null, params);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
