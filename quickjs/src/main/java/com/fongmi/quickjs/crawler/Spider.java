package com.fongmi.quickjs.crawler;

import android.content.Context;

import com.fongmi.quickjs.bean.Res;
import com.fongmi.quickjs.method.Console;
import com.fongmi.quickjs.method.Global;
import com.fongmi.quickjs.method.Local;
import com.fongmi.quickjs.utils.Async;
import com.fongmi.quickjs.utils.JSUtil;
import com.fongmi.quickjs.utils.Module;
import com.github.catvod.utils.Asset;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.UriUtil;
import com.github.catvod.utils.Util;
import com.whl.quickjs.wrapper.JSArray;
import com.whl.quickjs.wrapper.JSObject;
import com.whl.quickjs.wrapper.QuickJSContext;

import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dalvik.system.DexClassLoader;

/**
 * QuickJS Spider - JavaScript 爬虫桥接实现
 *
 * 将 Java 的 Spider 接口方法映射到 QuickJS 中的 JS 函数调用。
 * 方法名映射：
 * - homeContent → home
 * - homeVideoContent → homeVod
 * - categoryContent → category
 * - detailContent → detail
 * - searchContent → search
 * - playerContent → play
 * - liveContent → live
 * - manualVideoCheck → sniffer
 * - isVideoFormat → isVideo
 */
public class Spider extends com.github.catvod.crawler.Spider {

    private final ExecutorService executor;  // 单线程执行器，保证 QuickJS 线程安全
    private final DexClassLoader dex;        // 用于加载 Function 扩展
    private final String api;                // JS 文件路径

    private QuickJSContext ctx;  // QuickJS 上下文
    private JSObject jsObject;   // JS Spider 对象
    private boolean cat;         // 是否为 catvod 格式（__jsEvalReturn）

    public Spider(String api, DexClassLoader dex) {
        this.executor = Executors.newSingleThreadExecutor();
        this.api = api;
        this.dex = dex;
    }

