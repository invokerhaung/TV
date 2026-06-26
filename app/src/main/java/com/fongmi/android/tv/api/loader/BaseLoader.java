package com.fongmi.android.tv.api.loader;

import android.text.TextUtils;

import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;
import com.github.catvod.utils.Util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * BaseLoader - Spider 调度中心（单例模式）
 *
 * 核心职责：
 * 1. 根据 api 字段格式路由到对应的 Loader（JarLoader/JsLoader/PyLoader）
 * 2. 管理全局 jar 预加载
 * 3. 代理请求分发
 *
 * 路由规则：
 * - api 包含 ".py"  → PyLoader（Python 爬虫）
 * - api 包含 ".js"  → JsLoader（JavaScript 爬虫）
 * - api 以 "csp_" 开头 → JarLoader（Java 爬虫）
 * - 其他情况 → SpiderNull（空实现）
 */
public class BaseLoader {

    private final JarLoader jarLoader;  // Java 爬虫加载器（DexClassLoader）
    private final PyLoader pyLoader;    // Python 爬虫加载器（Chaquopy）
    private final JsLoader jsLoader;    // JavaScript 爬虫加载器（QuickJS）

    private BaseLoader() {
        jarLoader = new JarLoader();
        pyLoader = new PyLoader();
        jsLoader = new JsLoader();
    }

    /** 获取单例实例 */
    public static BaseLoader get() {
        return Loader.INSTANCE;
    }

    /** 判断是否为 JavaScript 爬虫 */
    private static boolean isJs(String api) {
        return api.contains(".js");
    }

    /** 判断是否为 Python 爬虫 */
    private static boolean isPy(String api) {
        return api.contains(".py");
    }

    /** 判断是否为 Java 爬虫（csp_ 前缀） */
    private static boolean isCsp(String api) {
        return api.startsWith("csp_");
    }

    public void clear() {
        Task.execute(() -> {
            jarLoader.clear();
            pyLoader.clear();
            jsLoader.clear();
        });
    }

    /**
     * 获取 Spider 实例 - 根据 api 格式路由到对应 Loader
     *
     * @param key 站点标识（如 "bili"）
     * @param api 爬虫标识（如 "csp_BiliBili" 或 "https://xxx/spider.js"）
     * @param ext 扩展配置 JSON
     * @param jar jar 文件路径（Java/JS 爬虫需要）
     * @return Spider 实例，加载失败返回 SpiderNull
     */
    public Spider getSpider(String key, String api, String ext, String jar) {
        if (isPy(api)) return pyLoader.getSpider(key, api, ext);       // Python 爬虫
        else if (isJs(api)) return jsLoader.getSpider(key, api, ext, jar); // JS 爬虫
        else if (isCsp(api)) return jarLoader.getSpider(key, api, ext, jar); // Java 爬虫
        else return new SpiderNull();  // 无法识别的格式，返回空实现
    }

    /**
     * 通过 siteKey 获取已缓存的 Spider 实例
     * @param key 站点标识
     * @return Spider 实例，不存在返回 SpiderNull
     */
    public Spider getSpider(String key) {
        Site site = VodConfig.get().getSite(key);
        Live live = LiveConfig.get().getLive(key);
        if (!site.isEmpty()) return site.spider();
        if (!live.isEmpty()) return live.spider();
        return new SpiderNull();
    }

    /**
     * 设置最近使用的 Loader（用于代理请求路由）
     */
    public void setRecent(String key, String api, String jar) {
        if (isJs(api)) jsLoader.setRecent(key);
        else if (isPy(api)) pyLoader.setRecent(key);
        else if (isCsp(api)) jarLoader.setRecent(Util.md5(jar));
    }

    /**
     * 代理请求分发 - 根据 params 中的标识路由到对应 Loader
     *
     * 路由规则：
     * - params 包含 "siteKey" → 直接调用对应 Spider 的 proxy
     * - params 的 "do" 为 "js" → JsLoader.proxy
     * - params 的 "do" 为 "py" → PyLoader.proxy
     * - 其他 → JarLoader.proxy
     */
    public Object[] proxy(Map<String, String> params) throws Exception {
        if (params.containsKey("siteKey")) return getSpider(params.get("siteKey")).proxy(params);
        if ("js".equals(params.get("do"))) return jsLoader.proxy(params);
        if ("py".equals(params.get("do"))) return pyLoader.proxy(params);
        return jarLoader.proxy(params);
    }

    /**
     * 预加载全局 jar 文件
     * @param jar jar 文件路径（支持 HTTP/file/assets 协议）
     * @param recent 是否设为最近使用的 jar
     */
    public void parseJar(String jar, boolean recent) {
        if (TextUtils.isEmpty(jar)) return;
        String key = Util.md5(jar);
        jarLoader.parseJar(key, jar);
        if (recent) jarLoader.setRecent(key);
    }

    public DexClassLoader dex(String jar) {
        return jarLoader.dex(jar);
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) throws Throwable {
        return jarLoader.jsonExt(key, jxs, url);
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) throws Throwable {
        return jarLoader.jsonExtMix(flag, key, name, jxs, url);
    }

    private static class Loader {
        static volatile BaseLoader INSTANCE = new BaseLoader();
    }
}