    private <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(callable);
    }

    private Object call(String func, Object... args) throws Exception {
        return submit(() -> Async.run(jsObject, func, args)).get().get();
    }

    /** 初始化：调用 JS 的 init(ext) */
    @Override
    public void init(Context context, String extend) throws Exception {
        initializeJS();
        call("init", submit(() -> getExt(extend)).get());
    }

    /** 首页分类：Java homeContent → JS home */
    @Override
    public String homeContent(boolean filter) throws Exception {
        return (String) call("home", filter);
    }

    /** 首页推荐：Java homeVideoContent → JS homeVod */
    @Override
    public String homeVideoContent() throws Exception {
        return (String) call("homeVod");
    }

    /** 分类列表：Java categoryContent → JS category */
    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        JSObject obj = submit(() -> JSUtil.toObject(ctx, extend)).get();
        return (String) call("category", tid, pg, filter, obj);
    }

    /** 视频详情：Java detailContent → JS detail（只传第一个 ID） */
    @Override
    public String detailContent(List<String> ids) throws Exception {
        return (String) call("detail", ids.get(0));
    }

    /** 搜索：Java searchContent → JS search */
    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return (String) call("search", key, quick);
    }

    /** 搜索（带分页）：Java searchContent → JS search */
    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        return (String) call("search", key, quick, pg);
    }

    /** 播放地址：Java playerContent → JS play */
    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        JSArray array = submit(() -> JSUtil.toArray(ctx, vipFlags)).get();
        return (String) call("play", flag, id, array);
    }

    /** 直播源：Java liveContent → JS live */
    @Override
    public String liveContent(String url) throws Exception {
        return (String) call("live", url);
    }

    /** 视频嗅探：Java manualVideoCheck → JS sniffer */
    @Override
    public boolean manualVideoCheck() throws Exception {
        return (Boolean) call("sniffer");
    }

    /** 视频格式：Java isVideoFormat → JS isVideo */
    @Override
    public boolean isVideoFormat(String url) throws Exception {
        return (Boolean) call("isVideo", url);
    }

    @Override
    public Object[] proxy(Map<String, String> params) throws Exception {
        return "catvod".equals(params.get("from")) ? proxy2(params) : proxy1(params);
    }

    @Override
    public String action(String action) throws Exception {
        return (String) call("action", action);
    }

    @Override
    public void destroy() {
        try {
            call("destroy");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            releaseJS();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
    }

    private void releaseJS() throws Exception {
        submit(() -> {
            jsObject.release();
            ctx.destroy();
            return null;
        }).get();
    }

    /**
     * 初始化 QuickJS 环境（在单线程执行器中运行）
     * 分三步：创建上下文 → 注入全局函数 → 加载 JS 模块
     */
    private void initializeJS() throws Exception {
        submit(() -> {
            createCtx();  // 1. 创建上下文和基础环境
            createFun();  // 2. 注入全局函数
            createObj();  // 3. 加载 JS 模块并创建 Spider 对象
            return null;
        }).get();
    }

    /**
     * 创建 QuickJS 上下文并注入基础环境
     * - console：日志输出
     * - http.js：网络请求函数（req/http）
     * - local：本地存储
     * - 模块加载器：支持 JS 模块导入
     */
    private void createCtx() {
        ctx = QuickJSContext.create();
        ctx.setConsole(new Console());
        ctx.evaluate(Asset.read("js/lib/http.js"));  // 注入 HTTP 请求函数
        ctx.getGlobalObject().setProperty("local", Local.class);  // 注入本地存储
        // 设置模块加载器，支持 import 语法
        ctx.setModuleLoader(new QuickJSContext.BytecodeModuleLoader() {
            @Override
            public String moduleNormalizeName(String baseModuleName, String moduleName) {
                return UriUtil.resolve(baseModuleName, moduleName);
            }

            @Override
            public byte[] getModuleBytecode(String moduleName) {
                return ctx.compileModule(Module.get().fetch(moduleName), moduleName);
            }
        });
    }

    /**
     * 注入全局函数
     * - Global 类提供：s2t/t2s/getProxy/js2Proxy/setTimeout/req/_http/加密函数等
     * - 尝试加载 jar 中的 com.github.catvod.js.Function 扩展
     */
    private void createFun() {
        try {
            Global.create(ctx, executor);
            Class<?> clz = dex.loadClass("com.github.catvod.js.Function");
            clz.getDeclaredConstructor(QuickJSContext.class).newInstance(ctx);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 加载 JS 模块并创建 Spider 对象
     * 1. 从网络/assets 读取 JS 文件
     * 2. 判断是否 catvod 格式（包含 __jsEvalReturn）
     * 3. 执行 JS 模块
     * 4. 执行 spider.js 桥接脚本，创建 __JS_SPIDER__ 对象
     */
    private void createObj() {
        String spider = "__JS_SPIDER__";
        String global = "globalThis." + spider;
        String content = Module.get().fetch(api);  // 获取 JS 文件内容
        cat = content.contains("__jsEvalReturn");  // 判断是否 catvod 格式
        ctx.evaluateModule(content.replace(spider, global), api);  // 执行 JS 模块
        ctx.evaluateModule(String.format(Asset.read("js/lib/spider.js"), api));  // 执行桥接脚本
        jsObject = (JSObject) ctx.getProperty(ctx.getGlobalObject(), spider);  // 获取 Spider 对象
    }

    private Object getExt(String ext) {
        if (!cat) return Json.isObj(ext) ? ctx.parse(ext) : ext;
        JSObject obj = ctx.createNewJSObject();
        obj.setProperty("stype", 3);
        obj.setProperty("skey", siteKey);
        if (!Json.isObj(ext)) obj.setProperty("ext", ext);
        else obj.setProperty("ext", (JSObject) ctx.parse(ext));
        return obj;
    }

    private Object[] proxy1(Map<String, String> params) throws Exception {
        JSObject obj = submit(() -> JSUtil.toObject(ctx, params)).get();
        JSArray proxy = (JSArray) call("proxy", obj);
        String json = submit(proxy::stringify).get();
        JSONArray array = new JSONArray(json);
        Map<String, String> headers = array.length() > 3 ? Json.toMap(array.optString(3)) : null;
        boolean base64 = array.length() > 4 && array.optInt(4) == 1;
        Object[] result = new Object[4];
        result[0] = array.optInt(0);
        result[1] = array.optString(1);
        result[2] = getStream(array.opt(2), base64);
        result[3] = headers;
        return result;
    }

    private Object[] proxy2(Map<String, String> params) throws Exception {
        String url = params.get("url");
        String header = params.get("header");
        JSArray array = submit(() -> JSUtil.toArray(ctx, Arrays.asList(url.split("/")))).get();
        Object object = submit(() -> ctx.parse(header)).get();
        String proxy = (String) call("proxy", array, object);
        Res res = Res.objectFrom(proxy);
        Object[] result = new Object[3];
        result[0] = res.getCode();
        result[1] = res.getContentType();
        result[2] = res.getStream();
        return result;
    }

    private ByteArrayInputStream getStream(Object o, boolean base64) {
        if (o instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) o);
        } else {
            String content = o.toString();
            if (base64 && content.contains("base64,")) content = content.split("base64,")[1];
            return new ByteArrayInputStream(base64 ? Util.decode(content) : content.getBytes());
        }
    }
}
